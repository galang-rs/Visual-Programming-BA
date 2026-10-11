package com.ecosort.app.model

data class ApiResponse<T>(
    val success: Boolean = true,
    val message: String? = null,
    val data: T? = null,
    val timestamp: String? = null
)

data class User(
    val id: Long,
    val fullName: String,
    val phoneNumber: String,
    val role: String,
    val address: String? = null,
    val balance: Double = 0.0,
    val totalWeightDeposited: Double? = 0.0
)

data class LoginRequest(
    val phoneNumber: String,
    val password: String
)

data class RegisterRequest(
    val fullName: String,
    val phoneNumber: String,
    val password: String,
    val address: String? = null,
    val role: String = "WARGA"
)

data class AuthResponse(
    val token: String,
    val tokenType: String = "Bearer",
    val id: Long,
    val fullName: String,
    val phoneNumber: String,
    val role: String,
    val balance: Double = 0.0
)

data class Commodity(
    val id: Long,
    val name: String,
    val description: String,
    val unit: String,
    val pricePerKg: Double,
    val cleaningInstructions: String,
    val iconUrl: String? = null,
    val active: Boolean = true
)

data class CalculateRequest(
    val categoryId: Long,
    val estimatedWeightKg: Double
)

data class CalculateResponse(
    val categoryId: Long,
    val categoryName: String,
    val pricePerKg: Double,
    val estimatedWeightKg: Double,
    val estimatedTotalPayout: Double
)

data class CreateDepositRequest(
    val citizenId: Long,
    val categoryId: Long,
    val weightKg: Double,
    val notes: String? = null
)

data class Deposit(
    val id: Long,
    val citizenId: Long,
    val citizenName: String,
    val citizenPhoneNumber: String,
    val officerId: Long? = null,
    val officerName: String? = null,
    val categoryId: Long,
    val categoryName: String,
    val weightKg: Double,
    val pricePerKg: Double,
    val totalAmount: Double,
    val notes: String? = null,
    val citizenBalanceAfter: Double = 0.0,
    val createdAt: String
)

data class RewardItem(
    val id: Long,
    val name: String,
    val description: String,
    val requiredBalance: Double,
    val stockQuantity: Int,
    val imageUrl: String? = null,
    val active: Boolean = true
)

data class ClaimRewardRequest(
    val rewardItemId: Long
)

data class VerifyClaimRequest(
    val claimVoucherCode: String
)

data class RewardClaim(
    val id: Long,
    val citizenId: Long,
    val citizenName: String,
    val citizenPhoneNumber: String,
    val rewardItemId: Long,
    val rewardItemName: String,
    val requiredBalance: Double,
    val claimVoucherCode: String,
    val status: String,
    val officerId: Long? = null,
    val officerName: String? = null,
    val claimedAt: String,
    val completedAt: String? = null
)

data class CashBookEntry(
    val id: Long,
    val transactionType: String,
    val amount: Double,
    val balanceAfter: Double,
    val referenceId: Long? = null,
    val description: String? = null,
    val createdAt: String
)

data class DashboardStats(
    val totalWeightCollected: Double = 0.0,
    val totalPayoutDistributed: Double = 0.0,
    val currentCashBalance: Double = 0.0,
    val totalDepositsCount: Long = 0,
    val totalCitizensCount: Long = 0,
    val estimatedCo2ReducedKg: Double = 0.0
)
