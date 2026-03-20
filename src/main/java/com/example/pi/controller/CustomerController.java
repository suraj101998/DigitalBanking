package com.example.pi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.pi.service.CustomerService;
import com.example.pi.security.JwtUtils;
import com.example.pi.response.PaginationResponse;
import com.example.pi.response.UserDepartmentResponse;
import com.example.pi.entity.Customers;
import com.example.pi.entity.Transactions;
import com.example.pi.request.PaymentRequest;

import java.sql.Date;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private static final int DEFAULT_PAGE_SIZE = 5;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/AddCustomers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Customers> addCustomer(@RequestBody Customers user) {
        try {
            Customers customer = customerService.addCustomer(user);
            return new ResponseEntity<>(customer, HttpStatus.CREATED);
        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/DisplayAllCustomers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginationResponse<Customers>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page) {
        try {
            Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE);
            PaginationResponse<Customers> response = customerService.getAllCustomers(pageable);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/FindByCustomerID/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Customers> getUserById(@PathVariable("id") int userId) {
        try {
            Customers customer = customerService.getCustomerById(userId);
            return new ResponseEntity<>(customer, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/UpdateCustomer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Customers> UpdateCustomer(@RequestBody Customers user) {
        try {
            Customers customer = customerService.UpdateCustomer(user);
            return new ResponseEntity<>(customer, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/DeleteCustomer/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Customers> deleteUserById(@PathVariable("id") int userId) {
        try {
            Customers customer = customerService.deleteCustomer(userId);
            return new ResponseEntity<>(customer, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/FilterCustomersByIDPoof/{id_type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginationResponse<Customers>> getCustomerByIdentityType(
            @PathVariable String id_type, @RequestParam(defaultValue = "0") int page) {
        try {
            Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE);
            PaginationResponse<Customers> response = customerService.getCustomerByIdentityType(id_type, pageable);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/AllTransactionsHistory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginationResponse<UserDepartmentResponse>> getAllUserDepartments(
            @RequestParam(defaultValue = "0") int page) {
        try {
            Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE);
            PaginationResponse<UserDepartmentResponse> response = customerService.getUserDepartMent(pageable);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/LatestTransactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginationResponse<UserDepartmentResponse>> getLatestTransactions(
            @RequestParam(defaultValue = "0") int page) {
        try {
            Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE);
            PaginationResponse<UserDepartmentResponse> response = customerService.getLatestTransactions(pageable);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/Ministatement/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<PaginationResponse<Transactions>> getCustomerByID(
            @PathVariable("id") int customer_id,
            @RequestParam(defaultValue = "0") int page,
            Authentication authentication) {
        try {
            Integer tokenCustomerId = getCustomerIdFromToken(authentication);

            if (authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                if (tokenCustomerId == null || !tokenCustomerId.equals(customer_id)) {
                    return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
                }
            }

            Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE);
            PaginationResponse<Transactions> response = customerService.getCustomerByID(customer_id, pageable);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/CheckBalance/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Transactions> getCheckBalance(
            @PathVariable("id") int customer_id,
            Authentication authentication) {
        try {
            Integer tokenCustomerId = getCustomerIdFromToken(authentication);

            if (authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                if (tokenCustomerId == null || !tokenCustomerId.equals(customer_id)) {
                    return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
                }
            }

            Transactions transaction = customerService.getCheckBalance(customer_id);
            return new ResponseEntity<>(transaction, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/CheckTransactionMode/{mode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<PaginationResponse<Transactions>> getTransactionByType(
            @PathVariable("mode") String transaction_mode,
            @RequestParam(defaultValue = "0") int page) {
        try {
            Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE);
            PaginationResponse<Transactions> response = customerService.getTransactionByType(transaction_mode, pageable);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/Banking")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Transactions> addTransactions(@RequestBody Transactions user) {
        try {
            Transactions transaction = customerService.addTransactions(user);
            return new ResponseEntity<>(transaction, HttpStatus.CREATED);
        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/payment/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> makePayment(
            @PathVariable int customerId,
            @RequestBody PaymentRequest paymentRequest,
            Authentication authentication) {
        try {
            Integer tokenCustomerId = getCustomerIdFromToken(authentication);

            if (authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                if (tokenCustomerId == null || !tokenCustomerId.equals(customerId)) {
                    return new ResponseEntity<>("Access Denied", HttpStatus.FORBIDDEN);
                }
            }

            Customers customer = customerService.getCustomerById(customerId);
            if (customer == null) {
                return new ResponseEntity<>("Customer not found", HttpStatus.NOT_FOUND);
            }

            if (paymentRequest.getTransactionAmount() <= 0) {
                return new ResponseEntity<>("Invalid amount", HttpStatus.BAD_REQUEST);
            }

            Transactions transaction = new Transactions();
            transaction.setCustomer_id(customerId);
            transaction.setTransaction_type(paymentRequest.getTransactionType());
            transaction.setTransaction_to(paymentRequest.getTransactionTo());
            transaction.setTransaction_amount(paymentRequest.getTransactionAmount());
            transaction.setTransaction_mode(paymentRequest.getTransactionMode());
            transaction.setTransaction_date(new Date(System.currentTimeMillis()));

            Transactions lastTransaction = customerService.getCheckBalance(customerId);
            long currentBalance = lastTransaction != null ? lastTransaction.getAvailable_balance() : customer.getAccount();

            if ("debit".equalsIgnoreCase(paymentRequest.getTransactionType())) {
                if (currentBalance < paymentRequest.getTransactionAmount()) {
                    return new ResponseEntity<>("Insufficient balance", HttpStatus.BAD_REQUEST);
                }
                long newBalance = currentBalance - paymentRequest.getTransactionAmount();
                transaction.setInitial_deposit(currentBalance);
                transaction.setAvailable_balance(newBalance);
            } else if ("credit".equalsIgnoreCase(paymentRequest.getTransactionType())) {
                long newBalance = currentBalance + paymentRequest.getTransactionAmount();
                transaction.setInitial_deposit(currentBalance);
                transaction.setAvailable_balance(newBalance);
            }

            Transactions savedTransaction = customerService.addTransactions(transaction);
            return new ResponseEntity<>(savedTransaction, HttpStatus.CREATED);

        } catch (Exception ex) {
            return new ResponseEntity<>("Payment failed: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Integer getCustomerIdFromToken(Authentication authentication) {
        String token = extractTokenFromAuth(authentication);
        if (token != null) {
            return jwtUtils.getCustomerIdFromToken(token);
        }
        return null;
    }

    private String extractTokenFromAuth(Authentication authentication) {
        if (authentication != null && authentication.getCredentials() instanceof String) {
            return (String) authentication.getCredentials();
        }
        return null;
    }
}
