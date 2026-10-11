package com.ecosort.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosort.app.model.DashboardStats
import com.ecosort.app.model.Deposit
import com.ecosort.app.model.User
import com.ecosort.app.repository.AuthRepository
import com.ecosort.app.repository.CashBookRepository
import com.ecosort.app.repository.DepositRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State Antarmuka Beranda Warga / Nasabah (CitizenHomeUiState)
 * 
 * Menyimpan data profil nasabah (nama, saldo tabungan aktif, total kg sampah),
 * 5 riwayat transaksi setoran fisik terakhir, statistik analitik lingkungan hidup,
 * status loading, serta pesan error.
 */
data class CitizenHomeUiState(
    val user: User? = null,
    val recentDeposits: List<Deposit> = emptyList(),
    val stats: DashboardStats? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel: CitizenHomeViewModel
 * 
 * Mengelola pemuatan data terpadu untuk layar Beranda Warga (CitizenHomeScreen).
 * Mengorkestrasikan tiga repositori data (Auth, Deposit, dan CashBook) secara paralel
 * menggunakan Coroutines di thread latar belakang untuk menyajikan saldo dan riwayat terkini.
 */
class CitizenHomeViewModel(
    private val authRepository: AuthRepository,
    private val depositRepository: DepositRepository,
    private val cashBookRepository: CashBookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CitizenHomeUiState())
    val uiState: StateFlow<CitizenHomeUiState> = _uiState.asStateFlow()

    /**
     * Memuat data dashboard nasabah secara real-time dari peladen backend.
     * 
     * Alur Logika Bisnis:
     * 1. Menampilkan indikator loading.
     * 2. Mengambil data profil profil nasabah (saldo terkini & akumulasi bobot sampah).
     * 3. Mengambil riwayat setoran nasabah dan membatasi hanya 5 transaksi terakhir untuk widget beranda.
     * 4. Mengambil statistik agregat bank sampah (termasuk estimasi reduksi CO2).
     * 5. Memperbarui status UI secara atomik.
     */
    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // [LOGIKA 1] Permintaan Profil Nasabah (Saldo Aktif & Nama)
            val profileResult = authRepository.getProfile()
            // [LOGIKA 2] Permintaan Riwayat Setoran Pribadi
            val depositsResult = depositRepository.getMyDeposits()
            // [LOGIKA 3] Permintaan Statistik Lingkungan Hidup
            val statsResult = cashBookRepository.getDashboardStats()

            val currentUser = profileResult.getOrNull()
            // [LOGIKA 4] Filter: Menampilkan 5 transaksi terbaru pada kartu ringkasan beranda
            val deposits = depositsResult.getOrDefault(emptyList()).take(5)
            val stats = statsResult.getOrNull()

            // [LOGIKA 5] Pembaruan State Reaktif ke Antarmuka Jetpack Compose
            _uiState.update {
                it.copy(
                    isLoading = false,
                    user = currentUser,
                    recentDeposits = deposits,
                    stats = stats,
                    errorMessage = profileResult.exceptionOrNull()?.message
                )
            }
        }
    }
}
