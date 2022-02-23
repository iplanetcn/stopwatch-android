package com.geekyouup.android.ustopwatch

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.os.VibratorManager
import androidx.appcompat.app.AppCompatActivity
import com.geekyouup.android.ustopwatch.compat.compatVibrator
import com.geekyouup.android.ustopwatch.databinding.SettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: SettingsBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.settingsSecondsSound.setOnCheckedChangeListener { _, isChecked ->
            isTicking = isChecked
        }
        binding.settingsAnimations.setOnCheckedChangeListener { _, isChecked ->
            isAnimating = isChecked
        }
        binding.settingsEndlessAlert.setOnCheckedChangeListener { _, isChecked ->
            isEndlessAlarm = isChecked
        }
        binding.settingsVibrate.setOnCheckedChangeListener { _, isChecked ->
            isVibrate = isChecked
        }
        binding.settingsEndlessAlert.isChecked = isEndlessAlarm
        binding.settingsSecondsSound.isChecked = isTicking
        binding.settingsVibrate.isChecked = isVibrate
        if (!compatVibrator().hasVibrator()) {
            binding.settingsVibrate.isChecked = false
            binding.settingsVibrate.isEnabled = false
        }
        binding.settingsAnimations.isChecked = isAnimating
    }


    override fun onPause() {
        super.onPause()
        val settings =
            getSharedPreferences(UltimateStopwatchActivity.PREFS_NAME, MODE_PRIVATE)
        val editor = settings.edit()
        editor.putBoolean(KEY_TICKING, isTicking)
        editor.putBoolean(KEY_ENDLESS_ALARM, isEndlessAlarm)
        editor.putBoolean(KEY_VIBRATE, isVibrate)
        editor.putBoolean(KEY_ANIMATING, isAnimating)
        editor.apply()
    }

    companion object {
        var isTicking = false
            private set
        var isEndlessAlarm = false
            private set
        var isVibrate = true
            private set
        var isAnimating = true
            private set
        private const val KEY_TICKING = "key_ticking_on"
        private const val KEY_ENDLESS_ALARM = "key_endless_alarm_on"
        private const val KEY_VIBRATE = "key_vibrate_on"
        private const val KEY_ANIMATING = "key_animations_on"

        //Called from parent Activity to ensure all settings are always loaded
        fun loadSettings(prefs: SharedPreferences) {
            isTicking = prefs.getBoolean(KEY_TICKING, false)
            isEndlessAlarm = prefs.getBoolean(KEY_ENDLESS_ALARM, false)
            isVibrate = prefs.getBoolean(KEY_VIBRATE, false)
            isAnimating = prefs.getBoolean(KEY_ANIMATING, true)
        }
    }
}