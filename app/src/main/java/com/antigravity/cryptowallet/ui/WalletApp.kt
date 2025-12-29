package com.antigravity.cryptowallet.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.antigravity.cryptowallet.ui.onboarding.IntroScreen
import com.antigravity.cryptowallet.ui.onboarding.SeedRevealScreen
import com.antigravity.cryptowallet.ui.wallet.WalletScreen
import com.antigravity.cryptowallet.ui.wallet.TokenDetailScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.antigravity.cryptowallet.ui.wallet.SendScreen
import com.antigravity.cryptowallet.ui.history.TransactionDetailScreen

@Composable
fun WalletApp(startDestination: String = "intro") {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = startDestination) {
        composable("intro") {
            IntroScreen(
                onCreateWallet = { navController.navigate("create_wallet") },
                onImportWallet = { navController.navigate("import_wallet") }
            )
        }
        composable("create_wallet") {
            com.antigravity.cryptowallet.ui.onboarding.CreateWalletScreen(
                onWalletCreated = {
                    navController.navigate("home") {
                        popUpTo("intro") { inclusive = true }
                    }
                }
            )
        }
        composable("import_wallet") {
            com.antigravity.cryptowallet.ui.onboarding.ImportWalletScreen(
                onWalletImported = {
                    navController.navigate("home") {
                        popUpTo("intro") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            val securityViewModel = androidx.hilt.navigation.compose.hiltViewModel<com.antigravity.cryptowallet.ui.security.SecurityViewModel>()
            MainScreen(
                onNavigateToSecuritySetup = { navController.navigate("security_setup") },
                onNavigateToSeedReveal = { navController.navigate("seed_reveal") },
                onNavigateToTokenDetail = { assetId -> navController.navigate("token_detail/$assetId") },
                onLogout = {
                    securityViewModel.logout()
                    navController.navigate("intro") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = "token_detail/{assetId}",
            arguments = listOf(navArgument("assetId") { type = NavType.StringType })
        ) { backStackEntry ->
            val assetId = backStackEntry.arguments?.getString("assetId") ?: ""
            TokenDetailScreen(
                assetId = assetId,
                onBack = { navController.popBackStack() },
                onNavigateToSend = { id -> navController.navigate("send/$id") },
                onNavigateToTxDetail = { hash -> navController.navigate("tx_detail/$hash") }
            )
        }

        composable(
            route = "send/{assetId}",
            arguments = listOf(navArgument("assetId") { type = NavType.StringType })
        ) { backStackEntry ->
            val assetId = backStackEntry.arguments?.getString("assetId") ?: ""
            SendScreen(
                assetId = assetId,
                onBack = { navController.popBackStack() },
                onSendSuccess = { txHash ->
                    navController.navigate("tx_detail/$txHash") {
                        popUpTo("token_detail/$assetId")
                    }
                }
            )
        }

        composable(
            route = "tx_detail/{txHash}",
            arguments = listOf(navArgument("txHash") { type = NavType.StringType })
        ) { backStackEntry ->
            val txHash = backStackEntry.arguments?.getString("txHash") ?: ""
            TransactionDetailScreen(
                txHash = txHash,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("seed_reveal") {
            SeedRevealScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("unlock") {
            val viewModel = androidx.hilt.navigation.compose.hiltViewModel<com.antigravity.cryptowallet.ui.security.SecurityViewModel>()
            com.antigravity.cryptowallet.ui.security.LockScreen(
                mode = com.antigravity.cryptowallet.ui.security.LockMode.UNLOCK,
                onUnlock = {
                    navController.navigate("home") {
                        popUpTo("unlock") { inclusive = true }
                    }
                },
                checkPin = { viewModel.checkPin(it) },
                biometricEnabled = viewModel.isBiometricEnabled()
            )
        }

        composable("security_setup") {
            val viewModel = androidx.hilt.navigation.compose.hiltViewModel<com.antigravity.cryptowallet.ui.security.SecurityViewModel>()
            com.antigravity.cryptowallet.ui.security.LockScreen(
                mode = com.antigravity.cryptowallet.ui.security.LockMode.SETUP,
                onPinSet = { pin ->
                    viewModel.setPin(pin)
                    navController.popBackStack()
                },
                onUnlock = {}, // Not used in setup
                biometricEnabled = false
            )
        }
    }
}
