package com.ecosort.app.network

import com.ecosort.app.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Kontrak Layanan Jaringan (Retrofit REST Client Interface: ApiService)
 * 
 * Mendefinisikan seluruh endpoint REST API yang disediakan oleh backend Spring Boot.
 * Menggunakan Kotlin Coroutines (`suspend fun`) untuk eksekusi asinkron non-blocking
 * pada thread latar belakang, memastikan kelancaran antarmuka (60+ FPS bebas lag).
 */
interface ApiService {

    // ==========================================
    // 1. Modul Autentikasi & Akun Pengguna
    // ==========================================

    /**
     * Pendaftaran akun baru (Warga/Petugas).
     * Endpoint: POST /api/auth/register
     */
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>

    /**
     * Masuk akun menggunakan nomor telepon dan kata sandi.
     * Endpoint: POST /api/auth/login
     */
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    /**
     * Mengambil data profil akun pengguna aktif (saldo dan berat total sampah).
     * Endpoint: GET /api/auth/profile
     */
    @GET("api/auth/profile")
    suspend fun getProfile(): Response<ApiResponse<User>>

    // ==========================================
    // 2. Modul Katalog Komoditas & Kalkulator
    // ==========================================

    /**
     * Mengambil daftar seluruh komoditas sampah dan tarif resmi per kg.
     * Endpoint: GET /api/catalog
     */
    @GET("api/catalog")
    suspend fun getCommodities(): Response<ApiResponse<List<Commodity>>>

    /**
     * Mengambil detail komoditas sampah berdasarkan ID.
     * Endpoint: GET /api/catalog/{id}
     */
    @GET("api/catalog/{id}")
    suspend fun getCommodityById(@Path("id") id: Long): Response<ApiResponse<Commodity>>

    /**
     * Simulasi perhitungan rupiah di peladen berdasarkan berat timbangan.
     * Endpoint: POST /api/catalog/calculate
     */
    @POST("api/catalog/calculate")
    suspend fun calculateEstimate(@Body request: CalculateRequest): Response<ApiResponse<CalculateResponse>>

    // ==========================================
    // 3. Modul Penyetoran Sampah Fisik
    // ==========================================

    /**
     * Mencatat transaksi setoran fisik sampah oleh Petugas (Atomik @Transactional).
     * Endpoint: POST /api/deposits
     */
    @POST("api/deposits")
    suspend fun createDeposit(@Body request: CreateDepositRequest): Response<ApiResponse<Deposit>>

    /**
     * Mengambil riwayat transaksi setoran fisik milik nasabah yang sedang login.
     * Endpoint: GET /api/deposits/my-deposits
     */
    @GET("api/deposits/my-deposits")
    suspend fun getMyDeposits(): Response<ApiResponse<List<Deposit>>>

    /**
     * Mengambil seluruh transaksi setoran sampah semua nasabah (khusus Petugas).
     * Endpoint: GET /api/deposits/all
     */
    @GET("api/deposits/all")
    suspend fun getAllDeposits(): Response<ApiResponse<List<Deposit>>>

    // ==========================================
    // 4. Modul Reward Sembako & Voucher Klaim
    // ==========================================

    /**
     * Mengambil katalog barang sembako yang tersedia untuk ditukar dengan saldo tabungan.
     * Endpoint: GET /api/rewards
     */
    @GET("api/rewards")
    suspend fun getRewards(): Response<ApiResponse<List<RewardItem>>>

    /**
     * Mengajukan penukaran saldo dengan paket sembako, menerbitkan kode voucher unik (ECO-XXXXXX).
     * Endpoint: POST /api/rewards/claim
     */
    @POST("api/rewards/claim")
    suspend fun claimReward(@Body request: ClaimRewardRequest): Response<ApiResponse<RewardClaim>>

    /**
     * Petugas memverifikasi kode voucher saat serah terima barang sembako fisik di pos.
     * Endpoint: POST /api/rewards/verify-claim
     */
    @POST("api/rewards/verify-claim")
    suspend fun verifyClaim(@Body request: VerifyClaimRequest): Response<ApiResponse<RewardClaim>>

    /**
     * Mengambil daftar voucher klaim milik nasabah yang sedang aktif login.
     * Endpoint: GET /api/rewards/my-claims
     */
    @GET("api/rewards/my-claims")
    suspend fun getMyClaims(): Response<ApiResponse<List<RewardClaim>>>

    /**
     * Mengambil seluruh riwayat klaim voucher di sistem untuk audit Petugas.
     * Endpoint: GET /api/rewards/all-claims
     */
    @GET("api/rewards/all-claims")
    suspend fun getAllClaims(): Response<ApiResponse<List<RewardClaim>>>

    // ==========================================
    // 5. Modul Buku Kas Operasional & Analitik
    // ==========================================

    /**
     * Mengambil mutasi pembukuan kas bank sampah (khusus Petugas).
     * Endpoint: GET /api/cashbook
     */
    @GET("api/cashbook")
    suspend fun getCashBook(): Response<ApiResponse<List<CashBookEntry>>>

    /**
     * Mengambil statistik performa bank sampah dan dampak reduksi emisi CO2.
     * Endpoint: GET /api/cashbook/stats
     */
    @GET("api/cashbook/stats")
    suspend fun getDashboardStats(): Response<ApiResponse<DashboardStats>>

    // ==========================================
    // 6. Modul Pencarian Data Nasabah Warga
    // ==========================================

    /**
     * Mencari data warga berdasarkan nama atau nomor telepon (khusus Petugas).
     * Endpoint: GET /api/users/citizens?query=...
     */
    @GET("api/users/citizens")
    suspend fun searchCitizens(@Query("query") query: String? = null): Response<ApiResponse<List<User>>>
}
