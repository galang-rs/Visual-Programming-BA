package com.ecosort.app.repository

import com.ecosort.app.model.ClaimRewardRequest
import com.ecosort.app.model.RewardClaim
import com.ecosort.app.model.RewardItem
import com.ecosort.app.model.VerifyClaimRequest
import com.ecosort.app.network.ApiService

class RewardRepository(private val apiService: ApiService) {

    suspend fun getRewards(): Result<List<RewardItem>> {
        return try {
            val response = apiService.getRewards()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal memuat reward"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun claimReward(rewardItemId: Long): Result<RewardClaim> {
        return try {
            val response = apiService.claimReward(ClaimRewardRequest(rewardItemId))
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal menukarkan reward"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyClaim(voucherCode: String): Result<RewardClaim> {
        return try {
            val response = apiService.verifyClaim(VerifyClaimRequest(voucherCode))
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal verifikasi voucher"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyClaims(): Result<List<RewardClaim>> {
        return try {
            val response = apiService.getMyClaims()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal memuat riwayat klaim"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllClaims(): Result<List<RewardClaim>> {
        return try {
            val response = apiService.getAllClaims()
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal memuat seluruh klaim"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
