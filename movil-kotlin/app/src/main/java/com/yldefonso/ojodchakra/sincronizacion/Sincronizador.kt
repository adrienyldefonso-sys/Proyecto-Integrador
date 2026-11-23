package com.yldefonso.ojodchakra.sincronizacion

import com.yldefonso.ojodchakra.local.SincronizacionDao
import com.yldefonso.ojodchakra.red.ClienteApi
import com.yldefonso.ojodchakra.red.ResultadoEnvio

class Sincronizador(
    private val dao: SincronizacionDao,
    private val cliente: ClienteApi
) {

    suspend fun sincronizar(): ResumenSincronizacion {
        var parcelasEnviadas = 0
        var diagnosticosEnviados = 0
        var rechazados = 0
        var primerError: String? = null
        val parcelasRechazadas = mutableSetOf<String>()

        fun resumen(sinConexion: Boolean) = ResumenSincronizacion(
            parcelasEnviadas, diagnosticosEnviados, rechazados, sinConexion, primerError
        )

        // Primero las parcelas: el servidor solo acepta un diagnóstico si su parcela ya existe allí.
        for (parcela in dao.parcelasPendientes()) {
            when (val resultado = cliente.enviarParcela(parcela)) {
                is ResultadoEnvio.Exito -> {
                    dao.marcarParcelaSincronizada(parcela.id)
                    parcelasEnviadas++
                }
                is ResultadoEnvio.Rechazado -> {
                    rechazados++
                    parcelasRechazadas += parcela.id
                    if (primerError == null) primerError = resultado.mensaje
                }
                is ResultadoEnvio.SinConexion -> {
                    if (primerError == null) primerError = resultado.mensaje
                    return resumen(sinConexion = true)
                }
            }
        }

        for (diagnostico in dao.diagnosticosPendientes()) {
            val parcelaId = diagnostico.parcelaId
            if (parcelaId != null && parcelaId in parcelasRechazadas) {
                rechazados++
                continue
            }
            when (val resultado = cliente.enviarDiagnostico(diagnostico)) {
                is ResultadoEnvio.Exito -> {
                    dao.marcarDiagnosticoSincronizado(diagnostico.id)
                    diagnosticosEnviados++
                }
                is ResultadoEnvio.Rechazado -> {
                    rechazados++
                    if (primerError == null) primerError = resultado.mensaje
                }
                is ResultadoEnvio.SinConexion -> {
                    if (primerError == null) primerError = resultado.mensaje
                    return resumen(sinConexion = true)
                }
            }
        }

        return resumen(sinConexion = false)
    }
}