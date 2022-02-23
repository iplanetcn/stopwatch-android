package com.geekyouup.android.ustopwatch

import android.content.Context
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.TextAppearanceSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.floor

object TimeUtils {
    private const val START_TIME = "00:00:00.000"
    var dlgSecs = 0
        private set
    var dlgMins = 0
        private set
    var dlgHours = 0
        private set

    fun createTimeString(time: Double): String {
        var time = time
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

    fun createTimeSelectDialogLayout(
        cxt: Context?,
        layoutInflater: LayoutInflater,
        hours: Int,
        mins: Int,
        secs: Int
    ): View {
        val countdownView = layoutInflater.inflate(R.layout.countdown, null)
        if (dlgSecs == 0) dlgSecs = secs
        if (dlgMins == 0) dlgMins = mins
        if (dlgHours == 0) dlgHours = hours
        val mSecsText = countdownView.findViewById<TextView>(R.id.secsTxt)
        val mMinsText = countdownView.findViewById<TextView>(R.id.minsTxt)
        val mHoursText = countdownView.findViewById<TextView>(R.id.hoursTxt)
        mSecsText.text = dlgSecs.toString()
        mSecsText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty()) {
                    dlgSecs = 0
                } else {
                    try {
                        dlgSecs = s.toString().toInt()
                    } catch (ignored: Exception) {
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })
        mMinsText.text = dlgMins.toString()
        mMinsText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty()) {
                    dlgMins = 0
                } else {
                    try {
                        dlgMins = s.toString().toInt()
                    } catch (ignored: Exception) {
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })
        mHoursText.text = dlgHours.toString()
        mHoursText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty()) {
                    dlgHours = 0
                } else try {
                    dlgHours = s.toString().toInt()
                } catch (ignored: Exception) {
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })
        val mSecsIncr = countdownView.findViewById<View>(R.id.secsBtnUp) as Button
        mSecsIncr.setOnClickListener { v: View? ->
            dlgSecs = (dlgSecs + 1) % 60
            mSecsText.text = dlgSecs.toString()
        }
        val mSecsDown = countdownView.findViewById<View>(R.id.secsBtnDn) as Button
        mSecsDown.setOnClickListener {
            dlgSecs--
            if (dlgSecs < 0) dlgSecs += 60
            mSecsText.text = dlgSecs.toString()
        }
        val mMinsIncr = countdownView.findViewById<View>(R.id.minsBtnUp) as Button
        mMinsIncr.setOnClickListener {
            dlgMins = (dlgMins + 1) % 60
            mMinsText.text = dlgMins.toString()
        }
        val mMinsDown = countdownView.findViewById<View>(R.id.minsBtnDn) as Button
        mMinsDown.setOnClickListener {
            dlgMins--
            if (dlgMins < 0) dlgMins += 60
            mMinsText.text = dlgMins.toString()
        }
        val mHoursIncr = countdownView.findViewById<View>(R.id.hoursBtnUp) as Button
        mHoursIncr.setOnClickListener {
            dlgHours = (dlgHours + 1) % 100
            mHoursText.text = dlgHours.toString()
        }
        val mHoursDown = countdownView.findViewById<View>(R.id.hoursBtnDn) as Button
        mHoursDown.setOnClickListener {
            dlgHours--
            if (dlgHours < 0) dlgHours += 100
            mHoursText.text = dlgHours.toString()
        }
        val ll = LinearLayout(cxt)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.addView(countdownView)
        ll.gravity = Gravity.CENTER
        return ll
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