package com.yldefonso.ojodchakra.screens.result

import com.yldefonso.ojodchakra.ui.iconos.IconosOjoD
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yldefonso.ojodchakra.ml.Clases
import com.yldefonso.ojodchakra.ml.MotorRecomendaciones
import com.yldefonso.ojodchakra.screens.camera.CameraViewModel
import com.yldefonso.ojodchakra.ui.componentes.BotonPrincipal
import com.yldefonso.ojodchakra.ui.componentes.BotonSecundario
import com.yldefonso.ojodchakra.ui.componentes.EncabezadoPantalla
import com.yldefonso.ojodchakra.ui.componentes.EstadoVacio
import com.yldefonso.ojodchakra.ui.componentes.EtiquetaSeccion
import com.yldefonso.ojodchakra.ui.componentes.Insignia
import com.yldefonso.ojodchakra.ui.componentes.SeparadorSuave
import com.yldefonso.ojodchakra.ui.componentes.Tarjeta
import com.yldefonso.ojodchakra.ui.componentes.TipoEstado
import com.yldefonso.ojodchakra.ui.componentes.colorDeEstado
import com.yldefonso.ojodchakra.ui.componentes.textoDeSeveridad
import com.yldefonso.ojodchakra.ui.componentes.tipoDeSeveridad
import com.yldefonso.ojodchakra.ui.theme.LocalColoresApp
import com.yldefonso.ojodchakra.util.FormatoParcela

@Composable
fun ResultScreen(
    viewModel: CameraViewModel,
    onScanAgain: () -> Unit,
    onGoHome: () -> Unit
) {
    val bitmap = viewModel.capturedBitmap.value
    val result = viewModel.result.value
    val diagnosticoId = viewModel.diagnosticoId.value
    val guardado = viewModel.diagnosticoGuardado.value
    val estadoRevision = guardado?.estadoRevision ?: "NINGUNA"
    val validada = guardado?.etiquetaValidada
    val observacionAgronomo = guardado?.observacionAgronomo
    val parcelaNombre = viewModel.parcelaNombre.value

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val volverInicio = {
        viewModel.reset()
        onGoHome()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        EncabezadoPantalla(
            titulo = "Resultado",
            subtitulo = "Análisis en tu celular",
            onVolver = volverInicio
        )

        if (result == null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                EstadoVacio(
                    icono = IconosOjoD.Ciencia,
                    titulo = "No se pudo obtener un diagnóstico",
                    descripcion = "Vuelve a escanear la hoja con buena luz y el cuadro bien centrado."
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                BotonPrincipal(
                    texto = "Escanear otra vez",
                    onClick = {
                        viewModel.reset()
                        onScanAgain()
                    },
                    icono = IconosOjoD.Escanear
                )
            }
            return@Column
        }

        // El resultado es el del modelo. La revisión del agrónomo se muestra aparte.
        // Si el agrónomo corrigió, se muestra su plaga y su recomendación; si no, la del modelo
        val etiqueta = if (estadoRevision == "CORREGIDA" && !validada.isNullOrBlank()) validada else result.label
        val nombre = Clases.nombre(etiqueta)
        val sana = etiqueta == "Potato___healthy"
        val recomendacion = MotorRecomendaciones.obtener(etiqueta)
        val nivel = recomendacion.nivelSeveridad
        val porcentaje = if (result.confidence <= 1.0f) result.confidence * 100f else result.confidence
        val fraccion = (porcentaje / 100f).coerceIn(0f, 1f)

        val progreso by animateFloatAsState(
            targetValue = if (visible) fraccion else 0f,
            animationSpec = tween(durationMillis = 1100),
            label = "progreso"
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 8 }
            ) {
                Hero(
                    bitmap = bitmap,
                    nombre = nombre,
                    sana = sana,
                    porcentaje = porcentaje,
                    progreso = progreso,
                    textoParcela = FormatoParcela.textoGuardado(parcelaNombre)
                )
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600, delayMillis = 150)) + slideInVertically(tween(600, delayMillis = 150)) { it / 8 }
            ) {
                Column {
                    EtiquetaSeccion("Revisión técnica")
                    Spacer(modifier = Modifier.height(10.dp))
                    TarjetaRevision(
                        estado = estadoRevision,
                        plagaModelo = Clases.nombre(result.label),
                        plagaValidada = validada?.let { Clases.nombre(it) },
                        observacion = observacionAgronomo,
                        puedeSolicitar = diagnosticoId != null,
                        onSolicitar = { viewModel.solicitarRevision() }
                    )
                }
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600, delayMillis = 300)) + slideInVertically(tween(600, delayMillis = 300)) { it / 8 }
            ) {
                Column {
                    EtiquetaSeccion("Recomendación de tratamiento")
                    Spacer(modifier = Modifier.height(10.dp))
                    TarjetaRecomendacion(
                        nivel = nivel,
                        descripcion = recomendacion.descripcion,
                        tratamiento = recomendacion.tratamiento,
                        prevencion = recomendacion.prevencion
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BotonPrincipal(
                texto = "Escanear otra vez",
                onClick = {
                    viewModel.reset()
                    onScanAgain()
                },
                icono = IconosOjoD.Escanear
            )
            BotonSecundario(
                texto = "Volver al inicio",
                onClick = volverInicio,
                icono = IconosOjoD.Inicio
            )
        }
    }
}

