package com.example.pi.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.pi.entity.Transactions;


@Repository
public interface TransactionsRepository extends CrudRepository<Transactions, Integer> {

	@Query(value="SELECT * from transactions t where t.customer_id = ?1",nativeQuery =true)
	Page<Transactions> getCustomerByID(@Param("customer_id") int customer_id, Pageable pageable);
	
	@Query(value="SELECT * from transactions t where t.customer_id = ?1 order by transaction_date DESC limit 1",nativeQuery =true)
	public List<Transactions> getCheckBalance(@Param("customer_id")int customer_id);
	
	@Query(value="SELECT * from transactions t where t.transaction_mode = ?1",nativeQuery =true)
	Page<Transactions> getTransactionByType(@Param("transaction_mode") String transaction_mode, Pageable pageable);


}