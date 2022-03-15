package com.example.pi.service;

import java.util.List;

import com.example.pi.response.UserDepartmentResponse;
import com.example.pi.entity.Customers;
import com.example.pi.entity.Transactions;

public interface CustomerService {
	
	public List<Customers> getAllCustomers(); //get all customers
	public Customers getCustomerById(int userId);//find individual customers
	public Customers addCustomer(Customers user);//add new customer
	public Customers UpdateCustomer(Customers user);//Update Exsisting Customers
	public Customers deleteCustomer(int userId) throws Exception;//delete a customer
	
	public List<Customers> getCustomerByIdentityType(String id_type);//find customers by id proof submitted
	
	public List<UserDepartmentResponse> getUserDepartMent();// all transactions history
	public List<UserDepartmentResponse> getLatestTransactions();//show latest transactions by date
	public List<Transactions> getCustomerByID(int customer_id);//check individual ministatement
	public List<Transactions> getCheckBalance(int customer_id);//check balance
	public List<Transactions> getTransactionByType(String transaction_mode);//filter by transaction type
	public Transactions addTransactions(Transactions user);//transfer or deposit
}