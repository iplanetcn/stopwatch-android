package io.github.iplanetcn.app.stopwatch.utils

import android.content.Context
import android.text.SpannableString
import android.text.style.TextAppearanceSpan
import io.github.iplanetcn.app.stopwatch.R
import timber.log.Timber
import kotlin.math.floor

object TimeUtils {
    private const val START_TIME = "00:00:00.000"
    private fun createTimeString(pTime: Double): String {
        var time = pTime
        if (time == 0.0) return START_TIME
        var isNeg = false
        if (time < 0) {
            isNeg = true
            time = -time
        }
        val numHours = floor(time / 3600000).toInt()
        val numMinutes = floor(time / 60000 - numHours * 60).toInt()
        val numSecs = floor(time / 1000 - numMinutes * 60 - numHours * 3600)
            .toInt()
        val numMillis = (time - numHours * 3600000 - numMinutes * 60000 - numSecs * 1000).toInt()
        return ((if (isNeg) "-" else "") + ((if (numHours < 10) "0" else "") + numHours) + ":" + ((if (numMinutes < 10) "0" else "") + numMinutes) + ":"
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
                    sString.setSpan(
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
            Timber.e(e,"USW%s", "Switched Fragment Error")
        }
        return sString
    }
}