package com.yldefonso.ojodchakra.red

import com.yldefonso.ojodchakra.data.model.Diagnostico
import com.yldefonso.ojodchakra.data.model.Parcela
import com.yldefonso.ojodchakra.sesion.SesionUsuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID

class ClienteApiHttp(
    private val urlBase: String = ConfigApi.URL_BASE,
    private val claveApi: String = ConfigApi.CLAVE_API,
    private val usuarioId: () -> String? = { SesionUsuario.usuarioId },
    private val token: () -> String? = { SesionUsuario.token }
) : ClienteApi {

    override suspend fun enviarParcela(parcela: Parcela): ResultadoEnvio = withContext(Dispatchers.IO) {
        val usuario = usuarioId() ?: return@withContext ResultadoEnvio.Rechazado(MENSAJE_SIN_SESION)
        val json = JSONObject()
            .put("id", parcela.id)
            .put("usuarioId", usuario)
            .put("nombre", parcela.nombre)
            .put("cultivo", parcela.cultivo)
            .put("referencia", parcela.referencia)
            .put("latitud", parcela.latitud ?: JSONObject.NULL)
            .put("longitud", parcela.longitud ?: JSONObject.NULL)
            .put("fechaRegistro", FechaIso.desdeMillis(parcela.fechaRegistro))
            .toString()

        enviar("/api/parcelas") { conexion ->
            conexion.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conexion.outputStream.use { it.write(json.toByteArray(Charsets.UTF_8)) }
        }
    }

    override suspend fun enviarDiagnostico(diagnostico: Diagnostico): ResultadoEnvio =
        withContext(Dispatchers.IO) {
            val usuario = usuarioId() ?: return@withContext ResultadoEnvio.Rechazado(MENSAJE_SIN_SESION)
            val archivo = File(diagnostico.rutaFoto)
            if (!archivo.exists()) {
                return@withContext ResultadoEnvio.Rechazado(
                    "No se encontró la foto del reporte en el celular."
                )
            }

            val datos = JSONObject()
                .put("id", diagnostico.id)
                .put("usuarioId", usuario)
                .put("plagaDetectada", diagnostico.plagaDetectada)
                .put("confianza", diagnostico.confianza.toDouble())
                .put("nivelSeveridad", diagnostico.nivelSeveridad ?: "")
                .put("recomendacion", diagnostico.recomendacion ?: "")
                .put("fechaCaptura", FechaIso.desdeMillis(diagnostico.fechaCaptura))
            diagnostico.parcelaId?.let { datos.put("parcelaId", it) }
            // Solo se pide revisión; la plaga final la decide el agrónomo en la web
            datos.put(
                "estadoRevision",
                if (diagnostico.estadoRevision == "SOLICITADA") "SOLICITADA" else "NINGUNA"
            )

            val foto = archivo.readBytes()
            val limite = "ojodchakra-" + UUID.randomUUID()

            enviar("/api/diagnosticos") { conexion ->
                conexion.setRequestProperty("Content-Type", "multipart/form-data; boundary=$limite")
                conexion.outputStream.use { escribirMultipart(it, limite, datos.toString(), foto) }
            }
        }

    override suspend fun obtenerRevisiones(): List<RevisionRemota>? = withContext(Dispatchers.IO) {
        val usuario = usuarioId() ?: return@withContext null
        val url = urlBase + "/api/diagnosticos/revisiones?usuarioId=" +
            URLEncoder.encode(usuario, "UTF-8")
        val conexion = try {
            URL(url).openConnection() as HttpURLConnection
        } catch (e: IOException) {
            return@withContext null
        }
        try {
            conexion.requestMethod = "GET"
            conexion.connectTimeout = 5_000
            conexion.readTimeout = 20_000
            conexion.setRequestProperty("X-Api-Key", claveApi)
            conexion.setRequestProperty("Accept", "application/json")
            autorizar(conexion)
            if (conexion.responseCode !in 200..299) return@withContext null

            val cuerpo = conexion.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val lista = JSONArray(cuerpo)
            (0 until lista.length()).map { indice ->
                val fila = lista.getJSONObject(indice)
                RevisionRemota(
                    id = fila.getString("id"),
                    estadoRevision = fila.getString("estadoRevision"),
                    etiquetaValidada = fila.optString("etiquetaValidada", "").ifBlank { null },
                    observacionAgronomo = fila.optString("observacionAgronomo", "").ifBlank { null },
                    fechaRevisionMillis = fila.optString("fechaRevision", "")
                        .takeIf { it.isNotBlank() }
                        ?.let { FechaIso.haciaMillis(it) }
                )
            }
        } catch (e: Exception) {
            null
        } finally {
            conexion.disconnect()
        }
    }

    /** Con sesión iniciada, el servidor sabe quién pide la información. */
    private fun autorizar(conexion: HttpURLConnection) {
        token()?.let { conexion.setRequestProperty("Authorization", "Bearer $it") }
    }

    private fun enviar(ruta: String, escribirCuerpo: (HttpURLConnection) -> Unit): ResultadoEnvio {
        val conexion = try {
            URL(urlBase + ruta).openConnection() as HttpURLConnection
        } catch (e: IOException) {
            return ResultadoEnvio.SinConexion("No se pudo conectar con el servidor.")
        }

        return try {
            conexion.requestMethod = "POST"
            conexion.connectTimeout = 5_000
            conexion.readTimeout = 20_000
            conexion.doOutput = true
            conexion.setChunkedStreamingMode(0)
            conexion.setRequestProperty("X-Api-Key", claveApi)
            autorizar(conexion)
            conexion.setRequestProperty("Accept", "application/json")
            escribirCuerpo(conexion)

            val codigo = conexion.responseCode
            ResultadoEnvio.desdeCodigo(codigo) { mensajeDeRechazo(conexion, codigo) }
        } catch (e: IOException) {
            ResultadoEnvio.SinConexion("No se pudo conectar con el servidor.")
        } finally {
            conexion.disconnect()
        }
    }

    private fun escribirMultipart(salida: OutputStream, limite: String, datosJson: String, foto: ByteArray) {
        val salto = "\r\n"
        salida.write(
            ("--$limite$salto" +
                    "Content-Disposition: form-data; name=\"datos\"$salto" +
                    "Content-Type: application/json; charset=utf-8$salto$salto").toByteArray(Charsets.UTF_8)
        )
        salida.write(datosJson.toByteArray(Charsets.UTF_8))
        salida.write(salto.toByteArray(Charsets.UTF_8))
        salida.write(
            ("--$limite$salto" +
                    "Content-Disposition: form-data; name=\"foto\"; filename=\"foto.jpg\"$salto" +
                    "Content-Type: image/jpeg$salto$salto").toByteArray(Charsets.UTF_8)
        )
        salida.write(foto)
        salida.write(salto.toByteArray(Charsets.UTF_8))
        salida.write("--$limite--$salto".toByteArray(Charsets.UTF_8))
    }

    private fun mensajeDeRechazo(conexion: HttpURLConnection, codigo: Int): String {
        val porDefecto = "El servidor rechazó la solicitud (código $codigo)."
        val cuerpo = try {
            conexion.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
        } catch (e: IOException) {
            null
        }
        if (cuerpo.isNullOrBlank()) return porDefecto

        return try {
            val json = JSONObject(cuerpo)
            val detalle = json.optString("detail", "")
            val errores = json.optJSONObject("errores")
            val campos = errores?.keys()?.asSequence()
                ?.joinToString("; ") { "$it: ${errores.optString(it)}" }
            listOfNotNull(detalle.ifBlank { null }, campos?.ifBlank { null })
                .joinToString(" ")
                .ifBlank { porDefecto }
        } catch (e: Exception) {
            porDefecto
        }
    }
}

private const val MENSAJE_SIN_SESION = "Inicia sesión para enviar tus datos."
