package com.cherry.android.stopwatch

import android.app.Application
import com.cherry.android.stopwatch.manager.SoundManager

/**
 * App
 *
 * @author john
 * @since 2022-02-24
 */
@Suppress("unused")
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        SoundManager.init(this)
    }
}