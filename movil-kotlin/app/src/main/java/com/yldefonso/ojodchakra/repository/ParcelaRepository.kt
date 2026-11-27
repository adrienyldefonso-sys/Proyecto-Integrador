package com.yldefonso.ojodchakra.repository

import android.content.Context
import com.yldefonso.ojodchakra.data.model.Parcela
import com.yldefonso.ojodchakra.data.model.ParcelaConConteo
import com.yldefonso.ojodchakra.data.model.ParcelaEnMapa
import com.yldefonso.ojodchakra.local.AppDatabase
import com.yldefonso.ojodchakra.sesion.SesionUsuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

class ParcelaRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context.applicationContext).parcelaDao()

    // Cada vez que cambia la cuenta, se vuelve a consultar solo lo de esa cuenta
    val parcelas: Flow<List<ParcelaConConteo>> =
        SesionUsuario.propietario.flatMapLatest { dao.observarConConteo(it) }

    val parcelasEnMapa: Flow<List<ParcelaEnMapa>> =
        SesionUsuario.propietario.flatMapLatest { dao.observarParaMapa(it) }

    suspend fun obtenerPorId(id: String): Parcela? = dao.obtenerPorId(id)

    // Las coordenadas son opcionales: null significa "sin ubicación por ahora"
    suspend fun registrar(
        nombre: String,
        cultivo: String,
        referencia: String,
        latitud: Double?,
        longitud: Double?
    ): String {
        val parcela = Parcela(
            usuarioId = SesionUsuario.propietarioActual,
            nombre = nombre.trim(),
            cultivo = cultivo.trim(),
            referencia = referencia.trim(),
            latitud = latitud,
            longitud = longitud
        )
        dao.insertar(parcela)
        return parcela.id
    }

}
