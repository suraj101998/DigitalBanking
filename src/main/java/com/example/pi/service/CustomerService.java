package com.example.pi.service;

import org.springframework.data.domain.Pageable;

import com.example.pi.dto.response.CustomerResponse;
import com.example.pi.dto.response.PaginationResponse;
import com.example.pi.dto.response.UserDepartmentResponse;
import com.example.pi.entity.Customers;
import com.example.pi.entity.Transactions;

public interface CustomerService {

    PaginationResponse<CustomerResponse> getAllCustomers(Pageable pageable);
    CustomerResponse getCustomerById(int customerId);
    CustomerResponse addCustomer(Customers customer);
    CustomerResponse updateCustomer(Customers customer);

    /**
     * Soft delete — marks the customer as deleted without physical removal (doc item #76).
     * Returns the customer DTO before deletion.
     */
    CustomerResponse softDeleteCustomer(int customerId);

    PaginationResponse<CustomerResponse> getCustomerByIdentityType(String idType, Pageable pageable);

    PaginationResponse<UserDepartmentResponse> getUserDepartMent(Pageable pageable);
    PaginationResponse<UserDepartmentResponse> getLatestTransactions(Pageable pageable);
    PaginationResponse<Transactions> getCustomerByID(int customerId, Pageable pageable);
    Transactions getCheckBalance(int customerId);
    PaginationResponse<Transactions> getTransactionByType(String transactionMode, Pageable pageable);
    Transactions addTransactions(Transactions user);

    /** @deprecated use the typed methods above instead. */
    @Deprecated
    Customers UpdateCustomer(Customers user);
    @Deprecated
    Customers deleteCustomer(int userId);
    @Deprecated
    Customers addCustomer_entity(Customers user);
}
