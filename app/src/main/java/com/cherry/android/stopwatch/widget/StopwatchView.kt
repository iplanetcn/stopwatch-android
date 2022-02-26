package com.cherry.android.stopwatch.widget

import android.animation.ValueAnimator
import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.cherry.android.stopwatch.MainActivity
import com.cherry.android.stopwatch.R
import com.cherry.android.stopwatch.SettingsActivity
import com.cherry.android.stopwatch.compat.dp
import com.cherry.android.stopwatch.compat.sp
import com.cherry.android.stopwatch.compat.startVibrate
import com.cherry.android.stopwatch.fragments.CountdownFragment
import com.cherry.android.stopwatch.manager.SoundManager
import com.cherry.android.stopwatch.utils.AlarmUpdater
import kotlin.math.*


private const val RADIUS_OFFSET_LABEL = 100
private const val KEY_STATE = "state_bool"
private const val KEY_LAST_TIME = "last_time"
private const val KEY_NOW_TIME = "current_time_int"
private const val KEY_COUNTDOWN_SUFFIX = "_cd"

private enum class Minutes(val label: Int) {
    M_5(R.string.number_5),
    M_10(R.string.number_10),
    M_15(R.string.number_15),
    M_20(R.string.number_20),
    M_25(R.string.number_25),
    M_30(R.string.number_30),
}

private enum class Numbers(val label: Int) {
    N_10(R.string.number_10),
    N_20(R.string.number_20),
    N_30(R.string.number_30),
    N_40(R.string.number_40),
    N_50(R.string.number_50),
    N_60(R.string.number_60),
    N_70(R.string.number_70),
    N_80(R.string.number_80),
    N_90(R.string.number_90),
}

private enum class Seconds(val label: Int) {
    S_5(R.string.number_5),
    S_10(R.string.number_10),
    S_15(R.string.number_15),
    S_20(R.string.number_20),
    S_25(R.string.number_25),
    S_30(R.string.number_30),
    S_35(R.string.number_35),
    S_40(R.string.number_40),
    S_45(R.string.number_45),
    S_50(R.string.number_50),
    S_55(R.string.number_55),
    S_60(R.string.number_60);
}

class StopwatchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var largeRadius: Float = 0.0f
    private var smallRadius: Float = 0.0f
    private var smallPivotY: Float = 0.0f
    private val pointPosition: PointF = PointF(0.0f, 0.0f)
    private val rect = Rect()
    private var isStopwatch: Boolean = true
    private var displayTimeMillis: Int = 0
    private val stopwatchMode: Boolean = true
    private var touching: Long = 0
    private var mLastTime: Long = 0
    private var mHandler: Handler? = null
    private var minutesAngle = 0f
    private var secondsAngle = 0f
    var isRunning: Boolean = false
        private set

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.LEFT
        textSize = 20f.sp
