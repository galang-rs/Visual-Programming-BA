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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.ecosort.app.ui.theme.*
import com.ecosort.app.viewmodel.CatalogUiState
import com.ecosort.app.viewmodel.CatalogViewModel

@Composable
fun CatalogCalculatorScreen(
    viewModel: CatalogViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCommodities()
    }

    CatalogCalculatorContent(
        state = state,
        onSelectCommodity = { viewModel.selectCommodity(it) },
        onWeightChange = { viewModel.onWeightChange(it) },
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun CatalogCalculatorContent(
    state: CatalogUiState,
    onSelectCommodity: (Commodity) -> Unit,
    onWeightChange: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Katalog & Kalkulator Nilai", fontWeight = FontWeight.Bold, color = EcoGreenDark) },
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
            // Interactive Calculator Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = EcoGreenLight.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Calculate, contentDescription = null, tint = EcoGreenDark, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Kalkulator Estimasi Nilai Sampah",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = EcoGreenDark
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Pilih komoditas di bawah, lalu masukkan perkiraan berat sampah Anda:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Category Selected Pill
                        state.selectedCommodity?.let { selected ->
                            Surface(
                                color = EcoGreenPrimary,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Terpilih: ${selected.name} (Rp ${String.format("%,.0f", selected.pricePerKg)}/kg)",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Weight Input Field
                        OutlinedTextField(
                            value = state.inputWeight,
                            onValueChange = onWeightChange,
                            label = { Text("Estimasi Berat Sampah (kg)") },
                            trailingIcon = { Text("kg", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 12.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Result Box
                        Card(
                            colors = CardDefaults.cardColors(containerColor = EcoSurface),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Proyeksi Saldo Didapat", fontSize = 11.sp, color = EcoTextSecondary)
                                    Text(
                                        "Rp ${String.format("%,.0f", state.estimatedPayout)}",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EcoGreenPrimary
                                    )
                                }
                                Surface(
                                    color = EcoAmberLight,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        "Hitungan Instan",
                                        color = EcoAmber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Commodity List Header
            item {
                Text(
                    text = "Daftar Komoditas & Panduan Pemilahan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Commodity Cards
            items(state.commodities) { commodity ->
                val isSelected = commodity.id == state.selectedCommodity?.id
                CommodityCard(
                    commodity = commodity,
                    isSelected = isSelected,
                    onClick = { onSelectCommodity(commodity) }
                )
            }
        }
    }
}

@Composable
fun CommodityCard(
    commodity: Commodity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) EcoGreenLight.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, EcoGreenPrimary) else null,
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) EcoGreenPrimary else EcoGreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Recycling,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else EcoGreenDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = commodity.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = commodity.description,
                            fontSize = 12.sp,
                            color = EcoTextSecondary,
                            maxLines = 2
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Price badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = EcoGreenPrimary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Rp ${String.format("%,.0f", commodity.pricePerKg)} / ${commodity.unit}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                if (isSelected) {
                    Text(
                        text = "Sedang Dihitung ✓",
                        color = EcoGreenPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Cleaning tips
            if (commodity.cleaningInstructions.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = EcoBorder)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = EcoAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tips Pembersihan: ${commodity.cleaningInstructions}",
                        fontSize = 11.sp,
                        color = EcoTextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CatalogCalculatorScreenPreview() {
    val sampleCommodities = listOf(
        Commodity(
            id = 1,
            name = "Botol Plastik PET Bening",
            description = "Botol air mineral transparan tanpa warna.",
            unit = "kg",
            pricePerKg = 3500.0,
            cleaningInstructions = "Lepas label, bilas sisa air, remas hingga pipih."
        ),
        Commodity(
            id = 2,
            name = "Kardus Lembaran Bersih",
            description = "Kardus gelombang cokelat tebal kemasan paket.",
            unit = "kg",
            pricePerKg = 2200.0,
            cleaningInstructions = "Bongkar lipatan, lepas lakban, pastikan kering."
        ),
        Commodity(
            id = 3,
            name = "Kaleng Aluminium Minuman",
            description = "Kaleng minuman ringan berbahan aluminium murni.",
            unit = "kg",
            pricePerKg = 14000.0,
            cleaningInstructions = "Bilas dengan air dan pipihkan."
        )
    )

    EcoSortTheme {
        CatalogCalculatorContent(
            state = CatalogUiState(
                commodities = sampleCommodities,
                selectedCommodity = sampleCommodities.first(),
                inputWeight = "5",
                estimatedPayout = 17500.0
            ),
            onSelectCommodity = {},
            onWeightChange = {},
            onNavigateBack = {}
        )
    }
}
