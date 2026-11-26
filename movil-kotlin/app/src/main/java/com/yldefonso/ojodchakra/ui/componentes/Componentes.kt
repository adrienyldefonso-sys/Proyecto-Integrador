package com.yldefonso.ojodchakra.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yldefonso.ojodchakra.ui.theme.LocalColoresApp
import kotlin.math.PI
import kotlin.math.sin

/** Tipos de estado con color semántico (éxito, alerta, peligro, información o neutro). */
enum class TipoEstado { EXITO, ALERTA, PELIGRO, INFO, NEUTRO }

/** Líneas de nivel de terreno: el motivo visual de la app. Se dibuja sin imágenes. */
@Composable
fun FondoCurvasNivel(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val lineas = 9
        val grosor = 1.dp.toPx()
        for (i in 0 until lineas) {
            val base = size.height * (i + 1) / (lineas + 1f)
            val amplitud = size.height * (0.04f + 0.02f * (i % 3))
            val frecuencia = 1.0f + i * 0.06f
            val trazo = Path()
            val pasos = 48
            for (k in 0..pasos) {
                val fraccion = k / pasos.toFloat()
                val x = size.width * fraccion
                val y = base + amplitud * sin(fraccion * 2f * PI.toFloat() * frecuencia + i * 0.7f)
                if (k == 0) trazo.moveTo(x, y) else trazo.lineTo(x, y)
            }
            drawPath(path = trazo, color = color, style = Stroke(width = grosor))
        }
    }
}

/**
 * Banda superior de todas las pantallas: flecha de volver a la izquierda, título centrado
 * y acciones a la derecha, sobre el fondo de curvas de nivel.
 */
@Composable
fun EncabezadoPantalla(
    titulo: String,
    subtitulo: String? = null,
    onVolver: (() -> Unit)? = null,
    acciones: @Composable () -> Unit = {}
) {
    val colores = LocalColoresApp.current
    Surface(
        color = colores.bandaFondo,
        contentColor = colores.bandaTexto,
        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box {
            FondoCurvasNivel(
                modifier = Modifier.matchParentSize(),
                color = colores.bandaTexto.copy(alpha = 0.08f)
            )
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    if (onVolver != null) {
                        IconButton(
                            onClick = onVolver,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(colores.bandaTexto.copy(alpha = 0.14f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = colores.bandaTexto
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.titleLarge,
                        color = colores.bandaTexto,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitulo != null) {
                        Text(
                            text = subtitulo,
                            style = MaterialTheme.typography.bodySmall,
                            color = colores.bandaTexto.copy(alpha = 0.78f),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    acciones()
                }
            }
        }
    }
}

/** Etiqueta de sección: barra de color de marca y título en sentence case. */
@Composable
fun EtiquetaSeccion(texto: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(start = 2.dp, top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/** Tarjeta plana con borde: sin sombras ni brillos. Si recibe [onClick], es tocable. */
@Composable
fun Tarjeta(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    padding: Dp = 16.dp,
    contenido: @Composable () -> Unit
) {
    val forma = RoundedCornerShape(28.dp)
    val borde: BorderStroke? = null
    val columna: @Composable () -> Unit = {
        Column(modifier = Modifier.padding(padding)) { contenido() }
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = forma,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            border = borde,
            content = columna
        )
    } else {
        Surface(
            modifier = modifier,
            shape = forma,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            border = borde,
            content = columna
        )
    }
}

/** Ícono dentro de un cuadro redondeado. Es el contenedor visual estándar de la app. */
@Composable
fun IconoContenedor(
    icono: ImageVector,
    modifier: Modifier = Modifier,
    tamano: Dp = 44.dp,
    fondo: Color = MaterialTheme.colorScheme.primaryContainer,
    tinte: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Box(
        modifier = modifier
            .size(tamano)
            .clip(RoundedCornerShape(18.dp))
            .background(fondo),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = tinte,
            modifier = Modifier.size(tamano * 0.5f)
        )
    }
}

/** Colores (fondo y texto) de un [TipoEstado] para el modo activo. */
@Composable
fun colorDeEstado(tipo: TipoEstado): Pair<Color, Color> {
    val colores = LocalColoresApp.current
    return when (tipo) {
        TipoEstado.EXITO -> colores.exitoFondo to colores.exitoTexto
        TipoEstado.ALERTA -> colores.alertaFondo to colores.alertaTexto
        TipoEstado.PELIGRO -> colores.peligroFondo to colores.peligroTexto
        TipoEstado.INFO -> colores.infoFondo to colores.infoTexto
        TipoEstado.NEUTRO -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
}

/** Indicador de estado: punto o ícono con el texto en el color del estado, sin caja. */
@Composable
fun Insignia(
    texto: String,
    tipo: TipoEstado,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null
) {
    val (_, tinte) = colorDeEstado(tipo)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icono != null) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = tinte,
                modifier = Modifier.size(16.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(tinte)
            )
        }
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            color = tinte,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Botón principal: ancho completo, color de marca y ícono opcional. */
@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    habilitado: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        if (icono != null) {
            Icon(imageVector = icono, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
        }
        Text(text = texto, style = MaterialTheme.typography.labelLarge)
    }
}

/** Botón secundario con borde de marca. */
@Composable
fun BotonSecundario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        if (icono != null) {
            Icon(imageVector = icono, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = texto, style = MaterialTheme.typography.labelLarge)
    }
}

/** Estado vacío: ícono, título y descripción centrados. */
@Composable
fun EstadoVacio(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconoContenedor(icono = icono, tamano = 64.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = descripcion,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/** Fila de ajuste o de lista: ícono, título, descripción y un extremo opcional. */
@Composable
fun FilaAjuste(
    icono: ImageVector,
    titulo: String,
    descripcion: String? = null,
    onClick: (() -> Unit)? = null,
    extremo: @Composable () -> Unit = {}
) {
    val contenido: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconoContenedor(icono = icono, tamano = 42.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (descripcion != null) {
                    Text(
                        text = descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            extremo()
        }
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            color = Color.Transparent,
            shape = RoundedCornerShape(14.dp)
        ) { contenido() }
    } else {
        contenido()
    }
}

/** Barra de progreso con el color indicado. [valor] va de 0 a 1. */
@Composable
fun BarraProgreso(valor: Float, color: Color, modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        progress = { valor.coerceIn(0f, 1f) },
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(50)),
        color = color,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

/** Separador fino usado dentro de tarjetas. */
@Composable
fun SeparadorSuave(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 1.dp
    )
}

/** Dato con etiqueta pequeña arriba y valor destacado abajo. */
@Composable
fun DatoEtiquetado(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
