package com.yldefonso.ojodchakra.ui.mapa

import androidx.compose.ui.graphics.Color

/** Colores semánticos de severidad para marcadores, leyenda y tarjetas del mapa. */
object MapaColores {
    val Alta = Color(0xFFD93A2F)
    val Media = Color(0xFFE08A1E)
    val Ninguna = Color(0xFF2E9448)
    val SinDatos = Color(0xFF2F7BC4)

    fun deSeveridad(severidad: String?): Color = when (severidad?.uppercase()) {
        "ALTA" -> Alta
        "MEDIA" -> Media
        "NINGUNA" -> Ninguna
        else -> SinDatos
    }
}
