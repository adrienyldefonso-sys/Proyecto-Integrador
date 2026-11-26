package com.yldefonso.ojodchakra.reporte

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.yldefonso.ojodchakra.data.model.Diagnostico
import com.yldefonso.ojodchakra.ml.Clases
import com.yldefonso.ojodchakra.ml.MotorRecomendaciones
import com.yldefonso.ojodchakra.screens.history.decodificarImagen
import com.yldefonso.ojodchakra.ui.componentes.textoDeSeveridad
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max

/** Reporte de un diagnóstico en una página A4, listo para compartir por WhatsApp u otra app. */
object ReportePdf {

    private const val ANCHO = 595
    private const val ALTO = 842
    private const val MARGEN = 40f
    private val VERDE = Color.parseColor("#1E5E32")
    private val TEXTO = Color.parseColor("#1B2A1F")
    private val GRIS = Color.parseColor("#5F6B63")
    private val zona = TimeZone.getTimeZone("America/Lima")

    fun generar(context: Context, diagnostico: Diagnostico, nombreParcela: String?): File {
        val documento = PdfDocument()
        val pagina = documento.startPage(PdfDocument.PageInfo.Builder(ANCHO, ALTO, 1).create())
        dibujar(pagina.canvas, diagnostico, nombreParcela)
        documento.finishPage(pagina)

        val carpeta = File(context.cacheDir, "pdf").apply { mkdirs() }
        val archivo = File(carpeta, "reporte_${diagnostico.id}.pdf")
        try {
            FileOutputStream(archivo).use { documento.writeTo(it) }
        } finally {
            documento.close()
        }
        return archivo
    }

