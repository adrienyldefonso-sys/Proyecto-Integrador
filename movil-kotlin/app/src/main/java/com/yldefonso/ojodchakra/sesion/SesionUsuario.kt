package com.yldefonso.ojodchakra.sesion

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/**
 * Quién usa la app en este celular.
 * - Con cuenta: los datos que se ven, se guardan y se envían son de esa cuenta.
 * - Invitado: escanea sin cuenta; esos datos quedan con el dueño [LOCAL] y se reclaman al iniciar sesión.
 * Cada cuenta que se usa en el celular queda guardada para entrar sin internet.
 */
object SesionUsuario {

    /** Dueño de los datos cuando no hay cuenta (modo invitado). */
    const val LOCAL = "usuario_local"

    private const val ARCHIVO = "ojodchakra_sesion"
    private const val K_ACTIVA = "cuenta_activa"
    private const val K_INVITADO = "invitado"
    private const val K_CUENTAS = "cuentas"
    private const val K_USUARIO_ID = "usuario_id"
    private const val K_USUARIO = "usuario"
    private const val K_NOMBRE = "nombre"
    private const val K_TOKEN = "token"

    private var cargada = false
    private var tokenActual: String? = null
    private val propietarioInterno = MutableStateFlow(LOCAL)

    /** Dueño de lo que se muestra y se guarda ahora. Las pantallas se vuelven a consultar cuando cambia. */
    val propietario: StateFlow<String> = propietarioInterno.asStateFlow()
    val propietarioActual: String get() = propietarioInterno.value

    var usuarioId by mutableStateOf<String?>(null)
        private set
    var usuario by mutableStateOf<String?>(null)
        private set
    var nombre by mutableStateOf<String?>(null)
        private set
    var invitado by mutableStateOf(false)
        private set
    var cuentaGuardada by mutableStateOf(false)
        private set

    val cuentaActiva: Boolean get() = usuarioId != null
    val puedeEntrar: Boolean get() = cuentaActiva || invitado
    val token: String? get() = tokenActual

    /** Se llama al abrir la app y también desde el trabajo en segundo plano (puede arrancar sin la actividad). */
    fun cargar(contexto: Context) {
        if (cargada) return
        leer(preferencias(contexto))
        cargada = true
    }

    fun guardarCuenta(
        contexto: Context,
        usuarioId: String,
        usuario: String,
        nombreCompleto: String,
        token: String,
        contrasena: String
    ) {
        val preferencias = preferencias(contexto)
        val clave = usuario.trim().lowercase()
        val credencial = CredencialLocal.crear(contrasena)
        val cuentas = leerCuentas(preferencias)
        cuentas.put(
            clave,
            JSONObject()
                .put("usuarioId", usuarioId)
                .put("nombre", nombreCompleto)
                .put("token", token)
                .put("algoritmo", credencial.algoritmo)
                .put("sal", credencial.sal)
                .put("hash", credencial.hash)
        )
        preferencias.edit()
            .putString(K_CUENTAS, cuentas.toString())
            .putBoolean(K_ACTIVA, true)
            .putBoolean(K_INVITADO, false)
            .putString(K_USUARIO_ID, usuarioId)
            .putString(K_USUARIO, clave)
            .putString(K_NOMBRE, nombreCompleto)
            .putString(K_TOKEN, token)
            .apply()
        leer(preferencias)
        cargada = true
    }

    /** Sin internet: entra solo si la contraseña coincide con esa cuenta guardada en este celular. */
    fun entrarSinConexion(contexto: Context, usuarioIngresado: String, contrasena: String): Boolean {
        val preferencias = preferencias(contexto)
        val clave = usuarioIngresado.trim().lowercase()
        val cuenta = leerCuentas(preferencias).optJSONObject(clave) ?: return false
        val credencial = CredencialLocal(
            algoritmo = cuenta.optString("algoritmo"),
            sal = cuenta.optString("sal"),
            hash = cuenta.optString("hash")
        )
        if (!credencial.coincide(contrasena)) return false
        preferencias.edit()
            .putBoolean(K_ACTIVA, true)
            .putBoolean(K_INVITADO, false)
            .putString(K_USUARIO_ID, cuenta.optString("usuarioId"))
            .putString(K_USUARIO, clave)
            .putString(K_NOMBRE, cuenta.optString("nombre"))
            .putString(K_TOKEN, cuenta.optString("token").ifBlank { null })
            .apply()
        leer(preferencias)
        return true
    }

    fun activarInvitado(contexto: Context) {
        preferencias(contexto).edit()
            .putBoolean(K_ACTIVA, false)
            .putBoolean(K_INVITADO, true)
            .apply()
        leer(preferencias(contexto))
    }

    /** Cierra la sesión. Los datos del celular se quedan y la cuenta sigue guardada para entrar sin internet. */
    fun cerrarSesion(contexto: Context) {
        val preferencias = preferencias(contexto)
        val cuentas = leerCuentas(preferencias)
        preferencias.getString(K_USUARIO, null)?.let { cuentas.optJSONObject(it)?.remove("token") }
        preferencias.edit()
            .putString(K_CUENTAS, cuentas.toString())
            .putBoolean(K_ACTIVA, false)
            .putBoolean(K_INVITADO, false)
            .remove(K_TOKEN)
            .apply()
        leer(preferencias)
    }

    private fun leer(preferencias: SharedPreferences) {
        val activa = preferencias.getBoolean(K_ACTIVA, false)
        usuarioId = if (activa) preferencias.getString(K_USUARIO_ID, null) else null
        usuario = preferencias.getString(K_USUARIO, null)
        nombre = preferencias.getString(K_NOMBRE, null)
        tokenActual = preferencias.getString(K_TOKEN, null)
        invitado = preferencias.getBoolean(K_INVITADO, false)
        cuentaGuardada = leerCuentas(preferencias).length() > 0
        propietarioInterno.value = usuarioId ?: LOCAL
    }

    private fun leerCuentas(preferencias: SharedPreferences): JSONObject =
        try {
            JSONObject(preferencias.getString(K_CUENTAS, "{}") ?: "{}")
        } catch (e: Exception) {
            JSONObject()
        }

    private fun preferencias(contexto: Context): SharedPreferences =
        contexto.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
}
