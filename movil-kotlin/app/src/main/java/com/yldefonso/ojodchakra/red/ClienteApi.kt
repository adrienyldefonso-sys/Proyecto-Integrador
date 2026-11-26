package com.yldefonso.ojodchakra.red

import com.yldefonso.ojodchakra.data.model.Diagnostico
import com.yldefonso.ojodchakra.data.model.Parcela

interface ClienteApi {
    suspend fun enviarParcela(parcela: Parcela): ResultadoEnvio
    suspend fun enviarDiagnostico(diagnostico: Diagnostico): ResultadoEnvio

    /** Revisiones confirmadas o corregidas por el agrónomo. null si no se pudo consultar. */
    suspend fun obtenerRevisiones(): List<RevisionRemota>? = null
}
