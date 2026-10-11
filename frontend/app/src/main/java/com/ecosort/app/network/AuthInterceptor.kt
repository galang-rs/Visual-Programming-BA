package com.ecosort.app.network

import com.ecosort.app.data.UserPreferences
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor Jaringan: AuthInterceptor
 * 
 * Bertugas menyisipkan token otorisasi JWT (JSON Web Token) secara otomatis
 * pada setiap tajuk (Header) permintaan HTTP keluar:
 * `Authorization: Bearer <token>`
 * 
 * Token diambil secara aman dari Jetpack DataStore Preferences (`UserPreferences`).
 * Jika token belum ada (misalnya saat proses login atau registrasi), permintaan
 * diteruskan apa adanya tanpa header Authorization.
 */
class AuthInterceptor(private val userPreferences: UserPreferences) : Interceptor {

    /**
     * Mencegat (intercept) setiap paket permintaan HTTP sebelum dikirim ke jaringan.
     * 
     * @param chain Rantai eksekusi interceptor OkHttp
     * @return Response balasan jaringan dari peladen
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // [LOGIKA 1] Membaca token JWT aktif dari DataStore Preferences lokal
        val token = runBlocking {
            userPreferences.authToken.firstOrNull()
        }

        // [LOGIKA 2] Menyisipkan Header 'Authorization: Bearer <token>' jika token tersedia
        val requestBuilder = original.newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        // [LOGIKA 3] Melanjutkan permintaan ke server
        return chain.proceed(requestBuilder.build())
    }
}
