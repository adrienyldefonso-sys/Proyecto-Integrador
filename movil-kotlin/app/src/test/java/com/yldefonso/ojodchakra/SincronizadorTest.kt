package com.yldefonso.ojodchakra

import com.yldefonso.ojodchakra.data.model.Diagnostico
import com.yldefonso.ojodchakra.data.model.Parcela
import com.yldefonso.ojodchakra.local.SincronizacionDao
import com.yldefonso.ojodchakra.red.ClienteApi
import com.yldefonso.ojodchakra.red.ResultadoEnvio
import com.yldefonso.ojodchakra.sincronizacion.Sincronizador
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SincronizadorTest {

    private class DaoFalso(
        val parcelas: List<Parcela> = emptyList(),
        val diagnosticos: List<Diagnostico> = emptyList()
    ) : SincronizacionDao {
        val parcelasMarcadas = mutableListOf<String>()
        val diagnosticosMarcados = mutableListOf<String>()

        override suspend fun parcelasPendientes(): List<Parcela> = parcelas
        override suspend fun diagnosticosPendientes(): List<Diagnostico> = diagnosticos
        override suspend fun marcarParcelaSincronizada(id: String) { parcelasMarcadas += id }
        override suspend fun marcarDiagnosticoSincronizado(id: String) { diagnosticosMarcados += id }
        override fun contarParcelasPendientes(): Flow<Int> = flowOf(parcelas.size)
        override fun contarDiagnosticosPendientes(): Flow<Int> = flowOf(diagnosticos.size)
    }

    private class ClienteFalso(
        val alParcela: (Parcela) -> ResultadoEnvio = { ResultadoEnvio.Exito },
        val alDiagnostico: (Diagnostico) -> ResultadoEnvio = { ResultadoEnvio.Exito }
    ) : ClienteApi {
        val llamadas = mutableListOf<String>()

        override suspend fun enviarParcela(parcela: Parcela): ResultadoEnvio {
            llamadas += "parcela:${parcela.id}"
            return alParcela(parcela)
        }

        override suspend fun enviarDiagnostico(diagnostico: Diagnostico): ResultadoEnvio {
            llamadas += "diagnostico:${diagnostico.id}"
            return alDiagnostico(diagnostico)
        }
    }

    private fun parcela(id: String) = Parcela(
        id = id, usuarioId = "usuario_local", nombre = "Parcela $id",
        cultivo = "Papa", latitud = -12.0, longitud = -77.0
    )

    private fun diagnostico(id: String, parcelaId: String? = null) = Diagnostico(
        id = id, usuarioId = "usuario_local", parcelaId = parcelaId,
        rutaFoto = "/x/$id.jpg", plagaDetectada = "Potato___Late_blight", confianza = 97.5f
    )

    @Test
    fun enviaLasParcelasAntesQueLosDiagnosticosYLosMarcaSincronizados() = runBlocking<Unit> {
        val dao = DaoFalso(listOf(parcela("p1")), listOf(diagnostico("d1", "p1")))
        val cliente = ClienteFalso()

        val resumen = Sincronizador(dao, cliente).sincronizar()

        assertEquals(listOf("parcela:p1", "diagnostico:d1"), cliente.llamadas)
        assertEquals(listOf("p1"), dao.parcelasMarcadas)
        assertEquals(listOf("d1"), dao.diagnosticosMarcados)
        assertEquals(1, resumen.parcelasEnviadas)
        assertEquals(1, resumen.diagnosticosEnviados)
    }

    @Test
    fun sinConexionCortaElEnvioYNoMarcaNada() = runBlocking<Unit> {
        val dao = DaoFalso(listOf(parcela("p1"), parcela("p2")), listOf(diagnostico("d1")))
        val cliente = ClienteFalso(alParcela = { ResultadoEnvio.SinConexion("sin red") })

        val resumen = Sincronizador(dao, cliente).sincronizar()

        assertEquals(listOf("parcela:p1"), cliente.llamadas)
        assertTrue(dao.parcelasMarcadas.isEmpty())
        assertTrue(dao.diagnosticosMarcados.isEmpty())
        assertTrue(resumen.sinConexion)
    }

    @Test
    fun unaParcelaRechazadaNoEnviaSusDiagnosticosPeroSiLosDemas() = runBlocking<Unit> {
        val dao = DaoFalso(listOf(parcela("p1")), listOf(diagnostico("d1", "p1"), diagnostico("d2")))
        val cliente = ClienteFalso(alParcela = { ResultadoEnvio.Rechazado("usuario inexistente") })

        val resumen = Sincronizador(dao, cliente).sincronizar()

        assertEquals(listOf("parcela:p1", "diagnostico:d2"), cliente.llamadas)
        assertEquals(listOf("d2"), dao.diagnosticosMarcados)
        assertEquals(2, resumen.rechazados)
        assertEquals("usuario inexistente", resumen.primerError)
    }

    @Test
    fun unDiagnosticoRechazadoNoImpideEnviarLosSiguientes() = runBlocking<Unit> {
        val dao = DaoFalso(diagnosticos = listOf(diagnostico("d1"), diagnostico("d2")))
        val cliente = ClienteFalso(alDiagnostico = {
            if (it.id == "d1") ResultadoEnvio.Rechazado("foto inválida") else ResultadoEnvio.Exito
        })

        val resumen = Sincronizador(dao, cliente).sincronizar()

        assertEquals(listOf("d2"), dao.diagnosticosMarcados)
        assertEquals(1, resumen.rechazados)
        assertEquals(1, resumen.diagnosticosEnviados)
    }

    @Test
    fun sinNadaPendienteNoLlamaAlCliente() = runBlocking<Unit> {
        val cliente = ClienteFalso()

        val resumen = Sincronizador(DaoFalso(), cliente).sincronizar()

        assertTrue(cliente.llamadas.isEmpty())
        assertEquals(0, resumen.totalEnviados)
        assertNull(resumen.primerError)
    }
}