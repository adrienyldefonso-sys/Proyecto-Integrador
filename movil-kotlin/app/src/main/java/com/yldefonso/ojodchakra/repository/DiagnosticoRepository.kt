package com.yldefonso.ojodchakra.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.yldefonso.ojodchakra.data.model.Diagnostico
import com.yldefonso.ojodchakra.local.AppDatabase
import com.yldefonso.ojodchakra.ml.ClassificationResult
import com.yldefonso.ojodchakra.ml.MotorRecomendaciones
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class DiagnosticoRepository(context: Context) {

    private val appContext = context.applicationContext
    private val dao = AppDatabase.getInstance(appContext).diagnosticoDao()

    val diagnosticos: Flow<List<Diagnostico>> = dao.observarTodos()

    fun observarPorId(id: String): Flow<Diagnostico?> = dao.observarPorId(id)

    suspend fun guardar(
        bitmap: Bitmap,
        resultado: ClassificationResult,
        parcelaId: String? = null
    ): String = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val carpeta = File(appContext.filesDir, "reportes").apply { mkdirs() }
        val archivo = File(carpeta, "$id.jpg")

        FileOutputStream(archivo).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
        }

        val recomendacion = MotorRecomendaciones.obtener(resultado.label)

        dao.insertar(
            Diagnostico(
                id = id,
                usuarioId = USUARIO_LOCAL,
                parcelaId = parcelaId,
                rutaFoto = archivo.absolutePath,
                plagaDetectada = resultado.label,
                confianza = resultado.confidence,
                nivelSeveridad = recomendacion.nivelSeveridad,
                recomendacion = recomendacion.tratamiento
            )
        )
        id
    }

    // El agricultor pide que un agrónomo revise el diagnóstico. Se envía al servidor en la sincronización.
    suspend fun solicitarRevision(id: String) = withContext(Dispatchers.IO) {
        dao.solicitarRevision(id)
    }

    suspend fun limpiarHistorial() = withContext(Dispatchers.IO) {
        val carpeta = File(appContext.filesDir, "reportes")
        if (carpeta.exists()) {
            carpeta.listFiles()?.forEach { it.delete() }
        }
        dao.limpiarHistorial()
    }

    companion object {
        const val USUARIO_LOCAL = "usuario_local"
    }
}
