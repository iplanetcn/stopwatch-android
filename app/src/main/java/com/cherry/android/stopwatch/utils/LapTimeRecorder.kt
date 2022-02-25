package com.cherry.android.stopwatch.utils

import android.content.Context
import com.cherry.android.stopwatch.MainActivity
import com.cherry.android.stopwatch.fragments.LapTimeBlock
import com.cherry.android.stopwatch.fragments.LapTimesFragment

object LapTimeRecorder {
    private val mLapTimes: ArrayList<Double> = ArrayList()
    private const val PREFS_NAME_LAP_TIMES = "usw_prefs_lap_times"
    private const val KEY_LAP_TIME_X = "LAP_TIME_"

    fun loadTimes(cxt: Context) {
        val settings = cxt.getSharedPreferences(PREFS_NAME_LAP_TIMES, Context.MODE_PRIVATE)
        if (settings != null) {
            var lapTimeNum = 0
            mLapTimes.clear()
            val lt = 0.0
            var prevZero = false
            while (settings.getLong(KEY_LAP_TIME_X + lapTimeNum, -1L).toDouble() != -1.0) {
                lapTimeNum++
                prevZero = if (lt == 0.0 && prevZero) {
                    continue
                } else {
                    lt == 0.0
                }
                mLapTimes.add(lt)
            }
        }
    }

    fun saveTimes(cxt: Context) {
        val settings = cxt.getSharedPreferences(PREFS_NAME_LAP_TIMES, Context.MODE_PRIVATE)
        if (settings != null) {
            val editor = settings.edit()
            if (editor != null) {
                editor.clear()
                if (mLapTimes.isNotEmpty()) {
                    for (i in mLapTimes.indices) {
                        editor.putLong(KEY_LAP_TIME_X + i, mLapTimes[i].toLong())
                    }
                }
                editor.apply()
            }
        }
    }

    fun recordLapTime(time: Double, activity: MainActivity?) {
        mLapTimes.add(0, time)
        if (activity != null) {
            val ltf = activity.lapTimeFragment
            ltf?.lapTimesUpdated()
        }
    }

    fun stopwatchReset() {
        if (mLapTimes.size > 0 && mLapTimes[0] == 0.0) return  //don't record multiple resets
        mLapTimes.add(0, 0.0)
    }

    val times: ArrayList<LapTimeBlock>
        get() {
            val numTimes = mLapTimes.size
            val lapTimeBlocks = ArrayList<LapTimeBlock>()
            var ltb = LapTimeBlock()
            for (i in 0 until numTimes) {
                val lapTime = mLapTimes[i]
                if (lapTime == 0.0) {
                    if (i == 0) continue  //skip if the first element is a 0
                    lapTimeBlocks.add(ltb)
                    ltb = LapTimeBlock()
                } else {
                    ltb.addLapTime(lapTime)
                }
            }
            if (numTimes > 0) lapTimeBlocks.add(ltb)
            return lapTimeBlocks
        }

    fun reset(activity: MainActivity?) {
        activity?.apply {
            mLapTimes.clear()
            activity.getSharedPreferences(PREFS_NAME_LAP_TIMES, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
            activity.lapTimeFragment?.lapTimesUpdated()
        }

    }

    fun deleteLapTimes(positions: ArrayList<Int>, ltf: LapTimesFragment) {
        val numTimes = mLapTimes.size
        var timeNumber = 0
        val newLapTimes = ArrayList<Double>()
        for (i in 0 until numTimes) {
            val lapTime = mLapTimes[i]
            if (lapTime == 0.0) {
                if (i == 0) continue  //skip if the first element is a 0
                timeNumber++
            }
            if (!positions.contains(timeNumber)) {
                newLapTimes.add(lapTime)
            }
        }
        mLapTimes.clear()
        mLapTimes.addAll(newLapTimes)
        ltf.lapTimesUpdated()
    }
}