package com.ecosort.backend.service;

import com.ecosort.backend.dto.CalculateEstimateRequest;
import com.ecosort.backend.dto.CalculateEstimateResponse;
import com.ecosort.backend.dto.CommodityDto;
import com.ecosort.backend.entity.WasteCategory;
import com.ecosort.backend.exception.BadRequestException;
import com.ecosort.backend.exception.ResourceNotFoundException;
import com.ecosort.backend.repository.WasteCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Layer: CatalogService
 * 
 * Bertanggung jawab menyediakan data katalog komoditas sampah anorganik
 * (Botol PET, Kardus, Kaleng Aluminium, Botol Kaca, Kertas Arsip),
 * petunjuk edukasi pemilahan material, tarif resmi per kilogram,
 * serta layanan kalkulator simulasi estimasi nilai rupiah bagi warga.
 */
@Service
public class CatalogService {

    @Autowired
    private WasteCategoryRepository categoryRepository;

    /**
     * Mengambil seluruh daftar komoditas sampah yang aktif di bank sampah.
     * 
     * @return List CommodityDto berisi nama, kode, tarif per kg, dan instruksi pembersihan
     */
    public List<CommodityDto> getAllActiveCommodities() {
        return categoryRepository.findByActiveTrue().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Mengambil detail komoditas sampah spesifik berdasarkan ID unik.
     * 
     * @param id ID kategori sampah
     * @return CommodityDto data komoditas
     */
    public CommodityDto getCommodityById(Long id) {
        WasteCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kategori sampah dengan ID " + id + " tidak ditemukan"));
        return toDto(category);
    }

    /**
     * Melayani kalkulator simulasi estimasi nilai sampah.
     * 
     * Alur Logika Bisnis:
     * 1. Validasi input: bobot estimasi timbangan harus > 0 kg.
     * 2. Pengambilan tarif per kg aktif dari basis data.
     * 3. Pengalian: estimasi total uang = tarif per kg * estimasi berat kg,
     *    dengan pembulatan 2 digit desimal (HALF_UP).
     * 
     * @param request DTO masukan estimasi (categoryId dan estimatedWeightKg)
     * @return CalculateEstimateResponse rincian proyeksi rupiah yang akan diperoleh warga
     */
    public CalculateEstimateResponse calculateEstimate(CalculateEstimateRequest request) {
        // [LOGIKA 1] Validasi Nilai Masukan: Berat tidak boleh nol atau negatif
        if (request.getEstimatedWeightKg() == null || request.getEstimatedWeightKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Berat sampah harus lebih besar dari 0 kg");
        }

        // [LOGIKA 2] Pengambilan Data Kategori dari Database
        WasteCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Kategori sampah tidak ditemukan"));

        // [LOGIKA 3] Kalkulasi Rumus: Total Payout = Tarif Resmi * Bobot Timbangan
        BigDecimal total = category.getPricePerKg()
                .multiply(request.getEstimatedWeightKg())
                .setScale(2, RoundingMode.HALF_UP);

        return CalculateEstimateResponse.builder()
                .categoryId(category.getId())
                .categoryName(category.getName())
                .pricePerKg(category.getPricePerKg())
                .estimatedWeightKg(request.getEstimatedWeightKg())
                .estimatedTotalPayout(total)
                .build();
    }

    /**
     * Konversi entitas JPA WasteCategory ke DTO representasi JSON.
     */
    public CommodityDto toDto(WasteCategory category) {
        return CommodityDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .pricePerKg(category.getPricePerKg())
                .unit(category.getUnit())
                .cleaningInstructions(category.getCleaningInstructions())
                .iconUrl(category.getIconUrl())
                .active(category.isActive())
                .build();
    }
}
