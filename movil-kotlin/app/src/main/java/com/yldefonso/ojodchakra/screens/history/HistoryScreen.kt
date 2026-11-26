package com.yldefonso.ojodchakra.screens.history

import com.yldefonso.ojodchakra.ui.iconos.IconosOjoD
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yldefonso.ojodchakra.data.model.Diagnostico
import com.yldefonso.ojodchakra.ui.componentes.EncabezadoPantalla
import com.yldefonso.ojodchakra.ui.componentes.EstadoVacio
import com.yldefonso.ojodchakra.ui.componentes.Insignia
import com.yldefonso.ojodchakra.ui.componentes.Tarjeta
import com.yldefonso.ojodchakra.ui.componentes.TipoEstado
import com.yldefonso.ojodchakra.ui.theme.LocalColoresApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun HistoryScreen(
    onReporteClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val diagnosticos by viewModel.diagnosticos.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }
    val colores = LocalColoresApp.current

    val subtitulo = if (diagnosticos.isEmpty()) "Sin reportes guardados"
    else "${diagnosticos.size} diagnóstico(s) registrado(s)"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        EncabezadoPantalla(
            titulo = "Historial",
            subtitulo = subtitulo,
            onVolver = onBack,
            acciones = {
                if (diagnosticos.isNotEmpty()) {
                    IconButton(
                        onClick = { mostrarDialogo = true },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(colores.bandaTexto.copy(alpha = 0.14f))
                    ) {
                        Icon(
                            imageVector = IconosOjoD.Papelera,
                            contentDescription = "Limpiar historial",
                            tint = colores.bandaTexto
                        )
                    }
                }
            }
        )

        if (diagnosticos.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                EstadoVacio(
                    icono = IconosOjoD.Historial,
                    titulo = "Aún no hay reportes",
                    descripcion = "Escanea un cultivo para generar tu primer diagnóstico. Quedará guardado aquí."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 20.dp,
                    vertical = 18.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(diagnosticos, key = { it.id }) { diagnostico ->
                    ReporteItem(
                        diagnostico = diagnostico,
                        onClick = { onReporteClick(diagnostico.id) }
                    )
                }
            }
        }
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("¿Eliminar todo el historial?") },
            text = {
                Text(
                    text = "Se borrarán permanentemente todos los reportes y sus fotos de este celular.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.borrarHistorial()
                        mostrarDialogo = false
                    }
                ) {
                    Text("Eliminar todo", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ReporteItem(diagnostico: Diagnostico, onClick: () -> Unit) {
    val pendiente = diagnostico.estadoSincronizacion == "PENDIENTE"

    Tarjeta(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        padding = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FotoReporte(
                ruta = diagnostico.rutaFoto,
                ladoMaximo = 200,
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = diagnostico.plagaEnEspanol,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = IconosOjoD.Reloj,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = formatearFecha(diagnostico.fechaCaptura),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Insignia(
                    texto = textoEstado(diagnostico.estadoSincronizacion),
                    tipo = if (pendiente) TipoEstado.ALERTA else TipoEstado.EXITO,
                    icono = if (pendiente) IconosOjoD.Sincronizar else IconosOjoD.NubeOk
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Ver detalle",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// ---- UTILIDADES COMPARTIDAS CON EL DETALLE ----

@Composable
internal fun FotoReporte(ruta: String, ladoMaximo: Int, modifier: Modifier) {
    val bitmap by produceState<Bitmap?>(initialValue = null, ruta) {
        value = withContext(Dispatchers.IO) { decodificarImagen(ruta, ladoMaximo) }
    }
    val imagen = bitmap
    if (imagen != null) {
        Image(
            bitmap = imagen.asImageBitmap(),
            contentDescription = "Foto del reporte",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant))
    }
}

internal fun decodificarImagen(ruta: String, ladoMaximo: Int): Bitmap? {
    val soloMedidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(ruta, soloMedidas)

    var muestra = 1
    while (soloMedidas.outWidth / muestra > ladoMaximo ||
        soloMedidas.outHeight / muestra > ladoMaximo
    ) {
        muestra *= 2
    }

    val opciones = BitmapFactory.Options().apply { inSampleSize = muestra }
    return BitmapFactory.decodeFile(ruta, opciones)
}

// Formateador fijado en la zona horaria de Perú (America/Lima - UTC-5)
internal fun formatearFecha(milisegundos: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "PE"))
    sdf.timeZone = TimeZone.getTimeZone("America/Lima")
    return sdf.format(Date(milisegundos))
}

internal fun textoEstado(estado: String): String = when (estado) {
    "PENDIENTE" -> "Pendiente de enviar"
    "SINCRONIZADO" -> "Enviado al servidor"
    else -> estado
}
