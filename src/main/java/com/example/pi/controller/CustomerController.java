package com.example.pi.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.pi.service.CustomerService;
import com.example.pi.response.UserDepartmentResponse;
import com.example.pi.entity.Customers;
import com.example.pi.entity.Transactions;


@RestController
@RequestMapping("/customers")
public class CustomerController {
	
	@Autowired
	private CustomerService customerService;
	
	@GetMapping("/DisplayAllCustomers")
	public ResponseEntity<List<Customers>> getAllCustomers(){
		
		List<Customers> customers = null;
		
		try {
			customers = customerService.getAllCustomers();
			
		} catch (Exception ex) {
			ex.getMessage();
		}
		
		return new ResponseEntity<List<Customers>>(customers, HttpStatus.OK);
	}
	
	@GetMapping("/FindByCustomerID/{id}")
	@Cacheable(value = "Customer", key = "#userId")
	public ResponseEntity<Customers> getUserById(@PathVariable("id") int userId){
		
		Customers customers = null;
		
		try {
			customers = customerService.getCustomerById(userId);
			
		} catch (Exception ex) {
			ex.getMessage();
		}
		
		return new ResponseEntity<Customers>(customers, HttpStatus.OK);
	}
	
	@PostMapping("/AddCustomers") 
	public ResponseEntity<Customers> addCustomer(@RequestBody Customers user){
		
		Customers customers = null;
		
		try {
			customers = customerService.addCustomer(user);
			
		} catch (Exception ex) {
			ex.getMessage();
		}
		
		return new ResponseEntity<Customers>(customers, HttpStatus.OK);
	}
	
	@PutMapping("/UpdateCustomer") 
	@CachePut(value = "Customer", key = "#userId")
	public ResponseEntity<Customers> UpdateCustomer(@RequestBody Customers user){
		
		Customers customers = null;
		
		try {
			customers = customerService.UpdateCustomer(user);
			
		} catch (Exception ex) {
			ex.getMessage();
		}
		
		return new ResponseEntity<Customers>(customers, HttpStatus.OK);
	}
	
	@DeleteMapping("/DeleteCustomer/{id}")
	@CacheEvict(value = "Customer", allEntries=true)
	public ResponseEntity<Customers> deleteUserById(@PathVariable("id") int userId){
		
		Customers customers = null;
		
		try {
			customers = customerService.deleteCustomer(userId);
		} catch (Exception ex) {
			ex.getMessage();
		}
		
		return new ResponseEntity<Customers>(customers, HttpStatus.OK);
	}
	
	@GetMapping("/FilterCustomersByIDPoof/{id_type}")
	@Cacheable(value = "Customer", key = "#id_type")
	public ResponseEntity<List<Customers>> getCustomerByIdentityType(@PathVariable String id_type){
		
		List<Customers> customers = null;
		
		try {
			customers = customerService.getCustomerByIdentityType(id_type);
			
		} catch (Exception ex) {
			ex.getMessage();
		}
		return new ResponseEntity<List<Customers>>(customers, HttpStatus.OK);
	}
	
	@GetMapping("/AllTransactionsHistory")
	public ResponseEntity<List<UserDepartmentResponse>> getAllUserDepartments(){
		
		List<UserDepartmentResponse> transactions = null;
		
		try {
			transactions = customerService.getUserDepartMent();
			
		} catch (Exception ex) {
			ex.getMessage();
		}
		
		return new ResponseEntity<List<UserDepartmentResponse>>(transactions, HttpStatus.OK);
	}
	
	@GetMapping("/LatestTransactions")
	public ResponseEntity<List<UserDepartmentResponse>> getLatestTransactions(){
		
		List<UserDepartmentResponse> transactions = null;
		
		try {
			transactions = customerService.getLatestTransactions();
			
		} catch (Exception ex) {
			ex.getMessage();
		}
		
		return new ResponseEntity<List<UserDepartmentResponse>>(transactions, HttpStatus.OK);
	}
	
	@GetMapping("/Ministatement/{id}")
	@Cacheable(value = "Transactions", key = "#customer_id")
	public ResponseEntity<List<Transactions>> getCustomerByID(@PathVariable("id") int customer_id){
		
		List<Transactions> transactions = null;
		
		try {
			transactions = customerService.getCustomerByID(customer_id);
			
		} catch (Exception ex) {
			ex.getMessage();
		}
		return new ResponseEntity<List<Transactions>>(transactions, HttpStatus.OK);
	}
	
	
	@GetMapping("/CheckBalance/{id}")
	@Cacheable(value = "Transactions", key = "#customer_id")
	public ResponseEntity<List<Transactions>>getCheckBalance(@PathVariable("id") int customer_id){
		
		List<Transactions> transactions = null;
		
		try {
			transactions = customerService.getCheckBalance(customer_id);
			
		} catch (Exception ex) {
			ex.getMessage();
		}
		return new ResponseEntity<List<Transactions>>(transactions, HttpStatus.OK);
	}
	
	@GetMapping("/CheckTransactionMode/{mode}")
	@Cacheable(value = "Transactions", key = "#transaction_mode")
	public ResponseEntity<List<Transactions>> getTransactionByType(@PathVariable("mode") String transaction_mode){
		
		List<Transactions> transactions = null;
		
		try {
			transactions = customerService.getTransactionByType(transaction_mode);
			
		} catch (Exception ex) {
			ex.getMessage();
		}
		return new ResponseEntity<List<Transactions>>(transactions, HttpStatus.OK);
	}
	
	@PostMapping("/Banking") 
	@CachePut(value = "Transactions", key = "#user")
	public ResponseEntity<Transactions> addTransactions(@RequestBody Transactions user){
		
		Transactions transactions = null;
		
		try {
			transactions = customerService.addTransactions(user);
			
		} catch (Exception ex) {
			ex.getMessage();
		}
		
		return new ResponseEntity<Transactions>(transactions, HttpStatus.OK);
	}

	
}