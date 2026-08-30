package com.example.pi.controller;

import com.example.pi.dto.request.PaymentRequest;
import com.example.pi.dto.response.BalanceResponse;
import com.example.pi.dto.response.CustomerResponse;
import com.example.pi.dto.response.PaginationResponse;
import com.example.pi.dto.response.TransactionResponse;
import com.example.pi.dto.response.UserDepartmentResponse;
import com.example.pi.entity.Customers;
import com.example.pi.entity.Transactions;
import com.example.pi.exception.UnauthorizedAccountAccessException;
import com.example.pi.security.JwtUtils;
import com.example.pi.service.CustomerService;
import com.example.pi.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Customer REST controller.
 *
 * Improvements:
 *  - All responses use CustomerResponse DTO — no JPA entity leaks (doc item #12, #20, #21)
 *  - DELETE returns 204 NO_CONTENT (doc item #75)
 *  - @Valid on all request bodies (doc item #24)
 *  - No try/catch — propagates to GlobalExceptionHandler (doc item #28)
 *  - Constructor injection (doc item #30)
 *  - API versioning /api/v1/customers (doc item #40)
 *  - Idempotency-Key header forwarded to TransactionService (doc item #7)
 */
@RestController
@RequestMapping("/api/v1/customers")
@Validated
public class CustomerController {

    private static final int MAX_PAGE_SIZE = 100;

    private final CustomerService    customerService;
    private final TransactionService transactionService;
    private final JwtUtils           jwtUtils;

    public CustomerController(CustomerService customerService,
                               TransactionService transactionService,
                               JwtUtils jwtUtils) {
        this.customerService    = customerService;
        this.transactionService = transactionService;
        this.jwtUtils           = jwtUtils;
    }

    // ── Customer CRUD ─────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerResponse> addCustomer(@Valid @RequestBody Customers customer) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.addCustomer(customer));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginationResponse<CustomerResponse>> getAllCustomers(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "5") @Min(1) @Max(MAX_PAGE_SIZE) int size) {
        return ResponseEntity.ok(customerService.getAllCustomers(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable int id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerResponse> updateCustomer(@PathVariable int id,
                                                           @Valid @RequestBody Customers customer) {
        customer.setCustomer_Id(id);
        return ResponseEntity.ok(customerService.updateCustomer(customer));
    }

    /**
     * Soft delete — marks customer as deleted; returns 204 NO_CONTENT (doc items #75, #76).
     * No financial records are physically deleted.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCustomer(@PathVariable int id) {
        customerService.softDeleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter/idtype/{idType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginationResponse<CustomerResponse>> filterByIdentityType(
            @PathVariable String idType,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "5") @Min(1) @Max(MAX_PAGE_SIZE) int size) {
        return ResponseEntity.ok(customerService.getCustomerByIdentityType(idType, PageRequest.of(page, size)));
    }

    @GetMapping("/transactions/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginationResponse<UserDepartmentResponse>> getAllTransactionHistory(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "5") @Min(1) @Max(MAX_PAGE_SIZE) int size) {
        return ResponseEntity.ok(customerService.getUserDepartMent(PageRequest.of(page, size)));
    }

    @GetMapping("/transactions/latest")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginationResponse<UserDepartmentResponse>> getLatestTransactions(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "5") @Min(1) @Max(MAX_PAGE_SIZE) int size) {
        return ResponseEntity.ok(customerService.getLatestTransactions(PageRequest.of(page, size)));
    }

    // ── Account operations ────────────────────────────────────────────────────

    @GetMapping("/{id}/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<PaginationResponse<Transactions>> getCustomerTransactions(
            @PathVariable int id,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "5") @Min(1) @Max(MAX_PAGE_SIZE) int size,
            Authentication authentication) {
        assertCanAccessCustomer(id, authentication);
        return ResponseEntity.ok(transactionService.getCustomerTransactions(id, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}/balance")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable int id, Authentication authentication) {
        assertCanAccessCustomer(id, authentication);
        return ResponseEntity.ok(transactionService.getBalance(id));
    }

    @GetMapping("/transactions/mode/{mode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<PaginationResponse<Transactions>> getTransactionsByMode(
            @PathVariable String mode,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "5") @Min(1) @Max(MAX_PAGE_SIZE) int size) {
        return ResponseEntity.ok(transactionService.getTransactionsByType(mode, PageRequest.of(page, size)));
    }

    @PostMapping("/{id}/payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<TransactionResponse> makePayment(
            @PathVariable int id,
            @Valid @RequestBody PaymentRequest paymentRequest,
            Authentication authentication,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        assertCanAccessCustomer(id, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                transactionService.createTransaction(
                        id,
                        paymentRequest.getTransactionType(),
                        paymentRequest.getTransactionTo(),
                        paymentRequest.getTransactionAmount(),
                        paymentRequest.getTransactionMode(),
                        idempotencyKey));
    }

    // ── Legacy URL compatibility ───────────────────────────────────────────────

    @PostMapping("/Banking")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TransactionResponse> createTransactionLegacy(
            @Valid @RequestBody PaymentRequest paymentRequest,
            @RequestParam int customerId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                transactionService.createTransaction(
                        customerId,
                        paymentRequest.getTransactionType(),
                        paymentRequest.getTransactionTo(),
                        paymentRequest.getTransactionAmount(),
                        paymentRequest.getTransactionMode()));
    }

    // ── Auth helpers ─────────────────────────────────────────────────────────

    private void assertCanAccessCustomer(int customerId, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return;

        Integer tokenCustomerId = getCustomerIdFromToken(authentication);
        if (tokenCustomerId == null || !tokenCustomerId.equals(customerId)) {
            throw new UnauthorizedAccountAccessException(
                    "Access denied: you can only access your own account");
        }
    }

    private Integer getCustomerIdFromToken(Authentication authentication) {
        if (authentication != null && authentication.getCredentials() instanceof String token) {
            return jwtUtils.getCustomerIdFromToken(token);
        }
        return null;
    }
}
