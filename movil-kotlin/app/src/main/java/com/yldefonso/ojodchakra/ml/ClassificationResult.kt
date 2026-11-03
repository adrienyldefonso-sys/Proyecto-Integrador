package com.yldefonso.ojodchakra.ml

data class ClassificationResult(
    val label: String,
    val confidence: Float
) {
    val labelEnEspanol: String
        get() = when (label) {
            "Potato___Early_blight" -> "Tizón temprano"
            "Potato___Late_blight" -> "Tizón tardío"
            "Potato___healthy" -> "Planta sana"
            else -> label
        }
}