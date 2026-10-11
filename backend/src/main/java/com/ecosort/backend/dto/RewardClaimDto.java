package com.ecosort.backend.dto;

import com.ecosort.backend.entity.ClaimStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardClaimDto {
    private Long id;
    private Long citizenId;
    private String citizenName;
    private String citizenPhoneNumber;
    private Long rewardItemId;
    private String rewardItemName;
    private BigDecimal requiredBalance;
    private String claimVoucherCode;
    private ClaimStatus status;
    private Long officerId;
    private String officerName;
    private LocalDateTime claimedAt;
    private LocalDateTime completedAt;
}
