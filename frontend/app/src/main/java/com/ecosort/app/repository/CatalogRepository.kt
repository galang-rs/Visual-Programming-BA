package com.ecosort.app.repository

import com.ecosort.app.model.CalculateRequest
import com.ecosort.app.model.CalculateResponse
import com.ecosort.app.model.Commodity
import com.ecosort.app.network.ApiService

class CatalogRepository(private val apiService: ApiService) {

    suspend fun getCommodities(): Result<List<Commodity>> {
        return try {
            val response = apiService.getCommodities()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal memuat katalog"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun calculateEstimate(categoryId: Long, estimatedWeightKg: Double): Result<CalculateResponse> {
        return try {
            val response = apiService.calculateEstimate(CalculateRequest(categoryId, estimatedWeightKg))
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal kalkulasi estimasi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
