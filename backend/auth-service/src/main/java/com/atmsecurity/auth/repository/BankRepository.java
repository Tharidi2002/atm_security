package com.atmsecurity.auth.repository;

import com.atmsecurity.auth.entity.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {
    Optional<Bank> findByCode(String code);
    List<Bank> findByActiveTrueOrderByNameAsc();
}
