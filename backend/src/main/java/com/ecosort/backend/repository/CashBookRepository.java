package com.ecosort.backend.repository;

import com.ecosort.backend.entity.CashBookEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CashBookRepository extends JpaRepository<CashBookEntry, Long> {
    List<CashBookEntry> findAllByOrderByCreatedAtDesc();
    Optional<CashBookEntry> findTopByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(SUM(CASE WHEN c.transactionType = 'CASH_INJECTION' THEN c.amount ELSE -c.amount END), 0) FROM CashBookEntry c")
    BigDecimal getCurrentCashBalance();
}
