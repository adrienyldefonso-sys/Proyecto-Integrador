package com.yldefonso.ojodchakra.util

/** Color y texto de cada severidad en el mapa. Los valores de hue son los de BitmapDescriptorFactory. */
object SeveridadMapa {
    const val HUE_ROJO = 0f
    const val HUE_NARANJA = 30f
    const val HUE_VERDE = 120f
    const val HUE_SIN_DATOS = 210f

    fun hue(severidad: String?): Float = when (severidad?.uppercase()) {
        "ALTA" -> HUE_ROJO
        "MEDIA" -> HUE_NARANJA
        "NINGUNA" -> HUE_VERDE
        else -> HUE_SIN_DATOS
    }

    fun etiqueta(severidad: String?): String = when (severidad?.uppercase()) {
        "ALTA" -> "Severidad alta"
        "MEDIA" -> "Severidad media"
        "NINGUNA" -> "Sin plaga"
        else -> "Sin diagnóstico"
    }
}
