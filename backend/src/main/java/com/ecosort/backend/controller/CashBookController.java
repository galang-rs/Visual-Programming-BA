package com.ecosort.backend.controller;

import com.ecosort.backend.dto.ApiResponse;
import com.ecosort.backend.dto.CashBookDto;
import com.ecosort.backend.dto.DashboardStatsDto;
import com.ecosort.backend.service.CashBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller Layer: CashBookController
 * 
 * Melayani audit trail buku kas digital dan data ringkasan KPI dasbor bank sampah.
 */
@Tag(name = "5. Buku Kas & Analitik Operasional", description = "Endpoints mutasi buku kas operasional dan statistik dasbor bank sampah")
@RestController
@RequestMapping("/api/cashbook")
public class CashBookController {

    @Autowired
    private CashBookService cashBookService;

    /**
     * Endpoint GET /api/cashbook (Otorisasi: PETUGAS)
     * Mengambil seluruh catatan mutasi pergerakan kas masuk dan keluar.
     */
    @Operation(summary = "Daftar Mutasi Buku Kas (Khusus Petugas)", description = "Mengambil seluruh entri mutasi keuangan operasional bank sampah.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CashBookDto>>> getAllEntries() {
        List<CashBookDto> entries = cashBookService.getAllEntries();
        return ResponseEntity.ok(ApiResponse.ok("Buku kas berhasil dimuat", entries));
    }

    /**
     * Endpoint GET /api/cashbook/stats
     * Mengambil metrik analitik: total berat sampah, total payout, saldo kas, dan estimasi reduksi CO2.
     */
    @Operation(summary = "Statistik Dasbor & Reduksi Emisi CO2", description = "Mengambil ringkasan indikator performa bank sampah dan dampak pelestarian lingkungan.")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getDashboardStats() {
        DashboardStatsDto stats = cashBookService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.ok("Statistik sistem berhasil dimuat", stats));
    }
}
