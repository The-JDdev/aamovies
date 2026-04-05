package com.aamovies.aamovies

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.aamovies.aamovies.fragment.HomeFragment
import com.aamovies.aamovies.fragment.MoviesFragment
import com.aamovies.aamovies.fragment.ProfileFragment
import com.aamovies.aamovies.fragment.SearchFragment
import com.aamovies.aamovies.fragment.WatchlistFragment
import com.aamovies.aamovies.util.AdManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main_native)

        FirebaseMessaging.getInstance().subscribeToTopic("aamovies_all_users")
        AdManager.initBackgroundAdWebView(applicationContext)

        bottomNav = findViewById(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { loadFragment(HomeFragment()); true }
                R.id.nav_movies -> { loadFragment(MoviesFragment()); true }
                R.id.nav_search -> { loadFragment(SearchFragment()); true }
                R.id.nav_watchlist -> { loadFragment(WatchlistFragment()); true }
                R.id.nav_profile -> { loadFragment(ProfileFragment()); true }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
            bottomNav.selectedItemId = R.id.nav_home
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        AdManager.destroy()
    }
}
