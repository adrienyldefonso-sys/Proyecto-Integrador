package com.yldefonso.ojodchakra.screens.sincronizacion

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yldefonso.ojodchakra.local.AppDatabase
import com.yldefonso.ojodchakra.red.ClienteApiHttp
import com.yldefonso.ojodchakra.sincronizacion.Sincronizador
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

data class EstadoSincronizacion(
    val sincronizando: Boolean = false,
    val mensaje: String? = null
)

class SincronizacionViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).sincronizacionDao()
    private val sincronizador = Sincronizador(dao, ClienteApiHttp())
    private val bloqueo = Mutex()

    val pendientes: StateFlow<Int> = combine(
        dao.contarParcelasPendientes(),
        dao.contarDiagnosticosPendientes()
    ) { parcelas, diagnosticos -> parcelas + diagnosticos }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _estado = MutableStateFlow(EstadoSincronizacion())
    val estado: StateFlow<EstadoSincronizacion> = _estado.asStateFlow()

    private val conectividad =
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    // Se dispara al abrir la app y cada vez que vuelve la señal
    private val alHaberRed = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            sincronizar()
        }
    }

    init {
        try {
            conectividad?.registerDefaultNetworkCallback(alHaberRed)
        } catch (e: RuntimeException) {
            // Sin el aviso automático, el botón manual sigue funcionando
        }
    }

    fun sincronizar() {
        if (!bloqueo.tryLock()) return // ya hay una sincronización en curso
        viewModelScope.launch {
            try {
                _estado.update { it.copy(sincronizando = true) }
                val resumen = sincronizador.sincronizar()
                _estado.value = EstadoSincronizacion(false, resumen.texto())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.value = EstadoSincronizacion(
                    false, "No se pudo sincronizar: ${e.message ?: "error inesperado"}"
                )
            } finally {
                bloqueo.unlock()
            }
        }
    }

    override fun onCleared() {
        try {
            conectividad?.unregisterNetworkCallback(alHaberRed)
        } catch (e: IllegalArgumentException) {
            // Ya estaba sin registrar
        }
        super.onCleared()
    }
}