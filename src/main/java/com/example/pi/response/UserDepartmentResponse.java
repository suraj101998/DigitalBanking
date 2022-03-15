package com.example.pi.response;
import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)


public class UserDepartmentResponse {
	
	private String CUSTOMER_NAME;
	private String ACCOUNT_NUMBER;
	private String transaction_type;
	private String transaction_id;
	private String transaction_to;
	private Date transaction_date;
	private int transaction_amount;
	private int customer_id;
	private int intial_deposit;
	private int available_balance;
	private int serial_number;
	private String transaction_mode;
	
	public String getTransaction_type() {
		return transaction_type;
	}
	public void setTransaction_type(String transaction_type) {
		this.transaction_type = transaction_type;
	}
	public String getTransaction_id() {
		return transaction_id;
	}
	public void setTransaction_id(String transaction_id) {
		this.transaction_id = transaction_id;
	}
	public String getTransaction_to() {
		return transaction_to;
	}
	public void setTransaction_to(String transaction_to) {
		this.transaction_to = transaction_to;
	}
	public Date getTransaction_date() {
		return transaction_date;
	}
	public void setTransaction_date(Date transaction_date) {
		this.transaction_date = transaction_date;
	}
	public String getCUSTOMER_NAME() {
		return CUSTOMER_NAME;
	}
	public void setCUSTOMER_NAME(String cUSTOMER_NAME) {
		CUSTOMER_NAME = cUSTOMER_NAME;
	}
	public String getACCOUNT_NUMBER() {
		return ACCOUNT_NUMBER;
	}
	public void setACCOUNT_NUMBER(String aCCOUNT_NUMBER) {
		ACCOUNT_NUMBER = aCCOUNT_NUMBER;
	}
	public int getTransaction_amount() {
		return transaction_amount;
	}
	public void setTransaction_amount(int transaction_amount) {
		this.transaction_amount = transaction_amount;
	}
	public int getCustomer_id() {
		return customer_id;
	}
	public void setCustomer_id(int customer_id) {
		this.customer_id = customer_id;
	}
	public int getIntial_deposit() {
		return intial_deposit;
	}
	public void setIntial_deposit(int intial_deposit) {
		this.intial_deposit = intial_deposit;
	}
	public int getAvailable_balance() {
		return available_balance;
	}
	public void setAvailable_balance(int available_balance) {
		this.available_balance = available_balance;
	}
	public int getSerial_number() {
		return serial_number;
	}
	public void setSerial_number(int serial_number) {
		this.serial_number = serial_number;
	}
	public String getTransaction_mode() {
		return transaction_mode;
	}
	public void setTransaction_mode(String transaction_mode) {
		this.transaction_mode = transaction_mode;
	}
	

}