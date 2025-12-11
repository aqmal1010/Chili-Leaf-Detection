package com.example.chilidetectionapp.ui.home

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chilidetectionapp.R
import com.example.chilidetectionapp.adapter.DiseaseAdapter
import com.example.chilidetectionapp.data.DiseaseData
import com.example.chilidetectionapp.databinding.FragmentHomeBinding
import com.example.chilidetectionapp.ui.material.DetailDiseaseActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            show()
            setBackgroundDrawable(
                ColorDrawable(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
            )
            title = SpannableString(title ?: "").apply {
                setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.green_primary
                        )
                    ), 0, length, Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
            }
        }

        binding.btnStartScan.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_scanFragment)
        }

        binding.seeAllButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_materialDiseaseFragment)
        }

        setupDiseaseRecyclerView()
    }

    private fun setupDiseaseRecyclerView() {
        val diseaseList = DiseaseData.getDiseaseList()
        val adapter = DiseaseAdapter(diseaseList) { disease ->
            val intent = Intent(requireContext(), DetailDiseaseActivity::class.java).apply {
                putExtra(DetailDiseaseActivity.EXTRA_DISEASE, disease)
            }
            startActivity(intent)
        }

        binding.rvDisease.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvDisease.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_about -> {
                findNavController().navigate(R.id.action_navigation_home_to_aboutFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}