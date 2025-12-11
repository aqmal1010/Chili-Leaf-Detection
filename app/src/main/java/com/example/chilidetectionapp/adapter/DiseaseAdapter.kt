package com.example.chilidetectionapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chilidetectionapp.adapter.DiseaseAdapter.DiseaseViewHolder
import com.example.chilidetectionapp.data.ChiliLeafDisease
import com.example.chilidetectionapp.databinding.ItemMaterialDiseaseBinding

class DiseaseAdapter(
    private val diseaseList: List<ChiliLeafDisease>,
    private val onItemClick: (ChiliLeafDisease) -> Unit
) : RecyclerView.Adapter<DiseaseViewHolder>() {

    inner class DiseaseViewHolder(private val binding: ItemMaterialDiseaseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(disease: ChiliLeafDisease) {
            binding.diseaseName.text = disease.name
            binding.diseaseDescription.text = disease.symptoms
            binding.diseaseImage.setImageResource(disease.imageResId)

            binding.root.setOnClickListener {
                onItemClick(disease)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiseaseViewHolder {
        val binding =
            ItemMaterialDiseaseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DiseaseViewHolder(binding)
    }

    override fun getItemCount(): Int = diseaseList.size

    override fun onBindViewHolder(holder: DiseaseViewHolder, position: Int) {
        holder.bind(diseaseList[position])
    }
}