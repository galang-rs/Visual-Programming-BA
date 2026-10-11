package com.ecosort.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ecosort.app.model.Commodity
import com.ecosort.app.model.Deposit
import com.ecosort.app.model.User
import com.ecosort.app.ui.theme.*
import com.ecosort.app.viewmodel.OfficerDepositUiState
import com.ecosort.app.viewmodel.OfficerDepositViewModel

/**
 * Composable Stateful: OfficerDepositScreen
 * 
 * Antarmuka Pos Penimbangan Fisik yang dioperasikan oleh Petugas Bank Sampah.
 * Menghubungkan OfficerDepositViewModel dengan antarmuka Compose,
 * memuat data inisial saat layar pertama kali dibuka (LaunchedEffect),
 * serta mengatur navigasi ke menu verifikasi voucher, riwayat audit, dan logout.
 */
@Composable
fun OfficerDepositScreen(
    viewModel: OfficerDepositViewModel,
    onNavigateToVerifyClaim: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onLogout: () -> Unit
) {
    // [LOGIKA 1] Mengamati State UI secara Reaktif
    val state by viewModel.uiState.collectAsState()

    // [LOGIKA 2] Memuat Kategori Sampah & Daftar Nasabah Saat Pertama Terbuka
    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
    }

    // Memanggil Composable Stateless murni
    OfficerDepositContent(
        state = state,
        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
        onSelectCitizen = { viewModel.selectCitizen(it) },
        onSelectCategory = { viewModel.selectCategory(it) },
        onWeightChange = { viewModel.onWeightChange(it) },
        onNotesChange = { viewModel.onNotesChange(it) },
        onSubmitDeposit = { viewModel.submitDeposit() },
        onDismissDialog = { viewModel.dismissSuccessDialog() },
        onNavigateToVerifyClaim = onNavigateToVerifyClaim,
        onNavigateToHistory = onNavigateToHistory,
        onLogout = onLogout
    )
}

/**
 * Composable Stateless: OfficerDepositContent
 * 
 * Menyajikan elemen visual pos penimbangan:
 * 1. Dialog Struk Digital Resmi setelah konfirmasi penimbangan.
 * 2. Top Bar dengan tombol navigasi verifikasi voucher klaim & logout.
 * 3. Pemilihan Nasabah Warga (dengan filter pencarian nomor telepon/nama).
 * 4. Pemilihan Kategori Komoditas Sampah Anorganik.
 * 5. Masukan Bobot Timbangan Riil (kg) dan Kalkulasi Otomatis Nilai Resmi.
 * 6. Tombol Konfirmasi & Simpan Setoran.
 */
