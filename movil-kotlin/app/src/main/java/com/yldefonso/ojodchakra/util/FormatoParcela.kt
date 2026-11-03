package com.yldefonso.ojodchakra.util

import java.util.Locale

object FormatoParcela {

    fun conteoDiagnosticos(total: Int): String = when (total) {
        0 -> "Sin diagnósticos"
        1 -> "1 diagnóstico"
        else -> "$total diagnósticos"
    }

    fun coordenadas(latitud: Double, longitud: Double): String =
        String.format(Locale.US, "%.5f, %.5f", latitud, longitud)
}