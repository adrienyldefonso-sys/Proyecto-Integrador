package com.yldefonso.ojodchakra.util

import org.junit.Assert.assertEquals
import org.junit.Test

class SeveridadMapaTest {

    @Test
    fun severidadAltaEsRoja() {
        assertEquals(SeveridadMapa.HUE_ROJO, SeveridadMapa.hue("ALTA"), 0.001f)
    }

    @Test
    fun severidadMediaEsNaranja() {
        assertEquals(SeveridadMapa.HUE_NARANJA, SeveridadMapa.hue("MEDIA"), 0.001f)
    }

    @Test
    fun sinPlagaEsVerde() {
        assertEquals(SeveridadMapa.HUE_VERDE, SeveridadMapa.hue("NINGUNA"), 0.001f)
    }

    @Test
    fun sinDiagnosticoOValorDesconocidoUsaAzul() {
        assertEquals(SeveridadMapa.HUE_SIN_DATOS, SeveridadMapa.hue(null), 0.001f)
        assertEquals(SeveridadMapa.HUE_SIN_DATOS, SeveridadMapa.hue("otra"), 0.001f)
    }

    @Test
    fun ignoraMayusculasYEtiquetaEsLegible() {
        assertEquals(SeveridadMapa.HUE_ROJO, SeveridadMapa.hue("alta"), 0.001f)
        assertEquals("Sin diagnóstico", SeveridadMapa.etiqueta(null))
    }
}
