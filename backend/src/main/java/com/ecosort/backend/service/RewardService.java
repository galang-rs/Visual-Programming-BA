package com.ecosort.backend.service;

import com.ecosort.backend.dto.ClaimRewardRequest;
import com.ecosort.backend.dto.RewardClaimDto;
import com.ecosort.backend.dto.RewardItemDto;
import com.ecosort.backend.dto.VerifyClaimRequest;
import com.ecosort.backend.entity.*;
import com.ecosort.backend.exception.BadRequestException;
import com.ecosort.backend.exception.ResourceNotFoundException;
import com.ecosort.backend.repository.CashBookRepository;
import com.ecosort.backend.repository.RewardClaimRepository;
import com.ecosort.backend.repository.RewardItemRepository;
import com.ecosort.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Layer: RewardService
 * 
 * Bertanggung jawab mengelola seluruh siklus ekonomi sirkular pada fitur Reward:
 * 1. Menampilkan katalog sembako aktif beserta syarat saldo tabungan.
 * 2. Memproses klaim penukaran saldo nasabah secara atomik (@Transactional).
 * 3. Menerbitkan kode voucher klaim digital unik bertaraf kriptografis (format ECO-XXXXXX).
 * 4. Memverifikasi kode voucher oleh Petugas saat penyerahan paket sembako fisik di pos bank sampah.
 */
@Service
public class RewardService {

    @Autowired
    private RewardItemRepository rewardItemRepository;

    @Autowired
    private RewardClaimRepository rewardClaimRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CashBookRepository cashBookRepository;

    // Karakter alfanumerik aman (menghindari karakter ambigu seperti 0, O, 1, I)
    private static final String VOUCHER_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom random = new SecureRandom();

