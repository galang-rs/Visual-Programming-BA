package com.ecosort.backend.dto;

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
public class DepositDto {
    private Long id;
    private Long citizenId;
    private String citizenName;
    private String citizenPhoneNumber;
    private Long officerId;
    private String officerName;
    private Long categoryId;
    private String categoryName;
    private BigDecimal weightKg;
    private BigDecimal pricePerKg;
    private BigDecimal totalAmount;
    private String notes;
    private BigDecimal citizenBalanceAfter;
    private LocalDateTime createdAt;
}
