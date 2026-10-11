package com.ecosort.backend.dto;

import com.ecosort.backend.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthRequest {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Login {
        @NotBlank(message = "Nomor telepon tidak boleh kosong")
        private String phoneNumber;

        @NotBlank(message = "Password tidak boleh kosong")
        private String password;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Register {
        @NotBlank(message = "Nama lengkap tidak boleh kosong")
        private String fullName;

        @NotBlank(message = "Nomor telepon tidak boleh kosong")
        private String phoneNumber;

        @NotBlank(message = "Password tidak boleh kosong")
        @Size(min = 6, message = "Password minimal 6 karakter")
        private String password;

        private String address;

        private Role role; // Optional: defaults to WARGA if null
    }
}
