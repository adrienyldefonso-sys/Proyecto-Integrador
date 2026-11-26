package com.yldefonso.ojodchakra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import com.yldefonso.ojodchakra.navigation.AppNavigation
import com.yldefonso.ojodchakra.sesion.SesionUsuario
import com.yldefonso.ojodchakra.sincronizacion.ProgramadorSincronizacion
import com.yldefonso.ojodchakra.ui.theme.OjoDChakraTheme
import com.yldefonso.ojodchakra.util.PreferenciasApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenciasApp.cargar(this)
        SesionUsuario.cargar(this)
        ProgramadorSincronizacion.programarPeriodica(this)
        enableEdgeToEdge()
        setContent {
            val oscuro = when (PreferenciasApp.tema) {
                PreferenciasApp.TEMA_OSCURO -> true
                PreferenciasApp.TEMA_CLARO -> false
                else -> isSystemInDarkTheme()
            }
            OjoDChakraTheme(darkTheme = oscuro) {
                AppNavigation()
            }
        }
    }
}
