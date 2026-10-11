package com.ecosort.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyClaimRequest {
    @NotBlank(message = "Kode voucher klaim wajib diisi")
    private String claimVoucherCode;
}
