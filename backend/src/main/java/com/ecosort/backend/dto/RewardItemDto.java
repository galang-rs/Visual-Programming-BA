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
public class RewardItemDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal requiredBalance;
    private Integer stockQuantity;
    private String imageUrl;
    private boolean active;
}
