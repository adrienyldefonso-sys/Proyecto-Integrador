package com.yldefonso.ojodchakra

import com.yldefonso.ojodchakra.sincronizacion.ResumenSincronizacion
import com.yldefonso.ojodchakra.sincronizacion.textoPendientes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResumenSincronizacionTest {

    @Test
    fun sinNadaPendienteLoDice() {
        assertEquals("No hay nada pendiente de enviar.", ResumenSincronizacion().texto())
    }

    @Test
    fun usaSingularYPluralSegunLaCantidad() {
        val resumen = ResumenSincronizacion(parcelasEnviadas = 1, diagnosticosEnviados = 3)
        assertEquals("Enviado: 1 parcela y 3 reportes.", resumen.texto())
    }

    @Test
    fun sinConexionExplicaQueSeReintentara() {
        assertTrue(ResumenSincronizacion(sinConexion = true).texto().contains("Se reintentará"))
    }

    @Test
    fun losRechazosMuestranElMotivoDelServidor() {
        val resumen = ResumenSincronizacion(rechazados = 2, primerError = "El usuario no existe")
        assertTrue(resumen.texto().contains("2 elementos"))
        assertTrue(resumen.texto().contains("El usuario no existe"))
    }

    @Test
    fun textoDePendientes() {
        assertEquals("Todo está enviado.", textoPendientes(0))
        assertEquals("1 elemento pendiente de enviar.", textoPendientes(1))
        assertEquals("4 elementos pendientes de enviar.", textoPendientes(4))
    }
}