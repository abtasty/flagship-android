package com.abtasty.flagshipqa

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.abtasty.flagship.utils.MurmurHash

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_config, R.id.navigation_context, R.id.navigation_modifications,
                R.id.navigation_events
            )
        )
        supportActionBar?.hide()
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        test_murmur()
    }

    fun test_murmur() {
        val ids = arrayOf("202072017183814142",
            "202072017183860649",
            "202072017183828850",
            "202072017183818733",
            "202072017183823773",
            "202072017183894922",
            "202072017183829817",
            "202072017183842202",
            "202072017233645009",
            "202072017233690230",
            "202072017183886606",
            "202072017183877657",
            "202072017183860380",
            "202072017183972690",
            "202072017183912618",
            "202072017183951364",
            "202072017183920657",
            "202072017183922748",
            "202072017183943575",
            "202072017183987677")


        var result_hash = ""
        for (i in ids) {
//            val m = MurmurHash.murmurhash3_x86_32(i)
//            val m = MurmurHash.murmurhash3_x86_32("bs8r119sbs4016meiiii" + i)
            val m = MurmurHash.murmurhash3_x86_32("bs8qvmo4nlr01fl9obpg" + i)
            result_hash += m.toString() + ","
        }
        println(result_hash)

        var result = ""
        for (i in ids) {
//            val m = MurmurHash.murmurhash3_x86_32(i)
//            val m = MurmurHash.murmurhash3_x86_32("bs8r119sbs4016meiiii" + i)
            val m = MurmurHash.murmurhash3_x86_32("bs8qvmo4nlr01fl9obpg" + i)
            result += if ((m % 100) <= 49) "1," else "2,"
        }
        println(result)
    }
}