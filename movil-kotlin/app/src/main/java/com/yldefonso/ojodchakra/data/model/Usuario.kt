package com.yldefonso.ojodchakra.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val celular: String,
    val rol: String, // AGRICULTOR o AGRONOMO
    val activo: Boolean = true,
    @ColumnInfo(name = "fecha_registro") val fechaRegistro: Long = System.currentTimeMillis()
)