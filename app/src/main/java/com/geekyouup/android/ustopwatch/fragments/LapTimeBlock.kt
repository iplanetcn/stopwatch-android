package com.geekyouup.android.ustopwatch.fragments

/**
 * Created with IntelliJ IDEA.
 * User: rhyndman
 * Date: 11/23/12
 * Time: 3:58 PM
 */
class LapTimeBlock {
    var lapTimes: ArrayList<Double> = ArrayList()
    fun addLapTime(lapTime: Double) {
        lapTimes.add(lapTime)
    }

}