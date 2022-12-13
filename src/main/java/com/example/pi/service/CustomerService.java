package com.example.pi.service;

import java.util.List;

import com.example.pi.response.UserDepartmentResponse;
import com.example.pi.entity.Customers;
import com.example.pi.entity.Transactions;

public interface CustomerService {
	
	public List<Customers> getAllCustomers(); //1.get all customers
	public Customers getCustomerById(int userId);//2.find individual customers
	public Customers addCustomer(Customers user);//3.add new customer
	public Customers UpdateCustomer(Customers user);//4.Update Exsisting Customers
	public Customers deleteCustomer(int userId) throws Exception;//5.delete a customer
	
	public List<Customers> getCustomerByIdentityType(String id_type);//6.find customers by id proof submitted
	
	public List<UserDepartmentResponse> getUserDepartMent();// 7.all transactions history
	public List<UserDepartmentResponse> getLatestTransactions();//8.show latest transactions by date
	public List<Transactions> getCustomerByID(int customer_id);//9.check individual ministatement
	public List<Transactions> getCheckBalance(int customer_id);//10.check balance
	public List<Transactions> getTransactionByType(String transaction_mode);//11.filter by transaction type
	public Transactions addTransactions(Transactions user);//12.transfer or deposit
}