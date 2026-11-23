package com.yldefonso.ojodchakra.local

import androidx.room.Dao
import androidx.room.Query
import com.yldefonso.ojodchakra.data.model.Diagnostico
import com.yldefonso.ojodchakra.data.model.Parcela
import kotlinx.coroutines.flow.Flow

@Dao
interface SincronizacionDao {

    @Query("SELECT * FROM parcelas WHERE estado_sincronizacion = 'PENDIENTE' ORDER BY fecha_registro ASC")
    suspend fun parcelasPendientes(): List<Parcela>

    @Query("SELECT * FROM diagnosticos WHERE estado_sincronizacion = 'PENDIENTE' ORDER BY fecha_captura ASC")
    suspend fun diagnosticosPendientes(): List<Diagnostico>

    @Query("UPDATE parcelas SET estado_sincronizacion = 'SINCRONIZADO' WHERE id = :id")
    suspend fun marcarParcelaSincronizada(id: String)

    @Query("UPDATE diagnosticos SET estado_sincronizacion = 'SINCRONIZADO' WHERE id = :id")
    suspend fun marcarDiagnosticoSincronizado(id: String)

    @Query("SELECT COUNT(*) FROM parcelas WHERE estado_sincronizacion = 'PENDIENTE'")
    fun contarParcelasPendientes(): Flow<Int>

    @Query("SELECT COUNT(*) FROM diagnosticos WHERE estado_sincronizacion = 'PENDIENTE'")
    fun contarDiagnosticosPendientes(): Flow<Int>
}