package com.ecosort.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ecosort.app.model.RewardClaim
import com.ecosort.app.model.RewardItem
import com.ecosort.app.ui.theme.*
import com.ecosort.app.viewmodel.RewardUiState
import com.ecosort.app.viewmodel.RewardViewModel

@Composable
fun RewardScreen(
    viewModel: RewardViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    RewardContent(
        state = state,
        onClaimReward = { viewModel.claimReward(it) },
        onDismissDialog = { viewModel.dismissSuccessDialog() },
        onClearError = { viewModel.clearError() },
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun RewardContent(
    state: RewardUiState,
    onClaimReward: (RewardItem) -> Unit,
    onDismissDialog: () -> Unit,
    onClearError: () -> Unit,
    onNavigateBack: () -> Unit
) {
    // Success Dialog with Voucher Code
    state.claimSuccessDialog?.let { claim ->
        AlertDialog(
            onDismissRequest = onDismissDialog,
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(EcoGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EcoGreenDark, modifier = Modifier.size(32.dp))
                }
            },
            title = {
                Text(
                    text = "Penukaran Berhasil!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Anda telah menukarkan saldo untuk:",
                        fontSize = 13.sp,
                        color = EcoTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = claim.rewardItemName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("KODE VOUCHER KLAIM:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = EcoTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        color = EcoAmberLight,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EcoAmber)
                    ) {
                        Text(
                            text = claim.claimVoucherCode,
                            color = EcoAmber,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Tunjukkan kode ini kepada petugas bank sampah untuk serah terima paket sembako fisik.",
                        fontSize = 12.sp,
                        color = EcoTextSecondary,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary)
                ) {
                    Text("Tutup", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Tukar Poin & Paket Sembako", fontWeight = FontWeight.Bold, color = EcoGreenDark) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Balance Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = EcoGreenPrimary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Saldo Tersedia untuk Penukaran", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                            Text(
                                "Rp ${String.format("%,.0f", state.userBalance)}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Icon(
                            Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // Error display
            state.errorMessage?.let { error ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = onClearError) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // Pending Claims Section (if any)
            val pendingClaims = state.myClaims.filter { it.status == "PENDING" }
            if (pendingClaims.isNotEmpty()) {
                item {
                    Text(
                        text = "Voucher Menunggu Penyerahan Fisik (${pendingClaims.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = EcoAmber
                    )
                }

                items(pendingClaims) { claim ->
                    PendingClaimCard(claim)
                }
            }

            // Rewards List Header
            item {
                Text(
                    text = "Katalog Paket Reward & Sembako",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Reward Items
            items(state.rewards) { item ->
                RewardItemCard(
                    item = item,
                    userBalance = state.userBalance,
                    isClaiming = state.isClaiming,
                    onClaim = { onClaimReward(item) }
                )
            }
        }
    }
}

@Composable
fun RewardItemCard(
    item: RewardItem,
    userBalance: Double,
    isClaiming: Boolean,
    onClaim: () -> Unit
) {
    val canAfford = userBalance >= item.requiredBalance
    val hasStock = item.stockQuantity > 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (canAfford) EcoGreenLight else EcoErrorLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = if (canAfford) EcoGreenDark else EcoError
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = item.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = item.description,
                            fontSize = 12.sp,
                            color = EcoTextSecondary,
                            maxLines = 2
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Syarat Saldo:", fontSize = 11.sp, color = EcoTextSecondary)
                    Text(
                        "Rp ${String.format("%,.0f", item.requiredBalance)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = if (canAfford) EcoGreenPrimary else EcoError
                    )
                }

                Surface(
                    color = if (hasStock) EcoGreenLight else EcoErrorLight,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (hasStock) "Tersedia: ${item.stockQuantity} paket" else "Stok Habis",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (hasStock) EcoGreenDark else EcoError,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClaim,
                enabled = canAfford && hasStock && !isClaiming,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EcoGreenPrimary,
                    disabledContainerColor = Color(0xFFE0E0E0)
                )
            ) {
                if (!canAfford) {
                    val shortage = item.requiredBalance - userBalance
                    Text(
                        "Kurang Rp ${String.format("%,.0f", shortage)}",
                        fontSize = 13.sp,
                        color = EcoTextSecondary
                    )
                } else if (!hasStock) {
                    Text("Stok Habis", fontSize = 13.sp)
                } else {
                    Text("Tukar Sekarang", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PendingClaimCard(claim: RewardClaim) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = EcoAmberLight.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, EcoAmber)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(claim.rewardItemName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EcoGreenDark)
                Text("Voucher: ${claim.claimVoucherCode}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = EcoAmber)
                Text("Klaim: ${claim.claimedAt.take(10)}", fontSize = 11.sp, color = EcoTextSecondary)
            }
            Surface(
                color = EcoAmber,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "Siap Diambil",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RewardScreenPreview() {
    val sampleRewards = listOf(
        RewardItem(
            id = 1,
            name = "Paket Beras Premium 5 kg",
            description = "Beras putih kualitas premium Pandan Wangi.",
            requiredBalance = 75000.0,
            stockQuantity = 20
        ),
        RewardItem(
            id = 2,
            name = "Minyak Goreng Sawit 2 Liter",
            description = "Minyak goreng pouch 2L jernih vitamin A.",
            requiredBalance = 35000.0,
            stockQuantity = 15
        ),
        RewardItem(
            id = 3,
            name = "Gula Pasir 1 kg",
            description = "Gula tebu murni 1 kg SNI.",
            requiredBalance = 17500.0,
            stockQuantity = 30
        )
    )

    EcoSortTheme {
        RewardContent(
            state = RewardUiState(
                rewards = sampleRewards,
                myClaims = listOf(
                    RewardClaim(
                        id = 10,
                        citizenId = 1,
                        citizenName = "Siti Aminah",
                        citizenPhoneNumber = "081987654321",
                        rewardItemId = 3,
                        rewardItemName = "Gula Pasir 1 kg",
                        requiredBalance = 17500.0,
                        claimVoucherCode = "ECO-GUL481",
                        status = "PENDING",
                        claimedAt = "2026-10-11T12:00:00"
                    )
                ),
                userBalance = 52500.0
            ),
            onClaimReward = {},
            onDismissDialog = {},
            onClearError = {},
            onNavigateBack = {}
        )
    }
}
