package com.yldefonso.ojodchakra.ui.iconos

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.unit.dp

/**
 * Iconos propios de OjoDChakra: trazo redondeado con un relleno suave (dos tonos).
 * Se pintan con el color del tema mediante `tint` en Icon.
 */
object IconosOjoD {

    val Parcela = icono("Parcela") {
        relleno(0.18f) { pinta(PIN) }
        trazo { pinta(PIN) }
        relleno(1f) { pinta(HOJA_PIN) }
    }

    val Escanear = icono("Escanear") {
        trazo {
            moveTo(4f, 9f); verticalLineTo(6f); quadTo(4f, 4f, 7f, 4f); horizontalLineTo(9f)
            moveTo(15f, 4f); horizontalLineTo(17f); quadTo(20f, 4f, 20f, 7f); verticalLineTo(9f)
            moveTo(20f, 15f); verticalLineTo(17f); quadTo(20f, 20f, 17f, 20f); horizontalLineTo(15f)
            moveTo(9f, 20f); horizontalLineTo(7f); quadTo(4f, 20f, 4f, 17f); verticalLineTo(15f)
        }
        relleno(1f) {
            moveTo(12f, 8.2f)
            curveTo(14.4f, 9.6f, 14.4f, 13.6f, 12f, 15.8f)
            curveTo(9.6f, 13.6f, 9.6f, 9.6f, 12f, 8.2f)
            close()
        }
    }

    val Ciencia = icono("Ciencia") {
        relleno(0.3f) {
            moveTo(6.2f, 16f); horizontalLineTo(17.8f); lineTo(20.2f, 18.7f)
            quadTo(21f, 21f, 18.6f, 21f); horizontalLineTo(5.4f)
            quadTo(3f, 21f, 3.8f, 18.7f); close()
        }
        trazo {
            moveTo(9.5f, 3f); horizontalLineTo(14.5f)
            moveTo(10f, 3f); verticalLineTo(9.2f); lineTo(4.4f, 18.2f)
            quadTo(3.6f, 21f, 5.4f, 21f); horizontalLineTo(18.6f)
            quadTo(20.4f, 21f, 19.6f, 18.2f); lineTo(14f, 9.2f); verticalLineTo(3f)
        }
    }

    val Agronomo = icono("Agronomo") {
        relleno(0.3f) { pinta(CABEZA) }
        trazo {
            pinta(CABEZA)
            moveTo(4f, 20f)
            curveTo(4f, 16.6f, 6.6f, 14.4f, 10f, 14.4f)
            curveTo(11.1f, 14.4f, 12.1f, 14.6f, 13f, 15f)
            pinta(INSIGNIA)
            moveTo(15.9f, 17.6f); lineTo(17.1f, 18.8f); lineTo(19.2f, 16.4f)
        }
    }

    val Salud = icono("Salud") {
        relleno(0.25f) { pinta(ESCUDO) }
        trazo { pinta(ESCUDO) }
        relleno(1f) {
            moveTo(12f, 8f)
            curveTo(13.7f, 9.1f, 13.7f, 12.7f, 12f, 14.8f)
            curveTo(10.3f, 12.7f, 10.3f, 9.1f, 12f, 8f)
            close()
        }
    }

    val NubeOk = icono("NubeOk") {
        relleno(0.25f) { pinta(NUBE) }
        trazo {
            pinta(NUBE)
            moveTo(9f, 13.5f); lineTo(11.2f, 15.6f); lineTo(15.2f, 11.6f)
        }
    }

    val NubeOff = icono("NubeOff") {
        relleno(0.25f) { pinta(NUBE) }
        trazo {
            pinta(NUBE)
            moveTo(3f, 3f); lineTo(21f, 21f)
        }
    }

    val Sincronizar = icono("Sincronizar") {
        trazo {
            moveTo(20f, 11.5f)
            curveTo(19.7f, 7f, 16.2f, 3.6f, 12f, 3.6f)
            curveTo(9.4f, 3.6f, 7.1f, 4.9f, 5.7f, 7f)
            moveTo(5.6f, 2.8f); verticalLineTo(7.2f); horizontalLineTo(10f)
            moveTo(4f, 12.5f)
            curveTo(4.3f, 17f, 7.8f, 20.4f, 12f, 20.4f)
            curveTo(14.6f, 20.4f, 16.9f, 19.1f, 18.3f, 17f)
            moveTo(18.4f, 21.2f); verticalLineTo(16.8f); horizontalLineTo(14f)
        }
    }

    val Tratamiento = icono("Tratamiento") {
        relleno(0.25f) { pinta(GOTA) }
        trazo {
            pinta(GOTA)
            moveTo(12f, 11f); verticalLineTo(17f)
            moveTo(9f, 14f); horizontalLineTo(15f)
        }
    }

