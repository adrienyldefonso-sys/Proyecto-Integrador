package com.yldefonso.ojodchakra.navigation

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
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
import com.yldefonso.ojodchakra.screens.ajustes.AjustesScreen
import com.yldefonso.ojodchakra.screens.home.HomeScreen
import com.yldefonso.ojodchakra.screens.parcelas.ParcelaFormScreen
import com.yldefonso.ojodchakra.screens.mapa.MapaParcelasScreen
import com.yldefonso.ojodchakra.screens.parcelas.ParcelasScreen
import com.yldefonso.ojodchakra.screens.auth.PantallaBienvenida
import com.yldefonso.ojodchakra.screens.auth.PantallaLogin
import com.yldefonso.ojodchakra.screens.auth.PantallaRegistro
import com.yldefonso.ojodchakra.screens.result.ResultScreen
import com.yldefonso.ojodchakra.sesion.SesionUsuario

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val cameraViewModel: CameraViewModel = viewModel()
    val contexto = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = if (SesionUsuario.puedeEntrar) "home" else "bienvenida"
    ) {

        // 0. ACCESO: bienvenida, inicio de sesión y registro
        composable("bienvenida") {
            PantallaBienvenida(
                onIniciarSesion = { navController.navigate("login") },
                onRegistrarse = { navController.navigate("registro") },
                onSinConexion = {
                    SesionUsuario.activarInvitado(contexto)
                    irAlInicio(navController, contexto, null)
                }
            )
        }
        composable("login") {
            PantallaLogin(
                onVolver = { navController.popBackStack() },
                onEntrar = { mensaje -> irAlInicio(navController, contexto, mensaje) },
                onIrRegistro = { navController.navigate("registro") }
            )
        }
        composable("registro") {
            PantallaRegistro(
                onVolver = { navController.popBackStack() },
                onRegistrado = {
                    irAlInicio(navController, contexto, "Cuenta creada. Tus datos se enviarán al servidor.")
                }
            )
        }

        // 1. PANTALLA PRINCIPAL (HOME)
        composable("home") {
            HomeScreen(
                // HU04-TAREA 4: Redirige a selección de parcela antes de abrir la cámara
                onStartScan = { navController.navigate("seleccion_parcela") },
                onVerHistorial = { navController.navigate("history") },
                onVerParcelas = { navController.navigate("parcelas") },
                onAjustes = { navController.navigate("ajustes") }
            )
        }

        // 2. PANTALLA DE CÁMARA / ESCÁNER
        composable("camera") {
            CameraScreen(
                viewModel = cameraViewModel,
                onDiagnosisReady = { navController.navigate("result") },
                onBack = {
                    cameraViewModel.reset()
                    navController.popBackStack()
                }
            )
        }

        // 3. PANTALLA DE RESULTADOS
        composable("result") {
            ResultScreen(
                viewModel = cameraViewModel,
                onScanAgain = {
                    // Mantiene la navegación directa a "camera" para conservar la parcela seleccionada
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

        // 6. LISTA DE PARCELAS (DESDE HOME)
        composable("parcelas") {
            ParcelasScreen(
                onBack = { navController.popBackStack() },
                onRegistrar = { navController.navigate("parcela_form") },
                onVerMapa = { navController.navigate("mapa_parcelas") }
            )
        }

        // 5b. AJUSTES (tema y datos del celular)
        composable("ajustes") {
            AjustesScreen(
                onBack = { navController.popBackStack() },
                onIrAcceso = { navController.navigate("bienvenida") { popUpTo(navController.graph.id) { inclusive = true } } }
            )
        }

        // 6b. MAPA DE PARCELAS (la pantalla valida que haya internet)
        composable("mapa_parcelas") {
            MapaParcelasScreen(onBack = { navController.popBackStack() })
        }

        // 7. FORMULARIO DE PARCELA
        composable("parcela_form") {
            ParcelaFormScreen(
                onBack = { navController.popBackStack() },
                onGuardada = { navController.popBackStack() }
            )
        }

        // 8. SELECCIÓN DE PARCELA PREVIA AL ESCANEO // HU04-TAREA 4: Ruta para elegir parcela antes de escanear
        composable("seleccion_parcela") {
            ParcelasScreen(
                titulo = "Elige la parcela",
                onBack = { navController.popBackStack() },
                onRegistrar = { navController.navigate("parcela_form") },
                onParcelaClick = { id ->
                    cameraViewModel.seleccionarParcela(id)
                    navController.navigate("camera")
                },
                onSinParcela = {
                    cameraViewModel.seleccionarParcela(null)
                    navController.navigate("camera")
                }
            )
        }
    }
}

/** Va al inicio y borra el historial de acceso, para que "atrás" no regrese al login. */
private fun irAlInicio(navController: NavHostController, contexto: Context, mensaje: String?) {
    if (mensaje != null) Toast.makeText(contexto, mensaje, Toast.LENGTH_LONG).show()
    navController.navigate("home") { popUpTo(navController.graph.id) { inclusive = true } }
}
