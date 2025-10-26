package com.example.quit_smok.ui.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.quit_smok.MainActivity
import com.example.quit_smok.databinding.FragmentStatisticsBinding
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.time.LocalDate
import java.time.Duration

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = activity as MainActivity
        updateStatistics()
    }

    private fun updateStatistics() {
        // Настройка графика сигарет
        val chartCigs = binding.chartCigs
        chartCigs.description.isEnabled = false
        val smokes = mainActivity.getDailySmokes().entries.sortedBy { LocalDate.parse(it.key) }
        val entriesCigs = smokes.mapIndexed { index, entry -> Entry(index.toFloat(), entry.value.toFloat()) }
        val dataSetCigs = LineDataSet(entriesCigs, "Сигареты по дням")
        dataSetCigs.color = ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
        chartCigs.data = LineData(dataSetCigs)
        chartCigs.invalidate()

        // Настройка графика загрязнения (пример: smokes * 10)
        val chartPollution = binding.chartPollution
        chartPollution.description.isEnabled = false
        val entriesPollution = smokes.mapIndexed { index, entry -> Entry(index.toFloat(), entry.value.toFloat() * 10) }
        val dataSetPollution = LineDataSet(entriesPollution, "Загрязнение")
        dataSetPollution.color = ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark)
        chartPollution.data = LineData(dataSetPollution)
        chartPollution.invalidate()

        // Медали (пример логики: visibility по прогрессу)
        val lastSmoke = mainActivity.getLastSmokeTimestamp()
        val duration = Duration.ofMillis(System.currentTimeMillis() - lastSmoke)
        val daysNotSmoked = duration.toDays().toInt()
        binding.ivMedal1.visibility = if (daysNotSmoked >= 1) View.VISIBLE else View.GONE
        binding.ivMedal2.visibility = if (daysNotSmoked >= 3) View.VISIBLE else View.GONE
        binding.ivMedal3.visibility = if (daysNotSmoked >= 7) View.VISIBLE else View.GONE

        // Время не курили
        val hours = duration.toHours() % 24
        val minutes = duration.toMinutes() % 60
        binding.btnNotSmoked.text = "Мы не курили $daysNotSmoked День $hours часов $minutes минут"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}