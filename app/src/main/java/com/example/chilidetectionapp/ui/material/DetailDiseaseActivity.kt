package com.example.chilidetectionapp.ui.material

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chilidetectionapp.R
import com.example.chilidetectionapp.data.ChiliLeafDisease
import com.example.chilidetectionapp.databinding.ActivityDetailDiseaseBinding

@Suppress("DEPRECATION")
class DetailDiseaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailDiseaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailDiseaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_detail_disease)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this@DetailDiseaseActivity, R.color.white)))
            title = SpannableString("Detail Penyakit").apply {
                setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(this@DetailDiseaseActivity, R.color.green_primary)),
                    0, length, SpannableString.SPAN_INCLUSIVE_INCLUSIVE
                )
            }
        }


        val disease = intent.getSerializableExtra(EXTRA_DISEASE) as? ChiliLeafDisease

        disease?.let {
            binding.diseaseDetailName.text = it.name
            binding.diseaseDetailScientificName.text = it.scientificName
            binding.diseaseDetailDescription.text = it.symptoms
            binding.diseaseDetailCauseText.text = it.cause
            binding.diseaseDetailTreatmentText.text = it.treatment
            binding.diseaseDetailImage.setImageResource(it.imageResId)
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }


    companion object {
        const val EXTRA_DISEASE = "extra_disease"
    }
}