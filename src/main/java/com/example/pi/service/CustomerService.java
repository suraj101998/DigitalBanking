package com.example.pi.service;

import org.springframework.data.domain.Pageable;

import com.example.pi.dto.response.PaginationResponse;
import com.example.pi.dto.response.UserDepartmentResponse;
import com.example.pi.entity.Customers;
import com.example.pi.entity.Transactions;

public interface CustomerService {

	PaginationResponse<Customers> getAllCustomers(Pageable pageable);
	Customers getCustomerById(int userId);
	Customers addCustomer(Customers user);
	Customers UpdateCustomer(Customers user);
	Customers deleteCustomer(int userId) throws Exception;

	PaginationResponse<Customers> getCustomerByIdentityType(String id_type, Pageable pageable);

	PaginationResponse<UserDepartmentResponse> getUserDepartMent(Pageable pageable);
	PaginationResponse<UserDepartmentResponse> getLatestTransactions(Pageable pageable);
	PaginationResponse<Transactions> getCustomerByID(int customer_id, Pageable pageable);
	Transactions getCheckBalance(int customer_id);
	PaginationResponse<Transactions> getTransactionByType(String transaction_mode, Pageable pageable);
	Transactions addTransactions(Transactions user);
}