package com.yldefonso.ojodchakra.screens.ajustes

import com.yldefonso.ojodchakra.ui.iconos.IconosOjoD
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.yldefonso.ojodchakra.data.repository.DiagnosticoRepository
import com.yldefonso.ojodchakra.respaldo.Respaldo
import com.yldefonso.ojodchakra.sesion.SesionUsuario
import com.yldefonso.ojodchakra.screens.auth.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import com.yldefonso.ojodchakra.ui.componentes.EncabezadoPantalla
import com.yldefonso.ojodchakra.ui.componentes.EtiquetaSeccion
import com.yldefonso.ojodchakra.ui.componentes.FilaAjuste
import com.yldefonso.ojodchakra.ui.componentes.IconoContenedor
import com.yldefonso.ojodchakra.ui.componentes.Tarjeta
import com.yldefonso.ojodchakra.util.PreferenciasApp
import kotlinx.coroutines.launch

/** Ajustes: apariencia (modo claro u oscuro), datos guardados en el celular e información de la app. */
@Composable
fun AjustesScreen(onBack: () -> Unit, onIrAcceso: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var confirmarBorrado by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    val crearRespaldo = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                mensaje = try {
                    val r = Respaldo.crear(context, uri)
                    "Respaldo creado: ${r.parcelas} parcelas, ${r.diagnosticos} reportes y ${r.fotos} fotos."
                } catch (e: Exception) {
                    "No se pudo crear el respaldo."
                }
            }
        }
    }

    val restaurarRespaldo = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                mensaje = try {
                    val r = Respaldo.restaurar(context, uri)
                    "Respaldo restaurado: ${r.parcelas} parcelas, ${r.diagnosticos} reportes y ${r.fotos} fotos."
                } catch (e: Exception) {
                    e.message ?: "No se pudo restaurar el respaldo."
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        EncabezadoPantalla(
            titulo = "Ajustes",
            subtitulo = "Personaliza tu aplicación",
            onVolver = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EtiquetaSeccion("Apariencia")
            Tarjeta(padding = 8.dp) {
                OpcionTema(
                    icono = IconosOjoD.Automatico,
                    texto = "Automático",
                    descripcion = "Sigue el modo del celular",
                    valor = PreferenciasApp.TEMA_SISTEMA,
                    actual = PreferenciasApp.tema,
                    onElegir = { PreferenciasApp.guardarTema(context, it) }
                )
                OpcionTema(
                    icono = IconosOjoD.Sol,
                    texto = "Claro",
                    descripcion = "Fondo claro con acentos verdes",
                    valor = PreferenciasApp.TEMA_CLARO,
                    actual = PreferenciasApp.tema,
                    onElegir = { PreferenciasApp.guardarTema(context, it) }
                )
                OpcionTema(
                    icono = IconosOjoD.Luna,
                    texto = "Oscuro",
                    descripcion = "Más cómodo para usar de noche",
                    valor = PreferenciasApp.TEMA_OSCURO,
                    actual = PreferenciasApp.tema,
                    onElegir = { PreferenciasApp.guardarTema(context, it) }
                )
            }

            EtiquetaSeccion("Datos del celular")
            Tarjeta(padding = 8.dp) {
                FilaAjuste(
                    icono = IconosOjoD.Papelera,
                    titulo = "Borrar historial de reportes",
                    descripcion = "Elimina los diagnósticos guardados en este celular",
                    onClick = { confirmarBorrado = true }
                )
            }

            mensaje?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            EtiquetaSeccion("Cuenta")
            val authVm: AuthViewModel = viewModel()
            Tarjeta(padding = 8.dp) {
                if (SesionUsuario.cuentaActiva) {
                    FilaAjuste(
                        icono = IconosOjoD.Agronomo,
                        titulo = "Cuenta: ${SesionUsuario.usuario.orEmpty()}",
                        descripcion = "Tus datos se envían a esta cuenta"
                    )
                    FilaAjuste(
                        icono = IconosOjoD.Ajustes,
                        titulo = "Cerrar sesión",
                        descripcion = "Tus datos siguen guardados en el celular",
                        onClick = { authVm.cerrarSesion(onIrAcceso) }
                    )
                } else {
                    FilaAjuste(
                        icono = IconosOjoD.Verificado,
                        titulo = "Iniciar sesión o crear cuenta",
                        descripcion = "Para enviar tus datos al servidor y revisar diagnósticos",
                        onClick = onIrAcceso
                    )
                }
            }

            EtiquetaSeccion("Respaldo")
            Tarjeta(padding = 8.dp) {
                FilaAjuste(
                    icono = Icons.Default.Storage,
                    titulo = "Crear respaldo",
                    descripcion = "Guarda parcelas, reportes y fotos en un archivo .zip",
                    onClick = { crearRespaldo.launch("ojodchakra-respaldo.zip") }
                )
                FilaAjuste(
                    icono = IconosOjoD.Historial,
                    titulo = "Restaurar respaldo",
                    descripcion = "Agrega los datos de un respaldo sin borrar los que ya tienes",
                    onClick = { restaurarRespaldo.launch(arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream")) }
                )
            }

            EtiquetaSeccion("Acerca de")
            Tarjeta(padding = 8.dp) {
                FilaAjuste(
                    icono = IconosOjoD.Info,
                    titulo = "OjoDChakra · versión 1.0",
                    descripcion = "Diagnóstico de plagas en papa, sin internet. Proyecto Integrador · TECSUP"
                )
                FilaAjuste(
                    icono = Icons.Default.Storage,
                    titulo = "Almacenamiento local",
                    descripcion = "Los reportes se guardan en el celular y se envían cuando hay internet"
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (confirmarBorrado) {
        AlertDialog(
            onDismissRequest = { confirmarBorrado = false },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            title = { Text("¿Borrar el historial?") },
            text = {
                Text(
                    "Se eliminarán los reportes y sus fotos de este celular. Los reportes que aún no " +
                            "se enviaron se perderán. Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarBorrado = false
                        scope.launch {
                            DiagnosticoRepository(context).limpiarHistorial()
                            mensaje = "Historial borrado de este celular."
                        }
                    }
                ) {
                    Text("Borrar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarBorrado = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun OpcionTema(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    texto: String,
    descripcion: String,
    valor: Int,
    actual: Int,
    onElegir: (Int) -> Unit
) {
    val seleccionada = actual == valor
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clickable { onElegir(valor) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconoContenedor(
            icono = icono,
            tamano = 42.dp,
            fondo = if (seleccionada) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant,
            tinte = if (seleccionada) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = texto,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        RadioButton(
            selected = seleccionada,
            onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
        )
    }
}
