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

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        val root: View = binding.root  // Теперь это View, а не menu

        // Assume TableLayout in layout with rows

        val initialCigs = (activity as MainActivity).getInitialCigsPerDay()
        val yesterday = LocalDate.now().minusDays(1)
        val yesterdayCigs = (activity as MainActivity).getSmokesForDate(yesterday)
        val cigProgress = if (yesterdayCigs > 0) initialCigs - yesterdayCigs else "-"

        val cigPrice = (activity as MainActivity).getCigarettePrice()
        val initialSpend = initialCigs * cigPrice
        val yesterdaySpend = yesterdayCigs * cigPrice
        val spendProgress = if (yesterdayCigs > 0) initialSpend - yesterdaySpend else "-"

        // Set texts in table cells
        binding.tvCigsInitial.text = initialCigs.toString()
        binding.tvCigsCurrent.text = if (yesterdayCigs > 0) yesterdayCigs.toString() else "-"
        binding.tvCigsProgress.text = cigProgress.toString()

        binding.tvSpendInitial.text = initialSpend.toString()
        binding.tvSpendCurrent.text = if (yesterdayCigs > 0) yesterdaySpend.toString() else "-"
        binding.tvSpendProgress.text = spendProgress.toString()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}