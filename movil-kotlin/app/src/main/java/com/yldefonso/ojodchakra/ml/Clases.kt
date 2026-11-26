package com.yldefonso.ojodchakra.ml

/** Clases que reconoce el modelo y su nombre para mostrar al agricultor. */
object Clases {
    val todas: List<String> = listOf(
        "Potato___Early_blight",
        "Potato___Late_blight",
        "Potato___healthy"
    )

    fun nombre(etiqueta: String): String = when (etiqueta) {
        "Potato___Early_blight" -> "Tizón temprano"
        "Potato___Late_blight" -> "Tizón tardío"
        "Potato___healthy" -> "Planta sana"
        else -> etiqueta
    }
}
