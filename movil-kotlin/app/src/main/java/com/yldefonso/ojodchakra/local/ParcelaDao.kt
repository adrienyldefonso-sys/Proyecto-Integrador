package com.yldefonso.ojodchakra.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yldefonso.ojodchakra.data.model.Parcela
import com.yldefonso.ojodchakra.data.model.ParcelaConConteo
import com.yldefonso.ojodchakra.data.model.ParcelaEnMapa
import kotlinx.coroutines.flow.Flow

@Dao
interface ParcelaDao {

    @Query("SELECT p.*, (SELECT COUNT(*) FROM diagnosticos d WHERE d.parcela_id = p.id) AS total_diagnosticos FROM parcelas p WHERE p.usuario_id = :usuarioId ORDER BY p.fecha_registro DESC")
    fun observarConConteo(usuarioId: String): Flow<List<ParcelaConConteo>>

    @Query("SELECT * FROM parcelas WHERE id = :id")
    suspend fun obtenerPorId(id: String): Parcela?

    @Insert
    suspend fun insertar(parcela: Parcela)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun reemplazar(parcela: Parcela)

    @Query("SELECT * FROM parcelas")
    suspend fun todas(): List<Parcela>

    @Query(
        "SELECT p.id AS id, p.nombre AS nombre, p.cultivo AS cultivo, " +
        "p.latitud AS latitud, p.longitud AS longitud, " +
        "(SELECT d.nivel_severidad FROM diagnosticos d WHERE d.parcela_id = p.id " +
        "ORDER BY d.fecha_captura DESC LIMIT 1) AS severidad " +
        "FROM parcelas p WHERE p.usuario_id = :usuarioId ORDER BY p.fecha_registro ASC"
    )
    fun observarParaMapa(usuarioId: String): Flow<List<ParcelaEnMapa>>
}
