package com.example.pi.dao;

import org.springframework.data.domain.Pageable;

import com.example.pi.response.PaginationResponse;
import com.example.pi.response.UserDepartmentResponse;

public interface UserDao {

	PaginationResponse<UserDepartmentResponse> getUserDepartment(Pageable pageable);
	PaginationResponse<UserDepartmentResponse> getLatestTransactions(Pageable pageable);

}
