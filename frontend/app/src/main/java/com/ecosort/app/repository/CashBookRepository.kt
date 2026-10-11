package com.ecosort.app.repository

import com.ecosort.app.model.CashBookEntry
import com.ecosort.app.model.DashboardStats
import com.ecosort.app.network.ApiService

class CashBookRepository(private val apiService: ApiService) {

    suspend fun getCashBook(): Result<List<CashBookEntry>> {
        return try {
            val response = apiService.getCashBook()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal memuat buku kas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDashboardStats(): Result<DashboardStats> {
        return try {
            val response = apiService.getDashboardStats()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal memuat statistik"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
