package com.yldefonso.ojodchakra.util

data class ErroresParcela(
    val nombre: String? = null,
    val cultivo: String? = null,
    val latitud: String? = null,
    val longitud: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(nombre, cultivo, latitud, longitud).any { it != null }
}

object ValidadorParcela {

    // Acepta coma o punto como separador decimal
    fun parsearCoordenada(texto: String): Double? =
        texto.trim().replace(',', '.').toDoubleOrNull()

    fun validar(
        nombre: String,
        cultivo: String,
        latitud: String,
        longitud: String
    ): ErroresParcela {
        val lat = parsearCoordenada(latitud)
        val lon = parsearCoordenada(longitud)

        return ErroresParcela(
            nombre = when {
                nombre.isBlank() -> "Escribe el nombre de la parcela"
                nombre.trim().length > 100 -> "Máximo 100 caracteres"
                else -> null
            },
            cultivo = when {
                cultivo.isBlank() -> "Escribe el cultivo"
                cultivo.trim().length > 50 -> "Máximo 50 caracteres"
                else -> null
            },
            latitud = when {
                lat == null -> "Ingresa una latitud válida"
                lat !in -90.0..90.0 -> "La latitud va de -90 a 90"
                else -> null
            },
            longitud = when {
                lon == null -> "Ingresa una longitud válida"
                lon !in -180.0..180.0 -> "La longitud va de -180 a 180"
                else -> null
            }
        )
    }
}