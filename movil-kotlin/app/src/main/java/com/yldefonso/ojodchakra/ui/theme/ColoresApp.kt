package com.yldefonso.ojodchakra.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Colores propios de la app que Material no cubre: banda de encabezado y estados. */
@Immutable
data class ColoresApp(
    val bandaFondo: Color,
    val bandaTexto: Color,
    val bandaAcento: Color,
    val exitoFondo: Color,
    val exitoTexto: Color,
    val alertaFondo: Color,
    val alertaTexto: Color,
    val peligroFondo: Color,
    val peligroTexto: Color,
    val infoFondo: Color,
    val infoTexto: Color
)

val ColoresAppClaros = ColoresApp(
    bandaFondo = VerdeBosque,
    bandaTexto = Color.White,
    bandaAcento = Color(0xFF9ED9A8),
    exitoFondo = Color(0xFFDDF2E1),
    exitoTexto = Color(0xFF1D6B33),
    alertaFondo = Color(0xFFFFEFD1),
    alertaTexto = Color(0xFF7A4F00),
    peligroFondo = Color(0xFFFBE0DC),
    peligroTexto = Color(0xFFA3261C),
    infoFondo = Color(0xFFDCEBFA),
    infoTexto = Color(0xFF1A5A94)
)

val ColoresAppOscuros = ColoresApp(
    bandaFondo = VerdeBosqueMuyOscuro,
    bandaTexto = TextoOscuro,
    bandaAcento = VerdeMenta,
    exitoFondo = Color(0xFF1F3D27),
    exitoTexto = Color(0xFF9EE0AE),
    alertaFondo = Color(0xFF47361A),
    alertaTexto = Color(0xFFF5C66B),
    peligroFondo = Color(0xFF4D2220),
    peligroTexto = Color(0xFFFFB4AB),
    infoFondo = Color(0xFF1B3550),
    infoTexto = Color(0xFFA9CCF2)
)

val LocalColoresApp = staticCompositionLocalOf { ColoresAppClaros }
