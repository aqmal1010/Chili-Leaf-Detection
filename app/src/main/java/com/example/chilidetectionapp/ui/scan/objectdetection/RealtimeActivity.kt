package com.example.chilidetectionapp.ui.scan.objectdetection

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.chilidetectionapp.databinding.ActivityRealtimeBinding
import com.example.chilidetectionapp.helper.Classifier
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RealtimeActivity: AppCompatActivity() {
    private lateinit var binding: ActivityRealtimeBinding
    private lateinit var previewView: PreviewView
    private lateinit var resultText: TextView
    private lateinit var btnAnalyze: FloatingActionButton
    private lateinit var classifier: Classifier
    private lateinit var cameraExecutor: ExecutorService
    private var lastAnalyzedTime = 0L
    private val ANALYZE_INTERVAL_MS = 200L // jeda antar prediksi 1 detik
    private var lastStableLabel: String? = null


    private var isCameraFrozen = false
    private var latestBitmap: Bitmap? = null
    private var bottomSheetDialog: BottomSheetDialog? = null

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalyzer: ImageAnalysis? = null

    private var isActivityActive = true

    private val predictionBuffer = ArrayDeque<Pair<String, Float>>(5)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRealtimeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        previewView = binding.previewView
        resultText = binding.resultText
        btnAnalyze = binding.btnAnalyze

        classifier = Classifier(this)
        cameraExecutor = Executors.newSingleThreadExecutor()

        btnAnalyze.isEnabled = false

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        btnAnalyze.setOnClickListener {
            if (!isCameraFrozen && latestBitmap != null && predictionBuffer.isNotEmpty()) {
                isCameraFrozen = true

                val (label, confidence) = getStablePrediction()
                val confidencePct = confidence.let { "%.2f%%".format(it * 100) }

                resultText.text = if (label == "Penyakit Tidak Diketahui") label else "$label ($confidencePct)"

                val bottomSheet = SuggestionBottomSheet.newInstance(label)
                bottomSheet.dismissListener = object : SuggestionBottomSheet.OnDismissListener {
                    override fun onDismissed() {
                        runOnUiThread {
                            isCameraFrozen = false
                            predictionBuffer.clear()
                            resultText.text = "Mendeteksi..."
                            btnAnalyze.isEnabled = false
                        }
                    }
                }
                bottomSheet.show(supportFragmentManager, bottomSheet.tag)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startCamera()
        else Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
    }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(baseContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageAnalyzer = ImageAnalysis.Builder()
                .setTargetResolution(Size(224, 224))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->
                        processImageProxy(imageProxy)
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (e: Exception) {
                Log.e("RealTime", "Gagal bind kamera: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImageProxy(imageProxy: ImageProxy) {
        if (!isActivityActive) {
            imageProxy.close()
            return
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAnalyzedTime < ANALYZE_INTERVAL_MS) {
            imageProxy.close()
            return
        }
        lastAnalyzedTime = currentTime

        if (!isCameraFrozen) {
            val bitmap = imageProxy.toBitmap()
            if (bitmap != null) {
                latestBitmap = bitmap
                val (label, confidence) = classifier.classifyImage(bitmap)

                synchronized(predictionBuffer) {
                    if (predictionBuffer.size >= 5) predictionBuffer.removeFirst()
                    predictionBuffer.addLast(label to (confidence ?: 0f))
                }

                val (stableLabel, stableConfidence) = getStablePrediction()

                runOnUiThread {
                    if (stableLabel == "Penyakit Tidak Diketahui") {
                        resultText.text = "Penyakit Tidak Diketahui"
                        btnAnalyze.isEnabled = false
                    } else {
                        val confText = "%.2f%%".format(stableConfidence * 100)
                        resultText.text = "$stableLabel ($confText)"
                        btnAnalyze.isEnabled = true
                    }
                }

            }
        }

        imageProxy.close()
    }


    private fun getStablePrediction(): Pair<String, Float> {
        val counts = predictionBuffer.groupingBy { it.first }.eachCount()
        val mostCommon = counts.maxByOrNull { it.value }?.key ?: "Tanaman Tidak Diketahui"
        val averageConfidence = predictionBuffer
            .filter { it.first == mostCommon }
            .map { it.second }
            .average()
            .toFloat()

        return mostCommon to averageConfidence
    }

    private fun ImageProxy.toBitmap(): Bitmap? {
        return try {
            val yBuffer = planes[0].buffer
            val uBuffer = planes[1].buffer
            val vBuffer = planes[2].buffer

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)

            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)

            val yuvImage = android.graphics.YuvImage(
                nv21, android.graphics.ImageFormat.NV21, width, height, null
            )
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(android.graphics.Rect(0, 0, width, height), 90, out)
            val imageBytes = out.toByteArray()
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } catch (e: Exception) {
            Log.e("RealTime", "Konversi ke Bitmap gagal: ${e.message}")
            null
        }
    }

    override fun onResume() {
        super.onResume()
        isActivityActive = true
        predictionBuffer.clear()
        isCameraFrozen = false
        resultText.text = "Mendeteksi..."
        btnAnalyze.isEnabled = false
    }

    override fun onPause() {
        super.onPause()
        isActivityActive = false
    }

    override fun onStop() {
        super.onStop()
        cameraProvider?.unbindAll()
        classifier.close()
        cameraExecutor.shutdown()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraProvider?.unbindAll()
        classifier.close()
        cameraExecutor.shutdown()
    }
}