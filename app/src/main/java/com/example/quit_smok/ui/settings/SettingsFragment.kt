package com.example.quit_smok.ui.settings

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.quit_smok.MainActivity
import com.example.quit_smok.R
import com.example.quit_smok.databinding.FragmentSettingsBinding
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = activity as MainActivity
        if (mainActivity.areSettingsSaved()) {
            binding.btnFirstTime.text = mainActivity.getFirstSmokeTime().format(DateTimeFormatter.ofPattern("HH:mm"))
            binding.btnLastTime.text = mainActivity.getLastSmokeTime().format(DateTimeFormatter.ofPattern("HH:mm"))
            binding.seekCigs.progress = mainActivity.getInitialCigsPerDay()
            binding.seekIncrease.progress = mainActivity.getIncreaseInterval()
            val packPrice = mainActivity.getCigarettePrice() * 20
            binding.etPackPrice.setText(packPrice.toString())
            updateCalculatedFields()

            // Disable edits and save button
            binding.btnFirstTime.isEnabled = false
            binding.btnLastTime.isEnabled = false
            binding.seekCigs.isEnabled = false
            binding.seekIncrease.isEnabled = false
            binding.etPackPrice.isEnabled = false
            binding.btnSave.isEnabled = false
        }

        binding.btnFirstTime.setOnClickListener {
            showTimePicker { time ->
                binding.btnFirstTime.text = time.format(DateTimeFormatter.ofPattern("HH:mm"))
                updateCalculatedFields()
            }
        }

        binding.btnLastTime.setOnClickListener {
            showTimePicker { time ->
                binding.btnLastTime.text = time.format(DateTimeFormatter.ofPattern("HH:mm"))
                updateCalculatedFields()
            }
        }

        binding.seekCigs.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                updateCalculatedFields()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.seekIncrease.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                updateCalculatedFields()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.btnSave.setOnClickListener {
            saveSettings()
        }

        binding.btnReset.setOnClickListener {
            mainActivity.resetSettings()
        }
    }

    private fun showTimePicker(onTimeSelected: (LocalTime) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        TimePickerDialog(requireContext(), { _, h, m ->
            onTimeSelected(LocalTime.of(h, m))
        }, hour, minute, true).show()
    }

    private fun saveSettings() {
        try {
            val firstStr = binding.btnFirstTime.text.toString()
            val lastStr = binding.btnLastTime.text.toString()
            val cigs = binding.seekCigs.progress
            val increase = binding.seekIncrease.progress
            val packPrice = binding.etPackPrice.text.toString().toDouble()

            if (firstStr.isEmpty() || lastStr.isEmpty() || cigs <= 0 || increase < 0 || packPrice <= 0) {
                Toast.makeText(context, "Все поля обязательны", Toast.LENGTH_SHORT).show()
                return
            }

            val first = LocalTime.parse(firstStr, DateTimeFormatter.ofPattern("HH:mm"))
            val last = LocalTime.parse(lastStr, DateTimeFormatter.ofPattern("HH:mm"))

            val mainActivity = activity as MainActivity
            mainActivity.saveSettings(first, last, cigs, increase, packPrice)

            updateCalculatedFields()

            // Disable fields and save button
            binding.btnFirstTime.isEnabled = false
            binding.btnLastTime.isEnabled = false
            binding.seekCigs.isEnabled = false
            binding.seekIncrease.isEnabled = false
            binding.etPackPrice.isEnabled = false
            binding.btnSave.isEnabled = false

            Toast.makeText(context, "Настройки сохранены", Toast.LENGTH_SHORT).show()

            // Перейти к TimerFragment после сохранения
            findNavController().navigate(R.id.navigation_timer)
        } catch (e: Exception) {
            Toast.makeText(context, "Ошибка ввода", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCalculatedFields() {
        val firstStr = binding.btnFirstTime.text.toString()
        val lastStr = binding.btnLastTime.text.toString()
        if (firstStr.isNotEmpty() && lastStr.isNotEmpty()) {
            try {
                val first = LocalTime.parse(firstStr, DateTimeFormatter.ofPattern("HH:mm"))
                val last = LocalTime.parse(lastStr, DateTimeFormatter.ofPattern("HH:mm"))
                val awakeDuration = Duration.between(first, last)
                val awakeMinutes = awakeDuration.toMinutes().toInt().let { if (it < 0) it + 24 * 60 else it }
                binding.tvAwakeTime.text = "Вы бодрствуете ${awakeMinutes / 60} часов ${awakeMinutes % 60} минут"
            } catch (e: Exception) {
                // Ignore
            }
        }

        val cigs = binding.seekCigs.progress
        val interval = if (cigs > 0) (/* awakeMinutes from above */ 0 / cigs) else 0 // Adjust to use awakeMinutes
        binding.tvInterval.text = "Вы курите каждые $interval минут"

        val packPriceStr = binding.etPackPrice.text.toString()
        val cigPrice = if (packPriceStr.isNotEmpty()) packPriceStr.toDouble() / 20 else 0.0
        binding.tvCigPrice.text = "Одна сигарета стоит %.2f".format(cigPrice)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}