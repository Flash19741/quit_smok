package com.example.quit_smok.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.quit_smok.MainActivity
import com.example.quit_smok.R
import com.example.quit_smok.databinding.FragmentSettingsBinding
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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

        // Проверяем настройки и обновляем UI после создания view
        val mainActivity = activity as MainActivity
        if (mainActivity.areSettingsSaved()) {
            // Заполняем поля сохранёнными данными
            binding.etFirstTime.setText(mainActivity.getFirstSmokeTime().format(DateTimeFormatter.ofPattern("HH:mm")))
            binding.etLastTime.setText(mainActivity.getLastSmokeTime().format(DateTimeFormatter.ofPattern("HH:mm")))
            binding.etCigs.setText(mainActivity.getInitialCigsPerDay().toString())
            binding.etIncrease.setText(mainActivity.getIncreaseInterval().toString())
            val packPrice = mainActivity.getCigarettePrice() * 20
            binding.etPackPrice.setText(packPrice.toString())
            updateCalculatedFields()

            // Disable edits and save button
            binding.etFirstTime.isEnabled = false
            binding.etLastTime.isEnabled = false
            binding.etCigs.isEnabled = false
            binding.etIncrease.isEnabled = false
            binding.etPackPrice.isEnabled = false
            binding.btnSave.isEnabled = false
        }

        binding.btnSave.setOnClickListener {
            saveSettings()
        }

        binding.btnReset.setOnClickListener {
            mainActivity.resetSettings()
        }
    }

    private fun saveSettings() {
        try {
            val firstStr = binding.etFirstTime.text.toString()
            val lastStr = binding.etLastTime.text.toString()
            val cigs = binding.etCigs.text.toString().toInt()
            val increase = binding.etIncrease.text.toString().toInt()
            val packPrice = binding.etPackPrice.text.toString().toDouble()

            if (firstStr.isEmpty() || lastStr.isEmpty() || cigs <= 0 || increase < 0 || packPrice <= 0) {
                Toast.makeText(context, "Все поля обязательны", Toast.LENGTH_SHORT).show()
                return
            }

            val first = LocalTime.parse(firstStr, DateTimeFormatter.ofPattern("H:m"))
            val last = LocalTime.parse(lastStr, DateTimeFormatter.ofPattern("H:m"))

            val mainActivity = activity as MainActivity
            mainActivity.saveSettings(first, last, cigs, increase, packPrice)

            updateCalculatedFields()

            // Disable fields and save button
            binding.etFirstTime.isEnabled = false
            binding.etLastTime.isEnabled = false
            binding.etCigs.isEnabled = false
            binding.etIncrease.isEnabled = false
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
        val mainActivity = activity as MainActivity
        val first = mainActivity.getFirstSmokeTime()
        val last = mainActivity.getLastSmokeTime()
        val awakeMinutes = mainActivity.calculateAwakeMinutes(first, last)
        binding.tvAwakeTime.text = "Вы бодрствуете ${awakeMinutes / 60} часов ${awakeMinutes % 60} минут"

        val cigs = mainActivity.getInitialCigsPerDay()
        val interval = mainActivity.getInitialInterval()
        binding.tvInterval.text = "Вы курите каждые $interval минут"

        val cigPrice = mainActivity.getCigarettePrice()
        binding.tvCigPrice.text = "Одна сигарета стоит $%.2f".format(cigPrice)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}