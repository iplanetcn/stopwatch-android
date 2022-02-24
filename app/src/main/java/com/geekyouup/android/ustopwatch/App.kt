package com.geekyouup.android.ustopwatch

import android.app.Application
import com.geekyouup.android.ustopwatch.manager.SoundManager

/**
 * App
 *
 * @author john
 * @since 2022-02-24
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        SoundManager.init(this)
    }
}