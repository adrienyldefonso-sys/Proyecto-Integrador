package com.yldefonso.ojodchakra.sincronizacion

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ProgramadorSincronizacion {

    private const val TRABAJO_PERIODICO = "sincronizacion-periodica"
    private const val TRABAJO_INMEDIATO = "sincronizacion-inmediata"

    // Solo se intenta cuando hay conexión; así no se gasta batería sin señal
    private val conRed = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /** Revisa cada 15 minutos (el mínimo de Android) si hay algo que enviar o alguna revisión nueva. */
    fun programarPeriodica(contexto: Context) {
        val trabajo = PeriodicWorkRequestBuilder<SincronizacionWorker>(15, TimeUnit.MINUTES)
            .setConstraints(conRed)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(contexto).enqueueUniquePeriodicWork(
            TRABAJO_PERIODICO,
            ExistingPeriodicWorkPolicy.KEEP,
            trabajo
        )
    }

    /** Envía un diagnóstico recién guardado sin esperar al siguiente ciclo. */
    fun sincronizarAhora(contexto: Context) {
        val trabajo = OneTimeWorkRequestBuilder<SincronizacionWorker>()
            .setConstraints(conRed)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(contexto).enqueueUniqueWork(
            TRABAJO_INMEDIATO,
            ExistingWorkPolicy.KEEP,
            trabajo
        )
    }
}
