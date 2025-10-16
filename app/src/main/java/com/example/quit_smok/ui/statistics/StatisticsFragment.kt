package com.example.quit_smok.ui.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.quit_smok.MainActivity
import com.example.quit_smok.R
import com.example.quit_smok.databinding.FragmentStatisticsBinding
import java.time.LocalDate
import java.util.Locale

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
        val root: View = binding.root

        mainActivity = activity as MainActivity
        updateStatistics()

        return root
    }

    private fun updateStatistics() {
        val initialCigsPerDay = mainActivity.getInitialCigsPerDay()
        val cigPrice = mainActivity.getCigarettePrice()
        val initialInterval = mainActivity.getInitialInterval()
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        // 1. Вы выкуривали (значение из настроек) сигарет в сутки
        binding.tvInitialCigsPerDay.text = "Вы выкуривали $initialCigsPerDay сигарет в сутки"

        // 2. Вчера вы выкурили (значение сколько сигарет скурил вчера) сигарет
        val yesterdayCigs = mainActivity.getSmokesForDate(yesterday)
        binding.tvYesterdayCigs.text = "Вчера вы выкурили $yesterdayCigs сигарет"

        // 3. Сегодня вы выкурили (значение сколько сигарет скурил сегодня) сигарет
        val todayCigs = mainActivity.getSmokesForDate(today)
        binding.tvTodayCigs.text = "Сегодня вы выкурили $todayCigs сигарет"

        // 4. Вы тратили (значение траты на сигареты при запуске приложения)
        val initialSpend = initialCigsPerDay * cigPrice
        binding.tvInitialSpend.text = "Вы тратили ${String.format(Locale.US, "%.2f", initialSpend)}"

        // 5. Вчера вы потратили (значение траты на сигареты вчера)
        val yesterdaySpend = yesterdayCigs * cigPrice
        binding.tvYesterdaySpend.text = "Вчера вы потратили ${String.format(Locale.US, "%.2f", yesterdaySpend)}"

        // 6. Сегодня вы потратили (значение траты на сигареты сегодня)
        val todaySpend = todayCigs * cigPrice
        binding.tvTodaySpend.text = "Сегодня вы потратили ${String.format(Locale.US, "%.2f", todaySpend)}"

        // 7. Сначала интервал между перекурами был (значение интервал между перекурами при запуске приложения) минут
        binding.tvInitialInterval.text = "Сначала интервал между перекурами был $initialInterval минут"

        // 8. Вчера интервал между перекурами был (значение интервал между перекурами вчера) минут
        // Предполагаем, что интервал вчера был таким же, как текущий, если не отслеживается изменение
        val yesterdayInterval = mainActivity.getCurrentInterval() // Или другой способ подсчёта, если есть
        binding.tvYesterdayInterval.text = "Вчера интервал между перекурами был $yesterdayInterval минут"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}