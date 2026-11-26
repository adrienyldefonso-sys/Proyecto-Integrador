package com.yldefonso.ojodchakra.red

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object FechaIso {

    /** Room guarda milisegundos; la API espera ISO-8601 en UTC. */
    fun desdeMillis(milisegundos: Long): String {
        val formato = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        formato.timeZone = TimeZone.getTimeZone("UTC")
        return formato.format(Date(milisegundos))
    }

    /** Convierte la fecha ISO del servidor (con o sin fracciones de segundo) a milisegundos. */
    fun haciaMillis(texto: String): Long? = try {
        val formato = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        formato.timeZone = TimeZone.getTimeZone("UTC")
        val sinFraccion = texto.substringBefore('.').substringBefore('Z')
        formato.parse(sinFraccion)?.time
    } catch (e: Exception) {
        null
    }
}