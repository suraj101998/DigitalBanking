package com.example.pi.entity;

import java.sql.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;




// Entity annotation defines that a class can be mapped to table

@Entity(name = "customers")
@Table(name = "customers")
public class Customers {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CUSTOMER_ID")
	private int Customer_Id;

	@Column(name = "CUSTOMER_NAME")
	private String name;

	@Column(name = "ACCOUNT_NUMBER")
	private long account;
	
	@Column(name = "IDENTITY_TYPE")
	private String id_type;
	
	@Column(name = "IDENTITY_NUMBER")
	private String id_num;
	
	@Column(name = "DATE_OF_BIRTH")
	private Date dob;

	@Column(name = "MOBILE_NUMBER")
	private long phone;
	
	@Column(name = "EMAIL_ID")
	private String email;
	
	@Column(name = "ADDRESS")
	private String address;
	
	@Column(name = "SEX")
	private String sex;

	public int getCustomer_Id() {
		return Customer_Id;
	}

	public void setCustomer_Id(int customer_Id) {
		Customer_Id = customer_Id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getAccount() {
		return account;
	}

	public void setAccount(long account) {
		this.account = account;
	}

	public String getId_type() {
		return id_type;
	}

	public void setId_type(String id_type) {
		this.id_type = id_type;
	}

	public String getId_num() {
		return id_num;
	}

	public void setId_num(String id_num) {
		this.id_num = id_num;
	}

	public Date getDob() {
		return dob;
	}

	public void setDob(Date dob) {
		this.dob = dob;
	}

	public long getPhone() {
		return phone;
	}

	public void setPhone(long phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public Customers(int customer_Id, String name, long account, String id_type, String id_num, Date dob, long phone,
			String email, String address, String sex) {
		super();
		Customer_Id = customer_Id;
		this.name = name;
		this.account = account;
		this.id_type = id_type;
		this.id_num = id_num;
		this.dob = dob;
		this.phone = phone;
		this.email = email;
		this.address = address;
		this.sex = sex;
	}

	public Customers() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "Customers [Customer_Id=" + Customer_Id + ", name=" + name + ", account=" + account + ", id_type="
				+ id_type + ", id_num=" + id_num + ", dob=" + dob + ", phone=" + phone + ", email=" + email
				+ ", address=" + address + ", sex=" + sex + "]";
	}



}
