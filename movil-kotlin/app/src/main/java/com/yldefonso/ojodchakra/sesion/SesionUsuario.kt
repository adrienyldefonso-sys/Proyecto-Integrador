package com.yldefonso.ojodchakra.sesion

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Quién usa la app en este celular.
 * - Con cuenta: los datos se envían a ese usuario del servidor.
 * - Invitado: puede escanear y guardar sin cuenta; no se envía nada hasta que inicie sesión.
 * La última cuenta queda guardada para entrar sin internet.
 */
object SesionUsuario {

    private const val ARCHIVO = "ojodchakra_sesion"
    private const val K_ACTIVA = "cuenta_activa"
    private const val K_INVITADO = "invitado"
    private const val K_USUARIO_ID = "usuario_id"
    private const val K_USUARIO = "usuario"
    private const val K_NOMBRE = "nombre"
    private const val K_TOKEN = "token"
    private const val K_ALGORITMO = "algoritmo"
    private const val K_SAL = "sal"
    private const val K_HASH = "hash"

    private var cargada = false
    private var tokenActual: String? = null

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
        val credencial = CredencialLocal.crear(contrasena)
        preferencias(contexto).edit()
            .putBoolean(K_ACTIVA, true)
            .putBoolean(K_INVITADO, false)
            .putString(K_USUARIO_ID, usuarioId)
            .putString(K_USUARIO, usuario.trim().lowercase())
            .putString(K_NOMBRE, nombreCompleto)
            .putString(K_TOKEN, token)
            .putString(K_ALGORITMO, credencial.algoritmo)
            .putString(K_SAL, credencial.sal)
            .putString(K_HASH, credencial.hash)
            .apply()
        leer(preferencias(contexto))
        cargada = true
    }

    /** Sin internet: entra solo si el usuario y la contraseña coinciden con la cuenta guardada en este celular. */
    fun entrarSinConexion(contexto: Context, usuarioIngresado: String, contrasena: String): Boolean {
        val preferencias = preferencias(contexto)
        val usuarioGuardado = preferencias.getString(K_USUARIO, null) ?: return false
        if (usuarioGuardado != usuarioIngresado.trim().lowercase()) return false
        val credencial = leerCredencial(preferencias) ?: return false
        if (!credencial.coincide(contrasena)) return false
        preferencias.edit().putBoolean(K_ACTIVA, true).putBoolean(K_INVITADO, false).apply()
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

    /** Cierra la sesión. Los datos del celular se quedan; la cuenta sigue guardada para entrar sin internet. */
    fun cerrarSesion(contexto: Context) {
        preferencias(contexto).edit()
            .putBoolean(K_ACTIVA, false)
            .putBoolean(K_INVITADO, false)
            .remove(K_TOKEN)
            .apply()
        leer(preferencias(contexto))
    }

    private fun leer(preferencias: SharedPreferences) {
        val activa = preferencias.getBoolean(K_ACTIVA, false)
        usuarioId = if (activa) preferencias.getString(K_USUARIO_ID, null) else null
        usuario = preferencias.getString(K_USUARIO, null)
        nombre = preferencias.getString(K_NOMBRE, null)
        tokenActual = preferencias.getString(K_TOKEN, null)
        invitado = preferencias.getBoolean(K_INVITADO, false)
        cuentaGuardada = preferencias.contains(K_HASH)
    }

    private fun leerCredencial(preferencias: SharedPreferences): CredencialLocal? {
        val hash = preferencias.getString(K_HASH, null) ?: return null
        val sal = preferencias.getString(K_SAL, null) ?: return null
        val algoritmo = preferencias.getString(K_ALGORITMO, null) ?: return null
        return CredencialLocal(algoritmo, sal, hash)
    }

    private fun preferencias(contexto: Context): SharedPreferences =
        contexto.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
}
