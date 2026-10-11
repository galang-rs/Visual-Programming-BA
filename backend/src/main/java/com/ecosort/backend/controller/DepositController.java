package com.ecosort.backend.controller;

import com.ecosort.backend.dto.ApiResponse;
import com.ecosort.backend.dto.CreateDepositRequest;
import com.ecosort.backend.dto.DepositDto;
import com.ecosort.backend.entity.User;
import com.ecosort.backend.service.DepositService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller Layer: DepositController
 * 
 * Melayani transaksi penyetoran sampah fisik di pos penimbangan oleh Petugas,
 * serta penyajian riwayat transaksi baik untuk nasabah individu maupun audit pengurus.
 */
@Tag(name = "3. Penyetoran Sampah (Pos Petugas)", description = "Endpoints pencatatan penimbangan sampah fisik dan riwayat setoran")
@RestController
@RequestMapping("/api/deposits")
public class DepositController {

    @Autowired
    private DepositService depositService;

    /**
     * Endpoint POST /api/deposits (Otorisasi: PETUGAS)
     * Mencatat penimbangan fisik sampah nasabah, mengkredit saldo, dan mencatat buku kas secara atomik.
     * 
     * @param officer Akun petugas yang sedang login
     * @param request Data permohonan setoran (citizenId, categoryId, weightKg, notes)
     * @return Struk digital resmi transaksi penyetoran
     */
    @Operation(summary = "Input Setoran Fisik Sampah (Khusus Petugas)", description = "Mencatat berat timbangan fisik sampah, menghitung rupiah otomatis di peladen, mengkredit saldo nasabah, dan memperbarui buku kas.")
    @PostMapping
    public ResponseEntity<ApiResponse<DepositDto>> createDeposit(
            @AuthenticationPrincipal User officer,
            @Valid @RequestBody CreateDepositRequest request) {
        DepositDto response = depositService.createDeposit(officer, request);
        return ResponseEntity.ok(ApiResponse.ok("Penyetoran sampah berhasil dicatat dan diverifikasi", response));
    }

    /**
     * Endpoint GET /api/deposits/my-deposits (Otorisasi: WARGA)
     * Mengambil riwayat setoran milik nasabah yang sedang aktif login.
     * 
     * @param citizen Akun nasabah yang sedang login
     * @return Daftar riwayat setoran nasabah
     */
    @Operation(summary = "Riwayat Setoran Saya (Nasabah)", description = "Mengambil seluruh riwayat transaksi setoran sampah milik nasabah yang sedang login.")
    @GetMapping("/my-deposits")
    public ResponseEntity<ApiResponse<List<DepositDto>>> getMyDeposits(@AuthenticationPrincipal User citizen) {
        List<DepositDto> deposits = depositService.getCitizenDeposits(citizen.getId());
        return ResponseEntity.ok(ApiResponse.ok("Riwayat penyetoran berhasil diambil", deposits));
    }

    /**
     * Endpoint GET /api/deposits/all (Otorisasi: PETUGAS)
     * Mengambil seluruh transaksi setoran di bank sampah untuk audit operasional.
     * 
     * @return Daftar seluruh setoran semua nasabah
     */
    @Operation(summary = "Semua Riwayat Setoran (Khusus Petugas)", description = "Mengambil seluruh transaksi setoran sampah semua nasabah untuk keperluan audit.")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<DepositDto>>> getAllDeposits() {
        List<DepositDto> deposits = depositService.getAllDeposits();
        return ResponseEntity.ok(ApiResponse.ok("Seluruh riwayat penyetoran berhasil diambil", deposits));
    }
}
