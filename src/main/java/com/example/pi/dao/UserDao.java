package com.example.pi.dao;

import org.springframework.data.domain.Pageable;

import com.example.pi.dto.response.PaginationResponse;
import com.example.pi.dto.response.UserDepartmentResponse;

/**
 * @deprecated Legacy JDBC DAO kept only for the AllTransactionsHistory / LatestTransactions
 * endpoints.  Replaced by TransactionHistoryService (JPA) in the next refactor pass.
 * Do not add new methods here.
 */
@Deprecated
public interface UserDao {

    PaginationResponse<UserDepartmentResponse> getUserDepartment(Pageable pageable);
    PaginationResponse<UserDepartmentResponse> getLatestTransactions(Pageable pageable);

}
