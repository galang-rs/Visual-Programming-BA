package com.ecosort.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ecosort.app.ui.theme.EcoGreenDark
import com.ecosort.app.ui.theme.EcoGreenPrimary
import com.ecosort.app.ui.theme.EcoSortTheme
import com.ecosort.app.viewmodel.AuthUiState
import com.ecosort.app.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: (role: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.authSuccess) {
        state.authSuccess?.let {
            onRegisterSuccess(it.role)
            viewModel.resetSuccess()
        }
    }

    RegisterContent(
        state = state,
        onRoleChange = { viewModel.onRoleChange(it) },
        onFullNameChange = { viewModel.onFullNameChange(it) },
        onPhoneNumberChange = { viewModel.onPhoneNumberChange(it) },
        onAddressChange = { viewModel.onAddressChange(it) },
        onPasswordChange = { viewModel.onPasswordChange(it) },
        onRegister = { viewModel.register() },
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun RegisterContent(
    state: AuthUiState,
    onRoleChange: (String) -> Unit,
    onFullNameChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegister: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Pendaftaran Akun Baru", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = EcoGreenDark
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Mari Bergabung Memilah Sampah",
                style = MaterialTheme.typography.titleLarge,
                color = EcoGreenDark,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Dapatkan saldo rupiah dan tukarkan dengan berbagai sembako kebutuhan rumah tangga",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Role Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = state.role == "WARGA",
                    onClick = { onRoleChange("WARGA") },
                    label = { Text("Warga / Nasabah") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = state.role == "PETUGAS",
                    onClick = { onRoleChange("PETUGAS") },
                    label = { Text("Petugas Bank Sampah") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Full Name Input
            OutlinedTextField(
                value = state.fullName,
                onValueChange = onFullNameChange,
                label = { Text("Nama Lengkap") },
                placeholder = { Text("Contoh: Siti Aminah") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = EcoGreenPrimary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Phone Input
            OutlinedTextField(
                value = state.phoneNumber,
                onValueChange = onPhoneNumberChange,
                label = { Text("Nomor Telepon (WhatsApp)") },
                placeholder = { Text("081234567890") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = EcoGreenPrimary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Address Input
            OutlinedTextField(
                value = state.address,
                onValueChange = onAddressChange,
                label = { Text("Alamat Domisili (RT/RW)") },
                placeholder = { Text("Jl. Melati No. 12, RT 02/05") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = EcoGreenPrimary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password Input
            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = { Text("Kata Sandi (Minimal 6 Karakter)") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = EcoGreenPrimary) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Error display
            state.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = onRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Buat Akun EcoSort", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    EcoSortTheme {
        RegisterContent(
            state = AuthUiState(
                fullName = "Siti Aminah",
                phoneNumber = "081987654321",
                address = "Jl. Melati No. 12, RT 02/05",
                role = "WARGA"
            ),
            onRoleChange = {},
            onFullNameChange = {},
            onPhoneNumberChange = {},
            onAddressChange = {},
            onPasswordChange = {},
            onRegister = {},
            onNavigateBack = {}
        )
    }
}
