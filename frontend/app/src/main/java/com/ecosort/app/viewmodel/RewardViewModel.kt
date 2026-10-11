package com.ecosort.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosort.app.model.RewardClaim
import com.ecosort.app.model.RewardItem
import com.ecosort.app.repository.AuthRepository
import com.ecosort.app.repository.RewardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State Antarmuka Modul Reward Sembako (RewardUiState)
 * 
 * Mempertahankan daftar paket sembako aktif, riwayat voucher klaim milik nasabah,
 * saldo aktif nasabah saat ini, indikator proses klaim, objek voucher untuk dialog sukses,
 * serta pesan peringatan kendala saldo tidak cukup.
 */
data class RewardUiState(
    val rewards: List<RewardItem> = emptyList(),
    val myClaims: List<RewardClaim> = emptyList(),
    val userBalance: Double = 0.0,
    val isLoading: Boolean = false,
    val isClaiming: Boolean = false,
    val claimSuccessDialog: RewardClaim? = null,
    val errorMessage: String? = null
)

/**
 * ViewModel: RewardViewModel
 * 
 * Mengelola interaksi penukaran saldo tabungan sampah nasabah dengan sembako (Ekonomi Sirkular).
 * Mengatur validasi kecukupan saldo tabungan di sisi klien sebelum pengiriman,
 * eksekusi API debit saldo backend, dan penerbitan dialog kode voucher klaim unik (ECO-XXXXXX).
 */
class RewardViewModel(
    private val rewardRepository: RewardRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RewardUiState())
    val uiState: StateFlow<RewardUiState> = _uiState.asStateFlow()

    /**
     * Memuat data saldo terbaru, daftar paket reward sembako, dan riwayat voucher nasabah.
     * 
     * Alur Logika:
     * 1. Menampilkan indikator loading.
     * 2. Mengambil saldo nasabah dari endpoint profil.
     * 3. Mengambil daftar barang reward aktif (Beras, Minyak, Gula, dll.).
     * 4. Mengambil riwayat voucher nasabah untuk tab 'Voucher Saya'.
     */
    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // [LOGIKA 1] Memastikan Saldo Nasabah Selalu Akurat dari Server
            val profileResult = authRepository.getProfile()
            val balance = profileResult.getOrNull()?.balance ?: 0.0

            // [LOGIKA 2] Memuat Item Reward & Riwayat Klaim Pribadi
            val rewardsResult = rewardRepository.getRewards()
            val claimsResult = rewardRepository.getMyClaims()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    userBalance = balance,
                    rewards = rewardsResult.getOrDefault(emptyList()),
                    myClaims = claimsResult.getOrDefault(emptyList()),
                    errorMessage = rewardsResult.exceptionOrNull()?.message
                )
            }
        }
    }

    /**
     * Mengajukan klaim penukaran saldo tabungan dengan paket sembako pilihan.
     * 
     * Alur Logika Bisnis:
     * 1. Validasi saldo di sisi klien: Jika saldo tabungan < harga barang, aksi diblokir
     *    dan memunculkan pesan informatif nominal kekurangan saldo.
     * 2. Menyalakan flag isClaiming (mencegah klik ganda).
     * 3. Mengirim permintaan klaim ke POST /api/rewards/claim.
     * 4. Jika sukses: Memperbarui saldo lokal seketika, membuka dialog kode voucher,
     *    dan menyinkronkan ulang data dari server.
     */
    fun claimReward(item: RewardItem) {
        // [LOGIKA 1] Pencegahan Penukaran Saat Saldo Tidak Cukup (Client-Side Guard)
        if (_uiState.value.userBalance < item.requiredBalance) {
            val shortage = item.requiredBalance - _uiState.value.userBalance
            _uiState.update {
                it.copy(errorMessage = "Saldo tidak cukup! Kurang Rp${String.format("%,.0f", shortage)}")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isClaiming = true, errorMessage = null) }
            // [LOGIKA 2] Eksekusi API Debit Saldo Transaksional di Peladen
            val result = rewardRepository.claimReward(item.id)
            result.onSuccess { claim ->
                // [LOGIKA 3] Memotong Saldo Lokal & Menampilkan Pop-Up Voucher Unik (ECO-XXXXXX)
                _uiState.update {
                    it.copy(
                        isClaiming = false,
                        claimSuccessDialog = claim,
                        userBalance = it.userBalance - item.requiredBalance
                    )
                }
                loadData()
            }.onFailure { error ->
                // [LOGIKA 4] Menangkap Kegagalan (Contoh: Stok Habis di Server)
                _uiState.update {
                    it.copy(isClaiming = false, errorMessage = error.message ?: "Gagal menukar reward")
                }
            }
        }
    }

    /**
     * Menutup dialog pop-up kode voucher setelah nasabah mencatat kodenya.
     */
    fun dismissSuccessDialog() {
        _uiState.update { it.copy(claimSuccessDialog = null) }
    }

    /**
     * Membersihkan pesan error/peringatan saldo.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
