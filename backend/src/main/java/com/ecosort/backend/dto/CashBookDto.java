package com.ecosort.backend.dto;

import com.ecosort.backend.entity.CashBookType;
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
public class CashBookDto {
    private Long id;
    private CashBookType transactionType;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private Long referenceId;
    private String description;
    private LocalDateTime createdAt;
}
