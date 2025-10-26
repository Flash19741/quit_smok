package com.example.quit_smok

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.example.quit_smok.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import java.time.Duration
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("quit_smok_prefs", Context.MODE_PRIVATE)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_timer, R.id.navigation_settings, R.id.navigation_statistics
            )
        )
        navView.setupWithNavController(navController)

        if (!areSettingsSaved()) {
            navController.navigate(R.id.navigation_settings)
        } else {
            navController.navigate(R.id.navigation_timer)
        }
    }

    fun areSettingsSaved(): Boolean {
        return prefs.contains("first_smoke_time") &&
                prefs.contains("last_smoke_time") &&
                prefs.contains("initial_cigs_per_day") &&
                prefs.contains("increase_interval") &&
                prefs.contains("pack_price")
    }

    fun saveSettings(first: LocalTime, last: LocalTime, cigsPerDay: Int, increaseInterval: Int, packPrice: Double) {
        val awakeMinutes = calculateAwakeMinutes(first, last)
        val intervalMinutes = if (cigsPerDay > 0) awakeMinutes / cigsPerDay else 0
        val cigPrice = packPrice / 20

        prefs.edit().apply {
            putString("first_smoke_time", first.format(DateTimeFormatter.ISO_LOCAL_TIME))
            putString("last_smoke_time", last.format(DateTimeFormatter.ISO_LOCAL_TIME))
            putInt("initial_cigs_per_day", cigsPerDay)
            putInt("increase_interval", increaseInterval)
            putFloat("pack_price", packPrice.toFloat())
            putFloat("cigarette_price", cigPrice.toFloat())
            putInt("initial_interval", intervalMinutes)
            putInt("current_interval", intervalMinutes)
            putLong("last_smoke_timestamp", 0L)
            apply()
        }
    }

    fun resetSettings() {
        prefs.edit().clear().apply()
        findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_settings)
    }

    fun getFirstSmokeTime(): LocalTime {
        return LocalTime.parse(prefs.getString("first_smoke_time", "00:00")!!, DateTimeFormatter.ISO_LOCAL_TIME)
    }

    fun getLastSmokeTime(): LocalTime {
        return LocalTime.parse(prefs.getString("last_smoke_time", "00:00")!!, DateTimeFormatter.ISO_LOCAL_TIME)
    }

    fun getInitialCigsPerDay(): Int {
        return prefs.getInt("initial_cigs_per_day", 0)
    }

    fun getIncreaseInterval(): Int {
        return prefs.getInt("increase_interval", 0)
    }

    fun getCigarettePrice(): Double {
        return prefs.getFloat("cigarette_price", 0f).toDouble()
    }

    fun getInitialInterval(): Int {
        return prefs.getInt("initial_interval", 0)
    }

    fun getCurrentInterval(): Int {
        return prefs.getInt("current_interval", getInitialInterval())
    }

    fun setCurrentInterval(interval: Int) {
        prefs.edit().putInt("current_interval", interval).apply()
    }

    fun getLastSmokeTimestamp(): Long {
        return prefs.getLong("last_smoke_timestamp", 0L)
    }

    fun setLastSmokeTimestamp(timestamp: Long) {
        prefs.edit().putLong("last_smoke_timestamp", timestamp).apply()
    }

    fun calculateAwakeMinutes(first: LocalTime, last: LocalTime): Int {
        var awakeMinutes = Duration.between(first, last).toMinutes().toInt()
        if (awakeMinutes < 0) {
            awakeMinutes += 24 * 60
        }
        return awakeMinutes
    }

    fun isWithinAwakeTime(now: LocalTime): Boolean {
        val first = getFirstSmokeTime()
        val last = getLastSmokeTime()
        return if (last.isAfter(first)) {
            now.isAfter(first) && now.isBefore(last)
        } else {
            now.isAfter(first) || now.isBefore(last)
        }
    }

    fun recordSmoke() {
        val today = LocalDate.now().toString()
        val dailySmokes = getDailySmokes().toMutableMap()
        val currentCount = dailySmokes.getOrDefault(today, 0)
        dailySmokes[today] = currentCount + 1
        prefs.edit().putString("daily_smokes", gson.toJson(dailySmokes)).apply()
    }

    fun getSmokesForDate(date: LocalDate): Int {
        return getDailySmokes()[date.toString()] ?: 0
    }

    fun getDailySmokes(): Map<String, Int> {
        val json = prefs.getString("daily_smokes", null)
        return if (json != null) {
            gson.fromJson(json, object : TypeToken<Map<String, Int>>() {}.type)
        } else {
            emptyMap()
        }
    }

    fun calculateTotalSavings(): Double {
        val dailySmokes: Map<String, Int> = getDailySmokes()

        val initialCigsPerDay = getInitialCigsPerDay()
        val cigPrice = getCigarettePrice()
        var totalSavings = 0.0

        for ((_, smokedCigs) in dailySmokes) {
            val expectedSpend = initialCigsPerDay * cigPrice
            val actualSpend = smokedCigs * cigPrice
            totalSavings += expectedSpend - actualSpend
        }

        return totalSavings
    }
}