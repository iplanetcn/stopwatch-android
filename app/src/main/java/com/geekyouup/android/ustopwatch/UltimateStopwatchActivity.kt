package com.geekyouup.android.ustopwatch

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.drawable.AnimationDrawable
import android.media.AudioManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.geekyouup.android.ustopwatch.fragments.CountdownFragment
import com.geekyouup.android.ustopwatch.fragments.LapTimeRecorder
import com.geekyouup.android.ustopwatch.fragments.LapTimesFragment
import com.geekyouup.android.ustopwatch.fragments.StopwatchFragment

class UltimateStopwatchActivity : AppCompatActivity() {
    private var mPowerMan: PowerManager? = null
    private var mWakeLock: WakeLock? = null
    var lapTimeFragment: LapTimesFragment? = null
        private set
    private var mCountdownFragment: CountdownFragment? = null
    private var mStopwatchFragment: StopwatchFragment? = null
    private var mSoundManager: SoundManager? = null
    private var mViewPager: ViewPager? = null
    private var mTabsAdapter: TabsAdapter? = null
    private var mMenu: Menu? = null
    private var mFlashResetIcon = false

    /**
     * Called when the activity is first created.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        window.setBackgroundDrawable(null)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.setIcon(R.drawable.icon_ab)
        }
        title = getString(R.string.app_name_caps)
        mSoundManager = SoundManager.getInstance(this)
        mViewPager = findViewById(R.id.viewpager)
        mViewPager?.offscreenPageLimit = 2
        setupActionBar()
        mPowerMan = getSystemService(POWER_SERVICE) as PowerManager
        volumeControlStream = AudioManager.STREAM_MUSIC

        // stop landscape on QVGA/HVGA
        val screenSize =
            resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        if (screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        //If launched from Countdown notification then goto countdown clock directly
        if (intent != null && intent.getBooleanExtra(
                AlarmUpdater.INTENT_EXTRA_LAUNCH_COUNTDOWN,
                false
            )
        ) {
            supportActionBar!!.setSelectedNavigationItem(2)
        }
    }

    private fun setupActionBar() {
        val ab = supportActionBar
        val tab1 = ab!!.newTab().setText(getString(R.string.stopwatch))
        val tab2 = ab.newTab().setText(getString(R.string.laptimes))
        val tab3 = ab.newTab().setText(getString(R.string.countdown))
        ab.navigationMode = ActionBar.NAVIGATION_MODE_TABS
        mTabsAdapter = TabsAdapter(this, mViewPager)
        mTabsAdapter!!.addTab(tab1, StopwatchFragment::class.java, null)
        mTabsAdapter!!.addTab(tab2, LapTimesFragment::class.java, null)
        mTabsAdapter!!.addTab(tab3, CountdownFragment::class.java, null)
    }

    override fun onPause() {
        super.onPause()
        mWakeLock!!.release()
        val settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = settings.edit()
        editor.putBoolean(KEY_AUDIO_STATE, SoundManager.isAudioOn)

        //if we're quitting with the countdown running and not the stopwatch then jump to countdown on relaunch
        if (mCountdownFragment != null && mCountdownFragment!!.isRunning &&
            mStopwatchFragment != null && !mStopwatchFragment!!.isRunning
        ) {
            editor.putInt(KEY_JUMP_TO_PAGE, 2)
        } else {
            editor.putInt(KEY_JUMP_TO_PAGE, -1)
        }
        editor.apply()
        LapTimeRecorder.instance?.saveTimes(this)
    }

    @SuppressLint("InvalidWakeLockTag")
    override fun onResume() {
        super.onResume()
        LapTimeRecorder.instance?.loadTimes(this)
        mWakeLock = mPowerMan!!.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, WAKE_LOCK_KEY)
        mWakeLock?.run { acquire() }
        val settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        mSoundManager!!.setAudioState(settings.getBoolean(KEY_AUDIO_STATE, true))
        SettingsActivity.Companion.loadSettings(settings)
        if (mMenu != null) {
            val audioButton = mMenu!!.findItem(R.id.menu_audiotoggle)
            audioButton?.setIcon(if (SoundManager.isAudioOn) R.drawable.audio_on else R.drawable.audio_off)
        }

        //jump straight to countdown if it was only item left running
        val jumpToPage = settings.getInt(KEY_JUMP_TO_PAGE, -1)
        if (jumpToPage != -1) {
            mViewPager!!.setCurrentItem(2, false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        val inflater = menuInflater
        when (mTabsAdapter?.currentTabNum) {
            1 -> inflater.inflate(R.menu.menu_laptimes, menu)
            2 -> {
                inflater.inflate(R.menu.menu_countdown, menu)
                if (mFlashResetIcon) //icon hint for set countdown time
                {
                    val item = menu.findItem(R.id.menu_resettime)
                    item.setActionView(R.layout.action_bar_settime_animation)
                    val iv = item.actionView.findViewById<ImageView>(R.id.settime_imageview)
                    (iv.drawable as AnimationDrawable).start()

                    //remove the action provider again after 1sec
                    object : AsyncTask<Void?, Int?, Void?>() {
                        override fun doInBackground(vararg params: Void?): Void? {
                            try {
                                Thread.sleep(1000)
                            } catch (ignored: InterruptedException) {
                            }
                            return null
                        }

                        override fun onPostExecute(result: Void?) {
                            item.actionView = null
                            mFlashResetIcon = false
                        }
                    }.execute(null as Void?)
                }
            }
            0 -> inflater.inflate(R.menu.menu_stopwatch, menu)
            else -> inflater.inflate(R.menu.menu_stopwatch, menu)
        }

        //get audio icon and set correct variant
        val audioButton = menu.findItem(R.id.menu_audiotoggle)
        audioButton?.setIcon(if (SoundManager.isAudioOn) R.drawable.audio_on else R.drawable.audio_off)
        mMenu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_rateapp) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(getString(R.string.play_store_uri))
            startActivity(intent)
        } else if (item.itemId == R.id.menu_clearlaps) {
            LapTimeRecorder.instance?.reset(this)
        } else if (item.itemId == R.id.menu_resettime) {
            //get hold of countdown fragment and call reset, call back to here?
            if (mCountdownFragment != null) {
                mCountdownFragment!!.requestTimeDialog()
            }
        } else if (item.itemId == R.id.menu_audiotoggle) {
            mSoundManager!!.setAudioState(!SoundManager.isAudioOn)
            item.setIcon(if (SoundManager.isAudioOn) R.drawable.audio_on else R.drawable.audio_off)
        } else if (item.itemId == R.id.menu_settings) {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.data = Uri.parse(getString(R.string.play_store_uri))
            startActivity(intent)
        }
        return true
    }

    fun registerLapTimeFragment(ltf: LapTimesFragment?) {
        lapTimeFragment = ltf
    }

    fun registerCountdownFragment(cdf: CountdownFragment?) {
        mCountdownFragment = cdf
    }

    fun registerStopwatchFragment(swf: StopwatchFragment?) {
        mStopwatchFragment = swf
    }

    fun flashResetTimeIcon() {
        mFlashResetIcon = true
        invalidateOptionsMenu()
    }

    companion object {
        const val MSG_UPDATE_COUNTER_TIME = "msg_update_counter"
        const val MSG_NEW_TIME_DOUBLE = "msg_new_time_double"
        const val MSG_STATE_CHANGE = "msg_state_change"
        private const val KEY_AUDIO_STATE = "key_audio_state"
        private const val KEY_JUMP_TO_PAGE = "key_start_page"
        private const val WAKE_LOCK_KEY = "ustopwatch"
        const val PREFS_NAME = "USW_PREFS"
    }
}