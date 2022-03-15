package com.example.pi.repository;

import java.util.List;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.pi.entity.Customers;

@Repository
public interface CustomerRepository extends CrudRepository<Customers, Integer> {
	
	//Custom Query to find customer by their ID type
	
	@Query(value="SELECT * from customers ud where ud.IDENTITY_TYPE = ?1",nativeQuery =true)
	public List<Customers> getCustomerByIdentityType(@Param("id_type")String id_type);




	
	
}