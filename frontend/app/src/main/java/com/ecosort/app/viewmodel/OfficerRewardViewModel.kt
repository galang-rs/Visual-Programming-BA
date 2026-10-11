package com.ecosort.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosort.app.model.RewardClaim
import com.ecosort.app.repository.RewardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State Antarmuka Verifikasi Voucher Petugas (OfficerRewardUiState)
 * 
 * Mempertahankan teks masukan kode voucher nasabah, status proses verifikasi,
 * objek klaim yang berhasil diverifikasi (untuk pop-up konfirmasi),
 * seluruh daftar riwayat klaim voucher, dan pesan kesalahan.
 */
data class OfficerRewardUiState(
    val voucherInput: String = "",
    val isVerifying: Boolean = false,
    val verifiedClaim: RewardClaim? = null,
    val allClaims: List<RewardClaim> = emptyList(),
    val errorMessage: String? = null
)

/**
 * ViewModel: OfficerRewardViewModel
 * 
 * Mengelola modul verifikasi voucher klaim sembako oleh Petugas Bank Sampah.
 * Memvalidasi kode voucher unik yang ditunjukkan warga saat serah terima barang fisik di pos,
 * mengirim permintaan konfirmasi ke backend, dan memperbarui status menjadi COMPLETED.
 */
class OfficerRewardViewModel(private val repository: RewardRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(OfficerRewardUiState())
    val uiState: StateFlow<OfficerRewardUiState> = _uiState.asStateFlow()

    /**
     * Memperbarui teks input kode voucher dan mengonversinya secara otomatis ke huruf kapital (Uppercase).
     */
    fun onVoucherChange(code: String) {
        _uiState.update { it.copy(voucherInput = code.uppercase(), errorMessage = null) }
    }

    /**
     * Memuat seluruh daftar riwayat voucher klaim untuk ditampilkan pada daftar monitoring petugas.
     */
    fun loadAllClaims() {
        viewModelScope.launch {
            // [LOGIKA 1] Memanggil Endpoint GET /api/rewards/all-claims
            val result = repository.getAllClaims()
            _uiState.update { it.copy(allClaims = result.getOrDefault(emptyList())) }
        }
    }

    /**
     * Mengeksekusi verifikasi kode voucher klaim saat penyerahan paket sembako fisik.
     * 
     * Alur Logika Bisnis:
     * 1. Validasi: Input kode tidak boleh kosong.
     * 2. Menyalakan flag isVerifying.
     * 3. Mengirim kode voucher ke backend (POST /api/rewards/verify-claim).
     * 4. Jika sukses: Menampilkan dialog konfirmasi serah terima fisik (status COMPLETED),
     *    mengosongkan field input, dan memuat ulang daftar klaim.
     * 5. Jika gagal: Menampilkan pesan error (misal: voucher tidak ditemukan atau sudah pernah diklaim).
     */
    fun verifyClaim() {
        val code = _uiState.value.voucherInput.trim()
        // [LOGIKA 1] Validasi Form Input
        if (code.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Masukkan kode voucher klaim") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isVerifying = true, errorMessage = null) }
            // [LOGIKA 2] Pengiriman Permintaan Verifikasi ke Backend
            val result = repository.verifyClaim(code)
            result.onSuccess { claim ->
                // [LOGIKA 3] Status Sukses: Menampilkan Pop-Up Konfirmasi Serah Terima Selesai
                _uiState.update {
                    it.copy(
                        isVerifying = false,
                        verifiedClaim = claim,
                        voucherInput = ""
                    )
                }
                loadAllClaims()
            }.onFailure { error ->
                // [LOGIKA 4] Status Gagal: Menangkap Peringatan Voucher Ganda/Tidak Valid
                _uiState.update {
                    it.copy(
                        isVerifying = false,
                        errorMessage = error.message ?: "Verifikasi gagal"
                    )
                }
            }
        }
    }

    /**
     * Menutup dialog pop-up konfirmasi verifikasi sukses.
     */
    fun dismissSuccessDialog() {
        _uiState.update { it.copy(verifiedClaim = null) }
    }
}
