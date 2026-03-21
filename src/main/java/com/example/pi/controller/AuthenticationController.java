package com.example.pi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.pi.repository.UserRepository;
import com.example.pi.repository.CustomerRepository;
import com.example.pi.dto.request.LoginRequest;
import com.example.pi.dto.request.UserRegisterRequest;
import com.example.pi.dto.response.LoginResponse;
import com.example.pi.security.JwtUtils;
import com.example.pi.entity.Customers;
import com.example.pi.entity.User;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUserName(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUserName());
            User user = userRepository.findByUserName(loginRequest.getUserName());

            if (user == null || !user.isActive()) {
                return new ResponseEntity<>(new LoginResponse("User not found or inactive"), HttpStatus.UNAUTHORIZED);
            }

            Integer customerId = null;
            if (user.getRoles().contains("ROLE_USER")) {
                if (user.getCustomerId() != null) {
                    Customers customer = customerRepository.findById(user.getCustomerId()).orElse(null);
                    if (customer == null) {
                        return new ResponseEntity<>(new LoginResponse("Customer not found"), HttpStatus.UNAUTHORIZED);
                    }
                    customerId = customer.getCustomer_Id();
                }
            }

            String token = jwtUtils.generateToken(userDetails, user.getRoles(), customerId);

            return new ResponseEntity<>(
                    new LoginResponse(token, user.getUserName(), user.getRoles(), customerId),
                    HttpStatus.OK
            );
        } catch (BadCredentialsException ex) {
            return new ResponseEntity<>(new LoginResponse("Invalid credentials"), HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            return new ResponseEntity<>(new LoginResponse("Authentication failed"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoginResponse> registerUser(@RequestBody UserRegisterRequest request) {
        try {
            if (userRepository.findByUserName(request.getUserName()) != null) {
                return new ResponseEntity<>(new LoginResponse("User already exists"), HttpStatus.BAD_REQUEST);
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

            User savedUser = userRepository.save(newUser);
            return new ResponseEntity<>(new LoginResponse("User registered successfully"), HttpStatus.CREATED);
        } catch (Exception ex) {
            return new ResponseEntity<>(new LoginResponse("Registration failed: " + ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/public-register")
    public ResponseEntity<LoginResponse> publicRegister(@RequestBody LoginRequest loginRequest) {
        try {
            if (userRepository.findByUserName(loginRequest.getUserName()) != null) {
                return new ResponseEntity<>(new LoginResponse("User already exists"), HttpStatus.BAD_REQUEST);
            }

            User newUser = new User();
            newUser.setUserName(loginRequest.getUserName());
            newUser.setPassword(passwordEncoder.encode(loginRequest.getPassword()));
            newUser.setActive(true);
            newUser.setRoles("ROLE_USER");

            User savedUser = userRepository.save(newUser);
            return new ResponseEntity<>(new LoginResponse("User registered successfully"), HttpStatus.CREATED);
        } catch (Exception ex) {
            return new ResponseEntity<>(new LoginResponse("Registration failed: " + ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
