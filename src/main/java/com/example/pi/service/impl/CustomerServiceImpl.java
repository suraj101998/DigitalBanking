package com.example.pi.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.pi.dto.response.PaginationResponse;
import com.example.pi.dto.response.UserDepartmentResponse;
import com.example.pi.util.PaginationUtil;
import com.example.pi.dao.UserDao;
import com.example.pi.entity.Customers;
import com.example.pi.entity.Transactions;
import com.example.pi.service.CustomerService;
import com.example.pi.repository.CustomerRepository;
import com.example.pi.repository.TransactionsRepository;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	private CustomerRepository userRepository;

	@Autowired
	private UserDao userDao;

	@Autowired
	private TransactionsRepository TransactionsRepository;

	@Override
	@Transactional(readOnly = true)
	public PaginationResponse<Customers> getAllCustomers(Pageable pageable) {
		Page<Customers> page = userRepository.findAll(pageable);
		return PaginationUtil.buildPaginationResponse(page);
	}

	@Override
	@Transactional(readOnly = true)
	public Customers getCustomerById(int userId) {
		return userRepository.findById(userId).orElse(null);
	}

	@Override
	@Transactional
	public Customers addCustomer(Customers user) {
		return userRepository.save(user);
	}

	@Override
	@Transactional
	public Customers deleteCustomer(int userId) throws Exception {
		Customers deletedCustomer = userRepository.findById(userId).orElse(null);
		if (deletedCustomer == null) {
			throw new Exception("User is not available in database");
		}
		userRepository.deleteById(userId);
		return deletedCustomer;
	}

	@Override
	@Transactional(readOnly = true)
	public PaginationResponse<Customers> getCustomerByIdentityType(String id_type, Pageable pageable) {
		Page<Customers> page = userRepository.getCustomerByIdentityType(id_type, pageable);
		return PaginationUtil.buildPaginationResponse(page);
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
	public PaginationResponse<Transactions> getCustomerByID(int customer_id, Pageable pageable) {
		Page<Transactions> page = TransactionsRepository.getCustomerByID(customer_id, pageable);
		return PaginationUtil.buildPaginationResponse(page);
	}

	@Override
	@Transactional(readOnly = true)
	public Transactions getCheckBalance(int customer_id) {
		var transactions = TransactionsRepository.getCheckBalance(customer_id);
		return transactions.isEmpty() ? null : transactions.get(0);
	}

	@Override
	@Transactional(readOnly = true)
	public PaginationResponse<Transactions> getTransactionByType(String transaction_mode, Pageable pageable) {
		Page<Transactions> page = TransactionsRepository.getTransactionByType(transaction_mode, pageable);
		return PaginationUtil.buildPaginationResponse(page);
	}

	@Override
	@Transactional
	public Customers UpdateCustomer(Customers user) {
		return userRepository.save(user);
	}

	@Override
	@Transactional
	public Transactions addTransactions(Transactions user) {
		return TransactionsRepository.save(user);
	}
}
