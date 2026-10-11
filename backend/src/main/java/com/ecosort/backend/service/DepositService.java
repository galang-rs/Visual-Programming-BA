package com.ecosort.backend.service;

import com.ecosort.backend.dto.CreateDepositRequest;
import com.ecosort.backend.dto.DepositDto;
import com.ecosort.backend.entity.*;
import com.ecosort.backend.exception.BadRequestException;
import com.ecosort.backend.exception.ResourceNotFoundException;
import com.ecosort.backend.repository.CashBookRepository;
import com.ecosort.backend.repository.UserRepository;
import com.ecosort.backend.repository.WasteCategoryRepository;
import com.ecosort.backend.repository.WasteDepositRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Layer: DepositService
 * 
 * Bertanggung jawab mengelola seluruh logika bisnis penyetoran sampah fisik
 * oleh Petugas Bank Sampah untuk akun Warga (Nasabah).
 * 
 * Menjamin integritas transaksi ACID melalui anotasi @Transactional,
 * kalkulasi tarif resmi di sisi peladen (authoritative server pricing),
 * serta sinkronisasi mutasi kas pada buku kas operasional.
 */
@Service
public class DepositService {

    @Autowired
    private WasteDepositRepository depositRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WasteCategoryRepository categoryRepository;

    @Autowired
    private CashBookRepository cashBookRepository;

    /**
     * Memproses pencatatan setoran sampah fisik secara atomik.
     * 
     * Alur Logika Bisnis:
     * 1. Validasi input: bobot timbangan wajib bernilai positif (> 0 kg).
     * 2. Pengambilan data entitas Nasabah dan Kategori Sampah dari basis data.
     * 3. Kalkulasi otoritatif peladen: total nominal = tarif resmi/kg * bobot riil.
     * 4. Pembaruan saldo nasabah secara real-time.
     * 5. Penyimpanan catatan riwayat setoran (WasteDeposit).
     * 6. Pencatatan otomatis mutasi keluar kas pada buku kas (CashBookEntry).
     * 
     * @param officer User petugas yang sedang login dan melakukan penimbangan
     * @param request DTO permohonan setoran (citizenId, categoryId, weightKg, notes)
     * @return DepositDto struk bukti transaksi digital dengan rincian saldo akhir
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public DepositDto createDeposit(User officer, CreateDepositRequest request) {
        // [LOGIKA 1] Validasi Nilai Bobot: Mencegah angka timbangan 0 atau negatif
        if (request.getWeightKg() == null || request.getWeightKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Berat sampah harus lebih besar dari 0 kg");
        }

        // [LOGIKA 2] Verifikasi Keberadaan Nasabah & Kategori Sampah
        User citizen = userRepository.findById(request.getCitizenId())
                .orElseThrow(() -> new ResourceNotFoundException("Nasabah dengan ID " + request.getCitizenId() + " tidak ditemukan"));

        WasteCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Kategori sampah dengan ID " + request.getCategoryId() + " tidak ditemukan"));

        // [LOGIKA 3] Kalkulasi Otoritatif Peladen (Authoritative Server Pricing):
        // Sistem tidak pernah memercayai nominal rupiah dari input klien Android,
        // melainkan mengalikan tarif resmi di database dengan bobot fisik terukur.
        BigDecimal officialPrice = category.getPricePerKg();
        BigDecimal totalAmount = officialPrice.multiply(request.getWeightKg()).setScale(2, RoundingMode.HALF_UP);

        // [LOGIKA 4 - ATOMIK TAHAP 1] Kredit Saldo Rekening Nasabah:
        // Menambahkan total rupiah hasil setoran ke saldo aktif nasabah.
        BigDecimal updatedCitizenBalance = (citizen.getBalance() != null ? citizen.getBalance() : BigDecimal.ZERO).add(totalAmount);
        citizen.setBalance(updatedCitizenBalance);
        userRepository.save(citizen);

        // [LOGIKA 5 - ATOMIK TAHAP 2] Simpan Riwayat Setoran (WasteDeposit):
        // Mencatat stempel waktu resmi, identitas nasabah, petugas penimbang, bobot, dan tarif.
        WasteDeposit deposit = WasteDeposit.builder()
                .citizen(citizen)
                .officer(officer)
                .wasteCategory(category)
                .weightKg(request.getWeightKg())
                .pricePerKg(officialPrice)
                .totalAmount(totalAmount)
                .notes(request.getNotes())
                .build();
        deposit = depositRepository.save(deposit);

        // [LOGIKA 6 - ATOMIK TAHAP 3] Pembaruan Buku Kas Digital (CashBookEntry):
        // Mencatat pengeluaran kas operasional bank sampah (kredit saldo nasabah) agar pembukuan selalu seimbang.
        BigDecimal currentCash = cashBookRepository.getCurrentCashBalance();
        BigDecimal newCashBalance = currentCash.subtract(totalAmount);

        CashBookEntry cashEntry = CashBookEntry.builder()
                .transactionType(CashBookType.DEPOSIT_PAYOUT)
                .amount(totalAmount)
                .balanceAfter(newCashBalance)
                .referenceId(deposit.getId())
                .description("Penyetoran sampah: " + category.getName() + " (" + request.getWeightKg() + " kg) oleh " + citizen.getFullName())
                .build();
        cashBookRepository.save(cashEntry);

        // Mengembalikan DTO bukti transaksi resmi
        return toDto(deposit, updatedCitizenBalance);
    }

    /**
     * Mengambil seluruh riwayat transaksi setoran sampah milik satu nasabah tertentu.
     * 
     * @param citizenId ID akun nasabah
     * @return List DTO riwayat setoran diurutkan dari transaksi terbaru
     */
    public List<DepositDto> getCitizenDeposits(Long citizenId) {
        return depositRepository.findByCitizenIdOrderByCreatedAtDesc(citizenId).stream()
                .map(d -> toDto(d, d.getCitizen().getBalance()))
                .collect(Collectors.toList());
    }

    /**
     * Mengambil seluruh riwayat transaksi setoran sampah dari semua nasabah (khusus Petugas).
     * 
     * @return List DTO seluruh setoran yang tercatat di sistem
     */
    public List<DepositDto> getAllDeposits() {
        return depositRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(d -> toDto(d, d.getCitizen().getBalance()))
                .collect(Collectors.toList());
    }

    /**
     * Konversi internal dari entitas JPA WasteDeposit menjadi objek transfer data (DTO).
     * Menjaga isolasi struktur database internal dari paparan jaringan publik.
     */
    private DepositDto toDto(WasteDeposit deposit, BigDecimal balanceAfter) {
        return DepositDto.builder()
                .id(deposit.getId())
                .citizenId(deposit.getCitizen().getId())
                .citizenName(deposit.getCitizen().getFullName())
                .citizenPhoneNumber(deposit.getCitizen().getPhoneNumber())
                .officerId(deposit.getOfficer() != null ? deposit.getOfficer().getId() : null)
                .officerName(deposit.getOfficer() != null ? deposit.getOfficer().getFullName() : null)
                .categoryId(deposit.getWasteCategory().getId())
                .categoryName(deposit.getWasteCategory().getName())
                .weightKg(deposit.getWeightKg())
                .pricePerKg(deposit.getPricePerKg())
                .totalAmount(deposit.getTotalAmount())
                .citizenBalanceAfter(balanceAfter)
                .notes(deposit.getNotes())
                .createdAt(deposit.getCreatedAt())
                .build();
    }
}
