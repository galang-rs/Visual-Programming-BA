package com.ecosort.backend.repository;

import com.ecosort.backend.entity.WasteDeposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface WasteDepositRepository extends JpaRepository<WasteDeposit, Long> {
    List<WasteDeposit> findByCitizenIdOrderByCreatedAtDesc(Long citizenId);
    List<WasteDeposit> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(SUM(wd.weightKg), 0) FROM WasteDeposit wd")
    BigDecimal getTotalWeightDeposited();

    @Query("SELECT COALESCE(SUM(wd.totalAmount), 0) FROM WasteDeposit wd")
    BigDecimal getTotalDepositPayout();

    @Query("SELECT COALESCE(SUM(wd.weightKg), 0) FROM WasteDeposit wd WHERE wd.citizen.id = :citizenId")
    BigDecimal getTotalWeightByCitizenId(@Param("citizenId") Long citizenId);
}
