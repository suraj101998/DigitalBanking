package com.example.pi.dao.impl;

import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import com.example.pi.dao.UserDao;
import com.example.pi.dao.support.NameParametersJdbcDaoSupportClass;
import com.example.pi.response.UserDepartmentResponse;

@Repository
public class UserDaoImpl extends NameParametersJdbcDaoSupportClass implements UserDao {

	@Override
	public List<UserDepartmentResponse> getUserDepartment() {
		
		List<UserDepartmentResponse> transactions = null;
		try {
			String query = "select CUSTOMER_NAME,ACCOUNT_NUMBER,transaction_mode,transaction_id,transaction_amount,transaction_to,transaction_date from customers as c join transactions as t on c.CUSTOMER_ID = t.customer_id";
			transactions = getNamedParameterJdbcTemplate()
					.getJdbcOperations()
					.query(
							query, 
							new BeanPropertyRowMapper<UserDepartmentResponse>(
									UserDepartmentResponse.class
									));
		} catch (Exception ex) {
			ex.getStackTrace();
		}
		
		return transactions;
	}
	
	@Override
	public List<UserDepartmentResponse> getLatestTransactions() {
		
		List<UserDepartmentResponse> transactions = null;
		
		try {
			String query = "select CUSTOMER_NAME,ACCOUNT_NUMBER,transaction_mode,transaction_id,transaction_amount,transaction_to,transaction_date from customers as c join transactions as t on c.CUSTOMER_ID = t.customer_id order by transaction_date DESC limit 5";
			transactions = getNamedParameterJdbcTemplate()
					.getJdbcOperations()
					.query(
							query, 
							new BeanPropertyRowMapper<UserDepartmentResponse>(
									UserDepartmentResponse.class
									));
		} catch (Exception ex) {
			ex.getStackTrace();
		}
		
		return transactions;
	}
	

}