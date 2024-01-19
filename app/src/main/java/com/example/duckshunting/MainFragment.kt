package com.example.duckshunting

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.duckshunting.CustomView.Companion.DUCKS_DISAPPEARING_TIME_MS
import java.util.concurrent.TimeUnit
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView

class MainFragment : Fragment(), ShowDialogListener, SoundListener {

    private lateinit var customView: CustomView
    private lateinit var confetti: KonfettiView
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var mediaPlayer: MediaPlayer

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
            confetti = findViewById(R.id.konfettiView)
            customView.showDialogListener = this@MainFragment
            customView.soundListener = this@MainFragment
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

    override fun showRetryGameDialog() {
        val builder = AlertDialog.Builder(requireContext())
        with(builder) {
            setCancelable(false)
            setTitle("GAME OVER!")
            setMessage("Press the button to retry the game")
            setIcon(R.drawable.duck_game_over)
            setPositiveButton("RETRY") { _, _ ->
                customView.reset()
            }
        }
        builder.show()
    }

    override fun showWinDialog() {
        val builder = AlertDialog.Builder(requireContext())
        with(builder) {
            setCancelable(false)
            setTitle("YOU WIN!")
            setMessage("Press the button to start the new game")
            setIcon(R.drawable.duck_win)
            setPositiveButton("START") { _, _ ->
                customView.reset()
            }
        }
        builder.show()
        showConfetti()
    }

    private fun showConfetti() {
        val party = Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            emitter = Emitter(duration = 1000, TimeUnit.MILLISECONDS).max(1000),
            position = Position.Relative(0.5, 0.3)
        )
        confetti.start(party)
    }

    override fun playShotSound() {
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.shot_sound)
        mediaPlayer.start()
    }

    override fun playDuckSound() {
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.duck_quacking)
        mediaPlayer.start()
    }

    override fun vibration() {
        val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(200)
        }
    }
}