    /**
     * Mengambil daftar seluruh item reward sembako yang aktif dan tersedia untuk ditukar.
     * 
     * @return List DTO reward items
     */
    public List<RewardItemDto> getAllActiveRewards() {
        return rewardItemRepository.findByActiveTrue().stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());
    }

    /**
     * Memproses klaim penukaran saldo tabungan nasabah dengan item reward sembako.
     * 
     * Alur Logika Bisnis:
     * 1. Validasi status aktif dan ketersediaan stok fisik barang.
     * 2. Validasi kecukupan saldo tabungan: Jika saldo kurang, sistem melempar BadRequestException
     *    dengan rincian sisa nominal yang harus dikumpulkan nasabah.
     * 3. Pemotongan saldo tabungan nasabah secara atomik.
     * 4. Pengurangan stok inventori barang.
     * 5. Pembuatan entri klaim berstatus PENDING dengan kode voucher unik.
     * 6. Pencatatan mutasi penukaran reward pada buku kas operasional.
     * 
     * @param citizen Akun nasabah yang sedang login
     * @param request DTO permohonan klaim yang memuat rewardItemId
     * @return RewardClaimDto data voucher klaim digital yang siap ditunjukkan ke petugas
     */
    @Transactional
    public RewardClaimDto claimReward(User citizen, ClaimRewardRequest request) {
        // [LOGIKA 1] Mengambil data nasabah dan reward item terkini dari database
        User freshCitizen = userRepository.findById(citizen.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        RewardItem rewardItem = rewardItemRepository.findById(request.getRewardItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item reward tidak ditemukan"));

        // [LOGIKA 2] Validasi Ketersediaan Item
        if (!rewardItem.isActive()) {
            throw new BadRequestException("Item reward sedang tidak aktif");
        }

        if (rewardItem.getStockQuantity() <= 0) {
            throw new BadRequestException("Stok item reward telah habis");
        }

        // [LOGIKA 3] Pengecekan Kecukupan Saldo Tabungan:
        // Menolak jika saldo nasabah lebih kecil dari harga penukaran reward
        BigDecimal currentBalance = freshCitizen.getBalance() != null ? freshCitizen.getBalance() : BigDecimal.ZERO;
        if (currentBalance.compareTo(rewardItem.getRequiredBalance()) < 0) {
            BigDecimal shortage = rewardItem.getRequiredBalance().subtract(currentBalance);
            throw new BadRequestException("Saldo tabungan tidak mencukupi. Kurang Rp" + shortage + " untuk menukarkan item ini.");
        }

        // [LOGIKA 4 - ATOMIK TAHAP 1] Debit Saldo Tabungan Nasabah:
        // Mengurangi saldo nasabah sebesar harga requiredBalance
        freshCitizen.setBalance(currentBalance.subtract(rewardItem.getRequiredBalance()));
        userRepository.save(freshCitizen);

        // [LOGIKA 5 - ATOMIK TAHAP 2] Pengurangan Stok Barang di Gudang Bank Sampah
        rewardItem.setStockQuantity(rewardItem.getStockQuantity() - 1);
        rewardItemRepository.save(rewardItem);

        // [LOGIKA 6 - ATOMIK TAHAP 3] Penerbitan Kode Voucher Digital Kriptografis Unik (Format: ECO-XXXXXX)
        String voucherCode = generateUniqueVoucherCode();

        RewardClaim claim = RewardClaim.builder()
                .citizen(freshCitizen)
                .rewardItem(rewardItem)
                .requiredBalance(rewardItem.getRequiredBalance())
                .claimVoucherCode(voucherCode)
                .status(ClaimStatus.PENDING) // Status awal menunggu pengambilan fisik
                .build();
        claim = rewardClaimRepository.save(claim);

        // [LOGIKA 7 - ATOMIK TAHAP 4] Pencatatan pada Buku Kas Operasional
        BigDecimal currentCash = cashBookRepository.getCurrentCashBalance();
        CashBookEntry cashEntry = CashBookEntry.builder()
                .transactionType(CashBookType.REWARD_REDEMPTION)
                .amount(rewardItem.getRequiredBalance())
                .balanceAfter(currentCash)
                .referenceId(claim.getId())
                .description("Penukaran reward: " + rewardItem.getName() + " oleh " + freshCitizen.getFullName() + " (Voucher: " + voucherCode + ")")
                .build();
        cashBookRepository.save(cashEntry);

        return toClaimDto(claim);
    }

    /**
     * Memverifikasi kode voucher klaim saat nasabah datang ke pos bank sampah
     * untuk mengambil paket sembako fisik.
     * 
     * Alur Logika Bisnis:
     * 1. Memeriksa keberadaan kode voucher pada sistem.
     * 2. Memastikan voucher berstatus PENDING (belum pernah diklaim sebelumnya).
     * 3. Memperbarui status menjadi COMPLETED, mencatat petugas yang menyerahkan barang,
     *    dan membubuhkan stempel waktu penyerahan.
     * 
     * @param officer Akun petugas yang sedang login dan melayani serah terima
     * @param request DTO verifikasi yang memuat voucherCode
     * @return RewardClaimDto data klaim yang statusnya telah COMPLETED
     */
    @Transactional
    public RewardClaimDto verifyClaim(User officer, VerifyClaimRequest request) {
        String code = request.getClaimVoucherCode() != null ? request.getClaimVoucherCode().trim().toUpperCase() : "";

        // [LOGIKA 1] Pencarian Voucher di Database
        RewardClaim claim = rewardClaimRepository.findByClaimVoucherCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher klaim dengan kode '" + code + "' tidak ditemukan"));

        // [LOGIKA 2] Pencegahan Penggunaan Ganda (Double Claiming):
        // Memastikan status belum COMPLETED atau CANCELLED
        if (claim.getStatus() == ClaimStatus.COMPLETED) {
            throw new BadRequestException("Voucher ini sudah pernah diverifikasi dan barang fisik telah diserahkan sebelumnya");
        }
        if (claim.getStatus() == ClaimStatus.CANCELLED) {
            throw new BadRequestException("Voucher ini telah dibatalkan");
        }

        // [LOGIKA 3] Konfirmasi Serah Terima Fisik
        claim.setStatus(ClaimStatus.COMPLETED);
        claim.setOfficer(officer);
        claim.setCompletedAt(LocalDateTime.now());
        claim = rewardClaimRepository.save(claim);

        return toClaimDto(claim);
    }

    /**
     * Alias method untuk verifyClaim untuk kompatibilitas uji otomatis.
     */
    @Transactional
    public RewardClaimDto verifyAndCompleteClaim(User officer, VerifyClaimRequest request) {
        return verifyClaim(officer, request);
    }

    /**
     * Mengambil seluruh daftar voucher klaim milik satu nasabah tertentu.
     * 
     * @param citizenId ID akun nasabah
     * @return List DTO riwayat klaim reward
     */
    public List<RewardClaimDto> getCitizenClaims(Long citizenId) {
        return rewardClaimRepository.findByCitizenIdOrderByClaimedAtDesc(citizenId).stream()
                .map(this::toClaimDto)
                .collect(Collectors.toList());
    }

    /**
     * Mengambil seluruh klaim voucher di sistem untuk keperluan audit dan monitoring petugas.
     * 
     * @return List DTO seluruh klaim voucher
     */
    public List<RewardClaimDto> getAllClaims() {
        return rewardClaimRepository.findAllByOrderByClaimedAtDesc().stream()
                .map(this::toClaimDto)
                .collect(Collectors.toList());
    }

    /**
     * Algoritma generator kode voucher unik:
     * Menghasilkan string acak sepanjang 6 karakter diawali prefiks 'ECO-'
     * dan memeriksa basis data agar tidak pernah terjadi tabrakan kode (collision-free).
     */
    private String generateUniqueVoucherCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder("ECO-");
            for (int i = 0; i < 6; i++) {
                sb.append(VOUCHER_CHARS.charAt(random.nextInt(VOUCHER_CHARS.length())));
            }
            code = sb.toString();
        } while (rewardClaimRepository.existsByClaimVoucherCode(code));
        return code;
    }

    /**
     * Konversi entitas RewardItem ke RewardItemDto.
     */
    private RewardItemDto toItemDto(RewardItem item) {
        return RewardItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .requiredBalance(item.getRequiredBalance())
                .stockQuantity(item.getStockQuantity())
                .imageUrl(item.getImageUrl())
                .active(item.isActive())
                .build();
    }

    /**
     * Konversi entitas RewardClaim ke RewardClaimDto.
     */
    private RewardClaimDto toClaimDto(RewardClaim claim) {
        return RewardClaimDto.builder()
                .id(claim.getId())
                .citizenId(claim.getCitizen().getId())
                .citizenName(claim.getCitizen().getFullName())
                .citizenPhoneNumber(claim.getCitizen().getPhoneNumber())
                .rewardItemId(claim.getRewardItem().getId())
                .rewardItemName(claim.getRewardItem().getName())
                .requiredBalance(claim.getRequiredBalance())
                .claimVoucherCode(claim.getClaimVoucherCode())
                .status(claim.getStatus())
                .officerId(claim.getOfficer() != null ? claim.getOfficer().getId() : null)
                .officerName(claim.getOfficer() != null ? claim.getOfficer().getFullName() : null)
                .claimedAt(claim.getClaimedAt())
                .completedAt(claim.getCompletedAt())
                .build();
    }
}
