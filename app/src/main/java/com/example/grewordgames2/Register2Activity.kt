package com.example.grewordgames2

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.widget.MediaController
import android.widget.Toolbar
import android.widget.VideoView
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.example.grewordgames2.databinding.ActivityRegister2Binding

class Register2Activity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityRegister2Binding

    private lateinit var videoViewHome: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegister2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarRegister2.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_register2)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_my_words, R.id.nav_practice, R.id.nav_about_game
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

//        videoViewHome = findViewById(R.id.backgroundVideoHome)
//        val uri = Uri.parse("android.resource://"+packageName+'/'+R.raw.homepage_video2)
//        videoViewHome.setVideoURI(uri)
//        videoViewHome.setOnPreparedListener { it.isLooping = true }
//        videoViewHome.start()

        val fragmentId = intent.getStringExtra("Fragment")
        intent.removeExtra("Fragment");

        if (savedInstanceState == null) {
            when (fragmentId) {
                "myWords" -> navController.navigate(R.id.nav_my_words)
                "practice" -> navController.navigate(R.id.nav_practice)
                "aboutGame" -> navController.navigate(R.id.nav_about_game)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.register2, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_register2)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}