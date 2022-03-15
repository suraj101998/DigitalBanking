package com.example.pi;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.pi.models.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUserName(String userName);
}