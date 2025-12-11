package com.example.chilidetectionapp.ui.material

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chilidetectionapp.R
import com.example.chilidetectionapp.adapter.DiseaseAdapter
import com.example.chilidetectionapp.data.DiseaseData
import com.example.chilidetectionapp.databinding.FragmentMaterialDiseaseBinding

class MaterialDiseaseFragment : Fragment() {

    private var _binding: FragmentMaterialDiseaseBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMaterialDiseaseBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            show()
            setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.white)))
            title = SpannableString(title ?: "").apply {
                setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.green_primary)), 0, length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            }
        }

        val diseaseList = DiseaseData.getDiseaseList()
        val adapter = DiseaseAdapter(diseaseList) { disease ->
            val intent = Intent(requireContext(), DetailDiseaseActivity::class.java).apply {
                putExtra(DetailDiseaseActivity.EXTRA_DISEASE, disease)
            }
            startActivity(intent)
        }

        binding.recyclerViewDisease.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewDisease.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}