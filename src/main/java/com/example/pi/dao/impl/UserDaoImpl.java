package com.example.pi.dao.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.example.pi.dao.UserDao;
import com.example.pi.dto.response.PaginationResponse;
import com.example.pi.dto.response.UserDepartmentResponse;
import com.example.pi.entity.Customers;
import com.example.pi.entity.Transactions;
import com.example.pi.repository.CustomerRepository;
import com.example.pi.repository.TransactionsRepository;
import com.example.pi.util.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Replacement for the legacy JDBC DAO (doc item #17).
 *
 * Original used raw JDBC + BeanPropertyRowMapper via NamedParameterJdbcDaoSupport.
 * This implementation uses JPA repositories — no raw SQL, no SELECT *, no silent exception swallowing.
 *
 * The old JDBC queries joined customers and transactions.  We replicate the same data
 * by loading both via JPA and projecting into UserDepartmentResponse.
 */
@Repository
@Deprecated  // Entire DAO layer is being replaced — see AllTransactionsHistory endpoint
public class UserDaoImpl implements UserDao {

    private final TransactionsRepository transactionsRepository;
    private final CustomerRepository     customerRepository;

    public UserDaoImpl(TransactionsRepository transactionsRepository,
                        CustomerRepository customerRepository) {
        this.transactionsRepository = transactionsRepository;
        this.customerRepository     = customerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<UserDepartmentResponse> getUserDepartment(Pageable pageable) {
        return buildJoinedResponse(pageable, Sort.by("transactionDate"));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<UserDepartmentResponse> getLatestTransactions(Pageable pageable) {
        return buildJoinedResponse(pageable, Sort.by(Sort.Direction.DESC, "transactionDate"));
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private PaginationResponse<UserDepartmentResponse> buildJoinedResponse(Pageable pageable, Sort sort) {
        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<Transactions> txPage = transactionsRepository.findAll(sorted);

        // Load all customers referenced by the transactions into a lookup map
        List<Integer> customerIds = txPage.getContent().stream()
                .map(Transactions::getCustomerId)
                .distinct()
                .collect(Collectors.toList());

        Map<Integer, Customers> customerMap = new HashMap<>();
        customerRepository.findAllById(customerIds)
                .forEach(c -> customerMap.put(c.getCustomerId(), c));

        List<UserDepartmentResponse> items = txPage.getContent().stream()
                .map(tx -> toResponse(tx, customerMap.get(tx.getCustomerId())))
                .collect(Collectors.toList());

        long total = txPage.getTotalElements();
        return PaginationUtil.buildPaginationResponse(items, pageable, total);
    }

    private UserDepartmentResponse toResponse(Transactions tx, Customers customer) {
        UserDepartmentResponse r = new UserDepartmentResponse();
        r.setTransaction_id(tx.getTransactionId());
        r.setTransaction_type(tx.getTransaction_type());
        r.setTransaction_mode(tx.getTransaction_mode());
        r.setTransaction_to(tx.getTransactionTo());
        r.setTransaction_date(tx.getTransaction_date());
        r.setTransaction_amount(tx.getTransactionAmount() != null
                ? tx.getTransactionAmount().intValue() : 0);
        r.setAvailable_balance(tx.getAvailableBalance() != null
                ? tx.getAvailableBalance().intValue() : 0);
        r.setIntial_deposit(tx.getInitialDeposit() != null
                ? tx.getInitialDeposit().intValue() : 0);
        r.setCustomer_id(tx.getCustomerId() != null ? tx.getCustomerId() : 0);
        r.setSerial_number(tx.getSerial_number());

        if (customer != null) {
            r.setCUSTOMER_NAME(customer.getName());
            r.setACCOUNT_NUMBER(customer.getAccountNumber());
        }
        return r;
    }
}
