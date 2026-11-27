package com.yldefonso.ojodchakra.ui.componentes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.random.Random

/**
 * Fondo de hojas dibujadas con trazos suaves y muy transparentes.
 * Las posiciones son fijas (semilla fija) para que no cambien al redibujar.
 */
@Composable
fun FondoHojas(modifier: Modifier = Modifier.fillMaxSize(), color: Color = Color.White) {
    Canvas(modifier = modifier) {
        val aleatorio = Random(7)
        repeat(26) {
            val cx = aleatorio.nextFloat() * size.width
            val cy = aleatorio.nextFloat() * size.height
            val largo = size.minDimension * (0.10f + aleatorio.nextFloat() * 0.12f)
            val angulo = aleatorio.nextFloat() * 360f
            val alfa = 0.05f + aleatorio.nextFloat() * 0.07f

            rotate(angulo, pivot = Offset(cx, cy)) {
                val hoja = Path().apply {
                    moveTo(cx, cy - largo / 2)
                    cubicTo(cx + largo * 0.45f, cy - largo * 0.25f, cx + largo * 0.35f, cy + largo * 0.35f, cx, cy + largo / 2)
                    cubicTo(cx - largo * 0.35f, cy + largo * 0.35f, cx - largo * 0.45f, cy - largo * 0.25f, cx, cy - largo / 2)
                    close()
                }
                drawPath(path = hoja, color = color.copy(alpha = alfa))
                drawLine(
                    color = color.copy(alpha = alfa * 1.6f),
                    start = Offset(cx, cy - largo / 2),
                    end = Offset(cx, cy + largo / 2),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
    }
}
