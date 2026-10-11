package com.ecosort.app.repository

import com.ecosort.app.data.UserPreferences
import com.ecosort.app.model.AuthResponse
import com.ecosort.app.model.LoginRequest
import com.ecosort.app.model.RegisterRequest
import com.ecosort.app.model.User
import com.ecosort.app.network.ApiService

class AuthRepository(
    private val apiService: ApiService,
    private val userPreferences: UserPreferences
) {
    suspend fun login(phoneNumber: String, password: String): Result<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(phoneNumber, password))
            if (response.isSuccessful && response.body()?.data != null) {
                val auth = response.body()!!.data!!
                userPreferences.saveAuth(auth)
                Result.success(auth)
            } else {
                val errorMsg = response.body()?.message ?: "Login gagal (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        fullName: String,
        phoneNumber: String,
        password: String,
        address: String?,
        role: String = "WARGA"
    ): Result<AuthResponse> {
        return try {
            val response = apiService.register(
                RegisterRequest(fullName, phoneNumber, password, address, role)
            )
            if (response.isSuccessful && response.body()?.data != null) {
                val auth = response.body()!!.data!!
                userPreferences.saveAuth(auth)
                Result.success(auth)
            } else {
                val errorMsg = response.body()?.message ?: "Pendaftaran gagal (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfile(): Result<User> {
        return try {
            val response = apiService.getProfile()
            if (response.isSuccessful && response.body()?.data != null) {
                val user = response.body()!!.data!!
                userPreferences.updateBalance(user.balance)
                Result.success(user)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal memuat profil"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        userPreferences.clearSession()
    }
}
