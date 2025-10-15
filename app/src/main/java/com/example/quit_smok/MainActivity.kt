package com.example.quit_smok

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.quit_smok.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация prefs ПЕРЕД любой инфлейтингом layout
        prefs = getSharedPreferences("quit_smok_prefs", Context.MODE_PRIVATE)

        // Теперь инфлейтим binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Настройка навигации
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_timer, R.id.navigation_settings, R.id.navigation_statistics
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Проверка сохранённых настроек и навигация
        if (areSettingsSaved()) {
            navController.navigate(R.id.navigation_timer)
        } else {
            navController.navigate(R.id.navigation_settings)
        }
    }

    fun areSettingsSaved(): Boolean {
        return prefs.contains("first_smoke_time") &&
                prefs.contains("last_smoke_time") &&
                prefs.contains("cigarettes_per_day") &&
                prefs.contains("increase_interval") &&
                prefs.contains("pack_price")
    }

    fun saveSettings(
        firstTime: LocalTime,
        lastTime: LocalTime,
        cigs: Int,
        increase: Int,
        packPrice: Double
    ) {
        val awakeMinutes = calculateAwakeMinutes(firstTime, lastTime)
        val interval = awakeMinutes / cigs
        val cigPrice = packPrice / 20.0

        with(prefs.edit()) {
            putString("first_smoke_time", firstTime.toString())
            putString("last_smoke_time", lastTime.toString())
            putInt("awake_minutes", awakeMinutes)
            putInt("cigarettes_per_day_initial", cigs)
            putInt("interval_minutes_initial", interval)
            putInt("increase_interval", increase)
            putFloat("pack_price", packPrice.toFloat())
            putFloat("cigarette_price", cigPrice.toFloat())
            putInt("current_interval", interval)
            putLong("last_smoke_timestamp", 0L)
            apply()
        }
    }

    fun resetSettings() {
        prefs.edit().clear().apply()
        findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.navigation_settings)
    }

    fun getFirstSmokeTime(): LocalTime = LocalTime.parse(prefs.getString("first_smoke_time", "00:00")!!)

    fun getLastSmokeTime(): LocalTime = LocalTime.parse(prefs.getString("last_smoke_time", "00:00")!!)

    fun getInitialCigsPerDay(): Int = prefs.getInt("cigarettes_per_day_initial", 0)

    fun getInitialInterval(): Int = prefs.getInt("interval_minutes_initial", 0)

    fun getIncreaseInterval(): Int = prefs.getInt("increase_interval", 0)

    fun getCigarettePrice(): Double = prefs.getFloat("cigarette_price", 0f).toDouble()

    fun getCurrentInterval(): Int = prefs.getInt("current_interval", getInitialInterval())

    fun setCurrentInterval(interval: Int) {
        prefs.edit().putInt("current_interval", interval).apply()
    }

    fun getLastSmokeTimestamp(): Long = prefs.getLong("last_smoke_timestamp", 0L)

    fun setLastSmokeTimestamp(timestamp: Long) {
        prefs.edit().putLong("last_smoke_timestamp", timestamp).apply()
    }

    fun calculateAwakeMinutes(first: LocalTime, last: LocalTime): Int {
        val firstMinutes = first.hour * 60 + first.minute
        var lastMinutes = last.hour * 60 + last.minute
        if (lastMinutes < firstMinutes) lastMinutes += 24 * 60
        return lastMinutes - firstMinutes
    }

    fun isWithinAwakeTime(now: LocalTime): Boolean {
        val first = getFirstSmokeTime()
        val last = getLastSmokeTime()
        if (last.isBefore(first)) {
            return now.isAfter(first) || now.isBefore(last)
        } else {
            return now.isAfter(first) && now.isBefore(last)
        }
    }

    fun recordSmoke() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        val gson = Gson()
        val type = object : TypeToken<MutableMap<String, Int>>() {}.type
        val dailySmokes: MutableMap<String, Int> = gson.fromJson(
            prefs.getString("daily_smokes", "{}"),
            type
        ) ?: mutableMapOf()

        dailySmokes[today] = (dailySmokes[today] ?: 0) + 1
        prefs.edit().putString("daily_smokes", gson.toJson(dailySmokes)).apply()
    }

    fun getSmokesForDate(date: LocalDate): Int {
        val gson = Gson()
        val type = object : TypeToken<Map<String, Int>>() {}.type
        val dailySmokes: Map<String, Int> = gson.fromJson(
            prefs.getString("daily_smokes", "{}"),
            type
        ) ?: mapOf()

        return dailySmokes[date.format(DateTimeFormatter.ISO_DATE)] ?: 0
    }
}