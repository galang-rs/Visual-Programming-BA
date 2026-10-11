package com.ecosort.backend.dto;

import com.ecosort.backend.entity.Role;
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
public class UserProfileResponse {
    private Long id;
    private String fullName;
    private String phoneNumber;
    private Role role;
    private String address;
    private BigDecimal balance;
    private BigDecimal totalWeightDeposited;
    private LocalDateTime createdAt;
}
