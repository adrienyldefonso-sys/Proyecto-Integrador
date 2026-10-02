package com.yldefonso.ojodchakra.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PlagaClassifier(context: Context) {

    private var interpreter: Interpreter
    private var labels: List<String>
    private val inputSize = 224

    init {
        val model = FileUtil.loadMappedFile(context, "model.tflite")
        interpreter = Interpreter(model)
        labels = FileUtil.loadLabels(context, "labels.txt")
    }

    fun classify(bitmap: Bitmap): ClassificationResult {
        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val inputBuffer = convertBitmapToByteBuffer(resized)

        val output = Array(1) { FloatArray(labels.size) }
        interpreter.run(inputBuffer, output)

        val probabilities = output[0]
        var maxIndex = 0
        for (i in probabilities.indices) {
            if (probabilities[i] > probabilities[maxIndex]) maxIndex = i
        }

        return ClassificationResult(
            label = labels[maxIndex],
            confidence = probabilities[maxIndex] * 100
        )
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputSize * inputSize)
        bitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

        for (pixel in pixels) {
            byteBuffer.putFloat(((pixel shr 16) and 0xFF) / 255.0f)
            byteBuffer.putFloat(((pixel shr 8) and 0xFF) / 255.0f)
            byteBuffer.putFloat((pixel and 0xFF) / 255.0f)
        }
        return byteBuffer
    }
}