package com.ecosort.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculateEstimateResponse {
    private Long categoryId;
    private String categoryName;
    private BigDecimal pricePerKg;
    private BigDecimal estimatedWeightKg;
    private BigDecimal estimatedTotalPayout;
}
