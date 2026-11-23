package com.yldefonso.ojodchakra.red

sealed interface ResultadoEnvio {

    /** El servidor lo guardó (201) o ya lo tenía (200). */
    data object Exito : ResultadoEnvio

    /** El servidor lo rechazó: reintentar igual no sirve hasta corregir el dato. */
    data class Rechazado(val mensaje: String) : ResultadoEnvio

    /** No hubo conexión o el servidor falló: se reintenta después. */
    data class SinConexion(val mensaje: String) : ResultadoEnvio

    companion object {
        fun desdeCodigo(codigo: Int, mensajeRechazo: () -> String): ResultadoEnvio = when (codigo) {
            200, 201 -> Exito
            in 500..599 -> SinConexion("El servidor respondió con un error ($codigo). Se reintentará.")
            else -> Rechazado(mensajeRechazo())
        }
    }
}