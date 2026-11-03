package com.yldefonso.ojodchakra

import com.yldefonso.ojodchakra.ml.MotorRecomendaciones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MotorRecomendacionesTest {

    private val etiquetas: List<String> =
        File("src/main/assets/labels.txt")
            .readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    @Test
    fun cadaEtiquetaDelModeloTieneSuRecomendacion() {
        assertTrue("labels.txt está vacío", etiquetas.isNotEmpty())
        etiquetas.forEach { etiqueta ->
            val r = MotorRecomendaciones.obtener(etiqueta)
            assertEquals("Falta recomendación para $etiqueta", etiqueta, r.plagaClave)
            assertTrue("Tratamiento vacío para $etiqueta", r.tratamiento.isNotBlank())
        }
    }

    @Test
    fun tizonTardioTieneSeveridadAlta() {
        assertEquals("ALTA", MotorRecomendaciones.obtener("Potato___Late_blight").nivelSeveridad)
    }

    @Test
    fun tizonTempranoTieneSeveridadMedia() {
        assertEquals("MEDIA", MotorRecomendaciones.obtener("Potato___Early_blight").nivelSeveridad)
    }

    @Test
    fun plantaSanaNoTieneSeveridad() {
        assertEquals("NINGUNA", MotorRecomendaciones.obtener("Potato___healthy").nivelSeveridad)
    }

    @Test
    fun etiquetaDesconocidaDevuelveRecomendacionPorDefecto() {
        assertEquals("desconocido", MotorRecomendaciones.obtener("Etiqueta_inventada").plagaClave)
    }
}