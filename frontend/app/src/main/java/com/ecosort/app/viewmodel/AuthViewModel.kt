package com.ecosort.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecosort.app.model.AuthResponse
import com.ecosort.app.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Representasi State Antarmuka untuk Fitur Autentikasi (UDF State)
 * 
 * Mempertahankan data input pengguna (nomor telepon, kata sandi, nama, peran),
 * indikator loading spinner, pesan error validasi, dan data sesi auth berhasil.
 */
data class AuthUiState(
    val phoneNumber: String = "",
    val password: String = "",
    val fullName: String = "",
    val address: String = "",
    val role: String = "WARGA",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val authSuccess: AuthResponse? = null
)

/**
 * ViewModel: AuthViewModel
 * 
 * Mengelola alur logika autentikasi (Login, Register, Logout) dengan arsitektur MVVM,
 * pola Unidirectional Data Flow (UDF), dan StateFlow reaktif.
 * Bertindak sebagai penengah antara layar Compose (LoginScreen, RegisterScreen)
 * dan lapisan data (AuthRepository & DataStore).
 */
class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    // Status internal yang dapat dimutasi secara aman di dalam ViewModel
    private val _uiState = MutableStateFlow(AuthUiState())
    // Status publik imutabel yang diamati secara reaktif oleh Compose View
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * Memperbarui nomor telepon pada state dan membersihkan pesan error sebelumnya.
     */
    fun onPhoneNumberChange(value: String) {
        _uiState.update { it.copy(phoneNumber = value, errorMessage = null) }
    }

    /**
     * Memperbarui kata sandi pada state dan membersihkan pesan error.
     */
    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    /**
     * Memperbarui nama lengkap pada form registrasi.
     */
    fun onFullNameChange(value: String) {
        _uiState.update { it.copy(fullName = value, errorMessage = null) }
    }

    /**
     * Memperbarui alamat domisili nasabah.
     */
    fun onAddressChange(value: String) {
        _uiState.update { it.copy(address = value, errorMessage = null) }
    }

    /**
     * Memilih peran akun (WARGA atau PETUGAS).
     */
    fun onRoleChange(value: String) {
        _uiState.update { it.copy(role = value, errorMessage = null) }
    }

    /**
     * Menjalankan aksi masuk (Login) ke sistem EcoSort.
     * 
     * Alur Logika:
     * 1. Validasi awal: Memastikan nomor HP dan password tidak kosong.
     * 2. Menyalakan indikator isLoading.
     * 3. Mengirim kredensial ke peladen backend via AuthRepository (Retrofit).
     * 4. Jika sukses: Menyimpan token JWT ke Jetpack DataStore dan memancarkan authSuccess.
     * 5. Jika gagal: Menampilkan pesan error kegagalan otentikasi.
     */
    fun login() {
        val state = _uiState.value
        // [LOGIKA 1] Validasi Form di Sisi Klien
        if (state.phoneNumber.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nomor telepon dan password wajib diisi") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            // [LOGIKA 2] Eksekusi API Login di Thread Latar Belakang (Coroutines)
            val result = repository.login(state.phoneNumber, state.password)
            result.onSuccess { auth ->
                // [LOGIKA 3] State Sukses: Memicu navigasi otomatis ke dasbor yang sesuai peran
                _uiState.update { it.copy(isLoading = false, authSuccess = auth) }
            }.onFailure { error ->
                // [LOGIKA 4] State Gagal: Menangkap pesan kesalahan dari backend
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Login gagal") }
            }
        }
    }

    /**
     * Menjalankan aksi pendaftaran akun baru (Register).
     * 
     * Alur Logika:
     * 1. Validasi form: Nama lengkap, nomor telepon, dan password wajib diisi.
     * 2. Mengirim data ke backend via POST /api/auth/register.
     * 3. Menyimpan sesi token JWT yang baru terbit langsung ke penyimpanan lokal.
     */
    fun register() {
        val state = _uiState.value
        // [LOGIKA 1] Validasi Kelengkapan Kolom Form
        if (state.fullName.isBlank() || state.phoneNumber.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Lengkapi seluruh kolom yang wajib diisi") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            // [LOGIKA 2] Eksekusi Permintaan Pendaftaran ke Peladen
            val result = repository.register(
                state.fullName,
                state.phoneNumber,
                state.password,
                state.address.ifBlank { null },
                state.role
            )
            result.onSuccess { auth ->
                _uiState.update { it.copy(isLoading = false, authSuccess = auth) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Pendaftaran gagal") }
            }
        }
    }

    /**
     * Mereset flag authSuccess setelah navigasi selesai dilakukan
     * agar tidak terjadi pengulangan navigasi yang tidak diinginkan.
     */
    fun resetSuccess() {
        _uiState.update { it.copy(authSuccess = null, errorMessage = null) }
    }

    /**
     * Mengeluarkan pengguna dari sistem (Logout).
     * Menghapus token JWT dari Jetpack DataStore dan mereset state ke kondisi default.
     * 
     * @param onComplete Callback aksi navigasi kembali ke layar Login
     */
    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            // [LOGIKA] Menghapus kredensial terisolasi di DataStore Preferences
            repository.logout()
            _uiState.update { AuthUiState() }
            onComplete()
        }
    }
}
