package com.example.duckshunting

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class SplashFragment : Fragment(R.layout.splash_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scheduleNavigationToMain()
    }

    private fun scheduleNavigationToMain() {
        val currentTime = System.currentTimeMillis()
        val isTimePassed = { (System.currentTimeMillis() - currentTime) >= DELAY_MILLIS }
        Thread {
            while (true) {
                if (isTimePassed()) {
                    break
                }
            }
            activity?.runOnUiThread {
                findNavController().navigate(R.id.action_splashFragment_to_mainFragment)
            }

        }.start()
    }

    companion object {

        private const val DELAY_MILLIS = 1000L
    }
}