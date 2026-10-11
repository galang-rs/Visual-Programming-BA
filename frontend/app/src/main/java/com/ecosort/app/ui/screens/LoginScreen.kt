package com.ecosort.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ecosort.app.ui.theme.EcoGreenDark
import com.ecosort.app.ui.theme.EcoGreenLight
import com.ecosort.app.ui.theme.EcoGreenPrimary
import com.ecosort.app.ui.theme.EcoSortTheme
import com.ecosort.app.viewmodel.AuthUiState
import com.ecosort.app.viewmodel.AuthViewModel

/**
 * Composable Stateful: LoginScreen
 * 
 * Bertanggung jawab mengikat (bind) ViewModel dengan antarmuka Compose.
 * Mengamati StateFlow `uiState` dan mendengarkan event sukses login melalui `LaunchedEffect`
 * untuk memicu navigasi berbasis peran pengguna (Role-Based Navigation: WARGA atau PETUGAS).
 * 
 * @param viewModel Instans AuthViewModel pengelola logika autentikasi
 * @param onLoginSuccess Callback navigasi yang menerima peran pengguna ('WARGA' atau 'PETUGAS')
 * @param onNavigateToRegister Callback untuk berpindah ke layar RegisterScreen
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (role: String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    // [LOGIKA 1] Mengamati StateFlow secara Reaktif di Compose Lifecycle
    val state by viewModel.uiState.collectAsState()

    // [LOGIKA 2] Efek Samping Navigasi: Dipicu hanya saat token JWT berhasil diterbitkan
    LaunchedEffect(state.authSuccess) {
        state.authSuccess?.let {
            onLoginSuccess(it.role)
            viewModel.resetSuccess()
        }
    }

    // Memanggil Composable Stateless murni
    LoginContent(
        state = state,
        onPhoneNumberChange = { viewModel.onPhoneNumberChange(it) },
        onPasswordChange = { viewModel.onPasswordChange(it) },
        onQuickFill = { phone, pass ->
            viewModel.onPhoneNumberChange(phone)
            viewModel.onPasswordChange(pass)
        },
        onLogin = { viewModel.login() },
        onNavigateToRegister = onNavigateToRegister
    )
}

/**
 * Composable Stateless: LoginContent
 * 
 * Komponen antarmuka murni tanpa dependensi langsung ke ViewModel.
 * Menyajikan elemen visual form masuk, tombol 1-Tap Quick Fill untuk pengujian cepat,
 * toggle visibilitas password, tombol aksi masuk, dan tautan pendaftaran.
 * Mendukung Compose Preview instan di Android Studio.
 */
@Composable
fun LoginContent(
    state: AuthUiState,
    onPhoneNumberChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onQuickFill: (String, String) -> Unit,
    onLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    // State lokal untuk visibilitas sensor kata sandi
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Logo Branding EcoSort
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(EcoGreenPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Recycling,
                    contentDescription = "EcoSort Logo",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "EcoSort",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = EcoGreenDark
            )
            Text(
                text = "Sistem Bank Sampah & Ekonomi Sirkular",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // [LOGIKA 3] Kartu Akses Cepat Pengujian (1-Tap Quick Fill):
            // Mempermudah dosen/evaluator langsung mengisi akun Petugas atau Warga tanpa mengetik manual
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EcoGreenLight.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Akses Cepat Pengujian (1-Tap):",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelLarge,
                        color = EcoGreenDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onQuickFill("081234567890", "password123") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Petugas", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { onQuickFill("081987654321", "password123") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Warga", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Kolom Masukan Nomor Telepon
            OutlinedTextField(
                value = state.phoneNumber,
                onValueChange = onPhoneNumberChange,
                label = { Text("Nomor Telepon") },
                placeholder = { Text("08xxxxxxxxxx") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = EcoGreenPrimary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Kolom Masukan Kata Sandi dengan Toggle Mata
            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = { Text("Kata Sandi") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = EcoGreenPrimary) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Sembunyikan" else "Tampilkan"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            // Tampilan Pesan Error Validasi / Autentikasi
            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // [LOGIKA 4] Tombol Eksekusi Masuk ke Sistem (Login)
            Button(
                onClick = onLogin,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Masuk ke EcoSort", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigasi ke Form Registrasi Nasabah Baru
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Belum punya akun warga? ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = onNavigateToRegister) {
                    Text("Daftar Sekarang", fontWeight = FontWeight.Bold, color = EcoGreenPrimary)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Preview Composable: LoginScreenPreview
 * 
 * Memfasilitasi preview visual instan di panel Split/Design Android Studio
 * tanpa memerlukan backend aktif maupun emulator.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    EcoSortTheme {
        LoginContent(
            state = AuthUiState(
                phoneNumber = "081987654321",
                password = "password123"
            ),
            onPhoneNumberChange = {},
            onPasswordChange = {},
            onQuickFill = { _, _ -> },
            onLogin = {},
            onNavigateToRegister = {}
        )
    }
}
