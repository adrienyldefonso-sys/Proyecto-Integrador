package com.yldefonso.ojodchakra.ml

import android.graphics.Bitmap

/**
 * Revisa la foto antes de analizarla: brillo y nitidez.
 * Corre en el celular y no usa el modelo, así que es rápido y funciona sin internet.
 */
object CalidadFoto {

    enum class Problema(val mensaje: String) {
        OSCURA("La foto está muy oscura. Busca más luz natural y vuelve a intentarlo."),
        SOBREEXPUESTA("La foto tiene demasiada luz. Evita el sol directo sobre la hoja."),
        BORROSA("La foto está borrosa. Mantén el celular firme y acércate un poco más.")
    }

    // Umbrales provisionales: se calibran con fotos reales tomadas en campo
    private const val BRILLO_MINIMO = 45.0
    private const val BRILLO_MAXIMO = 225.0
    private const val NITIDEZ_MINIMA = 80.0
    private const val LADO_ANALISIS = 160

    /** Devuelve el problema encontrado, o null si la foto tiene buena calidad. */
    fun evaluar(foto: Bitmap): Problema? {
        val muestra = Bitmap.createScaledBitmap(foto, LADO_ANALISIS, LADO_ANALISIS, true)
        val ancho = muestra.width
        val alto = muestra.height

        val pixeles = IntArray(ancho * alto)
        muestra.getPixels(pixeles, 0, ancho, 0, 0, ancho, alto)

        // Luminancia de cada píxel
        val gris = DoubleArray(pixeles.size)
        var sumaBrillo = 0.0
        for (i in pixeles.indices) {
            val p = pixeles[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            val y = 0.299 * r + 0.587 * g + 0.114 * b
            gris[i] = y
            sumaBrillo += y
        }
        val brillo = sumaBrillo / pixeles.size

        // Nitidez: varianza del laplaciano. Una foto borrosa tiene poca variación de bordes.
        var sumaLap = 0.0
        var sumaLapCuadrado = 0.0
        var cuenta = 0
        for (y in 1 until alto - 1) {
            for (x in 1 until ancho - 1) {
                val i = y * ancho + x
                val lap = 4 * gris[i] - gris[i - 1] - gris[i + 1] - gris[i - ancho] - gris[i + ancho]
                sumaLap += lap
                sumaLapCuadrado += lap * lap
                cuenta++
            }
        }
        val mediaLap = sumaLap / cuenta
        val nitidez = sumaLapCuadrado / cuenta - mediaLap * mediaLap

        return when {
            brillo < BRILLO_MINIMO -> Problema.OSCURA
            brillo > BRILLO_MAXIMO -> Problema.SOBREEXPUESTA
            nitidez < NITIDEZ_MINIMA -> Problema.BORROSA
            else -> null
        }
    }
}
