package com.ecosort.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculateEstimateRequest {
    @NotNull(message = "ID kategori sampah wajib diisi")
    private Long categoryId;

    @NotNull(message = "Berat estimasi wajib diisi")
    @DecimalMin(value = "0.01", message = "Berat sampah harus lebih besar dari 0 kg")
    private BigDecimal estimatedWeightKg;
}
