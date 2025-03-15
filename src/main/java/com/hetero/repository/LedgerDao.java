package com.hetero.repository;

import com.hetero.models.Ledger;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

//@Repository
//public interface LedgerDao extends JpaRepository<Ledger, Integer> {
//
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    Ledger findFirstByOrderByIdAsc();
//}

@Repository
public interface LedgerDao extends JpaRepository<Ledger, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Ledger> findFirstByOrderByIdAsc();
}