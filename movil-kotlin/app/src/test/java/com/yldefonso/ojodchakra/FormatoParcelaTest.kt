package com.yldefonso.ojodchakra

import com.yldefonso.ojodchakra.util.FormatoParcela
import org.junit.Assert.assertEquals
import org.junit.Test

class FormatoParcelaTest {

    @Test
    fun sinDiagnosticosUsaTextoEspecial() {
        assertEquals("Sin diagnósticos", FormatoParcela.conteoDiagnosticos(0))
    }

    @Test
    fun unDiagnosticoVaEnSingular() {
        assertEquals("1 diagnóstico", FormatoParcela.conteoDiagnosticos(1))
    }

    @Test
    fun variosDiagnosticosVanEnPlural() {
        assertEquals("5 diagnósticos", FormatoParcela.conteoDiagnosticos(5))
    }

    @Test
    fun coordenadasSeMuestranConCincoDecimales() {
        assertEquals("-12.04640, -77.04280", FormatoParcela.coordenadas(-12.0464, -77.0428))
    }

    @Test
    fun textoGuardadoConParcelaMuestraSuNombre() {
        assertEquals(
            "Guardado en la parcela: Finca Sol Naciente",
            FormatoParcela.textoGuardado("  Finca Sol Naciente ")
        )
    }

    @Test
    fun textoGuardadoSinParcelaUsaElMensajeGeneral() {
        assertEquals(
            "Guardado automáticamente en el historial local",
            FormatoParcela.textoGuardado(null)
        )
    }

    @Test
    fun textoGuardadoConNombreEnBlancoUsaElMensajeGeneral() {
        assertEquals(
            "Guardado automáticamente en el historial local",
            FormatoParcela.textoGuardado("   ")
        )
    }
}