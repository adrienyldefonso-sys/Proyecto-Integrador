package com.yldefonso.ojodchakra

import com.yldefonso.ojodchakra.util.ValidadorParcela
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorParcelaTest {

    @Test
    fun datosValidosNoGeneranErrores() {
        val e = ValidadorParcela.validar("Parcela Norte", "Papa", "-12.0464", "-77.0428")
        assertFalse(e.hayErrores)
    }

    @Test
    fun nombreVacioGeneraError() {
        val e = ValidadorParcela.validar("   ", "Papa", "-12.0", "-77.0")
        assertNotNull(e.nombre)
    }

    @Test
    fun nombreMuyLargoGeneraError() {
        val e = ValidadorParcela.validar("a".repeat(101), "Papa", "-12.0", "-77.0")
        assertNotNull(e.nombre)
    }

    @Test
    fun coordenadaConComaSeAcepta() {
        assertEquals(-12.5, ValidadorParcela.parsearCoordenada("-12,5")!!, 0.0001)
    }

    @Test
    fun latitudFueraDeRangoGeneraError() {
        val e = ValidadorParcela.validar("Parcela", "Papa", "95", "-77.0")
        assertNotNull(e.latitud)
    }

    @Test
    fun longitudNoNumericaGeneraError() {
        val e = ValidadorParcela.validar("Parcela", "Papa", "-12.0", "abc")
        assertNotNull(e.longitud)
    }

    @Test
    fun textoNaNNoEsCoordenadaValida() {
        val e = ValidadorParcela.validar("Parcela", "Papa", "NaN", "-77.0")
        assertTrue(e.latitud != null)
    }
}