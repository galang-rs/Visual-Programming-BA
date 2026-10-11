package com.ecosort.app.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Login : Screen("login", "Masuk")
    object Register : Screen("register", "Daftar Akun")
    object CitizenHome : Screen("citizen_home", "Beranda")
    object CatalogCalculator : Screen("catalog_calculator", "Katalog & Kalkulator")
    object Reward : Screen("reward", "Tukar Reward")
    object History : Screen("history", "Riwayat & Buku Kas")
    object OfficerDeposit : Screen("officer_deposit", "Setor Sampah (Petugas)")
    object OfficerVerifyClaim : Screen("officer_verify", "Verifikasi Klaim (Petugas)")
}
