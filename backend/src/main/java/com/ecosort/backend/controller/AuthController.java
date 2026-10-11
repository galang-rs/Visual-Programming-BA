package com.ecosort.backend.controller;

import com.ecosort.backend.dto.ApiResponse;
import com.ecosort.backend.dto.AuthRequest;
import com.ecosort.backend.dto.AuthResponse;
import com.ecosort.backend.dto.UserProfileResponse;
import com.ecosort.backend.entity.User;
import com.ecosort.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller Layer: AuthController
 * 
 * Gerbang Presentation Layer untuk melayani permintaan pendaftaran akun baru,
 * verifikasi masuk (login), dan pengambilan informasi profil pengguna saat ini.
 */
@Tag(name = "1. Autentikasi & Akun", description = "Endpoints untuk registrasi, login, dan manajemen profil pengguna")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Endpoint POST /api/auth/register
     * Mendaftarkan pengguna baru (Warga/Petugas) ke dalam sistem.
     * 
     * @param request Validasi DTO pendaftaran akun
     * @return Token JWT dan ringkasan akun terdaftar
     */
    @Operation(summary = "Pendaftaran Akun Baru", description = "Mendaftarkan pengguna baru dengan nomor telepon unik dan password terenkripsi BCrypt.")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody AuthRequest.Register request) {
        // [LOGIKA] Meneruskan data masukan ke AuthService untuk divalidasi dan disimpan
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.ok("Pendaftaran berhasil", response));
    }

    /**
     * Endpoint POST /api/auth/login
     * Otentikasi masuk pengguna menggunakan nomor telepon dan kata sandi.
     * 
     * @param request Validasi DTO login
     * @return Token JWT berotorisasi sesuai role pengguna
     */
    @Operation(summary = "Masuk ke Sistem (Login)", description = "Memverifikasi nomor telepon dan password, lalu menerbitkan JWT access token.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest.Login request) {
        // [LOGIKA] Memverifikasi kecocokan password BCrypt dan menerbitkan token sesi
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login berhasil", response));
    }

    /**
     * Endpoint GET /api/auth/profile
     * Mengambil profil nasabah yang sedang login berdasarkan token JWT di header.
     * 
     * @param currentUser Pengguna terotentikasi dari SecurityContext
     * @return Data profil nasabah lengkap dengan saldo dan riwayat berat sampah
     */
    @Operation(summary = "Cek Profil Pengguna (Me)", description = "Mengembalikan data profil pengguna saat ini beserta saldo tabungan dan total sampah yang disetor.")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@AuthenticationPrincipal User currentUser) {
        UserProfileResponse response = authService.getProfile(currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Profil berhasil diambil", response));
    }
}
