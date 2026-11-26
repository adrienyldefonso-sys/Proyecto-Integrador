package com.yldefonso.ojodchakra.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "parcelas")
data class Parcela(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "usuario_id") val usuarioId: String,
    val nombre: String,
    val cultivo: String,
    val referencia: String = "",
    // Opcionales: una parcela puede guardarse sin ubicación y completarse después
    val latitud: Double? = null,
    val longitud: Double? = null,
    @ColumnInfo(name = "fecha_registro") val fechaRegistro: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "estado_sincronizacion") val estadoSincronizacion: String = "PENDIENTE"
) {
    val tieneCoordenadas: Boolean
        get() = latitud != null && longitud != null
}
