package com.yldefonso.ojodchakra.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yldefonso.ojodchakra.data.model.Diagnostico
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagnosticoDao {

    @Query("SELECT * FROM diagnosticos ORDER BY fecha_captura DESC")
    fun observarTodos(): Flow<List<Diagnostico>>

    @Query("SELECT * FROM diagnosticos WHERE id = :id")
    fun observarPorId(id: String): Flow<Diagnostico?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(diagnostico: Diagnostico)

    // Solo cambia si todavía no había una solicitud, para no pisar una revisión ya confirmada
    // Se marca como pendiente de enviar para que la próxima sincronización lo mande al servidor
    @Query(
        "UPDATE diagnosticos SET estado_revision = 'SOLICITADA', estado_sincronizacion = 'PENDIENTE' " +
            "WHERE id = :id AND estado_revision = 'NINGUNA'"
    )
    suspend fun solicitarRevision(id: String)

    @Query("SELECT * FROM diagnosticos")
    suspend fun todos(): List<Diagnostico>

    @Query("DELETE FROM diagnosticos")
    suspend fun limpiarHistorial()
}
