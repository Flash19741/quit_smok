package com.example.quit_smok.ui.timer

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.quit_smok.MainActivity
import com.example.quit_smok.R
import com.example.quit_smok.databinding.FragmentTimerBinding
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
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
        binding.btnBreak.setOnClickListener {
            handleBreak()
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

            // Светло-красный фон (нельзя курить)
            binding.root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_red))
        } else {
            binding.tvNextSmoke.text = "Время следующего перекура: --"
            binding.tvTimer.text = "Вы можете покурить сейчас"

            // Светло-зелёный фон (можно курить)
            binding.root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_green))
        }
    }

    private fun handleBreak() {
        val nowTime = LocalTime.now()
        if (!(activity as MainActivity).isWithinAwakeTime(nowTime)) {
            showOutsideAwakeDialog()
        } else {
            checkAndHandleSmoke()
        }
    }

    private fun showOutsideAwakeDialog() {
        AlertDialog.Builder(context)
            .setMessage("В это время Вы обычно не курите!!!")
            .setPositiveButton("Курить") { _, _ ->
                performSmoke()
            }
            .setNegativeButton("Отмена") { _, _ -> }
            .show()
    }

    private fun checkAndHandleSmoke() {
        val remainingMillis = calculateRemainingMillis()
        if (remainingMillis > 0) {
            showEarlySmokeDialog(remainingMillis)
        } else {
            performSmoke()
        }
    }

    private fun showEarlySmokeDialog(remainingMillis: Long) {
        val minutes = (remainingMillis / 60000).toInt()
        AlertDialog.Builder(context)
            .setMessage("Слишком рано курить. До перекура $minutes минут")
            .setPositiveButton("Курить") { _, _ ->
                performSmoke()
            }
            .setNegativeButton("Подождать") { _, _ -> }
            .show()
    }

    private fun performSmoke() {
        val now = System.currentTimeMillis()
        (activity as MainActivity).setLastSmokeTimestamp(now)
        (activity as MainActivity).recordSmoke()
        val newInterval = (activity as MainActivity).getCurrentInterval() + (activity as MainActivity).getIncreaseInterval()
        (activity as MainActivity).setCurrentInterval(newInterval)
        updateTimer()
    }

    private fun calculateRemainingMillis(): Long {
        val last = (activity as MainActivity).getLastSmokeTimestamp()
        if (last == 0L) return 0L
        val intervalMillis = (activity as MainActivity).getCurrentInterval() * 60L * 1000L
        val end = last + intervalMillis
        return end - System.currentTimeMillis()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        _binding = null
    }
}