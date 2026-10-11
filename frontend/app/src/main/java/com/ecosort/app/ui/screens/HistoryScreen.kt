package com.ecosort.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ecosort.app.model.CashBookEntry
import com.ecosort.app.model.Deposit
import com.ecosort.app.model.RewardClaim
import com.ecosort.app.ui.theme.*
import com.ecosort.app.viewmodel.HistoryUiState
import com.ecosort.app.viewmodel.HistoryViewModel

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    isOfficer: Boolean,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    HistoryContent(
        state = state,
        isOfficer = isOfficer,
        onSelectTab = { viewModel.selectTab(it) },
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun HistoryContent(
    state: HistoryUiState,
    isOfficer: Boolean,
    onSelectTab: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val tabs = if (isOfficer) listOf("Setoran", "Reward", "Buku Kas") else listOf("Setoran", "Reward")

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Riwayat Transaksi & Audit", fontWeight = FontWeight.Bold, color = EcoGreenDark) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = EcoGreenPrimary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { onSelectTab(index) },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (state.selectedTab) {
                    0 -> { // Deposits
                        if (state.deposits.isEmpty()) {
                            item {
                                Text("Tidak ada transaksi penyetoran.", color = EcoTextSecondary, modifier = Modifier.padding(16.dp))
                            }
                        } else {
                            items(state.deposits) { deposit ->
                                DepositHistoryItem(deposit)
                            }
                        }
                    }
                    1 -> { // Claims
                        if (state.claims.isEmpty()) {
                            item {
                                Text("Tidak ada riwayat klaim reward.", color = EcoTextSecondary, modifier = Modifier.padding(16.dp))
                            }
                        } else {
                            items(state.claims) { claim ->
                                ClaimItemRow(claim)
                            }
                        }
                    }
                    2 -> { // CashBook (Officer only)
                        if (state.cashBook.isEmpty()) {
                            item {
                                Text("Buku kas digital kosong.", color = EcoTextSecondary, modifier = Modifier.padding(16.dp))
                            }
                        } else {
                            items(state.cashBook) { entry ->
                                CashBookItemRow(entry)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CashBookItemRow(entry: CashBookEntry) {
    val isCredit = entry.transactionType == "CASH_INJECTION"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.description ?: entry.transactionType,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 2
                )
                Text(entry.createdAt.take(16), fontSize = 11.sp, color = EcoTextMuted)
                Text("Saldo Kas: Rp ${String.format("%,.0f", entry.balanceAfter)}", fontSize = 11.sp, color = EcoTextSecondary)
            }

            Text(
                text = "${if (isCredit) "+" else "-"}Rp ${String.format("%,.0f", entry.amount)}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = if (isCredit) EcoGreenPrimary else EcoError
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HistoryScreenPreview() {
    val sampleDeposits = listOf(
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
        )
    )

    EcoSortTheme {
        HistoryContent(
            state = HistoryUiState(
                selectedTab = 0,
                deposits = sampleDeposits
            ),
            isOfficer = true,
            onSelectTab = {},
            onNavigateBack = {}
        )
    }
}
