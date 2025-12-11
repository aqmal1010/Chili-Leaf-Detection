package com.example.chilidetectionapp.ui.scan

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.chilidetectionapp.MainActivity
import com.example.chilidetectionapp.R
import com.example.chilidetectionapp.data.Detection
import com.example.chilidetectionapp.data.DiseaseDataResponse
import com.example.chilidetectionapp.databinding.ActivityResultScanBinding
import com.example.chilidetectionapp.ui.ViewModelFactory
import com.example.chilidetectionapp.ui.getCurrentTimestamp
import com.google.gson.Gson
import java.text.NumberFormat

class ResultScanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultScanBinding
    private lateinit var scanViewModel: ScanViewModel

    private lateinit var diseaseDataList: List<DiseaseDataResponse>

    companion object {
        const val RESULT_IMAGE = "RESULT_IMAGE"
        const val RESULT_TITLE = "RESULT_TITLE"
        const val RESULT_CONFIDENCE = "RESULT_CONFIDENCE"
        const val RESULT_DISEASE_DATA = "RESULT_DISEASE_DATA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Hasil Deteksi"

        scanViewModel = obtainViewModel(this@ResultScanActivity)

        // Menampilkan hasil gambar dan analisis
        val resultImage = intent.getStringExtra(RESULT_IMAGE)
        val imageUri = resultImage?.let { Uri.parse(it) }
        val resultTitle = intent.getStringExtra(RESULT_TITLE)
        val resultConfidence = intent.getFloatExtra(RESULT_CONFIDENCE, 0.0f)

        // Parse disease data
        val diseaseDataJson = intent.getStringExtra(RESULT_DISEASE_DATA)
        val diseaseData: DiseaseDataResponse? = if (!diseaseDataJson.isNullOrEmpty()) {
            try {
                Gson().fromJson(diseaseDataJson, DiseaseDataResponse::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }

        imageUri?.let {
            binding.resultImage.setImageURI(it)
        }
        binding.resultDisease.text = resultTitle

        binding.resultConfidence.text = getString(
            R.string.analyze_score,
            NumberFormat.getPercentInstance().format(resultConfidence)
        )

        binding.resultScientificName.text = diseaseData?.scientific_name
        binding.resultSymptoms.text = diseaseData?.symptoms
        binding.resultCauses.text = diseaseData?.causes
        binding.resultTreatmentBiological.text = diseaseData?.treatment?.hayati
        binding.resultTreatmentChemical.text = diseaseData?.treatment?.kimiawi

        // Save result to history
        val analyzeResult = Detection(
            uri = resultImage ?: "",
            disease_name = resultTitle ?: "",
            scientific_name = diseaseData?.scientific_name ?: "",
            confidence = resultConfidence,
            symptoms = diseaseData?.symptoms ?: "",
            causes = diseaseData?.causes ?: "",
            treatment_biological = diseaseData?.treatment?.hayati ?: "",
            treatment_chemical = diseaseData?.treatment?.kimiawi ?: "",
            detectedAt =  getCurrentTimestamp(),
        )

        scanViewModel.insertDetection(analyzeResult)

        binding.btnFinish.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun obtainViewModel(activity: AppCompatActivity): ScanViewModel {
        val factory = ViewModelFactory(activity.applicationContext)
        return ViewModelProvider(activity, factory)[ScanViewModel::class.java]
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}