package com.yldefonso.ojodchakra.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "diagnosticos")
data class Diagnostico(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "usuario_id") val usuarioId: String,
    @ColumnInfo(name = "parcela_id") val parcelaId: String? = null,
    @ColumnInfo(name = "ruta_foto") val rutaFoto: String,
    @ColumnInfo(name = "plaga_detectada") val plagaDetectada: String,
    val confianza: Float,
    @ColumnInfo(name = "nivel_severidad") val nivelSeveridad: String? = null,
    val recomendacion: String? = null,
    @ColumnInfo(name = "fecha_captura") val fechaCaptura: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "estado_sincronizacion") val estadoSincronizacion: String = "PENDIENTE"
) {
    val plagaEnEspanol: String
        get() = when (plagaDetectada) {
            "Potato___Early_blight" -> "Tizón temprano"
            "Potato___Late_blight" -> "Tizón tardío"
            "Potato___healthy" -> "Planta sana"
            else -> plagaDetectada
        }
}