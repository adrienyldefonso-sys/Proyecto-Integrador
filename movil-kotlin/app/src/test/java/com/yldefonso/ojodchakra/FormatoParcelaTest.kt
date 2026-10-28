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
}