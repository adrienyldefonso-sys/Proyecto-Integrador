package com.yldefonso.ojodchakra.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yldefonso.ojodchakra.R
import com.yldefonso.ojodchakra.ui.componentes.BotonPrincipal
import com.yldefonso.ojodchakra.ui.componentes.BotonSecundario
import com.yldefonso.ojodchakra.ui.componentes.FondoHojas
import com.yldefonso.ojodchakra.ui.theme.VerdeBosque
import com.yldefonso.ojodchakra.ui.theme.VerdeBosqueMuyOscuro

@Composable
fun PantallaBienvenida(
    onIniciarSesion: () -> Unit,
    onRegistrarse: () -> Unit,
    onSinConexion: () -> Unit
) {
    PantallaAcceso(
        titulo = "OjoDChakra",
        subtitulo = "Diagnóstico de plagas en papa",
        centrado = true,
        pie = {
            TextButton(
                onClick = onSinConexion,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Usar sin conexión", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
            Text(
                text = "Puedes escanear sin cuenta. Tus datos se envían al servidor cuando inicies sesión.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {
        Text(
            text = "Bienvenido",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Inicia sesión para guardar tus parcelas y revisar tus diagnósticos con un agrónomo.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        BotonPrincipal(
            texto = "Iniciar sesión",
            onClick = onIniciarSesion,
            modifier = Modifier.fillMaxWidth()
        )
        BotonSecundario(
            texto = "Crear cuenta",
            onClick = onRegistrarse,
            modifier = Modifier.fillMaxWidth()
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

    PantallaAcceso(
        titulo = "Iniciar sesión",
        subtitulo = "Entra con tu usuario para enviar tus datos",
        onVolver = onVolver
    ) {
        CampoAuth(usuario, { usuario = it }, "Usuario")
        CampoAuth(contrasena, { contrasena = it }, "Contraseña", contrasena = true)
        vm.error?.let { MensajeError(it) }
        BotonPrincipal(
            texto = if (vm.cargando) "Entrando..." else "Entrar",
            onClick = { vm.iniciarSesion(usuario, contrasena, onEntrar) },
            modifier = Modifier.fillMaxWidth(),
            habilitado = !vm.cargando && usuario.isNotBlank() && contrasena.isNotBlank()
        )
        TextButton(
            onClick = onIrRegistro,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("¿No tienes cuenta? Regístrate")
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

    PantallaAcceso(
        titulo = "Crear cuenta",
        subtitulo = "Necesitas internet solo para crear tu cuenta",
        onVolver = onVolver
    ) {
        CampoAuth(nombre, { nombre = it }, "Nombre completo")
        CampoAuth(usuario, { usuario = it }, "Usuario (letras, números, . o _)")
        CampoAuth(
            valor = telefono,
            // Solo dígitos, máximo 9: un número de celular peruano
            alCambiar = { telefono = it.filter(Char::isDigit).take(9) },
            etiqueta = "Celular (9 dígitos)",
            tipo = KeyboardType.Phone
        )
        CampoAuth(contrasena, { contrasena = it }, "Contraseña (mínimo 8 caracteres)", contrasena = true)
        CampoAuth(confirmacion, { confirmacion = it }, "Repite la contraseña", contrasena = true)
        vm.error?.let { MensajeError(it) }
        BotonPrincipal(
            texto = if (vm.cargando) "Creando cuenta..." else "Crear cuenta",
            onClick = { vm.registrar(usuario, contrasena, confirmacion, nombre, telefono) { onRegistrado() } },
            modifier = Modifier.fillMaxWidth(),
            habilitado = !vm.cargando
        )
    }
}

/**
 * Marco común de las pantallas de acceso: fondo verde con hojas, logo, título
 * y un panel blanco con el formulario. [centrado] reparte el contenido en el
 * centro de la pantalla (sin desplazamiento). [pie] va debajo del panel.
 */
@Composable
private fun PantallaAcceso(
    titulo: String,
    subtitulo: String,
    onVolver: (() -> Unit)? = null,
    centrado: Boolean = false,
    pie: @Composable ColumnScope.() -> Unit = {},
    contenido: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(VerdeBosque, VerdeBosqueMuyOscuro)))
    ) {
        FondoHojas(modifier = Modifier.fillMaxSize(), color = Color.White)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .then(if (centrado) Modifier else Modifier.verticalScroll(rememberScrollState()))
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (centrado) Arrangement.Center else Arrangement.Top
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                if (onVolver != null) {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                }
            }
            Image(
                painter = painterResource(id = R.drawable.logo_ojodchakra),
                contentDescription = null,
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(10.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    content = contenido
                )
            }
            Spacer(Modifier.height(20.dp))
            pie()
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MensajeError(texto: String) {
    Text(
        text = texto,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium
    )
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
