package com.ecosort.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ecosort.app.model.DashboardStats
import com.ecosort.app.model.Deposit
import com.ecosort.app.model.User
import com.ecosort.app.ui.theme.*
import com.ecosort.app.viewmodel.CitizenHomeUiState
import com.ecosort.app.viewmodel.CitizenHomeViewModel

@Composable
fun CitizenHomeScreen(
    viewModel: CitizenHomeViewModel,
    onNavigateToCatalog: () -> Unit,
    onNavigateToReward: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    CitizenHomeContent(
        state = state,
        onNavigateToCatalog = onNavigateToCatalog,
        onNavigateToReward = onNavigateToReward,
        onNavigateToHistory = onNavigateToHistory,
        onLogout = onLogout
    )
}

@Composable
fun CitizenHomeContent(
    state: CitizenHomeUiState,
    onNavigateToCatalog: () -> Unit,
    onNavigateToReward: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(EcoGreenPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Recycling, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("EcoSort Warga", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = EcoGreenDark)
                            Text("Bank Sampah Berkelanjutan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Keluar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
            // Citizen Greeting Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Halo, ${state.user?.fullName ?: "Warga"}! 👋",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = state.user?.phoneNumber ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        color = EcoGreenLight,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Nasabah Aktif",
                            color = EcoGreenDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Big Gradient Balance & Savings Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(EcoGreenDark, EcoGreenPrimary)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SALDO TABUNGAN SAMPAH",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Icon(
                                    Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.9f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Rp ${String.format("%,.0f", state.user?.balance ?: 0.0)}",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Total Sampah Disetor", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                                    Text(
                                        "${String.format("%.1f", state.user?.totalWeightDeposited ?: 0.0)} kg",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Estimasi Reduksi CO2", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                                    Text(
                                        "${String.format("%.1f", (state.user?.totalWeightDeposited ?: 0.0) * 2.5)} kg CO₂e",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC8E6C9)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Action Grid (3 Cards)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = "Katalog & Hitung",
                        icon = Icons.Default.Calculate,
                        color = EcoGreenPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToCatalog
                    )
                    QuickActionCard(
                        title = "Tukar Sembako",
                        icon = Icons.Default.ShoppingBag,
                        color = EcoAmber,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToReward
                    )
                    QuickActionCard(
                        title = "Riwayat",
                        icon = Icons.Default.History,
                        color = EcoEmerald,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToHistory
                    )
                }
            }

            // Recent Deposits Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Riwayat Penyetoran Terakhir",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = onNavigateToHistory) {
                        Text("Lihat Semua", color = EcoGreenPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (state.recentDeposits.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Inbox, contentDescription = null, tint = EcoTextMuted, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Belum ada riwayat setoran sampah.", color = EcoTextSecondary, fontSize = 13.sp)
                            Text("Bawa sampah terpilah Anda ke pos bank sampah terdekat!", color = EcoTextMuted, fontSize = 11.sp)
                        }
                    }
                }
            } else {
                items(state.recentDeposits) { deposit ->
                    DepositHistoryItem(deposit)
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
fun DepositHistoryItem(deposit: Deposit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(EcoGreenLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Recycling, contentDescription = null, tint = EcoGreenDark)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deposit.categoryName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${deposit.weightKg} kg × Rp ${String.format("%,.0f", deposit.pricePerKg)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = deposit.createdAt.take(10),
                    fontSize = 11.sp,
                    color = EcoTextMuted
                )
            }
            Text(
                text = "+Rp ${String.format("%,.0f", deposit.totalAmount)}",
                color = EcoGreenPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CitizenHomeScreenPreview() {
    EcoSortTheme {
        CitizenHomeContent(
            state = CitizenHomeUiState(
                user = User(
                    id = 1,
                    fullName = "Siti Aminah",
                    phoneNumber = "081987654321",
                    role = "WARGA",
                    address = "Jl. Melati No. 12",
                    balance = 52500.0,
                    totalWeightDeposited = 15.5
                ),
                recentDeposits = listOf(
                    Deposit(
                        id = 1,
                        citizenId = 1,
                        citizenName = "Siti Aminah",
                        citizenPhoneNumber = "081987654321",
                        categoryId = 1,
                        categoryName = "Botol Plastik PET Bening",
                        weightKg = 5.0,
                        pricePerKg = 3500.0,
                        totalAmount = 17500.0,
                        createdAt = "2026-10-11T10:00:00"
                    ),
                    Deposit(
                        id = 2,
                        citizenId = 1,
                        citizenName = "Siti Aminah",
                        citizenPhoneNumber = "081987654321",
                        categoryId = 3,
                        categoryName = "Kaleng Aluminium Minuman",
                        weightKg = 2.5,
                        pricePerKg = 14000.0,
                        totalAmount = 35000.0,
                        createdAt = "2026-10-10T14:30:00"
                    )
                ),
                stats = DashboardStats(
                    totalWeightCollected = 120.0,
                    totalPayoutDistributed = 450000.0,
                    currentCashBalance = 1550000.0,
                    estimatedCo2ReducedKg = 300.0
                )
            ),
            onNavigateToCatalog = {},
            onNavigateToReward = {},
            onNavigateToHistory = {},
            onLogout = {}
        )
    }
}
