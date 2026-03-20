package com.example.pi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.pi.models.User;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    User findByUserName(String userName);
}
