package com.ecosort.backend.repository;

import com.ecosort.backend.entity.RewardClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RewardClaimRepository extends JpaRepository<RewardClaim, Long> {
    List<RewardClaim> findByCitizenIdOrderByClaimedAtDesc(Long citizenId);
    Optional<RewardClaim> findByClaimVoucherCode(String claimVoucherCode);
    boolean existsByClaimVoucherCode(String claimVoucherCode);
    List<RewardClaim> findAllByOrderByClaimedAtDesc();
}
