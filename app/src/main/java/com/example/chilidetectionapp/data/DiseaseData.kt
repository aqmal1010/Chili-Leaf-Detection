package com.example.chilidetectionapp.data

import com.example.chilidetectionapp.R

object DiseaseData {
    fun getDiseaseList(): List<ChiliLeafDisease> {
        return listOf(
            ChiliLeafDisease(
                name = "Bercak Daun",
                scientificName = "Cercospora capsici",
                symptoms = "Muncul bercak bulat hingga oval berwarna coklat kehitaman pada permukaan daun.\n" +
                        "Tepi bercak biasanya berwarna lebih gelap dari bagian tengah.\n" +
                        "Bercak dapat meluas menyebabkan daun mengering dan rontok.",
                cause = "Disebabkan oleh jamur Cercospora capsici.\n" +
                        "Penyakit menyebar di lingkungan lembab, curah hujan tinggi, dan tanaman terlalu rapat",
                treatment = "Penyemprotan dengan ekstrak tanaman seperti daun sirih atau tembakau untuk menghambat perkembangan jamur.",
                imageResId = R.drawable.bercak_daun
            ),
            ChiliLeafDisease(
                name = "Keriting Daun",
                scientificName = "Akibat infeksi virus seperti CMV (Cucumber Mosaic Virus)",
                symptoms = "Daun menggulung ke atas atau ke bawah, tepi daun mengkerut.\n" +
                        "Pertumbuhan tanaman terhambat.\n" +
                        "Warna daun bisa menjadi lebih pucat.\nTanaman tampak kerdil dan berproduksi rendah.",
                cause = "Disebabkan oleh infeksi virus yang dibawa oleh serangga vektor seperti kutu daun (aphid).\n" +
                        "Penyebaran lebih cepat di lahan dengan kelembaban tinggi dan sanitasi buruk.",
                treatment = "Gunakan pestisida nabati seperti ekstrak bawang putih atau mimba untuk mengurangi populasi kutu daun.",
                imageResId = R.drawable.keriting_daun
            ),
            ChiliLeafDisease(
                name = "Virus Kuning",
                scientificName = "Pepper Yellow Leaf Curl Virus (PYLCV)",
                symptoms = "Daun menguning dimulai dari bagian tengah atau tepi.\n" +
                        "Daun menggulung dan menebal.\n" +
                        "Pertumbuhan terhambat dan buah berkurang.\n" +
                        "Tanaman terlihat lemah dan mudah layu.",
                cause = "Disebabkan oleh infeksi virus yang dibawa oleh serangga vektor seperti kutu kebul (Bemisia tabaci).\n" +
                        "Cuaca panas dan kering mempercepat penyebaran.",
                treatment = "Gunakan perangkap kuning lengket untuk mengurangi populasi kutu kebul.\n" +
                        "Tanam tanaman penghalang seperti jagung di sekitar lahan.",
                imageResId = R.drawable.virus_kuning
            )
        )
    }
}
