package com.example.quit_smok.ui.timer

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.quit_smok.MainActivity
import com.example.quit_smok.R
import com.example.quit_smok.databinding.FragmentTimerBinding
import java.time.LocalTime
import java.time.Duration

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

        // Рассчитываем оставшееся время
        val remainingMillis = calculateRemainingMillis()
        val intervalMillis = currentInterval
        val progress = if (intervalMillis > 0) ((intervalMillis - remainingMillis).toFloat() / intervalMillis * 100).toInt() else 0
        binding.progressCircle.progress = progress

        // Мотивация
        val timeNotSmoked = now - lastSmokeTimestamp
        val duration = Duration.ofMillis(timeNotSmoked)
        val hours = duration.toHours()
        val mins = duration.toMinutes() % 60
        binding.tvMotivation.text = "Отлично, Вы на пути к свободе!\nВы уже не курили $hours часа $mins минут"

        // Экономия
        val days = if (timeNotSmoked > 0) (timeNotSmoked / (24 * 60 * 60 * 1000)).toInt() + 1 else 1
        val totalSavings = mainActivity.calculateTotalSavings()
        val expectedTotal = (mainActivity.getInitialCigsPerDay().toDouble() * mainActivity.getCigarettePrice() * days.toDouble())
        val savingsPercent = if (expectedTotal > 0) (totalSavings / expectedTotal) * 100 else 0.0
        binding.tvSavings.text = "+${savingsPercent.toInt()}% ${ (100 - savingsPercent.toInt()) }%"

        if (remainingMillis > 0) {
            val minutes = (remainingMillis / 60000).toInt()
            val seconds = ((remainingMillis % 60000) / 1000).toInt()
            binding.tvInsideCircle.text = "$minutes мин\n$seconds сек"

            // Светло-красный фон (нельзя курить)
            binding.root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_red))
        } else {
            binding.tvInsideCircle.text = "Можно курить\nсейчас"

            // Светло-зелёный фон (можно курить)
            binding.root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_green))
        }
    }

    private fun handleBreak() {
        val nowTime = LocalTime.now()
        if (!mainActivity.isWithinAwakeTime(nowTime)) {
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
        mainActivity.setLastSmokeTimestamp(now)
        mainActivity.recordSmoke()
        val newInterval = mainActivity.getCurrentInterval() + mainActivity.getIncreaseInterval()
        mainActivity.setCurrentInterval(newInterval)
        updateTimer()
    }

    private fun calculateRemainingMillis(): Long {
        val last = mainActivity.getLastSmokeTimestamp()
        if (last == 0L) return 0L
        val intervalMillis = mainActivity.getCurrentInterval().toLong() * 60L * 1000L
        val end = last + intervalMillis
        return end - System.currentTimeMillis()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        _binding = null
    }
}