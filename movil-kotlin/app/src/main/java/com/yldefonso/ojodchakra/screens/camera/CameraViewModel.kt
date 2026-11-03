package com.yldefonso.ojodchakra.screens.camera

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yldefonso.ojodchakra.data.repository.DiagnosticoRepository
import com.yldefonso.ojodchakra.ml.ClassificationResult
import com.yldefonso.ojodchakra.repository.ParcelaRepository
import kotlinx.coroutines.launch

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DiagnosticoRepository(application)
    private val parcelaRepository = ParcelaRepository(application)

    private val _capturedBitmap = mutableStateOf<Bitmap?>(null)
    val capturedBitmap: State<Bitmap?> = _capturedBitmap

    private val _result = mutableStateOf<ClassificationResult?>(null)
    val result: State<ClassificationResult?> = _result

    // Parcela elegida antes de escanear. null significa escaneo sin parcela.
    private var parcelaId: String? = null
    private val _parcelaNombre = mutableStateOf<String?>(null)
    val parcelaNombre: State<String?> = _parcelaNombre

    fun seleccionarParcela(id: String?) {
        parcelaId = id
        _parcelaNombre.value = null
        if (id != null) {
            viewModelScope.launch {
                val nombre = parcelaRepository.obtenerPorId(id)?.nombre
                // Solo se aplica si sigue siendo la parcela elegida
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
        viewModelScope.launch { repository.guardar(bitmap, result, parcela) }
    }

    // Limpia la foto y el resultado. La parcela se conserva para "Escanear otra vez".
    fun reset() {
        _capturedBitmap.value = null
        _result.value = null
    }
}