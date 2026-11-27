package com.yldefonso.ojodchakra.screens.auth

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yldefonso.ojodchakra.local.AppDatabase
import com.yldefonso.ojodchakra.red.ClienteAuth
import com.yldefonso.ojodchakra.red.ResultadoAuth
import com.yldefonso.ojodchakra.sesion.SesionUsuario
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val cliente = ClienteAuth()

    var cargando by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    /** Al entrar, [alEntrar] recibe un mensaje opcional para mostrar en la pantalla de inicio. */
    fun iniciarSesion(usuario: String, contrasena: String, alEntrar: (String?) -> Unit) {
        viewModelScope.launch {
            cargando = true
            error = null
            val contexto = getApplication<Application>()
            val respuesta = if (hayRed(contexto)) {
                cliente.iniciarSesion(usuario.trim(), contrasena)
            } else {
                ResultadoAuth.SinConexion("Sin internet en el celular.")
            }
            when (val resultado = respuesta) {
                is ResultadoAuth.Exito -> {
                    reclamarDatosDeInvitado(resultado.usuarioId)
                    SesionUsuario.guardarCuenta(
                        contexto, resultado.usuarioId, resultado.usuario,
                        resultado.nombre, resultado.token, contrasena
                    )
                    cargando = false
                    alEntrar(null)
                }
                is ResultadoAuth.Rechazado -> {
                    error = resultado.mensaje
                    cargando = false
                }
                is ResultadoAuth.SinConexion -> {
                    // Sin internet: entra solo si cuenta y contraseña coinciden con este celular
                    if (SesionUsuario.entrarSinConexion(contexto, usuario, contrasena)) {
                        SesionUsuario.usuarioId?.let { reclamarDatosDeInvitado(it) }
                        cargando = false
                        alEntrar("Entraste sin conexión. Tus datos se enviarán cuando haya internet.")
                    } else {
                        error = if (SesionUsuario.cuentaGuardada) {
                            "Sin conexión. Revisa tu usuario y contraseña, o conéctate a internet."
                        } else {
                            "Para iniciar sesión por primera vez necesitas internet."
                        }
                        cargando = false
                    }
                }
            }
        }
    }

    fun registrar(
        usuario: String,
        contrasena: String,
        confirmacion: String,
        nombre: String,
        telefono: String,
        alRegistrar: () -> Unit
    ) {
        val problema = when {
            nombre.isBlank() -> "Escribe tu nombre."
            !Regex("^[A-Za-z0-9._]{3,30}$").matches(usuario.trim()) ->
                "El usuario debe tener de 3 a 30 letras, números, punto o guion bajo."
            !Regex("^\\d{9}$").matches(telefono.trim()) ->
                "El celular debe tener 9 dígitos."
            contrasena.length < 8 -> "La contraseña debe tener al menos 8 caracteres."
            contrasena != confirmacion -> "Las contraseñas no coinciden."
            else -> null
        }
        if (problema != null) {
            error = problema
            return
        }
        viewModelScope.launch {
            cargando = true
            error = null
            val contexto = getApplication<Application>()
            val respuesta = if (hayRed(contexto)) {
                cliente.registrar(usuario.trim(), contrasena, nombre.trim(), telefono.trim())
            } else {
                ResultadoAuth.SinConexion("Sin internet en el celular.")
            }
            when (val resultado = respuesta) {
                is ResultadoAuth.Exito -> {
                    reclamarDatosDeInvitado(resultado.usuarioId)
                    SesionUsuario.guardarCuenta(
                        contexto, resultado.usuarioId, resultado.usuario,
                        resultado.nombre, resultado.token, contrasena
                    )
                    cargando = false
                    alRegistrar()
                }
                is ResultadoAuth.Rechazado -> {
                    error = resultado.mensaje
                    cargando = false
                }
                is ResultadoAuth.SinConexion -> {
                    // La respuesta pudo perderse aunque el servidor ya creó la cuenta: se prueba entrar con esos datos
                    val intento = if (hayRed(contexto)) cliente.iniciarSesion(usuario.trim(), contrasena) else null
                    when (intento) {
                        is ResultadoAuth.Exito -> {
                            reclamarDatosDeInvitado(intento.usuarioId)
                            SesionUsuario.guardarCuenta(
                                contexto, intento.usuarioId, intento.usuario,
                                intento.nombre, intento.token, contrasena
                            )
                            cargando = false
                            alRegistrar()
                        }
                        else -> {
                            error = if (hayRed(contexto)) {
                                "No se pudo crear la cuenta: ${resultado.mensaje} Revisa que Spring esté corriendo."
                            } else {
                                "Para crear tu cuenta necesitas conexión a internet."
                            }
                            cargando = false
                        }
                    }
                }
            }
        }
    }

    /** Hay internet en el propio celular (no basta con que el servidor responda por otra vía). */
    private fun hayRed(contexto: Context): Boolean {
        val conectividad = contexto.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val red = conectividad.activeNetwork ?: return false
        val capacidades = conectividad.getNetworkCapabilities(red) ?: return false
        return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /** Cierra la sesión en el servidor y en el celular. Los datos guardados se quedan. */
    fun cerrarSesion(alCerrar: () -> Unit) {
        val token = SesionUsuario.token
        viewModelScope.launch {
            if (token != null) cliente.cerrarSesion(token)
            SesionUsuario.cerrarSesion(getApplication())
            alCerrar()
        }
    }

    /**
     * Los datos hechos sin cuenta pasan a la cuenta que inicia sesión primero.
     * Los datos de otras cuentas no se tocan.
     */
    private suspend fun reclamarDatosDeInvitado(usuarioId: String) {
        val base = AppDatabase.getInstance(getApplication())
        base.parcelaDao().reasignarUsuario(SesionUsuario.LOCAL, usuarioId)
        base.diagnosticoDao().reasignarUsuario(SesionUsuario.LOCAL, usuarioId)
    }
}
