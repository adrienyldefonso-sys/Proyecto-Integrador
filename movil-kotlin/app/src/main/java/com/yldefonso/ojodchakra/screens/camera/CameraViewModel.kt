package com.yldefonso.ojodchakra.screens.camera

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yldefonso.ojodchakra.data.model.Diagnostico
import com.yldefonso.ojodchakra.data.repository.DiagnosticoRepository
import com.yldefonso.ojodchakra.ml.ClassificationResult
import com.yldefonso.ojodchakra.repository.ParcelaRepository
import com.yldefonso.ojodchakra.sincronizacion.ProgramadorSincronizacion
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DiagnosticoRepository(application)
    private val parcelaRepository = ParcelaRepository(application)

    private val _capturedBitmap = mutableStateOf<Bitmap?>(null)
    val capturedBitmap: State<Bitmap?> = _capturedBitmap

    private val _result = mutableStateOf<ClassificationResult?>(null)
    val result: State<ClassificationResult?> = _result

    // Aviso cuando la foto tiene mala calidad o no se identificó con seguridad
    private val _mensajeRechazo = mutableStateOf<String?>(null)
    val mensajeRechazo: State<String?> = _mensajeRechazo

    // Parcela elegida antes de escanear. null significa escaneo sin parcela.
    private var parcelaId: String? = null
    private val _parcelaNombre = mutableStateOf<String?>(null)
    val parcelaNombre: State<String?> = _parcelaNombre

    // Diagnóstico guardado en este análisis
    private val _diagnosticoId = mutableStateOf<String?>(null)
    val diagnosticoId: State<String?> = _diagnosticoId

    // Copia en Room del diagnóstico: se actualiza sola cuando cambia la revisión del agrónomo
    private val _diagnosticoGuardado = mutableStateOf<Diagnostico?>(null)
    val diagnosticoGuardado: State<Diagnostico?> = _diagnosticoGuardado

    private var observacion: Job? = null

    fun seleccionarParcela(id: String?) {
        parcelaId = id
        _parcelaNombre.value = null
        _mensajeRechazo.value = null
        if (id != null) {
            viewModelScope.launch {
                val nombre = parcelaRepository.obtenerPorId(id)?.nombre
                if (parcelaId == id) _parcelaNombre.value = nombre
            }
        }
    }

    fun onPhotoCaptured(bitmap: Bitmap) {
        _capturedBitmap.value = bitmap
    }

    fun onResultReady(result: ClassificationResult) {
        _result.value = result
        val bitmap = _capturedBitmap.value ?: return
        val parcela = parcelaId
        viewModelScope.launch {
            val id = repository.guardar(bitmap, result, parcela)
            _diagnosticoId.value = id
            observarDiagnostico(id)
            ProgramadorSincronizacion.sincronizarAhora(getApplication())
        }
    }

    private fun observarDiagnostico(id: String) {
        observacion?.cancel()
        observacion = viewModelScope.launch {
            repository.observarPorId(id).collect { _diagnosticoGuardado.value = it }
        }
    }

    // El agricultor no decide la plaga: solo pide que un agrónomo lo revise
    fun solicitarRevision() {
        val id = _diagnosticoId.value ?: return
        viewModelScope.launch { repository.solicitarRevision(id) }
    }

    fun onRechazada(mensaje: String) {
        _capturedBitmap.value = null
        _result.value = null
        _mensajeRechazo.value = mensaje
    }

    fun limpiarRechazo() {
        _mensajeRechazo.value = null
    }

    // Limpia todo menos la parcela, que se conserva para "Escanear otra vez"
    fun reset() {
        observacion?.cancel()
        observacion = null
        _capturedBitmap.value = null
        _result.value = null
        _mensajeRechazo.value = null
        _diagnosticoId.value = null
        _diagnosticoGuardado.value = null
    }
}
