package com.yldefonso.ojodchakra.red

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

sealed interface ResultadoAuth {

    data class Exito(
        val usuarioId: String,
        val usuario: String,
        val nombre: String,
        val rol: String,
        val token: String
    ) : ResultadoAuth

    /** El servidor respondió y no aceptó la solicitud (contraseña mala, usuario repetido, datos inválidos). */
    data class Rechazado(val mensaje: String) : ResultadoAuth

    /** No hubo red o el servidor falló. */
    data class SinConexion(val mensaje: String) : ResultadoAuth
}

class ClienteAuth(
    private val urlBase: String = ConfigApi.URL_BASE,
    private val claveApi: String = ConfigApi.CLAVE_API
) {

    suspend fun registrar(
        usuario: String,
        contrasena: String,
        nombre: String,
        telefono: String?
    ): ResultadoAuth {
        val cuerpo = JSONObject()
            .put("usuario", usuario)
            .put("contrasena", contrasena)
            .put("nombre", nombre)
            .put("telefono", telefono?.takeIf { it.isNotBlank() } ?: JSONObject.NULL)
        return enviar("/api/auth/registro", cuerpo)
    }

    suspend fun iniciarSesion(usuario: String, contrasena: String): ResultadoAuth {
        val cuerpo = JSONObject()
            .put("usuario", usuario)
            .put("contrasena", contrasena)
        return enviar("/api/auth/login", cuerpo)
    }

    /** Invalida el token en el servidor. Si no hay red, el token vence solo a los 30 días. */
    suspend fun cerrarSesion(token: String): Boolean = withContext(Dispatchers.IO) {
        val conexion = try {
            URL(urlBase + "/api/auth/salir").openConnection() as HttpURLConnection
        } catch (e: IOException) {
            return@withContext false
        }
        try {
            conexion.requestMethod = "POST"
            conexion.connectTimeout = 5_000
            conexion.readTimeout = 10_000
            conexion.setRequestProperty("X-Api-Key", claveApi)
            conexion.setRequestProperty("Authorization", "Bearer $token")
            conexion.responseCode in 200..299
        } catch (e: IOException) {
            false
        } finally {
            conexion.disconnect()
        }
    }

    private suspend fun enviar(ruta: String, cuerpo: JSONObject): ResultadoAuth = withContext(Dispatchers.IO) {
        val conexion = try {
            URL(urlBase + ruta).openConnection() as HttpURLConnection
        } catch (e: IOException) {
            return@withContext ResultadoAuth.SinConexion(MENSAJE_SIN_RED)
        }

        try {
            conexion.requestMethod = "POST"
            conexion.connectTimeout = 5_000
            conexion.readTimeout = 20_000
            conexion.doOutput = true
            conexion.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conexion.setRequestProperty("Accept", "application/json")
            conexion.setRequestProperty("X-Api-Key", claveApi)
            conexion.outputStream.use { it.write(cuerpo.toString().toByteArray(Charsets.UTF_8)) }

            val codigo = conexion.responseCode
            when {
                codigo in 200..299 -> {
                    val json = JSONObject(leer(conexion.inputStream))
                    ResultadoAuth.Exito(
                        usuarioId = json.getString("usuarioId"),
                        usuario = json.getString("usuario"),
                        nombre = json.optString("nombre"),
                        rol = json.optString("rol"),
                        token = json.getString("token")
                    )
                }
                codigo in 500..599 -> ResultadoAuth.SinConexion("El servidor tuvo un problema. Intenta más tarde.")
                else -> ResultadoAuth.Rechazado(mensajeDeError(leer(conexion.errorStream), codigo))
            }
        } catch (e: IOException) {
            ResultadoAuth.SinConexion(MENSAJE_SIN_RED)
        } catch (e: JSONException) {
            ResultadoAuth.Rechazado("La respuesta del servidor no es válida.")
        } finally {
            conexion.disconnect()
        }
    }

    private fun leer(flujo: InputStream?): String =
        flujo?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()

    private fun mensajeDeError(cuerpo: String, codigo: Int): String {
        val porDefecto = when (codigo) {
            401 -> "Usuario o contraseña incorrectos."
            409 -> "Ese nombre de usuario ya está registrado."
            else -> "No se pudo completar la solicitud (código $codigo)."
        }
        if (cuerpo.isBlank()) return porDefecto
        return try {
            val json = JSONObject(cuerpo)
            val detalle = json.optString("detail", "")
            val errores = json.optJSONObject("errores")
            val campos = errores?.keys()?.asSequence()
                ?.joinToString("; ") { "$it: ${errores.optString(it)}" }
            listOfNotNull(detalle.ifBlank { null }, campos?.ifBlank { null })
                .joinToString(" ")
                .ifBlank { porDefecto }
        } catch (e: JSONException) {
            porDefecto
        }
    }

    private companion object {
        const val MENSAJE_SIN_RED = "No se pudo conectar con el servidor."
    }
}
