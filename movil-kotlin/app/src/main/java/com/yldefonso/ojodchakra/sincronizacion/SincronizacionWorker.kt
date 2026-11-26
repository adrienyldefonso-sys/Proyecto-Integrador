package com.yldefonso.ojodchakra.sincronizacion

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yldefonso.ojodchakra.local.AppDatabase
import com.yldefonso.ojodchakra.red.ClienteApiHttp
import com.yldefonso.ojodchakra.sesion.SesionUsuario

/**
 * Sincroniza en segundo plano, aunque la app esté cerrada.
 * Si no hay señal o el servidor falla, WorkManager reintenta más tarde.
 */
class SincronizacionWorker(
    contexto: Context,
    parametros: WorkerParameters
) : CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result {
        // El trabajo puede arrancar sin que se abra la app: hay que leer la sesión guardada
        SesionUsuario.cargar(applicationContext)
        // Invitado o sin cuenta: no hay a qué enviar los datos. Se intenta de nuevo cuando inicie sesión.
        if (!SesionUsuario.cuentaActiva) return Result.success()
        return try {
            val dao = AppDatabase.getInstance(applicationContext).sincronizacionDao()
            val resumen = Sincronizador(dao, ClienteApiHttp()).sincronizar()
            if (resumen.sinConexion) Result.retry() else Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
