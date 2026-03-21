package com.example.pi.service.impl;

import java.sql.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.pi.entity.Customers;
import com.example.pi.entity.Transactions;
import com.example.pi.repository.CustomerRepository;
import com.example.pi.repository.TransactionsRepository;
import com.example.pi.service.TransactionService;
import com.example.pi.util.PaginationUtil;
import com.example.pi.dto.response.BalanceResponse;
import com.example.pi.dto.response.PaginationResponse;
import com.example.pi.dto.response.TransactionResponse;

@Service
public class TransactionServiceImpl implements TransactionService {

	@Autowired
	private TransactionsRepository transactionsRepository;

	@Autowired
	private CustomerRepository customerRepository;

	@Override
	@Transactional
	public TransactionResponse createTransaction(int customerId, String transactionType, String transactionTo,
	                                            long transactionAmount, String transactionMode) throws Exception {
		// Validate customer exists
		Customers customer = customerRepository.findById(customerId)
				.orElseThrow(() -> new Exception("Customer not found"));

		// Validate transaction amount
		if (transactionAmount <= 0) {
			throw new Exception("Invalid transaction amount");
		}

		// Validate transaction type
		if (!isValidTransactionType(transactionType)) {
			throw new Exception("Invalid transaction type. Must be 'credit' or 'debit'");
		}

		// Get current balance
		Transactions lastTransaction = getLastTransaction(customerId);
		long currentBalance = lastTransaction != null ? lastTransaction.getAvailable_balance() : customer.getAccount();

		// Calculate new balance based on transaction type
		long newBalance;
		if ("debit".equalsIgnoreCase(transactionType)) {
			// Validate sufficient balance for debit
			if (currentBalance < transactionAmount) {
				throw new Exception("Insufficient balance. Current: " + currentBalance + ", Requested: " + transactionAmount);
			}
			newBalance = currentBalance - transactionAmount;
		} else {
			// Credit transaction
			newBalance = currentBalance + transactionAmount;
		}

		// Create and save transaction
		Transactions transaction = new Transactions();
		transaction.setTransaction_id(generateTransactionId());
		transaction.setCustomer_id(customerId);
		transaction.setTransaction_type(transactionType);
		transaction.setTransaction_to(transactionTo);
		transaction.setTransaction_amount(transactionAmount);
		transaction.setTransaction_mode(transactionMode);
		transaction.setTransaction_date(new Date(System.currentTimeMillis()));
		transaction.setInitial_deposit(currentBalance);
		transaction.setAvailable_balance(newBalance);

		Transactions saved = transactionsRepository.save(transaction);

		return mapToTransactionResponse(saved);
	}

	private String generateTransactionId() {
		return "TXN" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
	}

	@Override
	@Transactional(readOnly = true)
	public PaginationResponse<Transactions> getCustomerTransactions(int customerId, Pageable pageable) {
		Page<Transactions> page = transactionsRepository.getCustomerByID(customerId, pageable);
		return PaginationUtil.buildPaginationResponse(page);
	}

	@Override
	@Transactional(readOnly = true)
	public BalanceResponse getBalance(int customerId) {
		Transactions lastTransaction = getLastTransaction(customerId);
		Customers customer = customerRepository.findById(customerId).orElse(null);

		long availableBalance = lastTransaction != null ? lastTransaction.getAvailable_balance()
				: (customer != null ? customer.getAccount() : 0);
		long initialDeposit = lastTransaction != null ? lastTransaction.getInitial_deposit()
				: (customer != null ? customer.getAccount() : 0);

		return new BalanceResponse(customerId, availableBalance, initialDeposit);
	}

	@Override
	@Transactional(readOnly = true)
	public PaginationResponse<Transactions> getTransactionsByType(String transactionMode, Pageable pageable) {
		Page<Transactions> page = transactionsRepository.getTransactionByType(transactionMode, pageable);
		return PaginationUtil.buildPaginationResponse(page);
	}

	private Transactions getLastTransaction(int customerId) {
		var transactions = transactionsRepository.getCheckBalance(customerId);
		return transactions.isEmpty() ? null : transactions.get(0);
	}

	private boolean isValidTransactionType(String type) {
		return type != null && (type.equalsIgnoreCase("credit") || type.equalsIgnoreCase("debit"));
	}

	private TransactionResponse mapToTransactionResponse(Transactions transaction) {
		return new TransactionResponse(
			transaction.getSerial_number(),
			transaction.getTransaction_type(),
			transaction.getTransaction_id(),
			transaction.getTransaction_to(),
			transaction.getTransaction_date(),
			transaction.getTransaction_amount(),
			transaction.getCustomer_id(),
			transaction.getInitial_deposit(),
			transaction.getAvailable_balance(),
			transaction.getTransaction_mode()
		);
	}
}
