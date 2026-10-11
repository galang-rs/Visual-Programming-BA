package com.ecosort.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosort.app.model.Commodity
import com.ecosort.app.model.Deposit
import com.ecosort.app.model.User
import com.ecosort.app.repository.CatalogRepository
import com.ecosort.app.repository.DepositRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State Antarmuka Formulir Setoran Fisik Petugas (OfficerDepositUiState)
 * 
 * Mempertahankan daftar nasabah warga, teks pencarian nasabah, nasabah terpilih,
 * daftar kategori sampah, kategori terpilih, input bobot timbangan riil (kg),
 * catatan kondisi sampah, kalkulasi estimasi total rupiah resmi, indikator pengiriman,
 * struk digital sukses transaksi, dan pesan validasi/error.
 */
data class OfficerDepositUiState(
    val citizens: List<User> = emptyList(),
    val searchQuery: String = "",
    val selectedCitizen: User? = null,
    val categories: List<Commodity> = emptyList(),
    val selectedCategory: Commodity? = null,
    val weightInput: String = "",
    val notesInput: String = "",
    val calculatedTotal: Double = 0.0,
    val isSubmitting: Boolean = false,
    val successDeposit: Deposit? = null,
    val errorMessage: String? = null
)

/**
 * ViewModel: OfficerDepositViewModel
 * 
 * Mengelola alur pos penimbangan sampah fisik bank sampah oleh Petugas.
 * Menghubungkan pencarian data nasabah warga, pemilihan jenis komoditas sampah,
 * validasi ketat bobot timbangan fisik (> 0 kg), kalkulasi estimasi nilai,
 * serta pengiriman data transaksi setoran ke backend Spring Boot (@Transactional).
 */
class OfficerDepositViewModel(
    private val depositRepository: DepositRepository,
    private val catalogRepository: CatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OfficerDepositUiState())
    val uiState: StateFlow<OfficerDepositUiState> = _uiState.asStateFlow()

    /**
     * Memuat data inisial saat layar dibuka:
     * 1. Daftar komoditas sampah aktif beserta tarif resmi per kg.
     * 2. Daftar seluruh warga terdaftar sebagai nasabah bank sampah.
     */
    fun loadInitialData() {
        viewModelScope.launch {
            // [LOGIKA 1] Memuat Kategori & Daftar Nasabah dari Repository
            val categoriesResult = catalogRepository.getCommodities()
            val citizensResult = depositRepository.searchCitizens(null)

            _uiState.update {
                it.copy(
                    categories = categoriesResult.getOrDefault(emptyList()),
                    selectedCategory = categoriesResult.getOrNull()?.firstOrNull(),
                    citizens = citizensResult.getOrDefault(emptyList())
                )
            }
        }
    }

    /**
     * Memfilter daftar nasabah warga secara real-time saat petugas mengetik nama atau nomor HP.
     * 
     * @param query Kata kunci pencarian nama atau nomor telepon nasabah
     */
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            // [LOGIKA 2] Query Pencarian Dinamis ke Endpoint GET /api/users/citizens?query=...
            val result = depositRepository.searchCitizens(query)
            _uiState.update { it.copy(citizens = result.getOrDefault(emptyList())) }
        }
    }

    /**
     * Memilih satu nasabah yang datang membawa sampah ke pos penimbangan.
     */
    fun selectCitizen(citizen: User) {
        _uiState.update { it.copy(selectedCitizen = citizen, errorMessage = null) }
    }

    /**
     * Memilih jenis sampah yang sedang ditimbang (PET, Kardus, Kaleng, Kaca, Kertas).
     * Langsung mengalkulasikan ulang nilai total resmi di layar.
     */
    fun selectCategory(category: Commodity) {
        val weight = _uiState.value.weightInput.toDoubleOrNull() ?: 0.0
        // [LOGIKA 3] Kalkulasi Instan: Tarif Kategori Terpilih * Bobot Terukur
        _uiState.update {
            it.copy(
                selectedCategory = category,
                calculatedTotal = category.pricePerKg * weight
            )
        }
    }

    /**
     * Dipanggil saat petugas memasukkan angka timbangan riil (dalam kilogram).
     * Menghitung nilai rupiah resmi secara live.
     */
    fun onWeightChange(weight: String) {
        val w = weight.toDoubleOrNull() ?: 0.0
        val category = _uiState.value.selectedCategory
        val total = (category?.pricePerKg ?: 0.0) * w
        _uiState.update {
            it.copy(
                weightInput = weight,
                calculatedTotal = total,
                errorMessage = null
            )
        }
    }

    /**
     * Memperbarui catatan kondisi fisik sampah (opsional, misal: 'bersih dan kering').
     */
    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notesInput = notes) }
    }

    /**
     * Mengonfirmasi dan mengeksekusi pencatatan transaksi setoran sampah fisik.
     * 
     * Alur Logika Bisnis:
     * 1. Validasi: Nasabah wajib dipilih.
     * 2. Validasi: Kategori sampah wajib dipilih.
     * 3. Validasi: Bobot timbangan wajib bernilai positif (> 0 kg). Menolak angka 0 atau negatif.
     * 4. Mengirim transaksi ke backend (POST /api/deposits).
     * 5. Jika sukses: Menampilkan dialog bukti transaksi resmi (struk digital) lengkap dengan
     *    saldo akhir nasabah yang telah bertambah, dan mereset form masukan.
     */
    fun submitDeposit() {
        val state = _uiState.value
        val citizen = state.selectedCitizen
        // [LOGIKA 1] Validasi Kelayakan Nasabah
        if (citizen == null) {
            _uiState.update { it.copy(errorMessage = "Pilih data nasabah terlebih dahulu") }
            return
        }

        val category = state.selectedCategory
        // [LOGIKA 2] Validasi Kategori Sampah
        if (category == null) {
            _uiState.update { it.copy(errorMessage = "Pilih kategori sampah") }
            return
        }

        val weight = state.weightInput.toDoubleOrNull()
        // [LOGIKA 3] Validasi Edge Case: Berat timbangan harus > 0 kg
        if (weight == null || weight <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Berat sampah harus lebih besar dari 0 kg") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            // [LOGIKA 4] Pengiriman Data Transaksi ke Peladen Backend
            val result = depositRepository.createDeposit(
                citizenId = citizen.id,
                categoryId = category.id,
                weightKg = weight,
                notes = state.notesInput.ifBlank { null }
            )

            result.onSuccess { deposit ->
                // [LOGIKA 5] Transaksi Berhasil: Menyajikan Struk Digital & Mengosongkan Form
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        successDeposit = deposit,
                        weightInput = "",
                        notesInput = "",
                        calculatedTotal = 0.0,
                        selectedCitizen = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = error.message ?: "Gagal mencatat setoran")
                }
            }
        }
    }

    /**
     * Menutup dialog struk digital transaksi setoran.
     */
    fun dismissSuccessDialog() {
        _uiState.update { it.copy(successDeposit = null) }
    }

    /**
     * Membersihkan pesan error validasi.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
