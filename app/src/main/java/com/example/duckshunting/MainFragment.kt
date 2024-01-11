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
            // Метод для переключения видимости утки
            customView.showDucks()
            // Запуск того же Runnable через заданный интервал времени
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

    override fun onDestroyView() {
        super.onDestroyView()
        // Очистить все сообщения из очереди обработчика при уничтожении фрагмента
        handler.removeCallbacksAndMessages(null)
    }
}
