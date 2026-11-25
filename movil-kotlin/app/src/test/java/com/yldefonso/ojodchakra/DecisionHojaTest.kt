package com.yldefonso.ojodchakra

import com.yldefonso.ojodchakra.ml.ClassificationResult
import com.yldefonso.ojodchakra.ml.DecisionHoja
import com.yldefonso.ojodchakra.ml.DecisionHoja.MotivoRechazo
import com.yldefonso.ojodchakra.ml.DecisionHoja.Veredicto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DecisionHojaTest {

    private fun resultado(etiqueta: String, confianza: Float) =
        ClassificationResult(label = etiqueta, confidence = confianza)

    @Test
    fun unaHojaConConfianzaAltaSeAcepta() {
        val veredicto = DecisionHoja.evaluar(resultado("Potato___Late_blight", 97f))
        assertTrue(veredicto is Veredicto.Aceptada)
    }

    @Test
    fun unaConfianzaBajaSeRechaza() {
        val veredicto = DecisionHoja.evaluar(resultado("Potato___healthy", 55f))
        assertEquals(Veredicto.Rechazada(MotivoRechazo.CONFIANZA_BAJA), veredicto)
    }

    @Test
    fun laEtiquetaDeRechazoSeRechazaAunqueTengaConfianzaAlta() {
        val veredicto = DecisionHoja.evaluar(resultado(DecisionHoja.ETIQUETA_RECHAZO, 99f))
        assertEquals(Veredicto.Rechazada(MotivoRechazo.NO_ES_HOJA), veredicto)
    }

    @Test
    fun unaConfianzaIgualAlUmbralSeAcepta() {
        val veredicto = DecisionHoja.evaluar(
            resultado("Potato___healthy", DecisionHoja.UMBRAL_CONFIANZA)
        )
        assertTrue(veredicto is Veredicto.Aceptada)
    }

    @Test
    fun elUmbralSePuedeCambiar() {
        val veredicto = DecisionHoja.evaluar(resultado("Potato___healthy", 80f), umbral = 90f)
        assertEquals(Veredicto.Rechazada(MotivoRechazo.CONFIANZA_BAJA), veredicto)
    }

    @Test
    fun elMensajeIncluyeElMotivoYElConsejo() {
        val mensaje = DecisionHoja.mensaje(MotivoRechazo.NO_ES_HOJA)
        assertTrue(mensaje.contains("No se detectó una hoja"))
        assertTrue(mensaje.contains("Acércate"))
    }
}