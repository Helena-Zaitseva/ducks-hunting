package com.example.duckshunting

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.duckshunting.CustomView.Companion.DUCKS_DISAPPEARING_TIME_MS

class MainFragment : Fragment() {

    private lateinit var customView: CustomView
    private val handler = Handler(Looper.getMainLooper())

    private val duckToggleRunnable = object : Runnable {
        override fun run() {
            //ducks are on the screen
            customView.showDucks()
            // Running the same Runnable after a specified time interval
            handler.postDelayed(this, DUCKS_DISAPPEARING_TIME_MS)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        layoutInflater.inflate(R.layout.main_fragment, container, false).apply {
            customView = findViewById(R.id.customView)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startDuckDisplay()
    }

    private fun startDuckDisplay() {
        handler.postDelayed(duckToggleRunnable, DUCKS_DISAPPEARING_TIME_MS)
    }

    override fun onResume() {
        super.onResume()
        customView.startAnimation()
    }

    override fun onPause() {
        super.onPause()
        customView.stopAnimation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //Clear all messages from the handler queue when a fragment is destroyed
        handler.removeCallbacksAndMessages(null)
    }
}
