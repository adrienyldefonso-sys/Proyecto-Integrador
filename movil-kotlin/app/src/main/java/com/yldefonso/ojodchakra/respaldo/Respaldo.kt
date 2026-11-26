package com.yldefonso.ojodchakra.respaldo

import android.content.Context
import android.net.Uri
import com.yldefonso.ojodchakra.data.model.Diagnostico
import com.yldefonso.ojodchakra.data.model.Parcela
import com.yldefonso.ojodchakra.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class ResultadoRespaldo(val parcelas: Int, val diagnosticos: Int, val fotos: Int)

/**
 * Respaldo en un .zip: un JSON con parcelas y reportes, y las fotos.
 * Restaurar agrega lo que venga en el archivo; no borra nada de lo que ya hay en el celular.
 */
object Respaldo {

    private const val VERSION = 1
    private const val ARCHIVO_DATOS = "respaldo.json"
    private const val CARPETA_FOTOS = "fotos/"

    suspend fun crear(context: Context, destino: Uri): ResultadoRespaldo = withContext(Dispatchers.IO) {
        val db = AppDatabase.getInstance(context)
        val parcelas = db.parcelaDao().todas()
        val diagnosticos = db.diagnosticoDao().todos()

        val fotos = mutableListOf<File>()
        val jsonDiagnosticos = JSONArray()
        diagnosticos.forEach { d ->
            File(d.rutaFoto).takeIf { it.exists() }?.let { fotos += it }
            jsonDiagnosticos.put(d.aJson())
        }
        val jsonParcelas = JSONArray()
        parcelas.forEach { jsonParcelas.put(it.aJson()) }

        val datos = JSONObject()
            .put("version", VERSION)
            .put("creado", System.currentTimeMillis())
            .put("parcelas", jsonParcelas)
            .put("diagnosticos", jsonDiagnosticos)

        val salida = context.contentResolver.openOutputStream(destino)
            ?: throw IllegalStateException("No se pudo crear el archivo de respaldo.")
        ZipOutputStream(salida).use { zip ->
            zip.putNextEntry(ZipEntry(ARCHIVO_DATOS))
            zip.write(datos.toString().toByteArray(Charsets.UTF_8))
            zip.closeEntry()
            fotos.forEach { foto ->
                zip.putNextEntry(ZipEntry(CARPETA_FOTOS + foto.name))
                foto.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
        ResultadoRespaldo(parcelas.size, diagnosticos.size, fotos.size)
    }

    suspend fun restaurar(context: Context, origen: Uri): ResultadoRespaldo = withContext(Dispatchers.IO) {
        val carpetaFotos = File(context.filesDir, "reportes").apply { mkdirs() }
        val entrada = context.contentResolver.openInputStream(origen)
            ?: throw IllegalStateException("No se pudo abrir el respaldo.")

        var datosTexto: String? = null
        var fotos = 0
        ZipInputStream(entrada).use { zip ->
            var elemento = zip.nextEntry
            while (elemento != null) {
                val nombre = elemento.name
                when {
                    nombre == ARCHIVO_DATOS -> datosTexto = zip.readBytes().toString(Charsets.UTF_8)
                    nombre.startsWith(CARPETA_FOTOS) -> {
                        // Solo el nombre del archivo, para que nadie escriba fuera de la carpeta de fotos
                        val nombreFoto = File(nombre).name
                        File(carpetaFotos, nombreFoto).outputStream().use { zip.copyTo(it) }
                        fotos++
                    }
                }
                zip.closeEntry()
                elemento = zip.nextEntry
            }
        }

        val json = JSONObject(
            datosTexto ?: throw IllegalStateException("El archivo no es un respaldo de OjoDChakra.")
        )
        if (json.optInt("version") != VERSION) {
            throw IllegalStateException("La versión de este respaldo no es compatible.")
        }

        val db = AppDatabase.getInstance(context)
        val parcelas = json.getJSONArray("parcelas")
        for (i in 0 until parcelas.length()) {
            db.parcelaDao().reemplazar(parcelas.getJSONObject(i).aParcela())
        }
        val diagnosticos = json.getJSONArray("diagnosticos")
        for (i in 0 until diagnosticos.length()) {
            db.diagnosticoDao().insertar(diagnosticos.getJSONObject(i).aDiagnostico(carpetaFotos))
        }
        ResultadoRespaldo(parcelas.length(), diagnosticos.length(), fotos)
    }

    private fun Parcela.aJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("usuarioId", usuarioId)
        .put("nombre", nombre)
        .put("cultivo", cultivo)
        .put("referencia", referencia)
        .put("latitud", latitud ?: JSONObject.NULL)
        .put("longitud", longitud ?: JSONObject.NULL)
        .put("fechaRegistro", fechaRegistro)
        .put("estadoSincronizacion", estadoSincronizacion)

    private fun Diagnostico.aJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("usuarioId", usuarioId)
        .put("parcelaId", parcelaId ?: JSONObject.NULL)
        .put("foto", File(rutaFoto).name)
        .put("plagaDetectada", plagaDetectada)
        .put("confianza", confianza.toDouble())
        .put("nivelSeveridad", nivelSeveridad ?: JSONObject.NULL)
        .put("recomendacion", recomendacion ?: JSONObject.NULL)
        .put("fechaCaptura", fechaCaptura)
        .put("estadoSincronizacion", estadoSincronizacion)
        .put("estadoRevision", estadoRevision)
        .put("etiquetaValidada", etiquetaValidada ?: JSONObject.NULL)
        .put("observacionAgronomo", observacionAgronomo ?: JSONObject.NULL)
        .put("fechaRevision", fechaRevision ?: JSONObject.NULL)

    private fun JSONObject.aParcela(): Parcela = Parcela(
        id = getString("id"),
        usuarioId = getString("usuarioId"),
        nombre = getString("nombre"),
        cultivo = optString("cultivo"),
        referencia = optString("referencia"),
        latitud = if (isNull("latitud")) null else getDouble("latitud"),
        longitud = if (isNull("longitud")) null else getDouble("longitud"),
        fechaRegistro = getLong("fechaRegistro"),
        estadoSincronizacion = optString("estadoSincronizacion", "PENDIENTE")
    )

    private fun JSONObject.aDiagnostico(carpetaFotos: File): Diagnostico {
        val foto = optString("foto")
        return Diagnostico(
            id = getString("id"),
            usuarioId = getString("usuarioId"),
            parcelaId = textoONulo("parcelaId"),
            rutaFoto = if (foto.isBlank()) "" else File(carpetaFotos, foto).absolutePath,
            plagaDetectada = getString("plagaDetectada"),
            confianza = getDouble("confianza").toFloat(),
            nivelSeveridad = textoONulo("nivelSeveridad"),
            recomendacion = textoONulo("recomendacion"),
            fechaCaptura = getLong("fechaCaptura"),
            estadoSincronizacion = optString("estadoSincronizacion", "PENDIENTE"),
            estadoRevision = optString("estadoRevision", "NINGUNA"),
            etiquetaValidada = textoONulo("etiquetaValidada"),
            observacionAgronomo = textoONulo("observacionAgronomo"),
            fechaRevision = if (isNull("fechaRevision")) null else getLong("fechaRevision")
        )
    }

    private fun JSONObject.textoONulo(clave: String): String? =
        if (isNull(clave)) null else optString(clave).ifEmpty { null }
}
