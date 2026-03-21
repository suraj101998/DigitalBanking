package com.example.pi.dao;

import org.springframework.data.domain.Pageable;

import com.example.pi.dto.response.PaginationResponse;
import com.example.pi.dto.response.UserDepartmentResponse;

public interface UserDao {

	PaginationResponse<UserDepartmentResponse> getUserDepartment(Pageable pageable);
	PaginationResponse<UserDepartmentResponse> getLatestTransactions(Pageable pageable);

}
