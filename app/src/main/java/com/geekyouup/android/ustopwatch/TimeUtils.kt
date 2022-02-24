package com.geekyouup.android.ustopwatch

import android.content.Context
import android.text.SpannableString
import android.text.style.TextAppearanceSpan
import android.util.Log
import kotlin.math.floor

object TimeUtils {
    private const val START_TIME = "00:00:00.000"
    private fun createTimeString(_time: Double): String {
        var time = _time
        if (time == 0.0) return START_TIME
        var isNeg = false
        if (time < 0) {
            isNeg = true
            time = -time
        }
        val numHours = floor(time / 3600000).toInt()
        val numMins = floor(time / 60000 - numHours * 60).toInt()
        val numSecs = floor(time / 1000 - numMins * 60 - numHours * 3600)
            .toInt()
        val numMillis = (time - numHours * 3600000 - numMins * 60000 - numSecs * 1000).toInt()
        return ((if (isNeg) "-" else "") + ((if (numHours < 10) "0" else "") + numHours) + ":" + ((if (numMins < 10) "0" else "") + numMins) + ":"
                + ((if (numSecs < 10) "0" else "") + numSecs) + "." + (if (numMillis < 10) "00" else if (numMillis < 100) "0" else "")
                + numMillis)
    }

    fun createStyledSpannableString(
        context: Context?,
        time: Double,
        lightTheme: Boolean
    ): SpannableString? {
        val text = createTimeString(time)
        return createSpannableString(context, text, lightTheme)
    }

    private fun createSpannableString(
        context: Context?,
        timeText: String?,
        lightTheme: Boolean
    ): SpannableString? {
        var sString: SpannableString? = null
        try {
            if (timeText != null && context != null) {
                val textLength = timeText.length
                //calculate the span for the text colouring
                var lastLightChar = 0
                for (i in 0 until textLength) {
                    lastLightChar =
                        if (timeText[i] == '0' || timeText[i] == ':' || timeText[i] == '.' || timeText[i] == '-') {
                            i + 1
                        } else {
                            break
                        }
                }
                sString = SpannableString(timeText)
                if (lastLightChar > 0) {
                    if (lastLightChar > 0) sString.setSpan(
                        TextAppearanceSpan(
                            context,
                            if (lightTheme) R.style.TimeTextLight else R.style.TimeTextDarkThemeDark
                        ), 0, lastLightChar, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    if (lastLightChar < textLength) sString.setSpan(
                        TextAppearanceSpan(
                            context,
                            if (lightTheme) R.style.TimeTextDark else R.style.TimeTextDarkThemeLight
                        ), lastLightChar, textLength, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("USW", "Switched Fragment Error", e)
        }
        return sString
    }
}