package com.geekyouup.android.ustopwatch.fragments

import android.content.Context
import android.content.res.Configuration
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.geekyouup.android.ustopwatch.*
import kotlin.properties.Delegates

class StopwatchFragment : Fragment() {
    private lateinit var mStopwatchView: StopwatchCustomView
    private lateinit var mResetButton: Button
    private lateinit var mStartButton: Button
    private lateinit var mSaveLapTimeButton: Button
    private lateinit var mTimerText: TextView
    private var mCurrentTimeMillis: Double = 0.0
    private var mSoundManager: SoundManager? = null
    private var mRunningState: Boolean = false
    private var mLastSecond: Int = 0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mSoundManager = SoundManager.getInstance(context)
        val swView = inflater.inflate(R.layout.stopwatch_fragment, null)
        mTimerText = swView.findViewById<View>(R.id.counter_text) as TextView
        mStopwatchView = swView.findViewById<View>(R.id.swview) as StopwatchCustomView
        mStartButton = swView.findViewById<View>(R.id.startButton) as Button
        mStartButton.setOnTouchListener { _: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                startStop()
                return@setOnTouchListener false
            }
            false
        }
        mResetButton = swView.findViewById<View>(R.id.resetButton) as Button
        mResetButton.setOnClickListener { v: View? ->
            LapTimeRecorder.instance?.stopwatchReset()
            reset()
        }
        mSaveLapTimeButton = swView.findViewById(R.id.saveButton)
        mSaveLapTimeButton.setOnClickListener {
            if (isRunning) {
                LapTimeRecorder.instance?.recordLapTime(
                    mStopwatchView.watchTime,
                    activity as UltimateStopwatchActivity?
                )
                mSoundManager!!.playSound(SoundManager.SOUND_LAP_TIME)
            }
        }
        return swView
    }

    override fun onPause() {
        super.onPause()
        val settings = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putBoolean(PREF_IS_RUNNING, mRunningState)
        mStopwatchView.saveState(editor)
        editor.apply()
        try {
            if (isRunning && mCurrentTimeMillis > 0) AlarmUpdater.showChronometerNotification(
                activity, mCurrentTimeMillis.toLong()
            )
        } catch (ignored: Exception) {
        }
        mStopwatchView.stop()
    }

    override fun onResume() {
        super.onResume()
        mStopwatchView.handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(m: Message) {
                if (m.data.getBoolean(
                        UltimateStopwatchActivity.MSG_UPDATE_COUNTER_TIME,
                        false
                    )
                ) {
                    mCurrentTimeMillis = m.data.getDouble(UltimateStopwatchActivity.MSG_NEW_TIME_DOUBLE)
                    setTime(mCurrentTimeMillis)
                    val currentSecond = mCurrentTimeMillis.toInt() / 1000
                    if (currentSecond > mLastSecond) {
                        mSoundManager!!.doTick()
                    }
                    mLastSecond = currentSecond
                } else if (m.data.getBoolean(
                        UltimateStopwatchActivity.MSG_STATE_CHANGE,
                        false
                    )
                ) {
                    setUIState()
                }
            }
        }
        AlarmUpdater.cancelChronometerNotification(activity)
        val settings = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        mRunningState = settings.getBoolean(PREF_IS_RUNNING, false)
        mStopwatchView!!.restoreState(settings)
        (activity as UltimateStopwatchActivity?)!!.registerStopwatchFragment(this)

        //center the timer text in a fixed position, stops wiggling numbers
        val paint = Paint()
        val bounds = Rect()
        paint.typeface = Typeface.SANS_SERIF // your preference here
        paint.textSize =
            resources.getDimension(R.dimen.counter_font) // have this the same as your text size
        val counterText = getString(R.string.default_time) //00:00:00.000
        paint.getTextBounds(counterText, 0, counterText.length, bounds)
        val text_width = bounds.width()
        var width = resources.displayMetrics.widthPixels
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) width /= 2
        mTimerText.setPadding((width - text_width) / 2, 0, 0, 0)
    }

    fun startStop() {
        mStopwatchView.startStop()
        setUIState()
    }

    private fun setUIState() {
        val stateChanged = mRunningState != isRunning
        mRunningState = isRunning
        mResetButton.isEnabled = mRunningState || mCurrentTimeMillis != 0.0
        mSaveLapTimeButton.isEnabled = mRunningState || mCurrentTimeMillis != 0.0
        if (isAdded) mStartButton.text =
            if (mRunningState) getString(R.string.pause) else getString(R.string.start)
        if (stateChanged) mSoundManager!!.playSound(if (mRunningState) SoundManager.SOUND_START else SoundManager.Companion.SOUND_STOP)
    }

    fun reset() {
        mStopwatchView.setTime(0, 0, 0, true)
        mSoundManager!!.playSound(SoundManager.SOUND_RESET)
        mResetButton.isEnabled = false
        mSaveLapTimeButton.isEnabled = false
        mStartButton.text = getString(R.string.start)
    }

    private fun setTime(millis: Double) {
        mTimerText.text = TimeUtils.createStyledSpannableString(
            activity, millis, true
        )
    }

    val isRunning: Boolean
        get() = mStopwatchView != null && mStopwatchView!!.isRunning

    companion object {
        private const val PREFS_NAME = "USW_SWFRAG_PREFS"
        private const val PREF_IS_RUNNING = "key_stopwatch_is_running"
    }
}