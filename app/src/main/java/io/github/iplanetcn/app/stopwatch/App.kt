package io.github.iplanetcn.app.stopwatch

import android.app.Application
import io.github.iplanetcn.app.stopwatch.manager.SoundManager
import timber.log.Timber

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
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}