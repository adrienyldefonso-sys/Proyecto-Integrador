package com.yldefonso.ojodchakra.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yldefonso.ojodchakra.ui.componentes.BotonPrincipal
import com.yldefonso.ojodchakra.ui.componentes.BotonSecundario
import com.yldefonso.ojodchakra.ui.componentes.EncabezadoPantalla

@Composable
fun PantallaBienvenida(
    onIniciarSesion: () -> Unit,
    onRegistrarse: () -> Unit,
    onSinConexion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "OjoDChakra",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Diagnóstico de plagas en tus parcelas de papa.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(40.dp))
        BotonPrincipal(
            texto = "Iniciar sesión",
            onClick = onIniciarSesion,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        BotonSecundario(
            texto = "Crear cuenta",
            onClick = onRegistrarse,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onSinConexion) {
            Text("Usar sin conexión")
        }
        Text(
            text = "Puedes escanear sin cuenta. Tus datos se envían al servidor cuando inicies sesión.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PantallaLogin(
    onVolver: () -> Unit,
    onEntrar: (String?) -> Unit,
    onIrRegistro: () -> Unit
) {
    val vm: AuthViewModel = viewModel()
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        EncabezadoPantalla(titulo = "Iniciar sesión", onVolver = onVolver)
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CampoAuth(usuario, { usuario = it }, "Usuario")
            CampoAuth(contrasena, { contrasena = it }, "Contraseña", contrasena = true)
            vm.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            BotonPrincipal(
                texto = if (vm.cargando) "Entrando..." else "Entrar",
                onClick = { vm.iniciarSesion(usuario, contrasena, onEntrar) },
                modifier = Modifier.fillMaxWidth(),
                habilitado = !vm.cargando && usuario.isNotBlank() && contrasena.isNotBlank()
            )
            TextButton(onClick = onIrRegistro) {
                Text("¿No tienes cuenta? Regístrate")
            }
        }
    }
}

@Composable
fun PantallaRegistro(
    onVolver: () -> Unit,
    onRegistrado: () -> Unit
) {
    val vm: AuthViewModel = viewModel()
    var nombre by remember { mutableStateOf("") }
    var usuario by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        EncabezadoPantalla(titulo = "Crear cuenta", onVolver = onVolver)
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CampoAuth(nombre, { nombre = it }, "Nombre completo")
            CampoAuth(usuario, { usuario = it }, "Usuario (letras, números, . o _)")
            CampoAuth(telefono, { telefono = it }, "Teléfono (opcional)", tipo = KeyboardType.Phone)
            CampoAuth(contrasena, { contrasena = it }, "Contraseña (mínimo 8 caracteres)", contrasena = true)
            CampoAuth(confirmacion, { confirmacion = it }, "Repite la contraseña", contrasena = true)
            vm.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            BotonPrincipal(
                texto = if (vm.cargando) "Creando cuenta..." else "Crear cuenta",
                onClick = {
                    vm.registrar(usuario, contrasena, confirmacion, nombre, telefono) { onRegistrado() }
                },
                modifier = Modifier.fillMaxWidth(),
                habilitado = !vm.cargando
            )
            Text(
                text = "Necesitas internet solo para crear tu cuenta. Después podrás entrar sin conexión.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun CampoAuth(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    contrasena: Boolean = false,
    tipo: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        singleLine = true,
        visualTransformation = if (contrasena) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (contrasena) KeyboardType.Password else tipo
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
