package com.example.pi.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import com.example.pi.dao.UserDao;
import com.example.pi.dao.support.NameParametersJdbcDaoSupportClass;
import com.example.pi.dto.response.PaginationResponse;
import com.example.pi.dto.response.UserDepartmentResponse;
import com.example.pi.util.PaginationUtil;

@Repository
public class UserDaoImpl extends NameParametersJdbcDaoSupportClass implements UserDao {

	@Override
	public PaginationResponse<UserDepartmentResponse> getUserDepartment(Pageable pageable) {
		try {
			int offset = PaginationUtil.calculateOffset(pageable.getPageNumber(), pageable.getPageSize());
			long totalElements = getTotalTransactionCount();

			String query = buildPaginatedQuery(
					"SELECT CUSTOMER_NAME, ACCOUNT_NUMBER, transaction_mode, transaction_id, transaction_amount, transaction_to, transaction_date "
							+ "FROM customers c JOIN transactions t ON c.CUSTOMER_ID = t.customer_id",
					pageable.getPageSize(), offset);

			List<UserDepartmentResponse> transactions = getNamedParameterJdbcTemplate().getJdbcOperations()
					.query(query, new Object[] { pageable.getPageSize(), offset },
							new BeanPropertyRowMapper<>(UserDepartmentResponse.class));

			return PaginationUtil.buildPaginationResponse(transactions, pageable, totalElements);
		} catch (Exception ex) {
			ex.printStackTrace();
			return PaginationUtil.emptyResponse();
		}
	}

	@Override
	public PaginationResponse<UserDepartmentResponse> getLatestTransactions(Pageable pageable) {
		try {
			int offset = PaginationUtil.calculateOffset(pageable.getPageNumber(), pageable.getPageSize());
			long totalElements = getTotalTransactionCount();

			String query = buildPaginatedQuery(
					"SELECT CUSTOMER_NAME, ACCOUNT_NUMBER, transaction_mode, transaction_id, transaction_amount, transaction_to, transaction_date "
							+ "FROM customers c JOIN transactions t ON c.CUSTOMER_ID = t.customer_id "
							+ "ORDER BY t.transaction_date DESC",
					pageable.getPageSize(), offset);

			List<UserDepartmentResponse> transactions = getNamedParameterJdbcTemplate().getJdbcOperations()
					.query(query, new Object[] { pageable.getPageSize(), offset },
							new BeanPropertyRowMapper<>(UserDepartmentResponse.class));

			return PaginationUtil.buildPaginationResponse(transactions, pageable, totalElements);
		} catch (Exception ex) {
			ex.printStackTrace();
			return PaginationUtil.emptyResponse();
		}
	}

	private String buildPaginatedQuery(String baseQuery, int pageSize, int offset) {
		return baseQuery + " LIMIT ? OFFSET ?";
	}

	private long getTotalTransactionCount() {
		String countQuery = "SELECT COUNT(*) FROM customers c JOIN transactions t ON c.CUSTOMER_ID = t.customer_id";
		Integer total = getNamedParameterJdbcTemplate().getJdbcOperations()
				.queryForObject(countQuery, Integer.class);
		return total != null ? total : 0;
	}
}
