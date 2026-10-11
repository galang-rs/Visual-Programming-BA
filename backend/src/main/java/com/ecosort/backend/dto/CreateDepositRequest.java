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
public class CreateDepositRequest {
    @NotNull(message = "ID nasabah wajib diisi")
    private Long citizenId;

    @NotNull(message = "Kategori sampah wajib diisi")
    private Long categoryId;

    @NotNull(message = "Berat timbangan wajib diisi")
    @DecimalMin(value = "0.01", message = "Berat sampah harus lebih besar dari 0 kg")
    private BigDecimal weightKg;

    private String notes;
}