//        typeface = Typeface.DEFAULT
        typeface = ResourcesCompat.getFont(context, R.font.karla_regular)
    }

    init {
        isClickable = true
        val a = context.obtainStyledAttributes(attrs, R.styleable.StopwatchView)
        isStopwatch = a.getBoolean(R.styleable.StopwatchView_watchType, false)
        a.recycle()
        updateContentDescription()
        ViewCompat.setAccessibilityDelegate(this, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfoCompat
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                val customClick = AccessibilityNodeInfoCompat.AccessibilityActionCompat(
                    AccessibilityNodeInfo.ACTION_CLICK,
                    getContentDescriptionLabel()
                )
                info.addAction(customClick)
            }
        })
    }

    override fun performClick(): Boolean {
        if (super.performClick()) return true
        updateContentDescription()
        invalidate()
        return true
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
//        val xPad = paddingStart + paddingEnd
//        val yPad = paddingTop + paddingBottom
//        largeRadius = (min(width - xPad, height - yPad) / 2.0 * 0.7).toFloat()
        largeRadius = (min(width, height) / 2.0 * 0.7).toFloat()
        smallRadius = largeRadius * 0.3f
        smallPivotY = pivotY - largeRadius / 2.0f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawDial(canvas)
        drawScales(canvas)
        drawSeconds(canvas)
        drawMinutes(canvas)
        drawNumbers(canvas)
        drawHands(canvas)
    }

    private fun drawHand(canvas: Canvas, path: Path, angle: Float, pivot: PointF) {
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        canvas.save()
        canvas.rotate(angle, pivot.x, pivot.y)
        canvas.save()
        canvas.drawPath(path, paint)
        canvas.restore()
        canvas.restore()
    }

    private fun drawHands(canvas: Canvas) {
        val secondsHand = Path()
        secondsHand.moveTo(pivotX - 20f, pivotY)
        secondsHand.lineTo(pivotX + 20f, pivotY)
        secondsHand.lineTo(pivotX, pivotY - largeRadius - 80f)
        secondsHand.lineTo(pivotX - 20f, pivotY)
        secondsHand.close()
        drawHand(canvas, secondsHand, Math.toDegrees(secondsAngle.toDouble()).toFloat(), PointF(pivotX, pivotY))

        val minuteHand = Path()
        minuteHand.moveTo(pivotX - 10f, smallPivotY)
        minuteHand.lineTo(pivotX + 10f, smallPivotY)
        minuteHand.lineTo(pivotX, smallPivotY - smallRadius - 20f)
        minuteHand.lineTo(pivotX - 10f, smallPivotY)
        minuteHand.close()
        drawHand(canvas, minuteHand, Math.toDegrees(minutesAngle.toDouble()).toFloat(), PointF(pivotX, smallPivotY))
    }

    private fun drawDial(canvas: Canvas) {
        // draw large dial
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 8f.dp
        canvas.drawCircle(pivotX, pivotY, largeRadius, paint)

        paint.color = Color.WHITE
        paint.strokeWidth = 4f.dp
        paint.isDither = true
        paint.pathEffect = DashPathEffect(floatArrayOf(20f, 20f), 0f)
        canvas.drawCircle(pivotX, pivotY, largeRadius, paint)

        paint.color = Color.BLACK
        paint.isDither = false
        paint.pathEffect = null
        paint.strokeWidth = 2f.dp
        paint.color = Color.LTGRAY
        canvas.drawCircle(pivotX, smallPivotY, smallRadius, paint)

        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        canvas.drawCircle(pivotX, pivotY, 30f, paint)
        canvas.drawCircle(pivotX, smallPivotY, 15f, paint)
    }

    private fun drawScales(canvas: Canvas) {
        var scaleLength: Float?

        canvas.save()
        for (i in 0..239) {
            when {
                i % 20 == 0 -> {
                    paint.strokeWidth = 6f
                    paint.color = Color.BLACK
                    scaleLength = 40f
                }
                i % 4 == 0 -> {
                    paint.strokeWidth = 2f
                    paint.color = Color.LTGRAY
                    scaleLength = 40f
                }
                else -> {
                    paint.strokeWidth = 1f
                    paint.color = Color.LTGRAY
                    scaleLength = 30f
                }
            }
            canvas.drawLine(
                pivotX,
                pivotY + largeRadius + 6f.dp,
                pivotX,
                pivotY + largeRadius + 6f.dp + scaleLength,
                paint
            )
            canvas.rotate(360 / 240f, pivotX, pivotY)
        }
        //Restore the original state
        canvas.restore()

        canvas.save()
        paint.color = Color.rgb(155, 22, 29)
        for (i in 0..199) {
            when {
                i % 10 == 0 -> {
                    paint.strokeWidth = 2f
                    scaleLength = 30f
                }
                i % 2 == 0 -> {
                    paint.strokeWidth = 2f
                    scaleLength = 20f
                }
                else -> {
                    paint.strokeWidth = 1f
                    scaleLength = 20f
                }
            }
            canvas.drawLine(
                pivotX,
                pivotY + largeRadius - 8f.dp,
                pivotX,
                pivotY + largeRadius - 8f.dp - scaleLength,
                paint
            )
            canvas.rotate(360 / 200f, pivotX, pivotY)
        }
        //Restore the original state
        canvas.restore()

        canvas.save()
        for (i in 0..23) {
            paint.strokeWidth = 2f
            paint.color = Color.LTGRAY
            scaleLength = 20f
            canvas.drawLine(
                pivotX,
                smallPivotY - smallRadius,
                pivotX,
                smallPivotY - smallRadius + scaleLength,
                paint
            )
            canvas.rotate(360 / 24f, pivotX, smallPivotY)
        }
        //Restore the original state
        canvas.restore()

    }

    private fun drawMinutes(canvas: Canvas) {
        paint.color = Color.DKGRAY
        paint.style = Paint.Style.FILL
        paint.textSize = 12f.sp
        val labelRadius = smallRadius - 40f
        for (i in Minutes.values()) {
            pointPosition.computeXYForMinutes(i.ordinal, labelRadius)
            val label = resources.getString(i.label)
            paint.getTextBounds(label, 0, label.length, rect)
            canvas.drawText(
                label,
                pointPosition.x - rect.width() / 2,
                pointPosition.y + rect.height() / 2,
                paint
            )
        }
    }

    private fun PointF.computeXYForMinutes(pos: Int, radius: Float) {
        // from 5 min angle
        val startAngle = PI * (-1 / 6.0)
        // increase by 5 min angle
        val angle = startAngle + pos * (PI / 3.0)
        x = (radius * cos(angle)).toFloat() + width / 2
        y = (radius * sin(angle)).toFloat() + smallPivotY
    }

    private fun drawNumbers(canvas: Canvas) {
        paint.color = Color.rgb(155, 22, 29)
        paint.style = Paint.Style.FILL
        paint.textSize = 14f.sp
        val labelRadius = largeRadius - 80f
        for (i in Numbers.values()) {
            pointPosition.computeXYForNumbers(i.ordinal, labelRadius)
            val label = resources.getString(i.label)
            paint.getTextBounds(label, 0, label.length, rect)
            canvas.drawText(
                label,
                pointPosition.x - rect.width() / 2,
                pointPosition.y + rect.height() / 2,
                paint
            )
        }
    }

    private fun PointF.computeXYForNumbers(pos: Int, radius: Float) {
        val startAngle = PI * (-3.0 / 10.0)
        val angle = startAngle + pos / 5.0 * PI
        x = (radius * cos(angle)).toFloat() + width / 2
        y = (radius * sin(angle)).toFloat() + height / 2
    }

    private fun drawSeconds(canvas: Canvas) {
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        paint.textSize = 18f.sp
        val labelRadius = largeRadius + RADIUS_OFFSET_LABEL
        for (i in Seconds.values()) {
            pointPosition.computeXYForSeconds(i.ordinal, labelRadius)
            val label = resources.getString(i.label)
            paint.getTextBounds(label, 0, label.length, rect)
            canvas.drawText(
                label,
                pointPosition.x - rect.width() / 2,
                pointPosition.y + rect.height() / 2,
                paint
            )
        }
    }

    private fun PointF.computeXYForSeconds(pos: Int, _radius: Float) {
        // from 5 min angle
        val startAngle = PI * (-1 / 3.0)
        // increase by 5 min angle
        val angle = startAngle + pos * (PI / 6.0)
        x = (_radius * cos(angle)).toFloat() + width / 2
        y = (_radius * sin(angle)).toFloat() + height / 2
    }


    private fun updateContentDescription() {
        contentDescription = getContentDescriptionLabel()
    }

    private fun getContentDescriptionLabel(): String = if (isStopwatch) {
        resources.getString(R.string.stopwatch)
    } else {
        resources.getString(R.string.countdown)
    }

    fun setTime(hours: Int, minutes: Int, seconds: Int, resetting: Boolean) {
        isRunning = false
        mLastTime = System.currentTimeMillis()
        if (SettingsActivity.isAnimating) {
            animateWatchTo(hours, minutes, seconds, resetting)
        } else {
            //to fix bug #42, now the hands reset even when paused
            removeCallbacks(animator)
            post { //during the animation also roll back the clock time to the current hand times.
                secondsAngle = PI.toFloat() * 2 * (seconds.toFloat() / 60.0f) //ensure the hands have ended at correct position
                minutesAngle = PI.toFloat() * 2 * (minutes.toFloat() / 30.0f)
                displayTimeMillis = (hours * 3600000 + minutes * 60000 + seconds * 1000)
                broadcastClockTime(if (isStopwatch) displayTimeMillis.toDouble() else -displayTimeMillis.toDouble())
                invalidate()
            }
        }
    }

    private fun animateWatchTo(hours: Int, minutes: Int, seconds: Int, resetting: Boolean) {
        secondsAngle %= PI.toFloat() * 2 //avoids more than 1 rotation
        minutesAngle %= PI.toFloat() * 2 //avoids more than 1 rotation

        //forces hands to go back to 0 not forwards
        val toSecsAngle = shortestAngleToDestination(secondsAngle, PI.toFloat() * 2 * seconds / 60f, resetting)
        //avoid multiple minutes hands rotates as face is 0-29 not 0-59
        val toMinutesAngle = shortestAngleToDestination(
            minutesAngle,
            PI.toFloat() * 2 * ((if (minutes > 30) minutes - 30 else minutes) / 30f + seconds / 1800f),
            resetting
        )
        val maxAngleChange =
            abs(secondsAngle - toSecsAngle).coerceAtLeast(abs(toMinutesAngle - minutesAngle))
        val duration: Int =
            if (maxAngleChange < Math.PI / 2) 300 else if (maxAngleChange < Math.PI) 750 else 1250
        val secsAnimation = ValueAnimator.ofFloat(secondsAngle, toSecsAngle)
        secsAnimation.interpolator = AccelerateDecelerateInterpolator()
        secsAnimation.duration = duration.toLong()
        secsAnimation.start()
        val minutesAnimation = ValueAnimator.ofFloat(minutesAngle, toMinutesAngle)
        minutesAnimation.interpolator = AccelerateDecelerateInterpolator()
        minutesAnimation.duration = duration.toLong()
        minutesAnimation.start()
        val clockAnimation = ValueAnimator.ofInt(
            displayTimeMillis,
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
                if (secsAnimation.isRunning || minutesAnimation.isRunning || clockAnimation.isRunning) {
                    secondsAngle = secsAnimation.animatedValue as Float
                    minutesAngle = minutesAnimation.animatedValue as Float
                    broadcastClockTime((if (isStopwatch) (clockAnimation.animatedValue as Int).toDouble() else -(clockAnimation.animatedValue as Int).toDouble()))
                    invalidate()
                    postDelayed(this, 15)
                } else {
                    secondsAngle = toSecsAngle //ensure the hands have ended at correct position
                    minutesAngle = toMinutesAngle
                    displayTimeMillis = (hours * 3600000 + minutes * 60000 + seconds * 1000)
                    broadcastClockTime(if (isStopwatch) displayTimeMillis.toDouble() else -displayTimeMillis.toDouble())
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
        return if (resetting && isStopwatch) // hands must always go backwards
        {
            toAngle // stopwatch reset always returns to 0,
        } else if (resetting && !isStopwatch) //hands must always go forwards
        {
            //countdown reset can be to any clock position, ensure CW rotation
            if (toAngle > fromAngle) toAngle else toAngle + PI.toFloat() * 2
        } else  //not resetting hands must take shortest route
        {
            val absFromMinusTo = abs(fromAngle - toAngle)
            //toAngle-twoPi, toAngle, toAngle+twoPi
            if (absFromMinusTo < abs(fromAngle - (toAngle + PI.toFloat() * 2))) {
                if (abs(fromAngle - (toAngle - PI.toFloat() * 2)) < absFromMinusTo) {
                    toAngle - PI.toFloat() * 2
                } else {
                    toAngle
                }
            } else toAngle + PI.toFloat() * 2
        }
    }

    //Stopwatch and countdown animation runnable
    private val animator: Runnable = object : Runnable {
        override fun run() {
            updateWatchState(false)
            if (isRunning) {
                invalidate()
                removeCallbacks(this)
                ViewCompat.postOnAnimation(this@StopwatchView, this)
            }
        }
    }

    /**
     * Update the time
     */
    private fun updateWatchState(appResuming: Boolean) {
        val now = System.currentTimeMillis()
        if (isRunning) {
            if (isStopwatch) displayTimeMillis += (now - mLastTime).toInt() else displayTimeMillis -= (now - mLastTime).toInt()
        } else {
            mLastTime = now
        }

        // minutes is 0 to 30
        minutesAngle = PI.toFloat() * 2 * (displayTimeMillis / 1800000.0f)
        secondsAngle = PI.toFloat() * 2 * (displayTimeMillis / 60000.0f)
        if (displayTimeMillis < 0) displayTimeMillis = 0

        // send the time back to the Activity to update the other views
        broadcastClockTime(if (isStopwatch) displayTimeMillis.toDouble() else -displayTimeMillis.toDouble())
        mLastTime = now

        // stop timer at end
        if (isRunning && !isStopwatch && displayTimeMillis <= 0) {
            notifyCountdownComplete(appResuming)
        }
    }

    // Deal with touch events, either start/stop or swipe
    override fun onTouchEvent(event: MotionEvent): Boolean {
        performClick()
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (SoundManager.isEndlessAlarmSounding) {
                SoundManager.stopEndlessAlarm()
            } else {
                touching = System.currentTimeMillis()
            }
        } else if (event.action == MotionEvent.ACTION_MOVE) {
            if (touching > 0 && System.currentTimeMillis() - touching > 1000) touching =
                0L //reset touch if user is swiping
        } else if (event.action == MotionEvent.ACTION_UP) {
            if (touching > 0) startStop()
            touching = 0L
        }
        return true
    }

    fun startStop(): Boolean {
        if (isRunning) {
            stop()
            notifyStateChanged()
        } else if (isStopwatch || displayTimeMillis != 0) { // don't start the countdown if it is 0
            start()
            notifyStateChanged()
        } else { //displayTimeMillis == 0
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
            context.startVibrate(20)
        }
        removeCallbacks(animator)
        post(animator)
    }

    fun stop() {
        isRunning = false

        // vibrate
        if (SettingsActivity.isVibrate) {
            context.startVibrate(20)
        }
        removeCallbacks(animator)
    }

    val watchTime get() = displayTimeMillis.toDouble()

    /**
     * Dump state to the provided Bundle. Typically called when the
     * Activity is being suspended.
     */
    fun saveState(map: SharedPreferences.Editor) {
        if (!isStopwatch || displayTimeMillis > 0) {
            if (!isStopwatch && displayTimeMillis > 0 && isRunning) {
                AlarmUpdater.setCountdownAlarm(context, displayTimeMillis.toLong())
            } else {
                AlarmUpdater.cancelCountdownAlarm(context)
            }
            map.putBoolean(KEY_STATE + if (stopwatchMode) "" else KEY_COUNTDOWN_SUFFIX, isRunning)
            map.putLong(KEY_LAST_TIME + if (stopwatchMode) "" else KEY_COUNTDOWN_SUFFIX, mLastTime)
            map.putInt(
                KEY_NOW_TIME + if (stopwatchMode) "" else KEY_COUNTDOWN_SUFFIX,
                displayTimeMillis
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
                KEY_STATE + if (stopwatchMode) "" else KEY_COUNTDOWN_SUFFIX,
                false
            )
            mLastTime = savedState.getLong(
                KEY_LAST_TIME + if (stopwatchMode) "" else KEY_COUNTDOWN_SUFFIX,
                System.currentTimeMillis()
            )
            displayTimeMillis = savedState.getInt(
                KEY_NOW_TIME + if (stopwatchMode) "" else KEY_COUNTDOWN_SUFFIX,
                0
            )
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
        b.putBoolean(MainActivity.MSG_STATE_CHANGE, true)
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
        b.putBoolean(MainActivity.MSG_UPDATE_COUNTER_TIME, true)
        b.putDouble(MainActivity.MSG_NEW_TIME_DOUBLE, mTime)
        sendMessageToHandler(b)
    }

    private fun sendMessageToHandler(b: Bundle) {
        if (mHandler != null) {
            val msg = mHandler!!.obtainMessage()
            msg.data = b
            mHandler!!.sendMessage(msg)
        }
    }

}