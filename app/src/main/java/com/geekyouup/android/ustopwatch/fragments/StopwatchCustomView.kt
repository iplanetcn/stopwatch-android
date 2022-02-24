package com.geekyouup.android.ustopwatch.fragments

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import com.geekyouup.android.ustopwatch.*
import com.geekyouup.android.ustopwatch.compat.compatVibrator
import kotlin.math.abs

class StopwatchCustomView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var mIsStopwatch = true //true=stopwatch, false=countdown
    var isRunning = false
        private set
    private lateinit var mBackgroundImage: Bitmap
    private var mBackgroundStartY = 0
    private var mAppOffsetX = 0
    private var mAppOffsetY = 0
    private var mMinsAngle = 0f
    private var mSecsAngle = 0f
    private var mDisplayTimeMillis: Int = 0//max value is 100hours, 360000000ms
    private val twoPI = (Math.PI * 2.0).toFloat()
    private val mStopwatchMode = true
    private var mTouching: Long = 0
    private var mCanvasWidth = 320
    private var mCanvasHeight = 480
    private var mSecsCenterX = 156
    private var mSecsCenterY = 230
    private var mMinsCenterX = 156
    private var mMinsCenterY = 185
    private var mSecsHalfWidth = 0
    private var mSecsHalfHeight = 0
    private var mMinsHalfWidth = 0
    private var mMinsHalfHeight = 0
    //private Paint mBackgroundPaint;
    /**
     * Used to figure out elapsed time between frames
     */
    private var mLastTime: Long = 0
    private var mSecHand: Drawable? = null
    private var mMinHand: Drawable? = null

    //pass back messages to UI thread
    private var mHandler: Handler? = null
    private fun init() {
        val res = resources
        //the stopwatch graphics are square, so find the smallest dimension they must fit in and load appropriately
        val minDim = mCanvasHeight.coerceAtMost(mCanvasWidth)
        val options = BitmapFactory.Options()
        options.inScaled = false
        val handsScaleFactor: Double
        when {
            minDim >= 1000 -> {
                mBackgroundImage = BitmapFactory.decodeResource(
                    res,
                    if (mIsStopwatch) R.drawable.background1000 else R.drawable.background1000_cd,
                    options
                )
                handsScaleFactor = 1.388
            }
            minDim >= 720 -> {
                mBackgroundImage = BitmapFactory.decodeResource(
                    res,
                    if (mIsStopwatch) R.drawable.background720 else R.drawable.background720_cd,
                    options
                )
                handsScaleFactor = 1.0
            }
            minDim >= 590 -> {
                mBackgroundImage = BitmapFactory.decodeResource(
                    res,
                    if (mIsStopwatch) R.drawable.background590 else R.drawable.background590_cd,
                    options
                )
                handsScaleFactor = 0.82
            }
            minDim >= 460 -> {
                mBackgroundImage = BitmapFactory.decodeResource(
                    res,
                    if (mIsStopwatch) R.drawable.background460 else R.drawable.background460_cd,
                    options
                )
                handsScaleFactor = 0.64
            }
            minDim >= 320 -> {
                mBackgroundImage = BitmapFactory.decodeResource(
                    res,
                    if (mIsStopwatch) R.drawable.background320 else R.drawable.background320_cd,
                    options
                )
                handsScaleFactor = 0.444
            }
            minDim >= 240 -> {
                mBackgroundImage = BitmapFactory.decodeResource(
                    res,
                    if (mIsStopwatch) R.drawable.background240 else R.drawable.background240_cd,
                    options
                )
                handsScaleFactor = 0.333
            }
            else -> {
                mBackgroundImage = BitmapFactory.decodeResource(
                    res,
                    if (mIsStopwatch) R.drawable.background150 else R.drawable.background150_cd,
                    options
                )
                handsScaleFactor = 0.208
            }
        }

        mSecHand = ResourcesCompat.getDrawable(
            res,
            if (mIsStopwatch) R.drawable.sechand else R.drawable.sechand_cd,
            null
        )
        mMinHand = ResourcesCompat.getDrawable(
            res,
            if (mIsStopwatch) R.drawable.minhand else R.drawable.minhand_cd,
            null
        )
        mSecsHalfWidth = mSecHand!!.intrinsicWidth / 2
        mSecsHalfHeight = mSecHand!!.intrinsicHeight / 2
        mMinsHalfWidth = mMinHand!!.intrinsicWidth / 2
        mMinsHalfHeight = mMinHand!!.intrinsicHeight / 2
        mMinsHalfHeight = (mMinsHalfHeight.toDouble() * handsScaleFactor).toInt()
        mMinsHalfWidth = (mMinsHalfWidth.toDouble() * handsScaleFactor).toInt()
        mSecsHalfHeight = (mSecsHalfHeight.toDouble() * handsScaleFactor).toInt()
        mSecsHalfWidth = (mSecsHalfWidth.toDouble() * handsScaleFactor).toInt()
        mBackgroundStartY = (mCanvasHeight - mBackgroundImage.height) / 2
        mAppOffsetX = (mCanvasWidth - mBackgroundImage.width) / 2
        if (mBackgroundStartY < 0) mAppOffsetY = -mBackgroundStartY
        mSecsCenterY =
            mBackgroundStartY + mBackgroundImage.height / 2 //new graphics have watch center in center
        mMinsCenterY =
            mBackgroundStartY + mBackgroundImage.height * 314 / 1000 //mSecsCenterY - 44;
        mSecsCenterX = mCanvasWidth / 2
        mMinsCenterX = mCanvasWidth / 2
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // Account for padding
        val xpad = paddingLeft + paddingRight
        val ypad = paddingTop + paddingBottom
        mCanvasWidth = w - xpad
        mCanvasHeight = h - ypad
        init()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw the background image
        canvas.drawBitmap(
            mBackgroundImage,
            mAppOffsetX.toFloat(),
            (mBackgroundStartY + mAppOffsetY).toFloat(),
            null
        )

        // draw the mins hand with its current rotatiom
        canvas.save()
        canvas.rotate(
            Math.toDegrees(mMinsAngle.toDouble()).toFloat(),
            mMinsCenterX.toFloat(),
            (mMinsCenterY + mAppOffsetY).toFloat()
        )
        mMinHand!!.setBounds(
            mMinsCenterX - mMinsHalfWidth, mMinsCenterY - mMinsHalfHeight + mAppOffsetY,
            mMinsCenterX + mMinsHalfWidth, mMinsCenterY + mAppOffsetY + mMinsHalfHeight
        )
        mMinHand!!.draw(canvas)
        canvas.restore()

        // Draw the secs hand with its current rotation
        canvas.save()
        canvas.rotate(
            Math.toDegrees(mSecsAngle.toDouble()).toFloat(),
            mSecsCenterX.toFloat(),
            (mSecsCenterY + mAppOffsetY).toFloat()
        )
        mSecHand!!.setBounds(
            mSecsCenterX - mSecsHalfWidth, mSecsCenterY - mSecsHalfHeight + mAppOffsetY,
            mSecsCenterX + mSecsHalfWidth, mSecsCenterY + mAppOffsetY + mSecsHalfHeight
        )
        mSecHand!!.draw(canvas)
        canvas.restore()
    }

    //set the time on the stopwatch/countdown face, animating the hands if resettings countdown
    //To make the animation feel right, we always wind backwards when resetting
    fun setTime(hours: Int, minutes: Int, seconds: Int, resetting: Boolean) {
        isRunning = false
        mLastTime = System.currentTimeMillis()
        if (SettingsActivity.isAnimating) {
            animateWatchTo(hours, minutes, seconds, resetting)
        } else {
            //to fix bug #42, now the hands reset even when paused
            removeCallbacks(animator)
            post { //during the animation also roll back the clock time to the current hand times.
                mSecsAngle =
                    twoPI * (seconds.toFloat() / 60.0f) //ensure the hands have ended at correct position
                mMinsAngle = twoPI * (minutes.toFloat() / 30.0f)
                mDisplayTimeMillis = (hours * 3600000 + minutes * 60000 + seconds * 1000)
                broadcastClockTime(if (mIsStopwatch) mDisplayTimeMillis.toDouble() else -mDisplayTimeMillis.toDouble())
                invalidate()
            }
        }
    }

    private fun animateWatchTo(hours: Int, minutes: Int, seconds: Int, resetting: Boolean) {
        mSecsAngle %= twoPI //avoids more than 1 rotation
        mMinsAngle %= twoPI //avoids more than 1 rotation

        //forces hands to go back to 0 not forwards
        val toSecsAngle = shortestAngleToDestination(mSecsAngle, twoPI * seconds / 60f, resetting)
        //avoid multiple minutes hands rotates as face is 0-29 not 0-59
        val toMinsAngle = shortestAngleToDestination(
            mMinsAngle,
            twoPI * ((if (minutes > 30) minutes - 30 else minutes) / 30f + seconds / 1800f),
            resetting
        )
        val maxAngleChange =
            abs(mSecsAngle - toSecsAngle).coerceAtLeast(abs(toMinsAngle - mMinsAngle))
        val duration: Int =
            if (maxAngleChange < Math.PI / 2) 300 else if (maxAngleChange < Math.PI) 750 else 1250
        val secsAnimation = ValueAnimator.ofFloat(mSecsAngle, toSecsAngle)
        secsAnimation.interpolator = AccelerateDecelerateInterpolator()
        secsAnimation.duration = duration.toLong()
        secsAnimation.start()
        val minsAnimation = ValueAnimator.ofFloat(mMinsAngle, toMinsAngle)
        minsAnimation.interpolator = AccelerateDecelerateInterpolator()
        minsAnimation.duration = duration.toLong()
        minsAnimation.start()
        val clockAnimation = ValueAnimator.ofInt(
            mDisplayTimeMillis,
            hours * 3600000 + minutes * 60000 + seconds * 1000
        )
        clockAnimation.interpolator = AccelerateDecelerateInterpolator()
        clockAnimation.duration = duration.toLong()
        clockAnimation.start()

        //approach is to go from xMs to yMs
        removeCallbacks(animator)
        post(object : Runnable {
            override fun run() {
                //during the animation also roll back the clock time to the current hand times.
                if (secsAnimation.isRunning || minsAnimation.isRunning || clockAnimation.isRunning) {
                    mSecsAngle = secsAnimation.animatedValue as Float
                    mMinsAngle = minsAnimation.animatedValue as Float
                    broadcastClockTime((if (mIsStopwatch) (clockAnimation.animatedValue as Int).toDouble() else -(clockAnimation.animatedValue as Int).toDouble()))
                    invalidate()
                    postDelayed(this, 15)
                } else {
                    mSecsAngle = toSecsAngle //ensure the hands have ended at correct position
                    mMinsAngle = toMinsAngle
                    mDisplayTimeMillis = (hours * 3600000 + minutes * 60000 + seconds * 1000)
                    broadcastClockTime(if (mIsStopwatch) mDisplayTimeMillis.toDouble() else -mDisplayTimeMillis.toDouble())
                    invalidate()
                }
            }
        })
    }

    //This method returns the angle in rads closest to fromAngle that is equivalent to toAngle
    //unless we are animating a reset, as it feels better to always reset by reversing the hand direction
    //e.g. toAngle+2*Pi may be closer than toAngle
    //To get from -6 rads to 1 rads, shortest distance is clockwise through 0 rads
    //From 1 rads to 5 rads shortest distance is CCW back through 0 rads
    private fun shortestAngleToDestination(
        fromAngle: Float,
        toAngle: Float,
        resetting: Boolean
    ): Float {
        return if (resetting && mIsStopwatch) // hands must always go backwards
        {
            toAngle // stopwatch reset always returns to 0,
        } else if (resetting && !mIsStopwatch) //hands must always go forwards
        {
            //countdown reset can be to any clock position, ensure CW rotation
            if (toAngle > fromAngle) toAngle else toAngle + twoPI
        } else  //not restting hands must take shortest route
        {
            val absFromMinusTo = abs(fromAngle - toAngle)
            //toAngle-twoPi, toAngle, toAngle+twoPi
            if (absFromMinusTo < abs(fromAngle - (toAngle + twoPI))) {
                if (abs(fromAngle - (toAngle - twoPI)) < absFromMinusTo) {
                    toAngle - twoPI
                } else {
                    toAngle
                }
            } else toAngle + twoPI
        }
    }

    //Stopwatch and countdown animation runnable
    private val animator: Runnable = object : Runnable {
        override fun run() {
            updateWatchState(false)
            if (isRunning) {
                invalidate()
                removeCallbacks(this)
                ViewCompat.postOnAnimation(this@StopwatchCustomView, this)
            }
        }
    }

    /**
     * Update the time
     */
    private fun updateWatchState(appResuming: Boolean) {
        val now = System.currentTimeMillis()
        if (isRunning) {
            if (mIsStopwatch) mDisplayTimeMillis += (now - mLastTime).toInt() else mDisplayTimeMillis -= (now - mLastTime).toInt()
        } else {
            mLastTime = now
        }

        // mins is 0 to 30
        mMinsAngle = twoPI * (mDisplayTimeMillis / 1800000.0f)
        mSecsAngle = twoPI * (mDisplayTimeMillis / 60000.0f)
        if (mDisplayTimeMillis < 0) mDisplayTimeMillis = 0

        // send the time back to the Activity to update the other views
        broadcastClockTime(if (mIsStopwatch) mDisplayTimeMillis.toDouble() else -mDisplayTimeMillis.toDouble())
        mLastTime = now

        // stop timer at end
        if (isRunning && !mIsStopwatch && mDisplayTimeMillis <= 0) {
            notifyCountdownComplete(appResuming)
        }
    }

    // Deal with touch events, either start/stop or swipe
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val sm: SoundManager = SoundManager.getInstance(context)!!
            if (sm.isEndlessAlarmSounding) {
                sm.stopEndlessAlarm()
            } else {
                mTouching = System.currentTimeMillis()
            }
        } else if (event.action == MotionEvent.ACTION_MOVE) {
            if (mTouching > 0 && System.currentTimeMillis() - mTouching > 1000) mTouching =
                0L //reset touch if user is swiping
        } else if (event.action == MotionEvent.ACTION_UP) {
            if (mTouching > 0) startStop()
            mTouching = 0L
        }
        return true
    }

    fun startStop(): Boolean {
        if (isRunning) {
            stop()
            notifyStateChanged()
        } else if (mIsStopwatch || mDisplayTimeMillis != 0) { // don't start the countdown if it is 0
            start()
            notifyStateChanged()
        } else { //mDisplayTimeMillis == 0
            notifyIconHint()
            return false
        }
        return isRunning
    }

    private fun start() {
        mLastTime = System.currentTimeMillis()
        isRunning = true

        //vibrate
        if (SettingsActivity.isVibrate) {
            context.compatVibrator().vibrate(20)
        }
        removeCallbacks(animator)
        post(animator)
    }

    fun stop() {
        isRunning = false

        //vibrate
        if (SettingsActivity.isVibrate) {
            context.compatVibrator().vibrate(20)
        }
        removeCallbacks(animator)
    }

    val watchTime: Double
        get() = mDisplayTimeMillis.toDouble()

    /**
     * Dump state to the provided Bundle. Typically called when the
     * Activity is being suspended.
     */
    fun saveState(map: SharedPreferences.Editor) {
        if (!mIsStopwatch || mDisplayTimeMillis > 0) {
            if (!mIsStopwatch && mDisplayTimeMillis > 0 && isRunning) {
                AlarmUpdater.setCountdownAlarm(context, mDisplayTimeMillis.toLong())
            } else {
                AlarmUpdater.cancelCountdownAlarm(context) //just to be sure
            }
            map.putBoolean(KEY_STATE + if (mStopwatchMode) "" else KEY_COUNTDOWN_SUFFIX, isRunning)
            map.putLong(KEY_LAST_TIME + if (mStopwatchMode) "" else KEY_COUNTDOWN_SUFFIX, mLastTime)
            map.putInt(
                KEY_NOW_TIME + if (mStopwatchMode) "" else KEY_COUNTDOWN_SUFFIX,
                mDisplayTimeMillis
            )
        } else {
            map.clear()
        }
    }

    /**
     * Restores state from the indicated Bundle. Called when
     * the Activity is being restored after having been previously
     * destroyed.
     */
    @Synchronized
    fun restoreState(savedState: SharedPreferences?) {
        if (savedState != null) {
            isRunning = savedState.getBoolean(
                KEY_STATE + if (mStopwatchMode) "" else KEY_COUNTDOWN_SUFFIX,
                false
            )
            mLastTime = savedState.getLong(
                KEY_LAST_TIME + if (mStopwatchMode) "" else KEY_COUNTDOWN_SUFFIX,
                System.currentTimeMillis()
            )
            mDisplayTimeMillis =
                savedState.getInt(KEY_NOW_TIME + if (mStopwatchMode) "" else KEY_COUNTDOWN_SUFFIX, 0)
            updateWatchState(true)
            removeCallbacks(animator)
            if (isRunning) post(animator)
        }
        notifyStateChanged()
        AlarmUpdater.cancelCountdownAlarm(context) //just to be sure
    }

    //for optimization purposes
    override fun isOpaque(): Boolean {
        return true
    }

    //Message Handling between Activity/Fragment and View
    fun setHandler(handler: Handler?) {
        mHandler = handler
    }

    private fun notifyStateChanged() {
        val b = Bundle()
        b.putBoolean(UltimateStopwatchActivity.MSG_STATE_CHANGE, true)
        sendMessageToHandler(b)
    }

    private fun notifyIconHint() {
        val b = Bundle()
        b.putBoolean(CountdownFragment.MSG_REQUEST_ICON_FLASH, true)
        sendMessageToHandler(b)
    }

    private fun notifyCountdownComplete(appResuming: Boolean) {
        val b = Bundle()
        b.putBoolean(CountdownFragment.MSG_COUNTDOWN_COMPLETE, true)
        b.putBoolean(CountdownFragment.MSG_APP_RESUMING, appResuming)
        sendMessageToHandler(b)
    }

    //send the latest time to the parent fragment to populate the digits
    private fun broadcastClockTime(mTime: Double) {
        val b = Bundle()
        b.putBoolean(UltimateStopwatchActivity.MSG_UPDATE_COUNTER_TIME, true)
        b.putDouble(UltimateStopwatchActivity.MSG_NEW_TIME_DOUBLE, mTime)
        sendMessageToHandler(b)
    }

    private fun sendMessageToHandler(b: Bundle) {
        if (mHandler != null) {
            val msg = mHandler!!.obtainMessage()
            msg.data = b
            mHandler!!.sendMessage(msg)
        }
    }

    companion object {
        private const val KEY_STATE = "state_bool"
        private const val KEY_LAST_TIME = "last_time"
        private const val KEY_NOW_TIME = "current_time_int"
        private const val KEY_COUNTDOWN_SUFFIX = "_cd"
    }

    init {

        //find out if this view is specified as a stopwatch or countdown view
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.StopwatchCustomView,
            0, 0
        )
        mIsStopwatch = try {
            a.getBoolean(R.styleable.StopwatchCustomView_watchType, true)
        } finally {
            a.recycle()
        }
        init()
    }
}