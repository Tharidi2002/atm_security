package com.atmsecurity.station.repository;

import com.atmsecurity.station.entity.BankRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankRefRepository extends JpaRepository<BankRef, Long> {
}
