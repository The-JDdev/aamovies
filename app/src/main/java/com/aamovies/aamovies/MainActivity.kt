package com.aamovies.aamovies

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.aamovies.aamovies.fragment.HomeFragment
import com.aamovies.aamovies.fragment.ProfileFragment
import com.aamovies.aamovies.fragment.SearchFragment
import com.aamovies.aamovies.fragment.TrendingFragment
import com.aamovies.aamovies.fragment.WatchlistFragment
import com.aamovies.aamovies.util.AdManager
import com.aamovies.aamovies.util.SecurityManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var bottomNav: BottomNavigationView
    lateinit var drawerLayout: DrawerLayout
    private lateinit var navDrawer: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Security checks
        try {
            SecurityManager.runChecks(this)
        } catch (e: SecurityException) {
            when (e.message) {
                "APP_TAMPERED" -> showSecurityDialog(
                    getString(R.string.security_tampered_title),
                    getString(R.string.security_tampered_msg)
                )
                else -> showSecurityDialog(
                    getString(R.string.security_root_title),
                    getString(R.string.security_root_msg)
                )
            }
            return
        }

        setContentView(R.layout.activity_main_native)

        FirebaseMessaging.getInstance().subscribeToTopic("aamovies_all_users")
        AdManager.initBackgroundAdWebView(applicationContext)

        drawerLayout = findViewById(R.id.drawer_layout)
        navDrawer = findViewById(R.id.nav_drawer)
        bottomNav = findViewById(R.id.bottom_navigation)

        // Update drawer header email
        val headerView = navDrawer.getHeaderView(0)
        val tvEmail = headerView.findViewById<TextView>(R.id.tv_drawer_user_email)
        tvEmail.text = if (auth.currentUser?.isAnonymous == true) "Guest" else (auth.currentUser?.email ?: "")

        // Drawer navigation item clicks
        navDrawer.setNavigationItemSelectedListener { item ->
            drawerLayout.closeDrawer(GravityCompat.START)
            when (item.itemId) {
                R.id.drawer_watchlist -> {
                    loadFragment(WatchlistFragment())
                    bottomNav.selectedItemId = R.id.nav_watchlist
                }
                R.id.drawer_search -> {
                    loadFragment(SearchFragment())
                    bottomNav.selectedItemId = R.id.nav_search
                }
                R.id.drawer_liked -> {
                    startActivity(Intent(this, LikedMoviesActivity::class.java))
                }
                R.id.drawer_contact -> {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.contact_email)))
                        putExtra(Intent.EXTRA_SUBJECT, "Support: AAMovies 2.0")
                    }
                    if (intent.resolveActivity(packageManager) != null) startActivity(intent)
                    else Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }

        // Bottom navigation
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { loadFragment(HomeFragment()); true }
                R.id.nav_search -> { loadFragment(SearchFragment()); true }
                R.id.nav_trending -> { loadFragment(TrendingFragment()); true }
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

    fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun showSecurityDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Exit") { _, _ -> finishAffinity() }
            .show()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AdManager.destroy()
    }
}
