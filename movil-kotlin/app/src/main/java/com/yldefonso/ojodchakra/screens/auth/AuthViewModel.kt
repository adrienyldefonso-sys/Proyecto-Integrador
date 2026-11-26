package com.yldefonso.ojodchakra.screens.auth

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
            when (val resultado = cliente.iniciarSesion(usuario.trim(), contrasena)) {
                is ResultadoAuth.Exito -> {
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
                    // Sin internet: entra solo con la última cuenta de este celular
                    if (SesionUsuario.entrarSinConexion(contexto, usuario, contrasena)) {
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
            when (val resultado = cliente.registrar(usuario.trim(), contrasena, nombre.trim(), telefono.trim())) {
                is ResultadoAuth.Exito -> {
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
                    error = "${resultado.mensaje} Revisa que Spring esté corriendo y que el emulador tenga adb reverse."
                    cargando = false
                }
            }
        }
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
}
