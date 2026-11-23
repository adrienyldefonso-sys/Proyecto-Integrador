package com.yldefonso.ojodchakra.sincronizacion

data class ResumenSincronizacion(
    val parcelasEnviadas: Int = 0,
    val diagnosticosEnviados: Int = 0,
    val rechazados: Int = 0,
    val sinConexion: Boolean = false,
    val primerError: String? = null
) {
    val totalEnviados: Int get() = parcelasEnviadas + diagnosticosEnviados

    fun texto(): String = when {
        sinConexion -> "Sin conexión con el servidor. Se reintentará cuando haya señal."
        rechazados > 0 -> {
            val enviados = if (totalEnviados > 0) "Enviado: ${detalleEnviados()}. " else ""
            "${enviados}El servidor no aceptó ${plural(rechazados, "elemento", "elementos")}: " +
                    (primerError ?: "sin detalle")
        }
        totalEnviados == 0 -> "No hay nada pendiente de enviar."
        else -> "Enviado: ${detalleEnviados()}."
    }

    private fun detalleEnviados(): String =
        listOfNotNull(
            if (parcelasEnviadas > 0) plural(parcelasEnviadas, "parcela", "parcelas") else null,
            if (diagnosticosEnviados > 0) plural(diagnosticosEnviados, "reporte", "reportes") else null
        ).joinToString(" y ")

    private fun plural(cantidad: Int, singular: String, plural: String) =
        if (cantidad == 1) "1 $singular" else "$cantidad $plural"
}

fun textoPendientes(total: Int): String = when (total) {
    0 -> "Todo está enviado."
    1 -> "1 elemento pendiente de enviar."
    else -> "$total elementos pendientes de enviar."
}