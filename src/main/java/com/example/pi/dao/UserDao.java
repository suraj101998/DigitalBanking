package com.example.pi.dao;


import java.util.List;

import com.example.pi.response.UserDepartmentResponse;
public interface UserDao {

	public List<UserDepartmentResponse> getUserDepartment();
	public List<UserDepartmentResponse> getLatestTransactions();

}
