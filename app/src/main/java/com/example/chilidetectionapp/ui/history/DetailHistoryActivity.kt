package com.example.chilidetectionapp.ui.history

import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chilidetectionapp.R
import com.example.chilidetectionapp.data.Detection
import com.example.chilidetectionapp.databinding.ActivityDetailHistoryBinding
import java.text.NumberFormat

@Suppress("DEPRECATION")
class DetailHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Riwayat Deteksi"

        val analyze = intent.getParcelableExtra<Detection>(EXTRA_ANALYZE_ID)

        analyze?.let {
            binding.resultImage.setImageURI(Uri.parse(it.uri))
            binding.resultName.text = it.disease_name
            binding.resultScore.text = getString(
                R.string.analyze_score,
                NumberFormat.getPercentInstance().format(it.confidence)
            )

            binding.resultScientificName.text = it.scientific_name?.ifEmpty { "-" }
            binding.resultSymptoms.text = it.symptoms?.ifEmpty { "-" }
            binding.resultCauses.text = it.causes?.ifEmpty { "-" }
            binding.resultTreatmentBiological.text = it.treatment_biological?.ifEmpty { "-" }
            binding.resultTreatmentChemical.text = it.treatment_chemical?.ifEmpty { "-" }
            binding.resultDetectedAt.text = it.detectedAt
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        const val EXTRA_ANALYZE_ID = "extra_analyze"
    }
}