package com.cherry.android.stopwatch.fragments

class LapTimeBlock {
    val lapTimes = mutableListOf<Double>()
    fun addLapTime(lapTime: Double) {
        lapTimes.add(lapTime)
    }
}