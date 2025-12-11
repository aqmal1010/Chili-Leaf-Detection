package com.example.chilidetectionapp.helper

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import java.nio.MappedByteBuffer

class Classifier(context: Context) {

    private var interpreter: Interpreter
    private val inputImageSize = 224
    private val numClasses = 5
    private val confidenceTreshold = 0.6f

    private val labels = listOf(
        "Bercak Daun", "Bukan Daun Cabai", "Daun Sehat", "Keriting Daun", "Virus Kuning"
    )

    init {
        val model: MappedByteBuffer = FileUtil.loadMappedFile(context, "model_mobilenetv2.tflite")
        val options = Interpreter.Options().apply {
            setNumThreads(4)
        }
        interpreter = Interpreter(model, options)
        Log.d("Classifier", "Model MobileNetV2 berhasil dimuat")
    }

    fun classifyImage(bitmap: Bitmap): Pair<String, Float?> {
        val tensorImage = preprocessImage(bitmap)
        val outputArray = Array(1) { FloatArray(numClasses) }

        return try {
            interpreter.run(tensorImage.buffer, outputArray)
            val probabilities = outputArray[0]
            val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1
            val confidence = probabilities.getOrNull(maxIndex) ?: 0f
            val rawLabel = labels[maxIndex]

            probabilities.forEachIndexed { index, score ->
                Log.d("Classifier", "Kelas ${labels[index]}: $score")
            }

            // Top-3 log (debug only)
            val top3 = probabilities.mapIndexed { index, value -> index to value }
                .sortedByDescending { it.second }
                .take(3)

            top3.forEachIndexed { rank, (i, conf) ->
                Log.d("Classifier", "Top-${rank + 1}: ${labels[i]} (Confidence: $conf)")
            }

            // Kasus 1: confidence rendah
            if (confidence < confidenceTreshold) {
                Log.d("Classifier", "Confidence rendah (< 0.5). Output dianggap tidak diketahui.")
                return Pair("Bukan Daun Cabai", null)
            }

            // Kasus 2: model prediksi "unknown"
            if (rawLabel.lowercase() == "unknown") {
                Log.d("Classifier", "Model memprediksi unknown. Output akan ditampilkan sebagai 'Bukan Daun Cabai'.")
                return Pair("Bukan Daun Cabai", null)
            }

            // Kasus 3: prediksi normal
            Log.d("Classifier", "Prediksi: $rawLabel, Confidence: $confidence")
            return Pair(rawLabel, confidence)

        } catch (e: Exception) {
            Log.e("Classifier", "Terjadi kesalahan saat inferensi: ${e.message}")
            return Pair("Bukan Daun Cabai", null)
        }
    }

    private fun preprocessImage(bitmap: Bitmap): TensorImage {
        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(inputImageSize, inputImageSize, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 255f))
            .build()

        return imageProcessor.process(tensorImage)
    }

    fun close() {
        interpreter.close()
    }
}