    fun compartir(context: Context, archivo: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
        val intento = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intento, "Compartir reporte"))
    }

    private fun dibujar(c: Canvas, d: Diagnostico, nombreParcela: String?) {
        c.drawColor(Color.WHITE)
        dibujarCabecera(c)
        dibujarFoto(c, d.rutaFoto)

        // Si el agrónomo corrigió, el reporte muestra su plaga y su tratamiento
        val validada = d.etiquetaValidada?.takeIf { d.estadoRevision == "CORREGIDA" && it.isNotBlank() }
        val etiqueta = validada ?: d.plagaDetectada
        val recomendacion = MotorRecomendaciones.obtener(etiqueta)
        val nivel = if (validada != null) recomendacion.nivelSeveridad else d.nivelSeveridad
        val tratamiento = if (validada != null) recomendacion.tratamiento else (d.recomendacion ?: recomendacion.tratamiento)
        val porcentaje = if (d.confianza <= 1f) d.confianza * 100f else d.confianza

        var y = 404f
        y = etiquetaSeccion(c, "DIAGNÓSTICO", y)
        y = texto(c, Clases.nombre(etiqueta), y, 22f, TEXTO, negrita = true)
        if (validada != null) {
            y = texto(c, "El modelo indicó: ${Clases.nombre(d.plagaDetectada)}", y, 12f, GRIS)
        }
        y = texto(c, "Confianza: ${String.format(Locale("es", "PE"), "%.1f %%", porcentaje)}", y, 12f, TEXTO)
        y = texto(c, "Fecha: ${formatear(d.fechaCaptura)}", y, 12f, TEXTO)
        y = texto(c, "Parcela: ${nombreParcela?.takeIf { it.isNotBlank() } ?: "Sin parcela"}", y, 12f, TEXTO)
        y = texto(c, "Severidad: ${textoDeSeveridad(nivel)}", y, 12f, TEXTO)

        if (d.estadoRevision != "NINGUNA") {
            y += 10f
            y = etiquetaSeccion(c, "REVISIÓN DEL AGRÓNOMO", y)
            y = texto(c, textoRevision(d.estadoRevision), y, 13f, VERDE, negrita = true)
            d.observacionAgronomo?.takeIf { it.isNotBlank() }?.let {
                y = parrafo(c, "“$it”", y, GRIS)
            }
        }

        y += 10f
        y = etiquetaSeccion(c, "QUÉ HACER AHORA", y)
        y = parrafo(c, tratamiento ?: "Aún no disponible", y, TEXTO)

        y += 4f
        y = etiquetaSeccion(c, "CÓMO PREVENIRLO", y)
        parrafo(c, recomendacion.prevencion, y, TEXTO)

        dibujarPie(c)
    }

    private fun dibujarCabecera(c: Canvas) {
        c.drawRect(0f, 0f, ANCHO.toFloat(), 104f, Paint().apply { color = VERDE })
        c.drawText(
            "OjoDChakra", MARGEN, 50f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 26f
                typeface = Typeface.DEFAULT_BOLD
            }
        )
        c.drawText(
            "Reporte de diagnóstico de plagas en papa", MARGEN, 78f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 13f
            }
        )
    }

    private fun dibujarFoto(c: Canvas, ruta: String) {
        val marco = RectF(MARGEN, 128f, ANCHO - MARGEN, 378f)
        val foto = decodificarImagen(ruta, 1200)
        c.save()
        c.clipPath(Path().apply { addRoundRect(marco, 14f, 14f, Path.Direction.CW) })
        if (foto != null) {
            val escala = max(marco.width() / foto.width, marco.height() / foto.height)
            val ancho = foto.width * escala
            val alto = foto.height * escala
            c.drawBitmap(
                foto,
                null,
                RectF(
                    marco.centerX() - ancho / 2, marco.centerY() - alto / 2,
                    marco.centerX() + ancho / 2, marco.centerY() + alto / 2
                ),
                null
            )
        } else {
            c.drawRect(marco, Paint().apply { color = Color.parseColor("#E6ECE7") })
            c.drawText(
                "Foto no disponible", marco.centerX() - 60f, marco.centerY(),
                Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GRIS; textSize = 14f }
            )
        }
        c.restore()
    }

    private fun etiquetaSeccion(c: Canvas, titulo: String, y: Float): Float {
        c.drawText(
            titulo, MARGEN, y + 10f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = VERDE
                textSize = 10f
                typeface = Typeface.DEFAULT_BOLD
                letterSpacing = 0.08f
            }
        )
        return y + 26f
    }

    private fun texto(c: Canvas, valor: String, y: Float, tamano: Float, color: Int, negrita: Boolean = false): Float {
        c.drawText(
            valor, MARGEN, y + tamano,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                textSize = tamano
                if (negrita) typeface = Typeface.DEFAULT_BOLD
            }
        )
        return y + tamano + 10f
    }

    private fun parrafo(c: Canvas, valor: String, y: Float, color: Int): Float {
        val pintura = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = 12.5f
        }
        val capa = StaticLayout.Builder
            .obtain(valor, 0, valor.length, pintura, (ANCHO - 2 * MARGEN).toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.2f)
            .setIncludePad(false)
            .build()
        c.save()
        c.translate(MARGEN, y)
        capa.draw(c)
        c.restore()
        return y + capa.height + 14f
    }

    private fun dibujarPie(c: Canvas) {
        c.drawLine(
            MARGEN, ALTO - 62f, ANCHO - MARGEN, ALTO - 62f,
            Paint().apply { color = Color.parseColor("#D5DDD7"); strokeWidth = 1f }
        )
        val pintura = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = GRIS; textSize = 9f }
        c.drawText("Generado el ${formatear(System.currentTimeMillis())}", MARGEN, ALTO - 44f, pintura)
        c.drawText(
            "Diagnóstico automático: confírmalo con un agrónomo antes de aplicar tratamientos.",
            MARGEN, ALTO - 30f, pintura
        )
    }

    private fun textoRevision(estado: String): String = when (estado) {
        "SOLICITADA" -> "Revisión solicitada"
        "CONFIRMADA" -> "Confirmado por agrónomo"
        "CORREGIDA" -> "Corregido por agrónomo"
        else -> "Sin revisión"
    }

    private fun formatear(milisegundos: Long): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "PE"))
            .apply { timeZone = zona }
            .format(Date(milisegundos))
}
