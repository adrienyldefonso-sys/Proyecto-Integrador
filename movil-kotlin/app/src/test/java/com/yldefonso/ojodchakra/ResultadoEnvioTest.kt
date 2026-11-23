package com.yldefonso.ojodchakra

import com.yldefonso.ojodchakra.red.ResultadoEnvio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultadoEnvioTest {

    private fun clasificar(codigo: Int) = ResultadoEnvio.desdeCodigo(codigo) { "detalle" }

    @Test
    fun creadoYYaExistenteSonExito() {
        assertEquals(ResultadoEnvio.Exito, clasificar(201))
        assertEquals(ResultadoEnvio.Exito, clasificar(200))
    }

    @Test
    fun losErroresDelClienteSeRechazan() {
        assertEquals(ResultadoEnvio.Rechazado("detalle"), clasificar(400))
        assertEquals(ResultadoEnvio.Rechazado("detalle"), clasificar(401))
        assertEquals(ResultadoEnvio.Rechazado("detalle"), clasificar(409))
    }

    @Test
    fun losErroresDelServidorSeReintentan() {
        assertTrue(clasificar(500) is ResultadoEnvio.SinConexion)
        assertTrue(clasificar(503) is ResultadoEnvio.SinConexion)
    }
}