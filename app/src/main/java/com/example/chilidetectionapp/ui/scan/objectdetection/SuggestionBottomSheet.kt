package com.example.chilidetectionapp.ui.scan.objectdetection

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.chilidetectionapp.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.json.JSONArray
import org.json.JSONObject

class SuggestionBottomSheet : BottomSheetDialogFragment() {

    private lateinit var diseaseName: String

    interface OnDismissListener {
        fun onDismissed()
    }

    var dismissListener: OnDismissListener? = null

    // ... existing code

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.onDismissed()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        diseaseName = arguments?.getString("disease_name") ?: "Tidak Diketahui"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.suggestion_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val title = view.findViewById<TextView>(R.id.title)
        val suggestionText = view.findViewById<TextView>(R.id.suggestionText)

        title.text = "Saran untuk: $diseaseName"

        // Load JSON array dari assets
        val jsonString = requireContext().assets.open("chili_disease_names.json")
            .bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(jsonString)

        // Cari objek penyakit yang cocok dengan nama
        var hayati = "Tidak ada saran hayati."
        var kimiawi = "Tidak ada saran kimiawi."

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            if (obj.getString("disease_name").equals(diseaseName, ignoreCase = true)) {
                val treatment = obj.getJSONObject("treatment")
                hayati = treatment.optString("hayati", hayati)
                kimiawi = treatment.optString("kimiawi", kimiawi)
                break
            }
        }

        suggestionText.text = "💧 Hayati:\n$hayati\n\n🧪 Kimiawi:\n$kimiawi"
    }

    companion object {
        fun newInstance(diseaseName: String): SuggestionBottomSheet {
            val fragment = SuggestionBottomSheet()
            val args = Bundle()
            args.putString("disease_name", diseaseName)
            fragment.arguments = args
            return fragment
        }
    }
}