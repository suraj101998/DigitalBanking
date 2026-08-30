package com.example.pi.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.pi.entity.Customers;

/**
 * Repository for Customers.
 * Uses JpaRepository for full paging/sorting support (doc item #16).
 * Derived queries replace native SELECT * (doc items #16, #17).
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customers, Integer> {

    Page<Customers> findAll(Pageable pageable);

    /** Derived query – no native SQL needed (doc item #16). */
    Page<Customers> findByIdType(String idType, Pageable pageable);

    Optional<Customers> findByEmail(String email);

    // ── Legacy method name kept so existing service calls compile ──────────────

    /** @deprecated Use {@link #findByIdType} instead. */
    @Deprecated
    @Query("SELECT c FROM Customers c WHERE c.idType = :id_type")
    Page<Customers> getCustomerByIdentityType(@Param("id_type") String idType, Pageable pageable);
}
