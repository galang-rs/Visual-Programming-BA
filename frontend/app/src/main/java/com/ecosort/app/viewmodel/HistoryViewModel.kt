package com.ecosort.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosort.app.model.CashBookEntry
import com.ecosort.app.model.Deposit
import com.ecosort.app.model.RewardClaim
import com.ecosort.app.repository.CashBookRepository
import com.ecosort.app.repository.DepositRepository
import com.ecosort.app.repository.RewardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State Antarmuka Layar Riwayat & Pembukuan (HistoryUiState)
 * 
 * Mempertahankan index tab terpilih (0: Setoran Fisik, 1: Klaim Reward, 2: Buku Kas),
 * daftar riwayat setoran, daftar klaim voucher, mutasi buku kas operasional,
 * serta status loading dan pesan error.
 */
data class HistoryUiState(
    val selectedTab: Int = 0, // 0: Setoran Sampah, 1: Klaim Reward, 2: Buku Kas Operasional
    val deposits: List<Deposit> = emptyList(),
    val claims: List<RewardClaim> = emptyList(),
    val cashBook: List<CashBookEntry> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel: HistoryViewModel
 * 
 * Mengelola riwayat audit dan transparansi transaksi bank sampah.
 * Memisahkan hak akses data secara dinamis berdasarkan parameter `isOfficer`:
 * - Jika WARGA: Hanya memuat setoran dan voucher klaim milik akun pribadi.
 * - Jika PETUGAS: Memuat seluruh setoran nasabah, seluruh klaim, dan buku kas operasional.
 */
class HistoryViewModel(
    private val depositRepository: DepositRepository,
    private val rewardRepository: RewardRepository,
    private val cashBookRepository: CashBookRepository,
    private val isOfficer: Boolean
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    /**
     * Memilih tab riwayat (0: Setoran, 1: Reward, 2: Buku Kas).
     */
    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    /**
     * Memuat data riwayat dari peladen backend sesuai hak akses peran pengguna.
     * 
     * Alur Logika:
     * 1. Menampilkan indikator loading.
     * 2. Percabangan Otorisasi:
     *    - Petugas: Memanggil getAllDeposits(), getAllClaims(), dan getCashBook().
     *    - Warga: Memanggil getMyDeposits() dan getMyClaims(). Tab buku kas disembunyikan.
     * 3. Menyimpan hasil mutasi data ke dalam state reaktif.
     */
    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // [LOGIKA 1] Percabangan Endpoint Berdasarkan Peran (Role-Based Fetching)
            val depositsResult = if (isOfficer) depositRepository.getAllDeposits() else depositRepository.getMyDeposits()
            val claimsResult = if (isOfficer) rewardRepository.getAllClaims() else rewardRepository.getMyClaims()
            val cashBookResult = if (isOfficer) cashBookRepository.getCashBook() else Result.success(emptyList())

            // [LOGIKA 2] Memperbarui State UI
            _uiState.update {
                it.copy(
                    isLoading = false,
                    deposits = depositsResult.getOrDefault(emptyList()),
                    claims = claimsResult.getOrDefault(emptyList()),
                    cashBook = cashBookResult.getOrDefault(emptyList()),
                    errorMessage = depositsResult.exceptionOrNull()?.message
                )
            }
        }
    }
}
