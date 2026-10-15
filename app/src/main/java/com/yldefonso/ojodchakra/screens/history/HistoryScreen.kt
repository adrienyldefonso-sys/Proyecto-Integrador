package com.yldefonso.ojodchakra.screens.history

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yldefonso.ojodchakra.data.model.Diagnostico
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Paleta de Colores Institucional OjoDChakra
private val VerdePrincipal = Color(0xFF2E6A38)
private val FondoPantalla = Color(0xFFFAFCFA)
private val SuperficieTarjeta = Color(0xFFFFFFFF)
private val VerdeSubtil = Color(0xFFEBF3EC)
private val TextoOscuro = Color(0xFF1B2E21)
private val TextoMuted = Color(0xFF5A7060)

// Colores para estado de sincronización y borrado
private val FondoPendiente = Color(0xFFFFF3E0)
private val TextoPendiente = Color(0xFFD84315)
private val FondoSincronizado = Color(0xFFE8F5E9)
private val TextoSincronizado = Color(0xFF2E6A38)
private val RojoEliminar = Color(0xFFC62828)
private val FondoRojoSuave = Color(0xFFFFEBEE)

@Composable
fun HistoryScreen(
    onReporteClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val diagnosticos by viewModel.diagnosticos.collectAsState()
    var mostrarDialogo by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoPantalla)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ==========================================
            // 1. ENCABEZADO CON BOTÓN "LIMPIAR" REDISEÑADO
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Historial de reportes",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = TextoOscuro,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        )
                    )
                    Text(
                        text = if (diagnosticos.isEmpty()) "Sin reportes guardados" else "${diagnosticos.size} diagnóstico(s) registrado(s)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextoMuted,
                            fontSize = 13.sp
                        )
                    )
                }

                if (diagnosticos.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { mostrarDialogo = true },
                        shape = RoundedCornerShape(12.dp),
                        color = FondoRojoSuave,
                        border = BorderStroke(1.dp, RojoEliminar.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Limpiar historial",
                                tint = RojoEliminar,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Limpiar",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = RojoEliminar,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // 2. LISTA DE CARDS (CON EVENTO ONCLICK AL DETALLE)
            // ==========================================
            if (diagnosticos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        shape = RoundedCornerShape(24.dp),
                        color = SuperficieTarjeta,
                        border = BorderStroke(1.dp, VerdePrincipal.copy(alpha = 0.15f)),
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                modifier = Modifier.size(64.dp),
                                shape = CircleShape,
                                color = VerdeSubtil
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = VerdePrincipal,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Aún no hay reportes",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = TextoOscuro,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Escanea un cultivo para generar tu primer análisis local.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextoMuted,
                                    textAlign = TextAlign.Center,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
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

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // 3. BOTÓN VOLVER AL INICIO
            // ==========================================
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp), spotColor = VerdePrincipal),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdePrincipal,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Volver al inicio",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // ==========================================
    // DIÁLOGO DE BORRADO
    // ==========================================
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = SuperficieTarjeta,
            title = {
                Text(
                    text = "¿Eliminar todo el historial?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = TextoOscuro,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Text(
                    text = "Esta acción borrará permanentemente todos los reportes e imágenes locales.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextoMuted)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.borrarHistorial()
                        mostrarDialogo = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RojoEliminar),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Eliminar todo", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar", color = TextoMuted, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

@Composable
private fun ReporteItem(diagnostico: Diagnostico, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = Color.Black.copy(alpha = 0.06f)
            )
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = SuperficieTarjeta,
        border = BorderStroke(1.dp, VerdePrincipal.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // FOTO DE MUESTRA
            FotoReporte(
                ruta = diagnostico.rutaFoto,
                ladoMaximo = 200,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(14.dp))
            )

            Spacer(modifier = Modifier.width(14.dp))

            // INFORMACIÓN
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = diagnostico.plagaEnEspanol,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextoOscuro,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = TextoMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatearFecha(diagnostico.fechaCaptura),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextoMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                val esPendiente = diagnostico.estadoSincronizacion == "PENDIENTE"
                Surface(
                    color = if (esPendiente) FondoPendiente else FondoSincronizado,
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = if (esPendiente) Icons.Default.Sync else Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = if (esPendiente) TextoPendiente else TextoSincronizado,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = textoEstado(diagnostico.estadoSincronizacion),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (esPendiente) TextoPendiente else TextoSincronizado,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // FLECHA INDICADORA DE NAVEGACIÓN
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Ver detalle",
                tint = VerdePrincipal.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// ---- UTILIDADES ----

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
        Box(modifier = modifier.background(VerdeSubtil))
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
    "PENDIENTE" -> "Pendiente de sincronizar"
    "SINCRONIZADO" -> "Sincronizado"
    else -> estado
}