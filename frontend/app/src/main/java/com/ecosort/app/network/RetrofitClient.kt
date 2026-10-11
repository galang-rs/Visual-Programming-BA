package com.ecosort.app.network

import android.content.Context
import com.ecosort.app.data.UserPreferences
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Client Jaringan: RetrofitClient
 * 
 * Menginisialisasi dan menyediakan instans global tunggal `ApiService`.
 * Mengonfigurasi timeout jaringan, interceptor logging HTTP,
 * penyisipan otomatis token JWT via `AuthInterceptor`,
 * serta parsing otomatis JSON ke objek Kotlin Model menggunakan Gson.
 */
object RetrofitClient {

    /**
     * Alamat Base URL peladen backend Spring Boot:
     * - `http://10.0.2.2:8080/` untuk Android Virtual Device (Emulator)
     * - Ganti ke IP Wi-Fi lokal komputer (misal: `http://192.168.1.X:8080/`) bila menggunakan HP fisik.
     */
    var BASE_URL = "http://10.0.2.2:8080/"

    private var apiService: ApiService? = null

    /**
     * Mengambil atau membuat instans tunggal ApiService (Thread-Safe Lazy Singleton).
     * 
     * @param context Android Application Context
     * @return ApiService instans klien Retrofit siap pakai
     */
    fun getService(context: Context): ApiService {
        if (apiService == null) {
            val userPreferences = UserPreferences(context.applicationContext)

            // [LOGIKA 1] Interceptor Pencatatan Log HTTP (Membantu Debugging Jaringan)
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // [LOGIKA 2] Konfigurasi OkHttpClient dengan Timeout 15 Detik & Auth Token
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(userPreferences))
                .addInterceptor(loggingInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()

            // [LOGIKA 3] Membangun Instans Retrofit
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(ApiService::class.java)
        }
        return apiService!!
    }

    /**
     * Memperbarui alamat Base URL secara dinamis saat runtime
     * (berguna saat pengujian berpindah antara emulator dan perangkat fisik).
     */
    fun updateBaseUrl(newUrl: String, context: Context) {
        BASE_URL = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        apiService = null
        getService(context)
    }
}
