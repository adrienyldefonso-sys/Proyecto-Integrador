package com.yldefonso.ojodchakra.red

import com.yldefonso.ojodchakra.data.model.Diagnostico
import com.yldefonso.ojodchakra.data.model.Parcela
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class ClienteApiHttp(
    private val urlBase: String = ConfigApi.URL_BASE,
    private val claveApi: String = ConfigApi.CLAVE_API,
    private val usuarioServidorId: String = ConfigApi.USUARIO_SERVIDOR_ID
) : ClienteApi {

    override suspend fun enviarParcela(parcela: Parcela): ResultadoEnvio = withContext(Dispatchers.IO) {
        val json = JSONObject()
            .put("id", parcela.id)
            .put("usuarioId", usuarioServidorId)
            .put("nombre", parcela.nombre)
            .put("cultivo", parcela.cultivo)
            .put("referencia", parcela.referencia)
            .put("latitud", parcela.latitud)
            .put("longitud", parcela.longitud)
            .put("fechaRegistro", FechaIso.desdeMillis(parcela.fechaRegistro))
            .toString()

        enviar("/api/parcelas") { conexion ->
            conexion.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conexion.outputStream.use { it.write(json.toByteArray(Charsets.UTF_8)) }
        }
    }

    override suspend fun enviarDiagnostico(diagnostico: Diagnostico): ResultadoEnvio =
        withContext(Dispatchers.IO) {
            val archivo = File(diagnostico.rutaFoto)
            if (!archivo.exists()) {
                return@withContext ResultadoEnvio.Rechazado(
                    "No se encontró la foto del reporte en el celular."
                )
            }

            val datos = JSONObject()
                .put("id", diagnostico.id)
                .put("usuarioId", usuarioServidorId)
                .put("plagaDetectada", diagnostico.plagaDetectada)
                .put("confianza", diagnostico.confianza.toDouble())
                .put("nivelSeveridad", diagnostico.nivelSeveridad ?: "")
                .put("recomendacion", diagnostico.recomendacion ?: "")
                .put("fechaCaptura", FechaIso.desdeMillis(diagnostico.fechaCaptura))
            diagnostico.parcelaId?.let { datos.put("parcelaId", it) }

            val foto = archivo.readBytes()
            val limite = "ojodchakra-" + UUID.randomUUID()

            enviar("/api/diagnosticos") { conexion ->
                conexion.setRequestProperty("Content-Type", "multipart/form-data; boundary=$limite")
                conexion.outputStream.use { escribirMultipart(it, limite, datos.toString(), foto) }
            }
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