package com.yldefonso.ojodchakra.data.model

/** Parcela con la severidad de su último diagnóstico, para dibujarla en el mapa. */
data class ParcelaEnMapa(
    val id: String,
    val nombre: String,
    val cultivo: String,
    val latitud: Double?,
    val longitud: Double?,
    val severidad: String?
) {
    val tieneCoordenadas: Boolean
        get() = latitud != null && longitud != null
}
