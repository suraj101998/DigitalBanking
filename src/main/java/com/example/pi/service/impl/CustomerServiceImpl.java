package com.example.pi.service.impl;

import com.example.pi.dao.UserDao;
import com.example.pi.dto.response.CustomerResponse;
import com.example.pi.dto.response.PaginationResponse;
import com.example.pi.dto.response.UserDepartmentResponse;
import com.example.pi.entity.Customers;
import com.example.pi.entity.Transactions;
import com.example.pi.entity.enums.TransactionMode;
import com.example.pi.exception.CustomerNotFoundException;
import com.example.pi.exception.InvalidTransactionException;
import com.example.pi.repository.CustomerRepository;
import com.example.pi.repository.TransactionsRepository;
import com.example.pi.service.CustomerService;
import com.example.pi.util.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Customer service implementation.
 * - Returns CustomerResponse DTOs — no JPA entity leaks out of this layer (doc item #12).
 * - deleteCustomer performs soft delete (doc item #76).
 * - Constructor injection throughout (doc item #30).
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository     customerRepository;
    private final UserDao                userDao;
    private final TransactionsRepository transactionsRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                                UserDao userDao,
                                TransactionsRepository transactionsRepository) {
        this.customerRepository    = customerRepository;
        this.userDao               = userDao;
        this.transactionsRepository = transactionsRepository;
    }

    // ── Typed DTO methods ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<CustomerResponse> getAllCustomers(Pageable pageable) {
        Page<Customers> page = customerRepository.findAll(pageable);
        Page<CustomerResponse> dtoPage = page.map(this::toResponse);
        return PaginationUtil.buildPaginationResponse(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(int customerId) {
        return toResponse(customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId)));
    }

    @Override
    @Transactional
    public CustomerResponse addCustomer(Customers customer) {
        return toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(Customers customer) {
        customerRepository.findById(customer.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(customer.getCustomerId()));
        return toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public CustomerResponse softDeleteCustomer(int customerId) {
        Customers customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
        customer.setDeleted(true);
        customer.setDeletedAt(Instant.now());
        CustomerResponse response = toResponse(customer);
        customerRepository.save(customer);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<CustomerResponse> getCustomerByIdentityType(String idType, Pageable pageable) {
        Page<Customers> page = customerRepository.findByIdType(idType, pageable);
        return PaginationUtil.buildPaginationResponse(page.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<UserDepartmentResponse> getUserDepartMent(Pageable pageable) {
        return userDao.getUserDepartment(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<UserDepartmentResponse> getLatestTransactions(Pageable pageable) {
        return userDao.getLatestTransactions(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<Transactions> getCustomerByID(int customerId, Pageable pageable) {
        Page<Transactions> page = transactionsRepository.findByCustomerId(customerId, pageable);
        return PaginationUtil.buildPaginationResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public Transactions getCheckBalance(int customerId) {
        Pageable singleRow = PageRequest.of(0, 1);
        var list = transactionsRepository.findLatestByCustomerId(customerId, singleRow);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<Transactions> getTransactionByType(String transactionMode, Pageable pageable) {
        TransactionMode mode;
        try {
            mode = TransactionMode.valueOf(transactionMode.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidTransactionException(
                    "Invalid transaction mode '" + transactionMode + "'");
        }
        Page<Transactions> page = transactionsRepository.findByTransactionMode(mode, pageable);
        return PaginationUtil.buildPaginationResponse(page);
    }

    @Override
    @Transactional
    public Transactions addTransactions(Transactions user) {
        return transactionsRepository.save(user);
    }

    // ── Legacy bridge methods ──────────────────────────────────────────────────

    @Override
    @Deprecated
    @Transactional
    public Customers UpdateCustomer(Customers user) {
        return customerRepository.save(user);
    }

    @Override
    @Deprecated
    @Transactional
    public Customers deleteCustomer(int userId) {
        Customers c = customerRepository.findById(userId)
                .orElseThrow(() -> new CustomerNotFoundException(userId));
        c.setDeleted(true);
        c.setDeletedAt(Instant.now());
        customerRepository.save(c);
        return c;
    }

    @Override
    @Deprecated
    @Transactional
    public Customers addCustomer_entity(Customers user) {
        return customerRepository.save(user);
    }

    // ── Mapper ─────────────────────────────────────────────────────────────────

    private CustomerResponse toResponse(Customers c) {
        return new CustomerResponse(
                c.getCustomerId(),
                c.getName(),
                c.getAccountNumber(),
                c.getEmail(),
                c.getPhone(),
                c.getAddress(),
                c.getSex(),
                c.getDob(),
                c.getIdType(),
                c.getIdNum(),
                c.getAccountStatus()
        );
    }
}
