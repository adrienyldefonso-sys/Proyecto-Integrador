package com.yldefonso.ojodchakra.red

import com.yldefonso.ojodchakra.data.model.Diagnostico
import com.yldefonso.ojodchakra.data.model.Parcela

interface ClienteApi {
    suspend fun enviarParcela(parcela: Parcela): ResultadoEnvio
    suspend fun enviarDiagnostico(diagnostico: Diagnostico): ResultadoEnvio
}