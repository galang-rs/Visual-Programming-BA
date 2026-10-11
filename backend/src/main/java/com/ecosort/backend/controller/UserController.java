package com.ecosort.backend.controller;

import com.ecosort.backend.dto.ApiResponse;
import com.ecosort.backend.dto.UserProfileResponse;
import com.ecosort.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller Layer: UserController
 * 
 * Melayani pencarian dan manajemen data nasabah (Role: WARGA) oleh Petugas Bank Sampah.
 */
@Tag(name = "6. Manajemen Nasabah Warga", description = "Endpoints pencarian dan daftar nasabah untuk pos penimbangan")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Endpoint GET /api/users/citizens (Otorisasi: PETUGAS)
     * Mengambil dan memfilter daftar warga berdasarkan nama atau nomor telepon.
     * Digunakan saat petugas memilih nasabah yang datang menyetor sampah.
     * 
     * @param query Kata kunci pencarian (opsional)
     * @return Daftar profil nasabah lengkap dengan saldo dan total sampah yang disetor
     */
    @Operation(summary = "Daftar & Cari Nasabah Warga (Khusus Petugas)", description = "Mencari data nasabah warga berdasarkan nama atau nomor telepon untuk transaksi setoran.")
    @GetMapping("/citizens")
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> searchCitizens(
            @RequestParam(required = false) String query) {
        List<UserProfileResponse> citizens = userService.searchCitizens(query);
        return ResponseEntity.ok(ApiResponse.ok("Daftar nasabah berhasil diambil", citizens));
    }
}
