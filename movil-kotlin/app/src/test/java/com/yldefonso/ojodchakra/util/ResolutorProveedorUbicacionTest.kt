package com.yldefonso.ojodchakra.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolutorProveedorUbicacionTest {

    @Test
    fun conInternetYTodoActivo_primeroRedLuegoGps() {
        assertEquals(
            listOf(ProveedorUbicacion.RED, ProveedorUbicacion.GPS),
            ResolutorProveedorUbicacion.candidatos(true, true, true, hayInternet = true)
        )
    }

    @Test
    fun sinInternetConGps_usaSoloGps() {
        assertEquals(
            listOf(ProveedorUbicacion.GPS),
            ResolutorProveedorUbicacion.candidatos(true, true, true, hayInternet = false)
        )
    }

    @Test
    fun sinInternetYSinGps_noHayCandidatos() {
        assertEquals(
            emptyList<ProveedorUbicacion>(),
            ResolutorProveedorUbicacion.candidatos(true, false, true, hayInternet = false)
        )
    }

    @Test
    fun sinInternetConSoloAproximada_noHayCandidatos() {
        // Con ubicación aproximada no se puede usar el GPS, y sin internet no hay red
        assertEquals(
            emptyList<ProveedorUbicacion>(),
            ResolutorProveedorUbicacion.candidatos(false, true, true, hayInternet = false)
        )
    }

    @Test
    fun conInternetSinGpsUsaRed() {
        assertEquals(
            listOf(ProveedorUbicacion.RED),
            ResolutorProveedorUbicacion.candidatos(true, false, true, hayInternet = true)
        )
    }

    @Test
    fun conInternetSoloAproximadaUsaRed() {
        assertEquals(
            listOf(ProveedorUbicacion.RED),
            ResolutorProveedorUbicacion.candidatos(false, true, true, hayInternet = true)
        )
    }

    @Test
    fun conInternetSinNingunProveedor_noHayCandidatos() {
        assertEquals(
            emptyList<ProveedorUbicacion>(),
            ResolutorProveedorUbicacion.candidatos(true, false, false, hayInternet = true)
        )
    }

    @Test
    fun tiemposDeEsperaSonMasLargosSinInternet() {
        assertEquals(60_000L, ResolutorProveedorUbicacion.tiempoEsperaMs(ProveedorUbicacion.GPS, hayInternet = false))
        assertEquals(30_000L, ResolutorProveedorUbicacion.tiempoEsperaMs(ProveedorUbicacion.GPS, hayInternet = true))
        assertEquals(10_000L, ResolutorProveedorUbicacion.tiempoEsperaMs(ProveedorUbicacion.RED, hayInternet = true))
    }

    @Test
    fun cadaFalloTieneMensajeNoVacio() {
        FalloUbicacion.values().forEach { fallo ->
            assertTrue("Mensaje vacío para $fallo", fallo.mensaje.isNotBlank())
        }
    }
}