@Composable
fun OfficerDepositContent(
    state: OfficerDepositUiState,
    onSearchQueryChange: (String) -> Unit,
    onSelectCitizen: (User) -> Unit,
    onSelectCategory: (Commodity) -> Unit,
    onWeightChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSubmitDeposit: () -> Unit,
    onDismissDialog: () -> Unit,
    onNavigateToVerifyClaim: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onLogout: () -> Unit
) {
    // Success Receipt Dialog
    state.successDeposit?.let { deposit ->
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
                Text("Setoran Berhasil Disimpan!", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Bukti Transaksi Resmi Petugas:", fontSize = 12.sp, color = EcoTextSecondary)
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Nasabah:", fontSize = 13.sp, color = EcoTextSecondary)
                        Text(deposit.citizenName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Material:", fontSize = 13.sp, color = EcoTextSecondary)
                        Text(deposit.categoryName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Berat Riil:", fontSize = 13.sp, color = EcoTextSecondary)
                        Text("${deposit.weightKg} kg", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tarif per kg:", fontSize = 13.sp, color = EcoTextSecondary)
                        Text("Rp ${String.format("%,.0f", deposit.pricePerKg)}", fontSize = 13.sp)
                    }
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Kredit Saldo:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            "Rp ${String.format("%,.0f", deposit.totalAmount)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = EcoGreenPrimary
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Saldo Akhir Nasabah:", fontSize = 12.sp, color = EcoTextSecondary)
                        Text("Rp ${String.format("%,.0f", deposit.citizenBalanceAfter)}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary)
                ) {
                    Text("Selesai")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Column {
                        Text("Pos Penimbangan Petugas", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = EcoGreenDark)
                        Text("Verifikasi Timbangan & Kas Bank Sampah", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToVerifyClaim) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Verifikasi Voucher", tint = EcoAmber)
                    }
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "Buku Kas & Riwayat", tint = EcoEmerald)
                    }
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
            // Mode Banner
            item {
                Surface(
                    color = EcoGreenDark,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Scale, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Formulir Penyetoran Sampah Fisik", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Text("Kalkulasi otomatis & pembaruan buku kas atomik", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        }
                    }
                }
            }

            // Step 1: Select Citizen
            item {
                Text("1. Pilih Nasabah / Warga", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Cari nama atau nomor telepon nasabah...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }

            state.selectedCitizen?.let { selected ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = EcoGreenLight.copy(alpha = 0.6f)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, EcoGreenPrimary)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Nasabah Terpilih:", fontSize = 11.sp, color = EcoGreenDark, fontWeight = FontWeight.SemiBold)
                                Text(selected.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(selected.phoneNumber, fontSize = 12.sp, color = EcoTextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Saldo Saat Ini", fontSize = 11.sp, color = EcoTextSecondary)
                                Text("Rp ${String.format("%,.0f", selected.balance)}", fontWeight = FontWeight.Bold, color = EcoGreenPrimary)
                            }
                        }
                    }
                }
            } ?: run {
                items(state.citizens.take(3)) { citizen ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectCitizen(citizen) },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(citizen.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(citizen.phoneNumber, fontSize = 11.sp, color = EcoTextSecondary)
                            }
                            Text("Pilih >", color = EcoGreenPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Step 2: Select Waste Category
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("2. Pilih Kategori Sampah", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            items(state.categories) { category ->
                val isSelected = category.id == state.selectedCategory?.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelectCategory(category) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) EcoGreenLight.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, EcoGreenPrimary) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(category.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(
                            "Rp ${String.format("%,.0f", category.pricePerKg)}/kg",
                            fontWeight = FontWeight.Bold,
                            color = EcoGreenPrimary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Step 3: Input Weight
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("3. Masukkan Angka Timbangan Riil", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            item {
                OutlinedTextField(
                    value = state.weightInput,
                    onValueChange = onWeightChange,
                    label = { Text("Berat Terukur pada Timbangan Fisik (kg)") },
                    trailingIcon = { Text("kg", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = state.notesInput,
                    onValueChange = onNotesChange,
                    label = { Text("Catatan Kondisi Sampah (Opsional)") },
                    placeholder = { Text("Contoh: Botol sangat bersih dan kering") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }

            // Live Calculation Preview
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Nilai Resmi:", fontSize = 12.sp, color = EcoTextSecondary)
                            Text(
                                "Rp ${String.format("%,.0f", state.calculatedTotal)}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = EcoGreenPrimary
                            )
                        }
                        Surface(
                            color = EcoGreenLight,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Otomatis oleh Sistem",
                                color = EcoGreenDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Error display
            state.errorMessage?.let { error ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Submit Button
            item {
                Button(
                    onClick = onSubmitDeposit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                    enabled = !state.isSubmitting
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Konfirmasi & Simpan Setoran", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OfficerDepositScreenPreview() {
    val sampleCitizens = listOf(
        User(id = 1, fullName = "Siti Aminah", phoneNumber = "081987654321", role = "WARGA", balance = 25000.0),
        User(id = 2, fullName = "Ahmad Pratama", phoneNumber = "081112223333", role = "WARGA", balance = 85000.0)
    )
    val sampleCategories = listOf(
        Commodity(id = 1, name = "Botol Plastik PET Bening", description = "", unit = "kg", pricePerKg = 3500.0, cleaningInstructions = ""),
        Commodity(id = 2, name = "Kardus Lembaran", description = "", unit = "kg", pricePerKg = 2200.0, cleaningInstructions = "")
    )

    EcoSortTheme {
        OfficerDepositContent(
            state = OfficerDepositUiState(
                citizens = sampleCitizens,
                selectedCitizen = sampleCitizens.first(),
                categories = sampleCategories,
                selectedCategory = sampleCategories.first(),
                weightInput = "5.0",
                calculatedTotal = 17500.0
            ),
            onSearchQueryChange = {},
            onSelectCitizen = {},
            onSelectCategory = {},
            onWeightChange = {},
            onNotesChange = {},
            onSubmitDeposit = {},
            onDismissDialog = {},
            onNavigateToVerifyClaim = {},
            onNavigateToHistory = {},
            onLogout = {}
        )
    }
}
