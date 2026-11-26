package com.yldefonso.ojodchakra.screens.history

import com.yldefonso.ojodchakra.ui.iconos.IconosOjoD
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.material.icons.filled.Share
import kotlinx.coroutines.launch
import com.yldefonso.ojodchakra.reporte.ReportePdf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yldefonso.ojodchakra.ml.Clases
import com.yldefonso.ojodchakra.ml.MotorRecomendaciones
import com.yldefonso.ojodchakra.ui.componentes.BarraProgreso
import com.yldefonso.ojodchakra.ui.componentes.BotonSecundario
import com.yldefonso.ojodchakra.ui.componentes.DatoEtiquetado
import com.yldefonso.ojodchakra.ui.componentes.EncabezadoPantalla
import com.yldefonso.ojodchakra.ui.componentes.EstadoVacio
import com.yldefonso.ojodchakra.ui.componentes.EtiquetaSeccion
import com.yldefonso.ojodchakra.ui.componentes.IconoContenedor
import com.yldefonso.ojodchakra.ui.componentes.Insignia
import com.yldefonso.ojodchakra.ui.componentes.SeparadorSuave
import com.yldefonso.ojodchakra.ui.componentes.Tarjeta
import com.yldefonso.ojodchakra.ui.componentes.TipoEstado
import com.yldefonso.ojodchakra.ui.componentes.colorDeEstado
import com.yldefonso.ojodchakra.ui.componentes.textoDeSeveridad
import com.yldefonso.ojodchakra.ui.componentes.tipoDeSeveridad

@Composable
fun DetailScreen(
    id: String,
    onBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val diagnostico by remember(id) { viewModel.observarDiagnostico(id) }
        .collectAsState(initial = null)
    val d = diagnostico
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        EncabezadoPantalla(
            titulo = "Detalle del reporte",
            subtitulo = d?.let { formatearFecha(it.fechaCaptura) },
            onVolver = onBack
        )

        if (d == null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                EstadoVacio(
                    icono = IconosOjoD.Ciencia,
                    titulo = "No se encontró el reporte",
                    descripcion = "Es posible que se haya borrado del historial."
                )
            }
            return@Column
        }

        val esSana = d.plagaEnEspanol.contains("sana", ignoreCase = true)
        val porcentaje = if (d.confianza <= 1.0f) d.confianza * 100f else d.confianza
        val pendiente = d.estadoSincronizacion == "PENDIENTE"
        // Si el agrónomo corrigió la plaga, la severidad y el tratamiento son los de esa plaga
        val validada = d.etiquetaValidada?.takeIf { d.estadoRevision == "CORREGIDA" && it.isNotBlank() }
        val recomendacionValidada = validada?.let { MotorRecomendaciones.obtener(it) }
        val nivel = recomendacionValidada?.nivelSeveridad ?: d.nivelSeveridad
        val tratamiento = recomendacionValidada?.tratamiento ?: d.recomendacion

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Foto ampliada
            FotoReporte(
                ruta = d.rutaFoto,
                ladoMaximo = 1024,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(24.dp))
            )

            // Diagnóstico
            EtiquetaSeccion("Diagnóstico")
            Tarjeta {
                Insignia(
                    texto = if (esSana) "Cultivo saludable" else "Anomalía detectada",
                    tipo = if (esSana) TipoEstado.EXITO else TipoEstado.ALERTA,
                    icono = if (esSana) IconosOjoD.Salud else IconosOjoD.Ciencia
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = d.plagaEnEspanol,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = IconosOjoD.Reloj,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Capturado el ${formatearFecha(d.fechaCaptura)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Nivel de confianza",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = String.format("%.1f %%", porcentaje),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                BarraProgreso(
                    valor = porcentaje / 100f,
                    color = if (esSana) MaterialTheme.colorScheme.primary
                    else colorDeEstado(TipoEstado.ALERTA).second
                )

                Spacer(modifier = Modifier.height(14.dp))
                SeparadorSuave()
                Spacer(modifier = Modifier.height(12.dp))

                Insignia(
                    texto = textoEstado(d.estadoSincronizacion),
                    tipo = if (pendiente) TipoEstado.ALERTA else TipoEstado.EXITO,
                    icono = if (pendiente) IconosOjoD.Sincronizar else IconosOjoD.NubeOk
                )
            }

            // Revisión del agrónomo (llega con la sincronización)
            if (d.estadoRevision != "NINGUNA") {
                EtiquetaSeccion("Revisión del agrónomo")
                Tarjeta {
                    Insignia(
                        texto = textoRevision(d.estadoRevision),
                        tipo = when (d.estadoRevision) {
                            "SOLICITADA" -> TipoEstado.INFO
                            "CORREGIDA" -> TipoEstado.ALERTA
                            else -> TipoEstado.EXITO
                        },
                        icono = IconosOjoD.Agronomo
                    )
                    d.etiquetaValidada
                        ?.takeIf { it.isNotBlank() && d.estadoRevision == "CORREGIDA" }
                        ?.let {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Plaga validada: ${Clases.nombre(it)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    d.observacionAgronomo
                        ?.takeIf { it.isNotBlank() }
                        ?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "“$it”",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                }
            }

            // Severidad
            EtiquetaSeccion("Severidad")
            Tarjeta {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconoContenedor(icono = IconosOjoD.Salud, tamano = 46.dp)
                    Spacer(modifier = Modifier.width(14.dp))
                    DatoEtiquetado(
                        etiqueta = "Nivel de severidad",
                        valor = nivel?.let { textoDeSeveridad(it) } ?: "Sin evaluar",
                        modifier = Modifier.weight(1f)
                    )
                    Insignia(
                        texto = nivel?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Sin evaluar",
                        tipo = tipoDeSeveridad(nivel)
                    )
                }
            }

            // Recomendación
            EtiquetaSeccion("Recomendación de tratamiento")
            Tarjeta {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconoContenedor(icono = IconosOjoD.Tratamiento, tamano = 46.dp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Qué hacer ahora",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (validada != null) {
                    Text(
                        text = "Según la plaga validada por el agrónomo: ${Clases.nombre(validada)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = tratamiento ?: "Aún no disponible",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            if (d != null) {
                BotonSecundario(
                    texto = "Compartir reporte en PDF",
                    onClick = {
                        scope.launch {
                            try {
                                val archivo = viewModel.generarPdf(d)
                                ReportePdf.compartir(context, archivo)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No se pudo crear el PDF", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    icono = Icons.Default.Share
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            BotonSecundario(
                texto = "Volver al historial",
                onClick = onBack,
                icono = Icons.AutoMirrored.Filled.ArrowBack
            )
        }
    }
}

private fun textoRevision(estado: String): String = when (estado) {
    "SOLICITADA" -> "Revisión solicitada"
    "CONFIRMADA" -> "Confirmado por agrónomo"
    "CORREGIDA" -> "Corregido por agrónomo"
    else -> "Sin revisión"
}
