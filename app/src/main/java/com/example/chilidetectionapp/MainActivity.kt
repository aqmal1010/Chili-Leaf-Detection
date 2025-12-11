package com.example.chilidetectionapp

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.chilidetectionapp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.show()

        val navView: BottomNavigationView = binding.navView
        val navController =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.findNavController()
                ?: throw IllegalStateException("NavController not found")

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_material_disease,
                R.id.navigation_detection,
                R.id.navigation_history,
                R.id.navigation_guide
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_home -> setActionBarTitle("Chili Leaf Disease App")
                R.id.navigation_material_disease -> setActionBarTitle("Informasi Penyakit")
                R.id.navigation_detection -> setActionBarTitle("Deteksi Daun Tanaman Cabai")
                R.id.navigation_history -> setActionBarTitle("Riwayat Deteksi")
                R.id.navigation_guide -> setActionBarTitle("Panduan")
            }
        }

        navView.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.navigation_home) {
                navController.popBackStack(R.id.navigation_home, false)
            } else {
                navController.popBackStack(R.id.navigation_home, false)
                navController.navigate(item.itemId)
            }
            true
        }


        binding.btnCamera.setOnClickListener {
            navView.selectedItemId = R.id.navigation_detection
        }
    }

    private fun setActionBarTitle(title: String) {
        val actionBar = supportActionBar
        if (actionBar != null) {
            val spannableTitle = SpannableString(title).apply {
                setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            this@MainActivity,
                            R.color.green_primary
                        )
                    ),
                    0, length,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
            }
            actionBar.title = spannableTitle
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.findNavController()
                ?: return false
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}