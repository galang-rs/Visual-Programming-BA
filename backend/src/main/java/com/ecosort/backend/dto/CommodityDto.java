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
public class CommodityDto {
    private Long id;
    private String name;
    private String description;
    private String unit;
    private BigDecimal pricePerKg;
    private String cleaningInstructions;
    private String iconUrl;
    private boolean active;
}
