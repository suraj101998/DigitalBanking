package com.example.pi.entity;

import java.sql.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;


@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)

// Entity annotation defines that a class can be mapped to table

@Entity(name = "transactions")
@Table(name = "transactions")
public class Transactions {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	
	@Column(name = "serial_number")
	private int serial_number;

	@Column(name = "transaction_type")
	private String transaction_type;
	
	@Column(name = "transaction_id")
	private String transaction_id;
	
	@Column(name = "transaction_to")
	private String transaction_to;
	
	@Column(name = "transaction_date")
	private Date transaction_date;
	
	@Column(name = "transaction_amount")
	private int transaction_amount;
	
	@Column(name = "customer_id")
	private int customer_id;
	
	@Column(name = "initial_deposit")
	private int initial_deposit;
	
	@Column(name = "available_balance")
	private int available_balance;
	
	@Column(name = "transaction_mode")
	private String transaction_mode;
	
	
	public int getSerial_number() {
		return serial_number;
	}
	public void setSerial_number(int serial_number) {
		this.serial_number = serial_number;
	}
	
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
	public Date getTransaction_date() {
		return transaction_date;
	}
	public void setTransaction_date(Date transaction_date) {
		this.transaction_date = transaction_date;
	}
	public String getTransaction_to() {
		return transaction_to;
	}
	public void setTransaction_to(String transaction_to) {
		this.transaction_to = transaction_to;
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
	public int getInitial_deposit() {
		return initial_deposit;
	}
	public void setInitial_deposit(int initial_deposit) {
		this.initial_deposit = initial_deposit;
	}
	public int getAvailable_balance() {
		return available_balance;
	}
	public void setAvailable_balance(int available_balance) {
		this.available_balance = available_balance;
	}
	
	public String getTransaction_mode() {
		return transaction_mode;
	}
	public void setTransaction_mode(String transaction_mode) {
		this.transaction_mode = transaction_mode;
	}
	
//	public Transactions(String transaction_type, String transaction_id, String transaction_to, Date transaction_date,
//			int transaction_amount, int customer_id, int intial_deposit, int available_balance) {
//		super();
//		this.transaction_type = transaction_type;
//		this.transaction_id = transaction_id;
//		this.transaction_to = transaction_to;
//		this.transaction_date = transaction_date;
//		this.transaction_amount = transaction_amount;
//		this.customer_id = customer_id;
//		this.intial_deposit = intial_deposit;
//		this.available_balance = available_balance;
//	}
	
	
	public Transactions(int serial_number, String transaction_type, String transaction_id, String transaction_to,
			Date transaction_date, int transaction_amount, int customer_id, int initial_deposit, int available_balance,String transaction_mode) {
		super();
		this.serial_number = serial_number;
		this.transaction_type = transaction_type;
		this.transaction_id = transaction_id;
		this.transaction_to = transaction_to;
		this.transaction_date = transaction_date;
		this.transaction_amount = transaction_amount;
		this.customer_id = customer_id;
		this.initial_deposit = initial_deposit;
		this.available_balance = available_balance;
		this.transaction_mode = transaction_mode;
	}
	
	
	public Transactions() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	public String toString() {
		return "Transactions [serial_number=" + serial_number + ", transaction_type=" + transaction_type
				+ ", transaction_id=" + transaction_id + ", transaction_to=" + transaction_to + ", transaction_date="
				+ transaction_date + ", transaction_amount=" + transaction_amount + ", customer_id=" + customer_id
				+ ", initial_deposit=" + initial_deposit + ", available_balance=" + available_balance + ", transaction_mode=" + transaction_type + "]";
	}
	
	
	
//	@Override
//	public String toString() {
//		return "Transactions [transaction_type=" + transaction_type + ", transaction_id=" + transaction_id
//				+ ", transaction_to=" + transaction_to + ", transaction_date=" + transaction_date
//				+ ", transaction_amount=" + transaction_amount + ", customer_id=" + customer_id + ", intial_deposit="
//				+ intial_deposit + ", available_balance=" + available_balance + "]";
//	}

	
}