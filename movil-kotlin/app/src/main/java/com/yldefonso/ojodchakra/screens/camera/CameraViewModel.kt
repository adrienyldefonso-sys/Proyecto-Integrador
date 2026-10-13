package com.yldefonso.ojodchakra.screens.camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.yldefonso.ojodchakra.ml.ClassificationResult

class CameraViewModel : ViewModel() {

    private val _capturedBitmap = mutableStateOf<Bitmap?>(null)
    val capturedBitmap: State<Bitmap?> = _capturedBitmap

    private val _result = mutableStateOf<ClassificationResult?>(null)
    val result: State<ClassificationResult?> = _result

    fun onPhotoCaptured(bitmap: Bitmap) {
        _capturedBitmap.value = bitmap
    }

    fun onResultReady(result: ClassificationResult) {
        _result.value = result
    }

    fun reset() {
        _capturedBitmap.value = null
        _result.value = null
    }
}