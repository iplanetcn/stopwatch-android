@file:Suppress("DEPRECATION")

package com.geekyouup.android.ustopwatch.compat

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.appcompat.app.AppCompatActivity

fun Context.compatVibrator(): Vibrator {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            this.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator
    }
}

fun Context.startVibrate(milliseconds: Long) {
    val vibrator = compatVibrator()
    // 判断当前设备是否有震动器
    if (vibrator.hasVibrator()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // long milliseconds：震动毫秒数, int amplitude：震动强度，该值必须介于 1 ～ 255 之间，或者 DEFAULT_AMPLITUDE
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    milliseconds,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator.vibrate(milliseconds)
        }
    }
}