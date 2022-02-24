package com.cherry.android.stopwatch.fragments

class LapTimeBlock {
    var lapTimes: ArrayList<Double> = ArrayList()
    fun addLapTime(lapTime: Double) {
        lapTimes.add(lapTime)
    }
}