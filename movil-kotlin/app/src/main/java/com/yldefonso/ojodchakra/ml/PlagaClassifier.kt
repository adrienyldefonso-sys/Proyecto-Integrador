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
    private val inputSize = 256

    init {
        val model = FileUtil.loadMappedFile(context, "model.tflite")
        interpreter = Interpreter(model)
        labels = FileUtil.loadLabels(context, "labels.txt")
    }

    fun classify(bitmap: Bitmap): ClassificationResult {
        // 1. Recortar el centro de la imagen en formato cuadrado para eliminar bordes/mesas
        val squareBitmap = cropCenterSquare(bitmap)

        // 2. Redimensionar el centro recortado a 256x256
        val resized = Bitmap.createScaledBitmap(squareBitmap, inputSize, inputSize, true)
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

    // Recorta el cuadrado central de la foto eliminando los bordes laterales/superiores
    private fun cropCenterSquare(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val newSize = if (height > width) width else height

        val cropX = (width - newSize) / 2
        val cropY = (height - newSize) / 2

        return Bitmap.createBitmap(bitmap, cropX, cropY, newSize, newSize)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputSize * inputSize)
        bitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

        // Preprocesamiento flotante puro (0.0f a 255.0f)
        for (pixel in pixels) {
            val r = ((pixel shr 16) and 0xFF).toFloat()
            val g = ((pixel shr 8) and 0xFF).toFloat()
            val b = (pixel and 0xFF).toFloat()

            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }
        return byteBuffer
    }
}