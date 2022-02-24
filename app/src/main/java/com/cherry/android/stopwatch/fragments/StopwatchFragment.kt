package com.cherry.android.stopwatch.fragments

import android.annotation.SuppressLint
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
import androidx.fragment.app.Fragment
import com.cherry.android.stopwatch.*
import com.cherry.android.stopwatch.databinding.FragmentStopwatchBinding
import com.cherry.android.stopwatch.manager.SoundManager
import com.cherry.android.stopwatch.manager.Sounds

@SuppressLint("ClickableViewAccessibility")
class StopwatchFragment : Fragment() {
    private lateinit var binding: FragmentStopwatchBinding
    private var mCurrentTimeMillis: Double = 0.0
    private var mRunningState: Boolean = false
    private var mLastSecond: Int = 0
    val isRunning get() = binding.stopwatchView.isRunning

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStopwatchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.startButton.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                startStop()
                return@setOnTouchListener false
            }
            false
        }
        binding.resetButton.setOnClickListener {
            LapTimeRecorder.instance?.stopwatchReset()
            reset()
        }
        binding.saveButton.setOnClickListener {
            if (isRunning) {
                LapTimeRecorder.instance?.recordLapTime(
                    binding.stopwatchView.watchTime,
                    activity as MainActivity?
                )
                SoundManager.playSound(requireContext(), Sounds.SOUND_LAP_TIME)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val settings = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = settings.edit()
        editor.putBoolean(PREF_IS_RUNNING, mRunningState)
        binding.stopwatchView.saveState(editor)
        editor.apply()
        try {
            if (isRunning && mCurrentTimeMillis > 0) {
                AlarmUpdater.showChronometerNotification(requireContext(), mCurrentTimeMillis.toLong())
            }
        } catch (ignored: Exception) {
        }
        binding.stopwatchView.stop()
    }

    override fun onResume() {
        super.onResume()
        binding.stopwatchView.handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(m: Message) {
                if (m.data.getBoolean(
                        MainActivity.MSG_UPDATE_COUNTER_TIME,
                        false
                    )
                ) {
                    mCurrentTimeMillis =
                        m.data.getDouble(MainActivity.MSG_NEW_TIME_DOUBLE)
                    setTime(mCurrentTimeMillis)
                    val currentSecond = mCurrentTimeMillis.toInt() / 1000
                    if (currentSecond > mLastSecond) {
                        SoundManager.doTick(requireContext())
                    }
                    mLastSecond = currentSecond
                } else if (m.data.getBoolean(
                        MainActivity.MSG_STATE_CHANGE,
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
        binding.stopwatchView.restoreState(settings)
        (activity as MainActivity?)!!.registerStopwatchFragment(this)

        //center the timer text in a fixed position, stops wiggling numbers
        val paint = Paint()
        val bounds = Rect()
        paint.typeface = Typeface.SANS_SERIF // your preference here
        paint.textSize =
            resources.getDimension(R.dimen.counter_font) // have this the same as your text size
        val counterText = getString(R.string.default_time) //00:00:00.000
        paint.getTextBounds(counterText, 0, counterText.length, bounds)
        val textWidth = bounds.width()
        var width = resources.displayMetrics.widthPixels
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) width /= 2
        binding.counterText.setPadding((width - textWidth) / 2, 0, 0, 0)
    }

    private fun startStop() {
        binding.stopwatchView.startStop()
        setUIState()
    }

    private fun setUIState() {
        val stateChanged = mRunningState != isRunning
        mRunningState = isRunning
        binding.resetButton.isEnabled = mRunningState || mCurrentTimeMillis != 0.0
        binding.saveButton.isEnabled = mRunningState || mCurrentTimeMillis != 0.0
        if (isAdded) binding.startButton.text =
            if (mRunningState) getString(R.string.pause) else getString(R.string.start)
        if (stateChanged) SoundManager.playSound(requireContext(), if (mRunningState) Sounds.SOUND_START else Sounds.SOUND_STOP)
    }

    fun reset() {
        binding.stopwatchView.setTime(0, 0, 0, true)
        SoundManager.playSound(requireContext(), Sounds.SOUND_RESET)
        binding.resetButton.isEnabled = false
        binding.saveButton.isEnabled = false
        binding.startButton.text = getString(R.string.start)
    }

    private fun setTime(millis: Double) {
        binding.counterText.text = TimeUtils.createStyledSpannableString(
            activity, millis, true
        )
    }

    companion object {
        private const val PREFS_NAME = "USW_STOPWATCH_FRAGMENT_PREFS"
        private const val PREF_IS_RUNNING = "key_stopwatch_is_running"
    }
}