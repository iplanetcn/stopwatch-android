package com.geekyouup.android.ustopwatch

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool

class SoundManager private constructor(private val mContext: Context?) {
    private val soundPool: SoundPool = SoundPool(3, AudioManager.STREAM_MUSIC, 100)
    private val soundPoolMap: HashMap<Int, Int> = HashMap()
    private var mLoopingSoundId = -1
    @JvmOverloads
    fun playSound(soundId: Int, endlessLoop: Boolean = false) {
        if (isAudioOn) {
            if (endlessLoop) stopEndlessAlarm()
            val mgr = mContext!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val streamVolume = mgr
                .getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
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

    fun doTick() {
        if (isAudioOn && SettingsActivity.isTicking) {
            val mgr = mContext!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val streamVolume = mgr
                .getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
            soundPool.play(
                soundPoolMap[SOUND_TICK]!!, streamVolume,
                streamVolume, 1, 0, 1f
            )
        }
    }

    fun setAudioState(on: Boolean) {
        isAudioOn = on
    }

    val isEndlessAlarmSounding: Boolean
        get() = mLoopingSoundId != -1

    companion object {
        private var mSoundManagerInstance: SoundManager? = null
        const val SOUND_COUNTDOWN_ALARM = 1
        const val SOUND_LAP_TIME = 2
        const val SOUND_RESET = 3
        const val SOUND_START = 4
        const val SOUND_STOP = 5
        const val SOUND_TICK = 6
        var isAudioOn = true

        fun getInstance(cxt: Context?): SoundManager? {
            if (mSoundManagerInstance == null) mSoundManagerInstance = SoundManager(cxt)
            return mSoundManagerInstance
        }
    }

    init {
        soundPoolMap[SOUND_COUNTDOWN_ALARM] = soundPool.load(mContext, R.raw.countdown_alarm, 1)
        soundPoolMap[SOUND_LAP_TIME] = soundPool.load(mContext, R.raw.lap_time, 1)
        soundPoolMap[SOUND_RESET] = soundPool.load(mContext, R.raw.reset_watch, 1)
        soundPoolMap[SOUND_START] = soundPool.load(mContext, R.raw.start, 1)
        soundPoolMap[SOUND_STOP] = soundPool.load(mContext, R.raw.stop, 1)
        soundPoolMap[SOUND_TICK] = soundPool.load(mContext, R.raw.tok_repeatit, 2)
    }
}