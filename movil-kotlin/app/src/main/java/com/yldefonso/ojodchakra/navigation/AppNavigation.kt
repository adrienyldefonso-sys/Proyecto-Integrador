package com.yldefonso.ojodchakra.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yldefonso.ojodchakra.screens.camera.CameraScreen
import com.yldefonso.ojodchakra.screens.camera.CameraViewModel
import com.yldefonso.ojodchakra.screens.history.DetailScreen
import com.yldefonso.ojodchakra.screens.history.HistoryScreen
import com.yldefonso.ojodchakra.screens.home.HomeScreen
import com.yldefonso.ojodchakra.screens.parcelas.ParcelaFormScreen
import com.yldefonso.ojodchakra.screens.result.ResultScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val cameraViewModel: CameraViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {

        // 1. PANTALLA PRINCIPAL (HOME)
        composable("home") {
            HomeScreen(
                onStartScan = { navController.navigate("camera") },
                onVerHistorial = { navController.navigate("history") }
            )
        }

        // 2. PANTALLA DE CÁMARA / ESCÁNER
        composable("camera") {
            CameraScreen(
                viewModel = cameraViewModel,
                onDiagnosisReady = { navController.navigate("result") }
            )
        }

        // 3. PANTALLA DE RESULTADOS
        composable("result") {
            ResultScreen(
                viewModel = cameraViewModel,
                onScanAgain = {
                    navController.navigate("camera") {
                        popUpTo("result") { inclusive = true }
                    }
                },
                onGoHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        // 4. PANTALLA DE HISTORIAL
        composable("history") {
            HistoryScreen(
                onReporteClick = { id ->
                    navController.navigate("detail/$id")
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 5. PANTALLA DE DETALLE DEL REPORTE
        composable(
            route = "detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            DetailScreen(
                id = id,
                onBack = { navController.popBackStack() }
            )
        }

        // 6. FORMULARIO DE PARCELA
        composable("parcela_form") {
            ParcelaFormScreen(
                onBack = { navController.popBackStack() },
                onGuardada = { navController.popBackStack() }
            )
        }
    }
}