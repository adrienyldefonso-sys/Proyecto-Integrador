package com.yldefonso.ojodchakra.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yldefonso.ojodchakra.data.model.Parcela
import com.yldefonso.ojodchakra.data.model.ParcelaConConteo
import kotlinx.coroutines.flow.Flow

@Dao
interface ParcelaDao {

    @Query("SELECT p.*, (SELECT COUNT(*) FROM diagnosticos d WHERE d.parcela_id = p.id) AS total_diagnosticos FROM parcelas p WHERE p.usuario_id = :usuarioId ORDER BY p.fecha_registro DESC")
    fun observarConConteo(usuarioId: String): Flow<List<ParcelaConConteo>>

    @Query("SELECT * FROM parcelas WHERE id = :id")
    suspend fun obtenerPorId(id: String): Parcela?

    @Insert
    suspend fun insertar(parcela: Parcela)
}