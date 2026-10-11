package com.ecosort.app.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ecosort.app.data.UserPreferences
import com.ecosort.app.network.RetrofitClient
import com.ecosort.app.repository.*
import com.ecosort.app.ui.screens.*
import com.ecosort.app.viewmodel.*

@Composable
fun EcoSortNavGraph(
    context: Context,
    navController: NavHostController = rememberNavController()
) {
    val apiService = remember { RetrofitClient.getService(context) }
    val userPreferences = remember { UserPreferences(context) }

    val authRepository = remember { AuthRepository(apiService, userPreferences) }
    val catalogRepository = remember { CatalogRepository(apiService) }
    val depositRepository = remember { DepositRepository(apiService) }
    val rewardRepository = remember { RewardRepository(apiService) }
    val cashBookRepository = remember { CashBookRepository(apiService) }

    val authViewModel = remember { AuthViewModel(authRepository) }
    val citizenHomeViewModel = remember { CitizenHomeViewModel(authRepository, depositRepository, cashBookRepository) }
    val catalogViewModel = remember { CatalogViewModel(catalogRepository) }
    val rewardViewModel = remember { RewardViewModel(rewardRepository, authRepository) }
    val officerDepositViewModel = remember { OfficerDepositViewModel(depositRepository, catalogRepository) }
    val officerRewardViewModel = remember { OfficerRewardViewModel(rewardRepository) }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { role ->
                    if (role == "PETUGAS") {
                        navController.navigate(Screen.OfficerDeposit.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.CitizenHome.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = { role ->
                    if (role == "PETUGAS") {
                        navController.navigate(Screen.OfficerDeposit.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.CitizenHome.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.CitizenHome.route) {
            CitizenHomeScreen(
                viewModel = citizenHomeViewModel,
                onNavigateToCatalog = { navController.navigate(Screen.CatalogCalculator.route) },
                onNavigateToReward = { navController.navigate(Screen.Reward.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onLogout = {
                    authViewModel.logout {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.CatalogCalculator.route) {
            CatalogCalculatorScreen(
                viewModel = catalogViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Reward.route) {
            RewardScreen(
                viewModel = rewardViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.OfficerDeposit.route) {
            OfficerDepositScreen(
                viewModel = officerDepositViewModel,
                onNavigateToVerifyClaim = { navController.navigate(Screen.OfficerVerifyClaim.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onLogout = {
                    authViewModel.logout {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.OfficerVerifyClaim.route) {
            OfficerVerifyClaimScreen(
                viewModel = officerRewardViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            // Determine if current user is officer based on current destination or backstack
            val isOfficer = navController.previousBackStackEntry?.destination?.route == Screen.OfficerDeposit.route
            val historyViewModel = remember {
                HistoryViewModel(depositRepository, rewardRepository, cashBookRepository, isOfficer)
            }
            HistoryScreen(
                viewModel = historyViewModel,
                isOfficer = isOfficer,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
