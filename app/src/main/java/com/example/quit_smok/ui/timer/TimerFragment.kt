package com.example.quit_smok.ui.timer

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.quit_smok.MainActivity
import com.example.quit_smok.databinding.FragmentTimerBinding
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var mainActivity: MainActivity
    private val updateInterval = 1000L // Обновление каждую секунду

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity = activity as MainActivity

        // Обработчик кнопки для записи перекура
        binding.btnSmoke.setOnClickListener {
            val now = System.currentTimeMillis()
            mainActivity.setLastSmokeTimestamp(now)
            mainActivity.recordSmoke()
            updateTimer()
        }

        // Запускаем обновление таймера
        startTimer()
    }

    private fun startTimer() {
        handler.post(object : Runnable {
            override fun run() {
                updateTimer()
                handler.postDelayed(this, updateInterval)
            }
        })
    }

    private fun updateTimer() {
        val lastSmokeTimestamp = mainActivity.getLastSmokeTimestamp()
        val currentInterval = mainActivity.getCurrentInterval() * 60 * 1000L // Интервал в миллисекундах
        val now = System.currentTimeMillis()
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

        // Форматируем время последнего перекура
        if (lastSmokeTimestamp > 0) {
            val lastSmokeTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(lastSmokeTimestamp),
                ZoneId.systemDefault()
            )
            binding.tvLastSmoke.text = "Время последнего перекура: ${lastSmokeTime.format(formatter)}"
        } else {
            binding.tvLastSmoke.text = "Время последнего перекура: --"
        }

        // Вычисляем и форматируем время следующего перекура
        val nextSmokeTimestamp = lastSmokeTimestamp + currentInterval
        if (lastSmokeTimestamp > 0 && nextSmokeTimestamp > now) {
            val nextSmokeTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(nextSmokeTimestamp),
                ZoneId.systemDefault()
            )
            binding.tvNextSmoke.text = "Время следующего перекура: ${nextSmokeTime.format(formatter)}"

            // Вычисляем оставшееся время до следующего перекура
            val timeLeftMs = nextSmokeTimestamp - now
            val minutesLeft = (timeLeftMs / 1000 / 60).toInt()
            val secondsLeft = (timeLeftMs / 1000 % 60).toInt()
            binding.tvTimer.text = "Вы можете покурить через $minutesLeft мин $secondsLeft сек"
        } else {
            binding.tvNextSmoke.text = "Время следующего перекура: --"
            binding.tvTimer.text = "Вы можете покурить сейчас"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        _binding = null
    }
}