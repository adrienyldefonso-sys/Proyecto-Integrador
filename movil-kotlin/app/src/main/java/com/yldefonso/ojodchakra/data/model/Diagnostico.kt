package com.yldefonso.ojodchakra.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yldefonso.ojodchakra.ml.Clases
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
    @ColumnInfo(name = "estado_sincronizacion") val estadoSincronizacion: String = "PENDIENTE",
    // NINGUNA: sin revisión. SOLICITADA: el agricultor pidió revisión.
    // CONFIRMADA o CORREGIDA: lo decidió el agrónomo desde la web.
    @ColumnInfo(name = "estado_revision") val estadoRevision: String = "NINGUNA",
    // Plaga validada por el agrónomo. Solo tiene valor en CONFIRMADA o CORREGIDA.
    @ColumnInfo(name = "etiqueta_validada") val etiquetaValidada: String? = null,
    @ColumnInfo(name = "observacion_agronomo") val observacionAgronomo: String? = null,
    @ColumnInfo(name = "fecha_revision") val fechaRevision: Long? = null
) {
    val plagaEnEspanol: String
        get() = Clases.nombre(plagaDetectada)
}
