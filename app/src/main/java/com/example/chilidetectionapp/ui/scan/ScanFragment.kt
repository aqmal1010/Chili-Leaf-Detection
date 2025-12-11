package com.example.chilidetectionapp.ui.scan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.chilidetectionapp.R
import com.example.chilidetectionapp.data.DiseaseDataResponse
import com.example.chilidetectionapp.databinding.FragmentScanBinding
import com.example.chilidetectionapp.helper.Classifier
import com.example.chilidetectionapp.ui.createCustomTempFile
import com.example.chilidetectionapp.ui.scan.objectdetection.RealtimeActivity
import com.example.chilidetectionapp.ui.uriToFile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.lang.reflect.Type

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!
    private var getFile: File? = null
    private lateinit var currentPhotoPath: String

    private lateinit var classifier: Classifier
    private lateinit var mBitmap: Bitmap

    private val mJsonPath = "chili_disease_names.json"

    private lateinit var diseaseDataList: List<DiseaseDataResponse>

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        classifier = Classifier(requireContext())

        // Load disease data from JSON
        loadDiseaseData()

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                showToast("Permintaan izin dikabulkan")
            } else {
                showToast("Permintaan izin ditolak")
            }
        }

        checkCameraPermission()

        binding.btnUploadImage.setOnClickListener {
            showImageSourceChooser()
        }

        binding.btnRealtime.setOnClickListener {
            val intent = Intent(requireContext(), RealtimeActivity::class.java)
            startActivity(intent)
        }

        binding.btnAnalyze.setOnClickListener {
            getFile?.let {
                navigateToResult(it)
            } ?: run {
                showToast(getString(R.string.empty_image_warning))
            }
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // CAMERA
    private fun startCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(requireActivity().packageManager)

        createCustomTempFile(requireActivity().application).also {
            val photoUri: Uri = FileProvider.getUriForFile(
                requireContext(), "${requireContext().packageName}.fileprovider", it
            )

            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            launcherIntentCamera.launch(intent)
        }
    }

    private val launcherIntentCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                val myFile = File(currentPhotoPath)
                getFile = myFile

                val result = BitmapFactory.decodeFile(getFile?.path)
                binding.previewImageView.setImageBitmap(result)

                analyzeImage(myFile)
            }
        }

    // GALLERY
    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"

        val chooser = Intent.createChooser(intent, getString(R.string.choose_picture))
        launcherIntentGallery.launch(chooser)
    }

    private fun showImageSourceChooser() {
        val options = arrayOf("Kamera", "Galeri")
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Pilih Sumber Gambar")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> startCamera()
                    1 -> startGallery()
                }
            }
            .show()
    }

    private val launcherIntentGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val imageUri: Uri = result.data?.data as Uri
                val myFile = uriToFile(imageUri, requireContext())

                Log.d(TAG, imageUri.toString())
                Log.d(TAG, myFile.toString())

                myFile.let { file ->
                    getFile = file
                    binding.previewImageView.setImageBitmap(BitmapFactory.decodeFile(file.path))
                }

                getFile = myFile
                binding.previewImageView.setImageURI(imageUri)

                analyzeImage(myFile)
            }
        }

    private fun analyzeImage(file: File) {
        try {
            mBitmap = BitmapFactory.decodeFile(file.path)
            val (diseaseName, confidence) = classifier.classifyImage(mBitmap)
            Log.d(TAG, "Hasil Identifikasi: $diseaseName ($confidence)")
            // Tidak ada perubahan UI karena validasi sudah dihapus
        } catch (e: IOException) {
            e.printStackTrace()
            showToast(getString(R.string.error_loading_image))
        }
    }

    private fun navigateToResult(file: File) {
        try {
            mBitmap = BitmapFactory.decodeFile(file.path)

            val (diseaseName, confidence) = classifier.classifyImage(mBitmap)

            if (confidence != null && confidence > 0.0f) {
                val dataDisease = getDataDiseaseObject(diseaseName)

                if (dataDisease != null) {
                    val intent = Intent(requireContext(), ResultScanActivity::class.java).apply {
                        putExtra(ResultScanActivity.RESULT_IMAGE, getFile?.absolutePath)
                        putExtra(ResultScanActivity.RESULT_TITLE, diseaseName)
                        putExtra(ResultScanActivity.RESULT_CONFIDENCE, confidence)
                        putExtra(ResultScanActivity.RESULT_DISEASE_DATA, Gson().toJson(dataDisease))
                    }

                    startActivity(intent)
                } else {
                    showToast("Tidak ada informasi rinci yang ditemukan untuk $diseaseName")
                }

            } else {
                showToast(getString(R.string.error_no_result))
            }
        } catch (e: IOException) {
            e.printStackTrace()
            showToast(getString(R.string.error_loading_image))
        }
    }

    private fun loadDiseaseData() {
        try {
            val inputStream: InputStream = requireContext().assets.open(mJsonPath)
            val json = inputStream.bufferedReader().use { it.readText() }
            val type: Type = object : TypeToken<List<DiseaseDataResponse>>() {}.type
            diseaseDataList = Gson().fromJson(json, type)

            if (diseaseDataList.isEmpty()) {
                showToast("Tidak ada data penyakit yang ditemukan")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(
                requireContext(),
                "Gagal memuat data penyakit",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    private fun getDataDiseaseObject(diseaseName: String): DiseaseDataResponse? {
        // Return the first matching disease or null if not found
        return diseaseDataList.find {
            it.disease_name.equals(diseaseName, ignoreCase = true)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        Log.d(TAG, message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        classifier.close()
        _binding = null
    }

    companion object {
        const val TAG = "ScanFragment"
    }
}