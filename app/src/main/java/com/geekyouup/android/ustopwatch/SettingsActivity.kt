package com.geekyouup.android.ustopwatch

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Vibrator
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    /**
     * Called when the activity is first created.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)
        val mSwitchSoundTicking = findViewById<CompoundButton>(R.id.settings_seconds_sound)
        mSwitchSoundTicking.setOnCheckedChangeListener { compoundButton: CompoundButton?, b: Boolean ->
            isTicking = b
        }
        val mSwitchAnimating = findViewById<CompoundButton>(R.id.settings_animations)
        mSwitchAnimating.setOnCheckedChangeListener { compoundButton: CompoundButton?, b: Boolean ->
            isAnimating = b
        }
        val mSwitchEndlessAlarm = findViewById<CompoundButton>(R.id.settings_endless_alert)
        mSwitchEndlessAlarm.setOnCheckedChangeListener { compoundButton: CompoundButton?, b: Boolean ->
            isEndlessAlarm = b
        }
        val mSwitchVibrate = findViewById<CompoundButton>(R.id.settings_vibrate)
        mSwitchVibrate.setOnCheckedChangeListener { compoundButton: CompoundButton?, b: Boolean ->
            isVibrate = b
        }
        mSwitchEndlessAlarm.isChecked = isEndlessAlarm
        mSwitchSoundTicking.isChecked = isTicking
        mSwitchVibrate.isChecked = isVibrate
        if (!(getSystemService(VIBRATOR_SERVICE) as Vibrator).hasVibrator()) {
            mSwitchVibrate.isChecked = false
            mSwitchVibrate.isEnabled = false
        }
        mSwitchAnimating.isChecked = isAnimating
    }

    override fun onPause() {
        super.onPause()
        val settings =
            getSharedPreferences(UltimateStopwatchActivity.Companion.PREFS_NAME, MODE_PRIVATE)
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