    val Historial = icono("Historial") {
        trazo {
            moveTo(7f, 5f); horizontalLineTo(5.5f); quadTo(4f, 5f, 4f, 6.5f); verticalLineTo(19.5f)
            quadTo(4f, 21f, 5.5f, 21f); horizontalLineTo(18.5f); quadTo(20f, 21f, 20f, 19.5f)
            verticalLineTo(6.5f); quadTo(20f, 5f, 18.5f, 5f); horizontalLineTo(17f)
            moveTo(9f, 3f); horizontalLineTo(15f); verticalLineTo(6.5f); horizontalLineTo(9f); close()
            moveTo(7.5f, 14f); horizontalLineTo(9.8f); lineTo(11.2f, 10.8f); lineTo(13.2f, 17f)
            lineTo(14.6f, 14f); horizontalLineTo(16.5f)
        }
    }

    val Reloj = icono("Reloj") {
        relleno(0.25f) { pinta(CIRCULO) }
        trazo {
            pinta(CIRCULO)
            moveTo(12f, 7.5f); verticalLineTo(12f); lineTo(15f, 14.5f)
        }
    }

    val Inicio = icono("Inicio") {
        trazo { moveTo(12f, 21f); verticalLineTo(11.5f) }
        relleno(0.3f) {
            moveTo(12f, 11.5f)
            curveTo(12f, 7.4f, 9.2f, 5f, 4.6f, 5f)
            curveTo(4.6f, 9.2f, 7.6f, 11.5f, 12f, 11.5f); close()
        }
        trazo {
            moveTo(12f, 11.5f)
            curveTo(12f, 7.4f, 9.2f, 5f, 4.6f, 5f)
            curveTo(4.6f, 9.2f, 7.6f, 11.5f, 12f, 11.5f)
        }
        relleno(0.3f) {
            moveTo(12f, 14f)
            curveTo(12f, 10.2f, 14.6f, 7.6f, 19.4f, 7.6f)
            curveTo(19.4f, 11.6f, 16.4f, 14f, 12f, 14f); close()
        }
        trazo {
            moveTo(12f, 14f)
            curveTo(12f, 10.2f, 14.6f, 7.6f, 19.4f, 7.6f)
            curveTo(19.4f, 11.6f, 16.4f, 14f, 12f, 14f)
        }
    }

    val Info = icono("Info") {
        trazo {
            pinta(CIRCULO)
            moveTo(12f, 11f); verticalLineTo(16.5f)
            moveTo(12f, 7.6f); verticalLineTo(7.8f)
        }
    }

    val Mapa = icono("Mapa") {
        relleno(0.25f) { pinta(MAPA) }
        trazo {
            pinta(MAPA)
            moveTo(9f, 4.5f); verticalLineTo(17.5f)
            moveTo(15f, 6.5f); verticalLineTo(19.5f)
        }
    }

    val Gps = icono("Gps") {
        trazo {
            moveTo(12f, 6.5f)
            curveTo(15f, 6.5f, 17.5f, 9f, 17.5f, 12f)
            curveTo(17.5f, 15f, 15f, 17.5f, 12f, 17.5f)
            curveTo(9f, 17.5f, 6.5f, 15f, 6.5f, 12f)
            curveTo(6.5f, 9f, 9f, 6.5f, 12f, 6.5f); close()
            moveTo(12f, 2.5f); verticalLineTo(5f)
            moveTo(12f, 19f); verticalLineTo(21.5f)
            moveTo(2.5f, 12f); horizontalLineTo(5f)
            moveTo(19f, 12f); horizontalLineTo(21.5f)
        }
        relleno(1f) { pinta(CENTRO_GPS) }
    }

    val Papelera = icono("Papelera") {
        relleno(0.25f) {
            moveTo(6f, 6.5f); horizontalLineTo(18f); lineTo(17f, 19.5f)
            quadTo(16.9f, 21f, 15.4f, 21f); horizontalLineTo(8.6f)
            quadTo(7.1f, 21f, 7f, 19.5f); close()
        }
        trazo {
            moveTo(4f, 6.5f); horizontalLineTo(20f)
            moveTo(9.5f, 6.5f); verticalLineTo(4.5f); quadTo(9.5f, 3.5f, 10.5f, 3.5f)
            horizontalLineTo(13.5f); quadTo(14.5f, 3.5f, 14.5f, 4.5f); verticalLineTo(6.5f)
            moveTo(6f, 6.5f); lineTo(7f, 19.5f); quadTo(7.1f, 21f, 8.6f, 21f)
            horizontalLineTo(15.4f); quadTo(16.9f, 21f, 17f, 19.5f); lineTo(18f, 6.5f)
        }
    }

