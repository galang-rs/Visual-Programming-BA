package com.ecosort.app.repository

import com.ecosort.app.model.CreateDepositRequest
import com.ecosort.app.model.Deposit
import com.ecosort.app.model.User
import com.ecosort.app.network.ApiService

class DepositRepository(private val apiService: ApiService) {

    suspend fun createDeposit(
        citizenId: Long,
        categoryId: Long,
        weightKg: Double,
        notes: String?
    ): Result<Deposit> {
        return try {
            val response = apiService.createDeposit(
                CreateDepositRequest(citizenId, categoryId, weightKg, notes)
            )
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal memproses setoran"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyDeposits(): Result<List<Deposit>> {
        return try {
            val response = apiService.getMyDeposits()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal memuat riwayat setoran"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllDeposits(): Result<List<Deposit>> {
        return try {
            val response = apiService.getAllDeposits()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal memuat seluruh setoran"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchCitizens(query: String?): Result<List<User>> {
        return try {
            val response = apiService.searchCitizens(query)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal mencari nasabah"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
