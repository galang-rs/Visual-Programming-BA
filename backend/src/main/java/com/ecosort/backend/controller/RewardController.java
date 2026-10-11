package com.ecosort.backend.controller;

import com.ecosort.backend.dto.*;
import com.ecosort.backend.entity.User;
import com.ecosort.backend.service.RewardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller Layer: RewardController
 * 
 * Melayani alur katalog paket sembako, pengajuan klaim penukaran saldo nasabah,
 * verifikasi kode voucher fisik oleh Petugas, serta riwayat voucher.
 */
@Tag(name = "4. Reward Sembako & Voucher Klaim", description = "Endpoints katalog reward, penukaran saldo, dan verifikasi voucher klaim")
@RestController
@RequestMapping("/api/rewards")
public class RewardController {

    @Autowired
    private RewardService rewardService;

    /**
     * Endpoint GET /api/rewards
     * Mengambil seluruh katalog reward sembako aktif beserta syarat saldo yang dibutuhkan.
     */
    @Operation(summary = "Daftar Item Reward Sembako", description = "Mengambil seluruh item reward aktif beserta stok dan harga saldo tabungan.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RewardItemDto>>> getAllRewards() {
        List<RewardItemDto> rewards = rewardService.getAllActiveRewards();
        return ResponseEntity.ok(ApiResponse.ok("Daftar reward berhasil dimuat", rewards));
    }

    /**
     * Endpoint POST /api/rewards/claim (Otorisasi: WARGA)
     * Mengajukan penukaran saldo tabungan nasabah dengan item reward sembako.
     * Menerbitkan kode voucher unik format ECO-XXXXXX.
     */
    @Operation(summary = "Klaim Paket Reward (Nasabah)", description = "Mendebit saldo nasabah dan menerbitkan kode voucher klaim digital unik berstatus PENDING.")
    @PostMapping("/claim")
    public ResponseEntity<ApiResponse<RewardClaimDto>> claimReward(
            @AuthenticationPrincipal User citizen,
            @Valid @RequestBody ClaimRewardRequest request) {
        RewardClaimDto response = rewardService.claimReward(citizen, request);
        return ResponseEntity.ok(ApiResponse.ok("Penukaran reward berhasil diproses", response));
    }

    /**
     * Endpoint POST /api/rewards/verify-claim (Otorisasi: PETUGAS)
     * Petugas memverifikasi kode voucher yang ditunjukkan nasabah saat serah terima barang fisik.
     */
    @Operation(summary = "Verifikasi Kode Voucher Fisik (Khusus Petugas)", description = "Memvalidasi kode voucher nasabah dan mengubah status menjadi COMPLETED saat penyerahan sembako.")
    @PostMapping("/verify-claim")
    public ResponseEntity<ApiResponse<RewardClaimDto>> verifyClaim(
            @AuthenticationPrincipal User officer,
            @Valid @RequestBody VerifyClaimRequest request) {
        RewardClaimDto response = rewardService.verifyClaim(officer, request);
        return ResponseEntity.ok(ApiResponse.ok("Voucher reward berhasil diverifikasi dan diserahkan", response));
    }

    /**
     * Endpoint GET /api/rewards/my-claims (Otorisasi: WARGA)
     * Mengambil daftar riwayat voucher klaim milik nasabah yang sedang login.
     */
    @Operation(summary = "Riwayat Voucher Klaim Saya (Nasabah)", description = "Mengambil riwayat voucher klaim milik nasabah yang sedang aktif login.")
    @GetMapping("/my-claims")
    public ResponseEntity<ApiResponse<List<RewardClaimDto>>> getMyClaims(@AuthenticationPrincipal User citizen) {
        List<RewardClaimDto> claims = rewardService.getCitizenClaims(citizen.getId());
        return ResponseEntity.ok(ApiResponse.ok("Riwayat klaim reward berhasil diambil", claims));
    }

    /**
     * Endpoint GET /api/rewards/all-claims (Otorisasi: PETUGAS)
     * Mengambil seluruh klaim reward di bank sampah untuk audit operasional.
     */
    @Operation(summary = "Semua Riwayat Klaim Voucher (Khusus Petugas)", description = "Mengambil seluruh riwayat klaim reward untuk keperluan audit petugas.")
    @GetMapping("/all-claims")
    public ResponseEntity<ApiResponse<List<RewardClaimDto>>> getAllClaims() {
        List<RewardClaimDto> claims = rewardService.getAllClaims();
        return ResponseEntity.ok(ApiResponse.ok("Seluruh riwayat klaim berhasil diambil", claims));
    }
}