    val Ajustes = icono("Ajustes") {
        trazo {
            moveTo(4f, 7f); horizontalLineTo(20f)
            moveTo(4f, 17f); horizontalLineTo(20f)
        }
        relleno(1f) { pinta(PERILLA_ARRIBA) }
        relleno(1f) { pinta(PERILLA_ABAJO) }
    }

    val Sol = icono("Sol") {
        relleno(0.3f) { pinta(CIRCULO_SOL) }
        trazo {
            pinta(CIRCULO_SOL)
            moveTo(12f, 2.5f); verticalLineTo(4.5f)
            moveTo(12f, 19.5f); verticalLineTo(21.5f)
            moveTo(2.5f, 12f); horizontalLineTo(4.5f)
            moveTo(19.5f, 12f); horizontalLineTo(21.5f)
            moveTo(5.3f, 5.3f); lineTo(6.7f, 6.7f)
            moveTo(17.3f, 17.3f); lineTo(18.7f, 18.7f)
            moveTo(18.7f, 5.3f); lineTo(17.3f, 6.7f)
            moveTo(6.7f, 17.3f); lineTo(5.3f, 18.7f)
        }
    }

    val Luna = icono("Luna") {
        relleno(0.3f) { pinta(LUNA) }
        trazo { pinta(LUNA) }
    }

    val Automatico = icono("Automatico") {
        trazo { pinta(CIRCULO) }
        relleno(1f) {
            moveTo(12f, 3f)
            curveTo(16.9f, 3f, 21f, 7f, 21f, 12f)
            curveTo(21f, 17f, 16.9f, 21f, 12f, 21f); close()
        }
    }

    val Verificado = icono("Verificado") {
        relleno(0.25f) { pinta(CIRCULO) }
        trazo {
            pinta(CIRCULO)
            moveTo(8f, 12.4f); lineTo(10.9f, 15.2f); lineTo(16f, 9.6f)
        }
    }
}

