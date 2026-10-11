package com.ecosort.backend.controller;

import com.ecosort.backend.dto.ApiResponse;
import com.ecosort.backend.dto.CalculateEstimateRequest;
import com.ecosort.backend.dto.CalculateEstimateResponse;
import com.ecosort.backend.dto.CommodityDto;
import com.ecosort.backend.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller Layer: CatalogController
 * 
 * Melayani katalog jenis komoditas sampah anorganik dan simulasi kalkulator nilai sampah.
 */
@Tag(name = "2. Katalog Komoditas & Kalkulator", description = "Endpoints daftar kategori sampah dan simulasi kalkulator estimasi rupiah")
@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    @Autowired
    private CatalogService catalogService;

    /**
     * Endpoint GET /api/catalog
     * Mengambil seluruh daftar jenis sampah daur ulang beserta tarif resmi dan petunjuk pembersihan.
     */
    @Operation(summary = "Ambil Seluruh Katalog Sampah", description = "Mengembalikan daftar kategori sampah daur ulang aktif lengkap dengan harga per kg.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CommodityDto>>> getAllCommodities() {
        List<CommodityDto> commodities = catalogService.getAllActiveCommodities();
        return ResponseEntity.ok(ApiResponse.ok("Katalog berhasil dimuat", commodities));
    }

    /**
     * Endpoint GET /api/catalog/{id}
     * Mengambil informasi spesifik satu kategori sampah berdasarkan ID.
     */
    @Operation(summary = "Detail Komoditas Sampah", description = "Mengambil detail satu kategori sampah berdasarkan ID unik.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CommodityDto>> getCommodityById(@PathVariable Long id) {
        CommodityDto commodity = catalogService.getCommodityById(id);
        return ResponseEntity.ok(ApiResponse.ok("Detail komoditas berhasil dimuat", commodity));
    }

    /**
     * Endpoint POST /api/catalog/calculate
     * Melayani kalkulator simulasi estimasi nominal uang berdasarkan berat timbangan.
     */
    @Operation(summary = "Simulasi Kalkulator Sampah", description = "Menghitung proyeksi rupiah dari berat estimasi sampah yang dimasukkan.")
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<CalculateEstimateResponse>> calculateEstimate(@Valid @RequestBody CalculateEstimateRequest request) {
        CalculateEstimateResponse response = catalogService.calculateEstimate(request);
        return ResponseEntity.ok(ApiResponse.ok("Simulasi kalkulasi nilai berhasil", response));
    }
}
