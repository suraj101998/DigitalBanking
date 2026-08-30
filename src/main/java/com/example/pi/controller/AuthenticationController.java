package com.example.pi.controller;

import com.example.pi.dto.request.LoginRequest;
import com.example.pi.dto.request.UserRegisterRequest;
import com.example.pi.dto.response.LoginResponse;
import com.example.pi.entity.Customers;
import com.example.pi.entity.RefreshToken;
import com.example.pi.entity.User;
import com.example.pi.exception.DuplicateTransactionException;
import com.example.pi.repository.CustomerRepository;
import com.example.pi.repository.UserRepository;
import com.example.pi.security.JwtUtils;
import com.example.pi.service.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller.
 *
 * Improvements:
 *  - No try/catch — exceptions propagate to GlobalExceptionHandler (doc item #28)
 *  - @Valid on all request bodies (doc item #24)
 *  - Constructor injection (doc item #30)
 *  - BadCredentialsException re-thrown as domain-friendly response (login must still return 401)
 *  - Duplicate username returns 409 CONFLICT via DuplicateTransactionException (doc item #74)
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationManager  authenticationManager;
    private final UserRepository         userRepository;
    private final CustomerRepository     customerRepository;
    private final UserDetailsService     userDetailsService;
    private final JwtUtils               jwtUtils;
    private final PasswordEncoder        passwordEncoder;
    private final RefreshTokenService    refreshTokenService;

    public AuthenticationController(AuthenticationManager authenticationManager,
                                     UserRepository userRepository,
                                     CustomerRepository customerRepository,
                                     UserDetailsService userDetailsService,
                                     JwtUtils jwtUtils,
                                     PasswordEncoder passwordEncoder,
                                     RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository        = userRepository;
        this.customerRepository    = customerRepository;
        this.userDetailsService    = userDetailsService;
        this.jwtUtils              = jwtUtils;
        this.passwordEncoder       = passwordEncoder;
        this.refreshTokenService   = refreshTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        // ── Pre-auth: check account lockout (doc item #65) ─────────────────────
        User user = userRepository.findByUserName(loginRequest.getUserName());
        if (user != null && user.isAccountLocked()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new LoginResponse(
                            "Account is temporarily locked due to too many failed attempts. "
                            + "Please try again in " + User.LOCK_DURATION_MINUTES + " minutes."));
        }

        // ── Authenticate ───────────────────────────────────────────────────────
        // BadCredentialsException is caught here specifically because login must return 401.
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUserName(),
                            loginRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            // Record failed attempt (doc item #65)
            if (user != null) {
                user.recordFailedLogin();
                userRepository.save(user);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("Invalid credentials"));
        }

        // Re-fetch after possible lockout update
        user = userRepository.findByUserName(loginRequest.getUserName());

        if (user == null || !user.isActive()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("User not found or inactive"));
        }

        // ── Successful login: reset failed counter (doc item #65) ──────────────
        user.recordSuccessfulLogin();
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUserName());

        Integer customerId = null;
        if (user.getRoles().contains("ROLE_USER") && user.getCustomerId() != null) {
            customerId = customerRepository.findById(user.getCustomerId())
                    .orElseThrow(() -> new com.example.pi.exception.CustomerNotFoundException(
                            "Customer linked to this user not found"))
                    .getCustomerId();
        }

        String token = jwtUtils.generateToken(userDetails, user.getRoles(), customerId);

        // ── Issue refresh token (doc item #67) ────────────────────────────────
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return ResponseEntity.ok(
                new LoginResponse(token, refreshToken.getToken(),
                                  user.getUserName(), user.getRoles(), customerId));
    }

    /**
     * Exchanges a valid refresh token for a new JWT access token (doc item #67).
     * The refresh token is rotated on every use (old one invalidated, new one issued).
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody java.util.Map<String, String> body) {
        String rawToken = body.get("refreshToken");
        if (rawToken == null || rawToken.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new LoginResponse("refreshToken field is required"));
        }

        RefreshToken validated = refreshTokenService.validateRefreshToken(rawToken);
        User user = refreshTokenService.getUserByRefreshToken(validated);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUserName());
        String newAccessToken   = jwtUtils.generateToken(userDetails, user.getRoles(), user.getCustomerId());

        // Rotate: delete old refresh token, issue new one
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

        return ResponseEntity.ok(
                new LoginResponse(newAccessToken, newRefreshToken.getToken(),
                                  user.getUserName(), user.getRoles(), user.getCustomerId()));
    }

    /**
     * Revokes the user's refresh token (logout — doc item #68).
     * The caller must include their JWT to identify themselves.
     */
    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("Authentication required"));
        }

        User user = userRepository.findByUserName(userDetails.getUsername());
        if (user != null) {
            refreshTokenService.revokeByUserId(user.getId());
        }
        return ResponseEntity.ok(new LoginResponse("Logged out successfully"));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoginResponse> registerUser(@Valid @RequestBody UserRegisterRequest request) {
        if (userRepository.findByUserName(request.getUserName()) != null) {
            throw new DuplicateTransactionException("Username already exists: " + request.getUserName());
        }

        User newUser = new User();
        newUser.setUserName(request.getUserName());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setActive(true);
        newUser.setRoles(request.getRoles() != null ? request.getRoles() : "ROLE_USER");

        if (request.getCustomerDetails() != null) {
            Customers savedCustomer = customerRepository.save(request.getCustomerDetails());
            newUser.setCustomerId(savedCustomer.getCustomer_Id());
        }

        userRepository.save(newUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new LoginResponse("User registered successfully"));
    }

    @PostMapping("/public-register")
    public ResponseEntity<LoginResponse> publicRegister(@Valid @RequestBody LoginRequest loginRequest) {
        if (userRepository.findByUserName(loginRequest.getUserName()) != null) {
            throw new DuplicateTransactionException("Username already exists: " + loginRequest.getUserName());
        }

        User newUser = new User();
        newUser.setUserName(loginRequest.getUserName());
        newUser.setPassword(passwordEncoder.encode(loginRequest.getPassword()));
        newUser.setActive(true);
        newUser.setRoles("ROLE_USER");

        userRepository.save(newUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new LoginResponse("User registered successfully"));
    }
}
