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
import com.ecosort.app.ui.theme.*
import com.ecosort.app.viewmodel.OfficerRewardUiState
import com.ecosort.app.viewmodel.OfficerRewardViewModel

@Composable
fun OfficerVerifyClaimScreen(
    viewModel: OfficerRewardViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAllClaims()
    }

    OfficerVerifyClaimContent(
        state = state,
        onVoucherChange = { viewModel.onVoucherChange(it) },
        onVerifyClaim = { viewModel.verifyClaim() },
        onDismissDialog = { viewModel.dismissSuccessDialog() },
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun OfficerVerifyClaimContent(
    state: OfficerRewardUiState,
    onVoucherChange: (String) -> Unit,
    onVerifyClaim: () -> Unit,
    onDismissDialog: () -> Unit,
    onNavigateBack: () -> Unit
) {
    // Success Handover Dialog
    state.verifiedClaim?.let { claim ->
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
                Text("Verifikasi Sukses!", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Serah terima paket fisik telah selesai divalidasi:", fontSize = 12.sp, color = EcoTextSecondary)
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Voucher:", fontSize = 13.sp, color = EcoTextSecondary)
                        Text(claim.claimVoucherCode, fontWeight = FontWeight.Bold, color = EcoAmber)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Nasabah:", fontSize = 13.sp, color = EcoTextSecondary)
                        Text(claim.citizenName, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Barang:", fontSize = 13.sp, color = EcoTextSecondary)
                        Text(claim.rewardItemName, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Status:", fontSize = 13.sp, color = EcoTextSecondary)
                        Text("COMPLETED", fontWeight = FontWeight.Bold, color = EcoGreenPrimary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary)
                ) {
                    Text("Tutup")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Verifikasi Voucher Klaim", fontWeight = FontWeight.Bold, color = EcoGreenDark) },
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
            // Voucher Input Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Masukkan Kode Voucher Nasabah:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = state.voucherInput,
                            onValueChange = onVoucherChange,
                            placeholder = { Text("Contoh: ECO-A7B2X9") },
                            leadingIcon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = EcoAmber) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Error Display
                        state.errorMessage?.let { error ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(10.dp),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onVerifyClaim,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                            enabled = !state.isVerifying
                        ) {
                            if (state.isVerifying) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text("Verifikasi & Serahkan Barang Fisik", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // All Claims List
            item {
                Text(
                    text = "Daftar Seluruh Klaim Voucher",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (state.allClaims.isEmpty()) {
                item {
                    Text("Belum ada pengajuan klaim voucher.", color = EcoTextSecondary, fontSize = 13.sp)
                }
            } else {
                items(state.allClaims) { claim ->
                    ClaimItemRow(claim)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OfficerVerifyClaimScreenPreview() {
    val sampleClaims = listOf(
        RewardClaim(
            id = 1,
            citizenId = 2,
            citizenName = "Siti Aminah",
            citizenPhoneNumber = "081987654321",
            rewardItemId = 1,
            rewardItemName = "Paket Beras Premium 5 kg",
            requiredBalance = 75000.0,
            claimVoucherCode = "ECO-BER492",
            status = "PENDING",
            claimedAt = "2026-10-11T11:20:00"
        ),
        RewardClaim(
            id = 2,
            citizenId = 3,
            citizenName = "Ahmad Pratama",
            citizenPhoneNumber = "081112223333",
            rewardItemId = 2,
            rewardItemName = "Minyak Goreng Sawit 2L",
            requiredBalance = 35000.0,
            claimVoucherCode = "ECO-MIN318",
            status = "COMPLETED",
            claimedAt = "2026-10-10T09:15:00"
        )
    )

    EcoSortTheme {
        OfficerVerifyClaimContent(
            state = OfficerRewardUiState(
                voucherInput = "ECO-BER492",
                allClaims = sampleClaims
            ),
            onVoucherChange = {},
            onVerifyClaim = {},
            onDismissDialog = {},
            onNavigateBack = {}
        )
    }
}

@Composable
fun ClaimItemRow(claim: RewardClaim) {
    val isCompleted = claim.status == "COMPLETED"

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
                Text(claim.rewardItemName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Nasabah: ${claim.citizenName} (${claim.citizenPhoneNumber})", fontSize = 12.sp, color = EcoTextSecondary)
                Text("Kode: ${claim.claimVoucherCode}", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = EcoAmber)
                Text(claim.claimedAt.take(16), fontSize = 11.sp, color = EcoTextMuted)
            }

            Surface(
                color = if (isCompleted) EcoGreenLight else EcoAmberLight,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = if (isCompleted) "Selesai ✓" else "Menunggu",
                    color = if (isCompleted) EcoGreenDark else EcoAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
