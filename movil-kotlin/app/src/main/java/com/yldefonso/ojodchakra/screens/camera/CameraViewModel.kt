package com.yldefonso.ojodchakra.screens.camera

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yldefonso.ojodchakra.data.repository.DiagnosticoRepository
import com.yldefonso.ojodchakra.ml.ClassificationResult
import kotlinx.coroutines.launch

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DiagnosticoRepository(application)

    private val _capturedBitmap = mutableStateOf<Bitmap?>(null)
    val capturedBitmap: State<Bitmap?> = _capturedBitmap

    private val _result = mutableStateOf<ClassificationResult?>(null)
    val result: State<ClassificationResult?> = _result

    fun onPhotoCaptured(bitmap: Bitmap) {
        _capturedBitmap.value = bitmap
    }

    fun onResultReady(result: ClassificationResult) {
        _result.value = result
        val bitmap = _capturedBitmap.value ?: return
        viewModelScope.launch { repository.guardar(bitmap, result) }
    }

    fun reset() {
        _capturedBitmap.value = null
        _result.value = null
    }
}