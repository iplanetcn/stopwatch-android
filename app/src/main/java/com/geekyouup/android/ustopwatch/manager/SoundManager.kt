package com.geekyouup.android.ustopwatch.manager

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import com.geekyouup.android.ustopwatch.R
import com.geekyouup.android.ustopwatch.SettingsActivity


object SoundManager {
    private val soundPool: SoundPool
    private val soundPoolMap: HashMap<Int, Int>
    private var mLoopingSoundId = -1
    var isAudioOn = true

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPoolMap = HashMap()
    }

    fun init(context: Context) {
        soundPoolMap[Sounds.SOUND_COUNTDOWN_ALARM] =
            soundPool.load(context, R.raw.countdown_alarm, 1)
        soundPoolMap[Sounds.SOUND_LAP_TIME] = soundPool.load(context, R.raw.lap_time, 1)
        soundPoolMap[Sounds.SOUND_RESET] = soundPool.load(context, R.raw.reset_watch, 1)
        soundPoolMap[Sounds.SOUND_START] = soundPool.load(context, R.raw.start, 1)
        soundPoolMap[Sounds.SOUND_STOP] = soundPool.load(context, R.raw.stop, 1)
        soundPoolMap[Sounds.SOUND_TICK] = soundPool.load(context, R.raw.tok_repeatit, 2)
    }

    fun playSound(context: Context, soundId: Int, endlessLoop: Boolean = false) {
        if (isAudioOn) {
            if (endlessLoop) stopEndlessAlarm()
            val mgr = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val streamVolume = mgr.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
            val playingSoundId = soundPool.play(
                soundPoolMap[soundId]!!, streamVolume,
                streamVolume, 1, if (endlessLoop) 35 else 0, 1f
            )
            if (endlessLoop) mLoopingSoundId = playingSoundId
        }
    }

    fun stopEndlessAlarm() {
        try {
            if (mLoopingSoundId != -1) soundPool.stop(mLoopingSoundId)
            mLoopingSoundId = -1
        } catch (ignored: Exception) {
        }
    }

    fun doTick(context: Context) {
        if (isAudioOn && SettingsActivity.isTicking) {
            val mgr = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val streamVolume = mgr.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
            soundPool.play(
                soundPoolMap[Sounds.SOUND_TICK]!!, streamVolume,
                streamVolume, 1, 0, 1f
            )
        }
    }

    fun setAudioState(on: Boolean) {
        isAudioOn = on
    }

    val isEndlessAlarmSounding get() = mLoopingSoundId != -1
}