package com.example.quit_smok.ui.timer

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.quit_smok.MainActivity
import com.example.quit_smok.R
import com.example.quit_smok.databinding.FragmentTimerBinding
import java.time.LocalTime

class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var tvTimer: TextView
    private lateinit var btnBreak: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        val root: View = binding.root

        tvTimer = binding.tvTimer
        btnBreak = binding.btnBreak // "Перекур"

        btnBreak.setOnClickListener {
            handleBreak()
        }

        updateTimerDisplay()
        startTimerUpdate()

        return root
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
        updateTimerDisplay()
    }

    private fun calculateRemainingMillis(): Long {
        val last = (activity as MainActivity).getLastSmokeTimestamp()
        if (last == 0L) return 0L
        val intervalMillis = (activity as MainActivity).getCurrentInterval() * 60L * 1000L
        val end = last + intervalMillis
        return end - System.currentTimeMillis()
    }

    private fun updateTimerDisplay() {
        val remainingMillis = calculateRemainingMillis()
        if (remainingMillis > 0) {
            val minutes = (remainingMillis / 60000).toInt()
            val seconds = ((remainingMillis % 60000) / 1000).toInt()
            tvTimer.text = "Вы можете покурить через $minutes:$seconds"
        } else {
            tvTimer.text = "Вы можете покурить"
        }
    }

    private fun startTimerUpdate() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                updateTimerDisplay()
                handler.postDelayed(this, 1000)
            }
        }, 1000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        _binding = null
    }
}