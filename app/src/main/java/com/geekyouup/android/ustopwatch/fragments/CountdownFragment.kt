package com.geekyouup.android.ustopwatch.fragments

import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.*
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import com.geekyouup.android.ustopwatch.*


class CountdownFragment : Fragment() {
    private var mCountdownView: StopwatchCustomView? = null
    private var mCurrentTimeMillis = 0.0
    private lateinit var mResetButton: Button
    private lateinit var mStartButton: Button
    private lateinit var mTimerText: TextView
    private var mSoundManager: SoundManager? = null
    private var mLastHour = 0
    private var mLastMin = 0
    private var mLastSec = 0
    private var mRunningState = false
    private var mLastSecondTicked = 0

    //countdown picker dialog variables
    private var mDialogOnScreen = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mSoundManager = SoundManager.getInstance(activity)
        val cdView = inflater.inflate(R.layout.countdown_fragment, null)
        mCountdownView = cdView.findViewById(R.id.cdview)
        mTimerText = cdView.findViewById(R.id.time_counter)
        mTimerText.setOnTouchListener { v, event ->
            if (mCurrentTimeMillis == 0.0) {
                requestTimeDialog()
            }
            true
        }
        mResetButton = cdView.findViewById(R.id.resetButton)
        mResetButton.setOnClickListener {
            reset()
            mSoundManager!!.stopEndlessAlarm()
            mSoundManager!!.playSound(SoundManager.SOUND_RESET)
        }
        mStartButton = cdView.findViewById(R.id.startButton)
        mStartButton.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                startStop()
                return@setOnTouchListener false
            }
            false
        }
        return cdView
    }

    override fun onPause() {
        super.onPause()
        val settings = requireActivity().getSharedPreferences(COUNTDOWN_PREFS, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putBoolean(PREF_IS_RUNNING, mRunningState)
        mCountdownView!!.saveState(editor)
        editor.putInt(KEY_LAST_HOUR, mLastHour)
        editor.putInt(KEY_LAST_MIN, mLastMin)
        editor.putInt(KEY_LAST_SEC, mLastSec)
        editor.apply()
        mCountdownView!!.stop()
    }

    override fun onResume() {
        super.onResume()
        // cancel next alarm if there is one, and clear notification bar
        AlarmUpdater.cancelCountdownAlarm(activity)
        mCountdownView!!.handler = object : Handler(Looper.myLooper()!!) {
            override fun handleMessage(m: Message) {
                if (m.data.getBoolean(MSG_REQUEST_ICON_FLASH, false)) {
                    (activity as UltimateStopwatchActivity?)!!.flashResetTimeIcon()
                } else if (m.data.getBoolean(MSG_COUNTDOWN_COMPLETE, false)) {
                    val appResuming = m.data.getBoolean(MSG_APP_RESUMING, false)
                    if (!appResuming) {
                        mSoundManager!!.playSound(
                            SoundManager.SOUND_COUNTDOWN_ALARM,
                            SettingsActivity.isEndlessAlarm
                        )
                        if (SettingsActivity.isVibrate && activity != null) {
                            val vibrator =
                                activity!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                            vibrator.vibrate(1000)
                        }
                    }
                    reset(!appResuming && SettingsActivity.isEndlessAlarm)
                } else if (m.data.getBoolean(
                        UltimateStopwatchActivity.MSG_UPDATE_COUNTER_TIME,
                        false
                    )
                ) {
                    mCurrentTimeMillis = m.data.getDouble(
                        UltimateStopwatchActivity.MSG_NEW_TIME_DOUBLE
                    )

                    //If we've crossed into a new second then make the tick sound
                    val currentSecond = mCurrentTimeMillis.toInt() / 1000
                    if (currentSecond > mLastSecondTicked) {
                        mSoundManager!!.doTick()
                    }
                    mLastSecondTicked = currentSecond
                    setTime(mCurrentTimeMillis)
                } else if (m.data.getBoolean(
                        UltimateStopwatchActivity.MSG_STATE_CHANGE,
                        false
                    )
                ) {
                    setUIState(false)
                }
            }
        }
        val settings = requireActivity().getSharedPreferences(COUNTDOWN_PREFS, Context.MODE_PRIVATE)
        mLastHour = settings.getInt(KEY_LAST_HOUR, 0)
        mLastMin = settings.getInt(KEY_LAST_MIN, 0)
        mLastSec = settings.getInt(KEY_LAST_SEC, 0)
        mRunningState = settings.getBoolean(PREF_IS_RUNNING, false)
        mCountdownView!!.restoreState(settings)
        mCurrentTimeMillis = mCountdownView!!.watchTime
        (activity as UltimateStopwatchActivity?)!!.registerCountdownFragment(this)
        val paint = Paint()
        val bounds = Rect()
        paint.typeface = Typeface.SANS_SERIF // your preference here
        paint.textSize =
            resources.getDimension(R.dimen.counter_font) // have this the same as your text size
        val text = "-00:00:00.000"
        paint.getTextBounds(text, 0, text.length, bounds)
        val text_width = bounds.width()
        var width = resources.displayMetrics.widthPixels
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            width /= 2
        }
        mTimerText.setPadding((width - text_width) / 2, 0, 0, 0)
    }

    fun startStop() {
        if (!isRunning && mCurrentTimeMillis == 0.0) {
            //flash the choose time button in the action bar
            (activity as UltimateStopwatchActivity?)!!.flashResetTimeIcon()
        } else {
            mCountdownView!!.startStop()
            mResetButton.isEnabled = true
            mStartButton.text =
                if (isRunning) getString(R.string.pause) else getString(R.string.start)
        }
    }

    @JvmOverloads
    fun reset(endlessAlarmSounding: Boolean = false) {
        mResetButton.isEnabled = endlessAlarmSounding
        mStartButton.text = if (isAdded) getString(R.string.start) else "START"
        mCountdownView!!.setTime(mLastHour, mLastMin, mLastSec, true)
    }

    fun setTime(hour: Int, minute: Int, seconds: Int, disableReset: Boolean) {
        mLastHour = hour
        mLastMin = minute
        mLastSec = seconds
        mCountdownView!!.setTime(hour, minute, seconds, false)
        setUIState(disableReset)
    }

    private fun setTime(millis: Double) {
        mTimerText.text = TimeUtils.createStyledSpannableString(
            activity,
            millis,
            false
        )
    }

    val isRunning: Boolean
        get() = mCountdownView != null && mCountdownView!!.isRunning

    private fun setUIState(disableReset: Boolean) {
        val stateChanged = mRunningState != isRunning
        mRunningState = isRunning
        mResetButton!!.isEnabled = mSoundManager!!.isEndlessAlarmSounding || !disableReset
        if (!mRunningState && mCurrentTimeMillis == 0.0 && mHoursValue == 0 && mMinsValue == 0 && mSecsValue == 0 && isAdded) {
            mStartButton!!.text = getString(R.string.start)
        } else {
            if (isAdded) {
                mStartButton!!.text =
                    if (mRunningState) getString(R.string.pause) else getString(R.string.start)
            }
            if (stateChanged) {
                mSoundManager!!.playSound(if (isRunning) SoundManager.Companion.SOUND_START else SoundManager.Companion.SOUND_STOP)
            }
        }
    }

    fun requestTimeDialog() {
        if (mHoursValue == 0) {
            mHoursValue = mLastHour
        }
        if (mMinsValue == 0) {
            mMinsValue = mLastMin
        }
        if (mSecsValue == 0) {
            mSecsValue = mLastSec
        }
        requestAPI11TimeDialog()
    }

    private fun requestAPI11TimeDialog() {
        // stop stacking of dialogs
        if (mDialogOnScreen) {
            return
        }
        val wrapper = ContextThemeWrapper(activity, R.style.Theme_theme_usw)
        val inflater = wrapper.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val ll = inflater.inflate(R.layout.countdown_picker, null)
        val npHours = ll.findViewById<View>(R.id.numberPickerHours) as NumberPicker
        npHours.maxValue = 99
        npHours.value = mHoursValue
        val npMins = ll.findViewById<View>(R.id.numberPickerMins) as NumberPicker
        npMins.maxValue = 59
        npMins.value = mMinsValue
        val npSecs = ll.findViewById<View>(R.id.numberPickerSecs) as NumberPicker
        npSecs.maxValue = 59
        npSecs.value = mSecsValue
        val mSelectTime = AlertDialog.Builder(wrapper).create()
        mSelectTime.setView(ll)
        mSelectTime.setTitle(getString(R.string.timer_title))
        mSelectTime.setButton(
            AlertDialog.BUTTON_POSITIVE, getString(R.string.timer_start)
        ) { _: DialogInterface?, _: Int ->
            mDialogOnScreen = false
            mHoursValue = npHours.value
            mMinsValue = npMins.value
            mSecsValue = npSecs.value
            setTime(
                mHoursValue,
                mMinsValue, mSecsValue, true
            )
        }
        mSelectTime.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.timer_cancel)) { _, _ ->
            mDialogOnScreen = false }
        mSelectTime.setOnCancelListener {
            mDialogOnScreen = false
        }
        mSelectTime.show()
        mDialogOnScreen = true
    }

    companion object {
        private const val COUNTDOWN_PREFS = "USW_CDFRAG_PREFS"
        private const val PREF_IS_RUNNING = "key_countdown_is_running"
        private const val KEY_LAST_HOUR = "key_last_hour"
        private const val KEY_LAST_MIN = "key_last_min"
        private const val KEY_LAST_SEC = "key_last_sec"
        const val MSG_REQUEST_ICON_FLASH = "msg_flash_icon"
        const val MSG_COUNTDOWN_COMPLETE = "msg_countdown_complete"
        const val MSG_APP_RESUMING = "msg_app_resuming"
        private var mHoursValue = 0
        private var mMinsValue = 0
        private var mSecsValue = 0
    }
}