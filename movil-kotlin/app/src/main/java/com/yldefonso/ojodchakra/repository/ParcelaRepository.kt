package com.yldefonso.ojodchakra.repository

import android.content.Context
import com.yldefonso.ojodchakra.data.model.Parcela
import com.yldefonso.ojodchakra.data.model.ParcelaConConteo
import com.yldefonso.ojodchakra.local.AppDatabase
import kotlinx.coroutines.flow.Flow

class ParcelaRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context.applicationContext).parcelaDao()

    val parcelas: Flow<List<ParcelaConConteo>> = dao.observarConConteo(USUARIO_LOCAL)

    suspend fun obtenerPorId(id: String): Parcela? = dao.obtenerPorId(id)

    suspend fun registrar(
        nombre: String,
        cultivo: String,
        referencia: String,
        latitud: Double,
        longitud: Double
    ): String {
        val parcela = Parcela(
            usuarioId = USUARIO_LOCAL,
            nombre = nombre.trim(),
            cultivo = cultivo.trim(),
            referencia = referencia.trim(),
            latitud = latitud,
            longitud = longitud
        )
        dao.insertar(parcela)
        return parcela.id
    }

    companion object {
        // Provisional: mismo valor que DiagnosticoRepository, se reemplaza con el inicio de sesión
        const val USUARIO_LOCAL = "usuario_local"
    }
}