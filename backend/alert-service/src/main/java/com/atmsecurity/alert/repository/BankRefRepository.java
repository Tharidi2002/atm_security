package com.atmsecurity.alert.repository;

import com.atmsecurity.alert.entity.BankRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankRefRepository extends JpaRepository<BankRef, Long> {
}
