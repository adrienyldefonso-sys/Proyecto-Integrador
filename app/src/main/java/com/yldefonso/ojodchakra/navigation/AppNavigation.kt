package com.yldefonso.ojodchakra.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yldefonso.ojodchakra.screens.camera.CameraScreen
import com.yldefonso.ojodchakra.screens.camera.CameraViewModel
import com.yldefonso.ojodchakra.screens.home.HomeScreen
import com.yldefonso.ojodchakra.screens.result.ResultScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val cameraViewModel: CameraViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            HomeScreen(
                onStartScan = { navController.navigate("camera") }
            )
        }

        composable("camera") {
            CameraScreen(
                viewModel = cameraViewModel,
                onDiagnosisReady = { navController.navigate("result") }
            )
        }

        composable("result") {
            ResultScreen(
                viewModel = cameraViewModel,
                onScanAgain = { navController.navigate("camera") }
            )
        }
    }
}