/** Cabecera con la foto, el nombre del diagnóstico y el anillo de confianza. */
@Composable
private fun Hero(
    bitmap: Bitmap?,
    nombre: String,
    sana: Boolean,
    porcentaje: Float,
    progreso: Float,
    textoParcela: String
) {
    val colores = LocalColoresApp.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                if (bitmap == null) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Foto analizada",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Sombra suave en la parte baja para leer el texto sobre la foto
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.45f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.8f)
                        )
                    )
            )
        }

        // Anillo de confianza
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp)
                .size(68.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(56.dp)) {
                val grosor = 5.dp.toPx()
                drawArc(
                    color = Color.White.copy(alpha = 0.25f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = grosor, cap = StrokeCap.Round)
                )
                drawArc(
                    color = colores.bandaAcento,
                    startAngle = -90f,
                    sweepAngle = 360f * progreso,
                    useCenter = false,
                    style = Stroke(width = grosor, cap = StrokeCap.Round)
                )
            }
            Text(
                text = String.format("%.0f%%", porcentaje),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White
            )
        }

        // Nombre del diagnóstico y estado
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(22.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Text(
                    text = if (sana) "Cultivo saludable" else "Anomalía detectada",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = nombre,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = textoParcela,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Estado de la revisión del agrónomo. El agricultor solo puede pedirla;
 * confirmar o corregir lo hace el agrónomo desde la web.
 */
@Composable
private fun TarjetaRevision(
    estado: String,
    plagaModelo: String,
    plagaValidada: String?,
    observacion: String?,
    puedeSolicitar: Boolean,
    onSolicitar: () -> Unit
) {
    val titulo = when (estado) {
        "SOLICITADA" -> "Revisión solicitada"
        "CONFIRMADA" -> "Confirmada por un agrónomo"
        "CORREGIDA" -> "Corregida por un agrónomo"
        else -> "¿Tienes dudas con este resultado?"
    }
    val descripcion = when (estado) {
        "SOLICITADA" -> "Se enviará al agrónomo cuando tu celular tenga conexión."
        "CONFIRMADA" -> "Un agrónomo revisó la foto y confirmó este diagnóstico."
        "CORREGIDA" -> "El modelo indicó $plagaModelo. El agrónomo determinó: ${plagaValidada ?: ""}."
        else -> "Un agrónomo puede revisar la foto y confirmar el diagnóstico."
    }
    val tipo = when (estado) {
        "SOLICITADA" -> TipoEstado.INFO
        "CONFIRMADA" -> TipoEstado.EXITO
        "CORREGIDA" -> TipoEstado.ALERTA
        else -> TipoEstado.NEUTRO
    }
    val (fondo, tinte) = colorDeEstado(tipo)

    Tarjeta(padding = 18.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(fondo),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconosOjoD.Agronomo,
                    contentDescription = null,
                    tint = tinte,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (!observacion.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "“$observacion”",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (estado == "NINGUNA" && puedeSolicitar) {
            Spacer(modifier = Modifier.height(14.dp))
            BotonSecundario(
                texto = "Pedir revisión a un agrónomo",
                onClick = onSolicitar,
                icono = IconosOjoD.Agronomo
            )
        }
    }
}

@Composable
private fun TarjetaRecomendacion(
    nivel: String?,
    descripcion: String,
    tratamiento: String,
    prevencion: String
) {
    Tarjeta(padding = 18.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Severidad",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Insignia(
                texto = textoDeSeveridad(nivel),
                tipo = tipoDeSeveridad(nivel)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        BloqueRecomendacion(
            icono = IconosOjoD.Info,
            tipo = TipoEstado.INFO,
            titulo = "Qué es",
            texto = descripcion
        )
        SeparadorSuave(modifier = Modifier.padding(vertical = 14.dp))
        BloqueRecomendacion(
            icono = IconosOjoD.Tratamiento,
            tipo = TipoEstado.ALERTA,
            titulo = "Qué hacer ahora",
            texto = tratamiento
        )
        SeparadorSuave(modifier = Modifier.padding(vertical = 14.dp))
        BloqueRecomendacion(
            icono = IconosOjoD.Salud,
            tipo = TipoEstado.EXITO,
            titulo = "Cómo prevenirlo",
            texto = prevencion
        )
    }
}

@Composable
private fun BloqueRecomendacion(
    icono: ImageVector,
    tipo: TipoEstado,
    titulo: String,
    texto: String
) {
    val (fondo, tinte) = colorDeEstado(tipo)
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(fondo),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = tinte,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
