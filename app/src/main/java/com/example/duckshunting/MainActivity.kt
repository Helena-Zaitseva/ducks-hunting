package com.example.duckshunting

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var navigationView: NavigationView

    private val navController: NavController by lazy {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navHostFragment.navController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun setUpActionBar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        setupNavigation()
    }

    private fun setupNavigation() {
        navigationView.setupWithNavController(navController)
        NavigationUI.setupActionBarWithNavController(
            activity = this,
            navController = navController
        )
    }
}

fun Fragment.attachToolbarToMainActivity(toolbar: Toolbar, @DrawableRes icon: Int) {
    (requireActivity() as? MainActivity)?.setUpActionBar(toolbar)
    toolbar.setNavigationIcon(icon)
}
