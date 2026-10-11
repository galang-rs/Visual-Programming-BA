package com.ecosort.backend.service;

import com.ecosort.backend.dto.CashBookDto;
import com.ecosort.backend.dto.DashboardStatsDto;
import com.ecosort.backend.entity.CashBookEntry;
import com.ecosort.backend.entity.Role;
import com.ecosort.backend.repository.CashBookRepository;
import com.ecosort.backend.repository.UserRepository;
import com.ecosort.backend.repository.WasteDepositRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Layer: CashBookService
 * 
 * Bertanggung jawab mengelola buku kas digital bank sampah,
 * mencatat audit trail setiap mutasi keuangan (setoran nasabah & penukaran reward),
 * serta menghitung indikator performa agregat (total sampah, perputaran uang kas,
 * dan estimasi reduksi emisi karbon CO2 untuk dampak lingkungan hidup).
 */
@Service
public class CashBookService {

    @Autowired
    private CashBookRepository cashBookRepository;

    @Autowired
    private WasteDepositRepository depositRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Mengambil seluruh catatan mutasi buku kas operasional.
     * Digunakan oleh Petugas/Pengurus untuk keperluan audit dan transparansi pembukuan.
     * 
     * @return List CashBookDto entri buku kas diurutkan dari transaksi terbaru
     */
    public List<CashBookDto> getAllEntries() {
        return cashBookRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Menghitung metrik performa utama (KPI) bank sampah secara terpusat:
     * 1. Akumulasi total berat sampah daur ulang yang terkumpul (kg).
     * 2. Total nominal rupiah insentif yang telah didistribusikan ke warga.
     * 3. Sisa saldo kas operasional fisik yang tersedia di kas bank sampah.
     * 4. Jumlah transaksi setoran dan jumlah nasabah aktif.
     * 5. Dampak lingkungan: estimasi reduksi jejak karbon (CO2 reduction)
     *    berdasarkan rasio standar 1 kg sampah daur ulang setara ~2.5 kg reduksi emisi CO2 eq.
     * 
     * @return DashboardStatsDto ringkasan data analitik untuk dasbor
     */
    public DashboardStatsDto getDashboardStats() {
        // [LOGIKA 1] Agregasi Query Database untuk Indikator Utama
        BigDecimal totalWeight = depositRepository.getTotalWeightDeposited();
        BigDecimal totalPayout = depositRepository.getTotalDepositPayout();
        BigDecimal cashBalance = cashBookRepository.getCurrentCashBalance();
        long depositsCount = depositRepository.count();
        long citizensCount = userRepository.findByRole(Role.WARGA).size();

        // [LOGIKA 2] Formula Dampak Lingkungan (Eco Metric Formula):
        // Setiap 1 kg sampah terkelola mereduksi rata-rata 2.5 kg emisi gas rumah kaca CO2
        BigDecimal co2Saved = (totalWeight != null ? totalWeight : BigDecimal.ZERO)
                .multiply(BigDecimal.valueOf(2.5))
                .setScale(2, RoundingMode.HALF_UP);

        return DashboardStatsDto.builder()
                .totalWeightCollected(totalWeight != null ? totalWeight : BigDecimal.ZERO)
                .totalPayoutDistributed(totalPayout != null ? totalPayout : BigDecimal.ZERO)
                .currentCashBalance(cashBalance != null ? cashBalance : BigDecimal.ZERO)
                .totalDepositsCount(depositsCount)
                .totalCitizensCount(citizensCount)
                .estimatedCo2ReducedKg(co2Saved)
                .build();
    }

    /**
     * Konversi entitas JPA CashBookEntry ke DTO untuk JSON API.
     */
    private CashBookDto toDto(CashBookEntry entry) {
        return CashBookDto.builder()
                .id(entry.getId())
                .transactionType(entry.getTransactionType())
                .amount(entry.getAmount())
                .balanceAfter(entry.getBalanceAfter())
                .referenceId(entry.getReferenceId())
                .description(entry.getDescription())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}