// Figuras compartidas
private val PIN: PathBuilder.() -> Unit = {
    moveTo(5f, 10f)
    curveTo(5f, 6.1f, 8.1f, 3f, 12f, 3f)
    curveTo(15.9f, 3f, 19f, 6.1f, 19f, 10f)
    curveTo(19f, 15.5f, 12f, 21.5f, 12f, 21.5f)
    curveTo(12f, 21.5f, 5f, 15.5f, 5f, 10f); close()
}
private val HOJA_PIN: PathBuilder.() -> Unit = {
    moveTo(12f, 6.2f)
    curveTo(14.6f, 7.8f, 14.6f, 11.2f, 12f, 13.6f)
    curveTo(9.4f, 11.2f, 9.4f, 7.8f, 12f, 6.2f); close()
}
private val CABEZA: PathBuilder.() -> Unit = {
    moveTo(6f, 8f)
    curveTo(6f, 5.8f, 7.8f, 4f, 10f, 4f)
    curveTo(12.2f, 4f, 14f, 5.8f, 14f, 8f)
    curveTo(14f, 10.2f, 12.2f, 12f, 10f, 12f)
    curveTo(7.8f, 12f, 6f, 10.2f, 6f, 8f); close()
}
private val INSIGNIA: PathBuilder.() -> Unit = {
    moveTo(21f, 17.5f)
    curveTo(21f, 19.4f, 19.4f, 21f, 17.5f, 21f)
    curveTo(15.6f, 21f, 14f, 19.4f, 14f, 17.5f)
    curveTo(14f, 15.6f, 15.6f, 14f, 17.5f, 14f)
    curveTo(19.4f, 14f, 21f, 15.6f, 21f, 17.5f); close()
}
private val ESCUDO: PathBuilder.() -> Unit = {
    moveTo(12f, 3f); lineTo(19f, 5.6f); verticalLineTo(11f)
    curveTo(19f, 15.6f, 16f, 19.4f, 12f, 21f)
    curveTo(8f, 19.4f, 5f, 15.6f, 5f, 11f); verticalLineTo(5.6f); close()
}
private val NUBE: PathBuilder.() -> Unit = {
    moveTo(7.5f, 18.5f)
    curveTo(4.9f, 18.5f, 3f, 16.6f, 3f, 14.1f)
    curveTo(3f, 11.9f, 4.6f, 10.1f, 6.8f, 9.8f)
    curveTo(7.6f, 6.9f, 10.2f, 5f, 13.3f, 5f)
    curveTo(16.7f, 5f, 19.3f, 7.5f, 19.5f, 10.6f)
    curveTo(21.5f, 11f, 22f, 12.8f, 22f, 14.2f)
    curveTo(22f, 16.8f, 20f, 18.5f, 17.6f, 18.5f); close()
}
private val GOTA: PathBuilder.() -> Unit = {
    moveTo(12f, 3f)
    curveTo(12f, 3f, 5.5f, 10.2f, 5.5f, 14.6f)
    curveTo(5.5f, 18.2f, 8.4f, 21f, 12f, 21f)
    curveTo(15.6f, 21f, 18.5f, 18.2f, 18.5f, 14.6f)
    curveTo(18.5f, 10.2f, 12f, 3f, 12f, 3f); close()
}
private val CIRCULO: PathBuilder.() -> Unit = {
    moveTo(12f, 3f)
    curveTo(16.9f, 3f, 21f, 7f, 21f, 12f)
    curveTo(21f, 17f, 16.9f, 21f, 12f, 21f)
    curveTo(7f, 21f, 3f, 17f, 3f, 12f)
    curveTo(3f, 7f, 7f, 3f, 12f, 3f); close()
}
private val CIRCULO_SOL: PathBuilder.() -> Unit = {
    moveTo(12f, 8f)
    curveTo(14.2f, 8f, 16f, 9.8f, 16f, 12f)
    curveTo(16f, 14.2f, 14.2f, 16f, 12f, 16f)
    curveTo(9.8f, 16f, 8f, 14.2f, 8f, 12f)
    curveTo(8f, 9.8f, 9.8f, 8f, 12f, 8f); close()
}
private val LUNA: PathBuilder.() -> Unit = {
    moveTo(20.5f, 14.2f)
    curveTo(19.2f, 15.4f, 17.4f, 16f, 15.5f, 16f)
    curveTo(10.8f, 16f, 7f, 12.2f, 7f, 7.5f)
    curveTo(7f, 5.6f, 7.6f, 3.9f, 8.6f, 2.6f)
    curveTo(4.8f, 3.6f, 2f, 7.1f, 2f, 11.5f)
    curveTo(2f, 16.7f, 6.3f, 21f, 11.5f, 21f)
    curveTo(15.4f, 21f, 18.8f, 18.6f, 20.5f, 14.2f); close()
}
private val MAPA: PathBuilder.() -> Unit = {
    moveTo(3f, 6.5f); lineTo(9f, 4.5f); lineTo(15f, 6.5f); lineTo(21f, 4.5f)
    verticalLineTo(17.5f); lineTo(15f, 19.5f); lineTo(9f, 17.5f); lineTo(3f, 19.5f); close()
}
private val CENTRO_GPS: PathBuilder.() -> Unit = {
    moveTo(12f, 10.6f)
    curveTo(12.8f, 10.6f, 13.4f, 11.2f, 13.4f, 12f)
    curveTo(13.4f, 12.8f, 12.8f, 13.4f, 12f, 13.4f)
    curveTo(11.2f, 13.4f, 10.6f, 12.8f, 10.6f, 12f)
    curveTo(10.6f, 11.2f, 11.2f, 10.6f, 12f, 10.6f); close()
}
private val PERILLA_ARRIBA: PathBuilder.() -> Unit = {
    moveTo(9f, 4.5f)
    curveTo(10.4f, 4.5f, 11.5f, 5.6f, 11.5f, 7f)
    curveTo(11.5f, 8.4f, 10.4f, 9.5f, 9f, 9.5f)
    curveTo(7.6f, 9.5f, 6.5f, 8.4f, 6.5f, 7f)
    curveTo(6.5f, 5.6f, 7.6f, 4.5f, 9f, 4.5f); close()
}
private val PERILLA_ABAJO: PathBuilder.() -> Unit = {
    moveTo(15f, 14.5f)
    curveTo(16.4f, 14.5f, 17.5f, 15.6f, 17.5f, 17f)
    curveTo(17.5f, 18.4f, 16.4f, 19.5f, 15f, 19.5f)
    curveTo(13.6f, 19.5f, 12.5f, 18.4f, 12.5f, 17f)
    curveTo(12.5f, 15.6f, 13.6f, 14.5f, 15f, 14.5f); close()
}

// Helpers de dibujo
private fun icono(nombre: String, dibujar: ImageVector.Builder.() -> Unit): ImageVector =
    ImageVector.Builder(nombre, 24.dp, 24.dp, 24f, 24f).apply(dibujar).build()

private fun ImageVector.Builder.trazo(dibujo: PathBuilder.() -> Unit): ImageVector.Builder =
    path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathBuilder = dibujo
    )

private fun ImageVector.Builder.relleno(alfa: Float, dibujo: PathBuilder.() -> Unit): ImageVector.Builder =
    path(fill = SolidColor(Color.Black), fillAlpha = alfa, pathBuilder = dibujo)

// Permite reutilizar una figura compartida dentro de un trazo o relleno
private fun PathBuilder.pinta(figura: PathBuilder.() -> Unit) = figura()
