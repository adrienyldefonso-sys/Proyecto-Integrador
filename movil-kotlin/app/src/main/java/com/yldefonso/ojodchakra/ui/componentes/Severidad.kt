package com.yldefonso.ojodchakra.ui.componentes

/** Traduce el nivel de severidad de la recomendación a un estado visual. */
fun tipoDeSeveridad(nivel: String?): TipoEstado = when (nivel?.uppercase()) {
    "ALTA" -> TipoEstado.PELIGRO
    "MEDIA" -> TipoEstado.ALERTA
    "BAJA", "NINGUNA" -> TipoEstado.EXITO
    else -> TipoEstado.NEUTRO
}

/** Texto legible para el nivel de severidad. */
fun textoDeSeveridad(nivel: String?): String = when (nivel?.uppercase()) {
    "ALTA" -> "Severidad alta"
    "MEDIA" -> "Severidad media"
    "BAJA" -> "Severidad baja"
    "NINGUNA" -> "Sin riesgo"
    else -> "Sin evaluar"
}
