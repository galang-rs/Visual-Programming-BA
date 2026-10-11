package com.ecosort.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosort.app.model.Commodity
import com.ecosort.app.repository.CatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State Antarmuka Katalog & Kalkulator Sampah (CatalogUiState)
 * 
 * Mempertahankan daftar kategori komoditas sampah aktif dari backend,
 * komoditas yang sedang dipilih oleh pengguna, input perkiraan bobot (kg),
 * hasil proyeksi rupiah terkalkulasi secara instan, serta status loading/error.
 */
data class CatalogUiState(
    val commodities: List<Commodity> = emptyList(),
    val selectedCommodity: Commodity? = null,
    val inputWeight: String = "1",
    val estimatedPayout: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel: CatalogViewModel
 * 
 * Mengelola pemuatan katalog komoditas sampah dan eksekusi kalkulator estimasi nilai sampah.
 * Mengimplementasikan kalkulasi interaktif di sisi klien (Client-Side Real-Time Calculation)
 * dengan latensi 0ms tanpa menunggu jeda jaringan, memberi kepastian proyeksi rupiah bagi warga.
 */
class CatalogViewModel(private val repository: CatalogRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    /**
     * Mengambil daftar seluruh komoditas sampah dari server backend.
     * 
     * Alur Logika:
     * 1. Menampilkan indikator loading.
     * 2. Memanggil CatalogRepository via GET /api/catalog.
     * 3. Jika berhasil: Memilih item pertama sebagai default seleksi dan
     *    menginisialisasi kalkulasi awal dengan estimasi bobot 1 kg.
     */
    fun loadCommodities() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            // [LOGIKA 1] Permintaan Jaringan ke Backend
            val result = repository.getCommodities()
            result.onSuccess { list ->
                // [LOGIKA 2] Menetapkan Komoditas Pertama sebagai Default Pilihan
                val first = list.firstOrNull()
                val payout = (first?.pricePerKg ?: 0.0) * (_uiState.value.inputWeight.toDoubleOrNull() ?: 1.0)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        commodities = list,
                        selectedCommodity = first,
                        estimatedPayout = payout
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }

    /**
     * Mengubah jenis komoditas sampah yang dipilih pengguna pada daftar kartu.
     * 
     * Alur Logika:
     * Langsung menghitung ulang proyeksi rupiah secara instan berdasarkan tarif komoditas baru
     * dikalikan bobot timbangan yang sedang tertera di input teks.
     */
    fun selectCommodity(commodity: Commodity) {
        val weight = _uiState.value.inputWeight.toDoubleOrNull() ?: 0.0
        // [LOGIKA 3] Rumus Kalkulasi: Tarif Komoditas Baru * Bobot Timbangan Saat Ini
        val payout = commodity.pricePerKg * weight
        _uiState.update {
            it.copy(
                selectedCommodity = commodity,
                estimatedPayout = payout
            )
        }
    }

    /**
     * Dipanggil setiap kali pengguna mengetik atau mengubah angka bobot (kg) pada kalkulator.
     * 
     * Alur Logika:
     * Mengubah string input menjadi double (fallback 0.0 jika kosong/invalid),
     * lalu mengalikan tarif aktif untuk menghasilkan estimasi rupiah tanpa jeda jaringan (0ms latency).
     */
    fun onWeightChange(weightStr: String) {
        val commodity = _uiState.value.selectedCommodity
        val weight = weightStr.toDoubleOrNull() ?: 0.0
        // [LOGIKA 4] Kalkulasi Live Sisi Klien (Reactive Zero-Latency Computation)
        val payout = (commodity?.pricePerKg ?: 0.0) * weight
        _uiState.update {
            it.copy(
                inputWeight = weightStr,
                estimatedPayout = payout
            )
        }
    }
}
