package com.geekyouup.android.ustopwatch.fragments

import android.content.Context
import com.geekyouup.android.ustopwatch.UltimateStopwatchActivity

class LapTimeRecorder {
    fun loadTimes(cxt: Context) {
        val settings = cxt.getSharedPreferences(PREFS_NAME_LAPTIMES, Context.MODE_PRIVATE)
        if (settings != null) {
            var lapTimeNum = 0
            mLapTimes!!.clear()
            var lt = 0.0
            var prevZero = false
            while (settings.getLong(KEY_LAPTIME_X + lapTimeNum, -1L).toDouble() != -1.0) {
                lapTimeNum++
                prevZero = if (lt == 0.0 && prevZero) {
                    continue
                } else lt == 0.0
                mLapTimes!!.add(lt)
            }
        }
    }

    fun saveTimes(cxt: Context) {
        val settings = cxt.getSharedPreferences(PREFS_NAME_LAPTIMES, Context.MODE_PRIVATE)
        if (settings != null) {
            val editor = settings.edit()
            if (editor != null) {
                editor.clear()
                if (mLapTimes != null && mLapTimes!!.size > 0) {
                    for (i in mLapTimes!!.indices) {
                        editor.putLong(KEY_LAPTIME_X + i, mLapTimes!![i].toLong())
                    }
                }
                editor.apply()
            }
        }
    }

    fun recordLapTime(time: Double, activity: UltimateStopwatchActivity?) {
        mLapTimes!!.add(0, time)
        if (activity != null) {
            val ltf = activity.lapTimeFragment
            ltf?.lapTimesUpdated()
        }
    }

    fun stopwatchReset() {
        if (mLapTimes!!.size > 0 && mLapTimes!![0] == 0.0) return  //don't record multiple resets
        mLapTimes!!.add(0, 0.0)
    }

    //skip if the first element is a 0
    val times: ArrayList<LapTimeBlock>
        get() {
            val numTimes = mLapTimes!!.size
            val lapTimeBlocks = ArrayList<LapTimeBlock>()
            var ltb = LapTimeBlock()
            for (i in 0 until numTimes) {
                val laptime = mLapTimes!![i]
                if (laptime == 0.0) {
                    if (i == 0) continue  //skip if the first element is a 0
                    lapTimeBlocks.add(ltb)
                    ltb = LapTimeBlock()
                } else {
                    ltb.addLapTime(laptime)
                }
            }
            if (numTimes > 0) lapTimeBlocks.add(ltb)
            return lapTimeBlocks
        }

    fun reset(activity: UltimateStopwatchActivity?) {
        mLapTimes!!.clear()
        val settings = activity!!.getSharedPreferences(PREFS_NAME_LAPTIMES, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.clear()
        editor.commit()
        if (activity != null) {
            val ltf = activity.lapTimeFragment
            ltf?.lapTimesUpdated()
        }
    }

    fun deleteLapTimes(positions: ArrayList<Int?>?, ltf: LapTimesFragment) {
        val numTimes = mLapTimes!!.size
        var timeNumber = 0
        val newLapTimes = ArrayList<Double>()
        for (i in 0 until numTimes) {
            val laptime = mLapTimes!![i]
            if (laptime == 0.0) {
                if (i == 0) continue  //skip if the first element is a 0
                timeNumber++
            }
            if (!positions!!.contains(timeNumber)) {
                newLapTimes.add(laptime)
            }
        }
        mLapTimes = newLapTimes
        ltf.lapTimesUpdated()
    }

    companion object {
        private var mLapTimes: ArrayList<Double>? = ArrayList()
        private const val PREFS_NAME_LAPTIMES = "usw_prefs_laptimes"
        private const val KEY_LAPTIME_X = "LAPTIME_"
        private var mSelf: LapTimeRecorder? = null

        //if a 0 laptime is stored then it is a reset signal and start of new block
        val instance: LapTimeRecorder?
            get() {
                if (mSelf == null) mSelf = LapTimeRecorder()
                return mSelf
            }
    }
}