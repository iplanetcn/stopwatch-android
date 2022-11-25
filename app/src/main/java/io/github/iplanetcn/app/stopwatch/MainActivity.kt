package io.github.iplanetcn.app.stopwatch

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.drawable.AnimationDrawable
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.github.iplanetcn.app.stopwatch.databinding.ActivityMainBinding
import io.github.iplanetcn.app.stopwatch.fragments.CountdownFragment
import io.github.iplanetcn.app.stopwatch.fragments.LapTimesFragment
import io.github.iplanetcn.app.stopwatch.fragments.StopwatchFragment
import io.github.iplanetcn.app.stopwatch.manager.SoundManager
import io.github.iplanetcn.app.stopwatch.utils.AlarmUpdater
import io.github.iplanetcn.app.stopwatch.utils.LapTimeRecorder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var mPowerMan: PowerManager? = null
    private var mWakeLock: WakeLock? = null
    var lapTimeFragment: LapTimesFragment? = null
    private var mCountdownFragment: CountdownFragment? = null
    private var mStopwatchFragment: StopwatchFragment? = null
    private var mMenu: Menu? = null
    private var mFlashResetIcon = false

    /**
     * Called when the activity is first created.
     */

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        window.setBackgroundDrawable(null)
        title = getString(R.string.app_name)
        binding.viewpager.offscreenPageLimit = 2
        setupActionBar()
        mPowerMan = getSystemService(POWER_SERVICE) as PowerManager
        volumeControlStream = AudioManager.STREAM_MUSIC
        val screenSize =
            resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        if (screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            @SuppressLint("SourceLockedOrientationActivity")
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        if (intent != null && intent.getBooleanExtra(
                AlarmUpdater.INTENT_EXTRA_LAUNCH_COUNTDOWN,
                false
            )
        ) {
            binding.viewpager.setCurrentItem(2, true)
        }
    }

    private fun setupActionBar() {
        val titles = arrayListOf(
            getString(R.string.stopwatch),
            getString(R.string.lap_times),
            getString(R.string.countdown)
        )

        val fragments = arrayListOf(
            StopwatchFragment(),
            LapTimesFragment(),
            CountdownFragment()
        )

        val tab1 = binding.tabLayout.newTab().setText(titles[0])
        val tab2 = binding.tabLayout.newTab().setText(titles[1])
        val tab3 = binding.tabLayout.newTab().setText(titles[2])

        binding.tabLayout.addTab(tab1)
        binding.tabLayout.addTab(tab2)
        binding.tabLayout.addTab(tab3)

        binding.viewpager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return fragments.size
            }

            override fun createFragment(position: Int): Fragment {
                return fragments[position]
            }
        }

        TabLayoutMediator(binding.tabLayout, binding.viewpager) { tab, position ->
            tab.text = titles[position]
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                invalidateOptionsMenu()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                /* no-op */
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                /* no-op */
            }

        })
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
        LapTimeRecorder.saveTimes(this)
    }

    @SuppressLint("InvalidWakeLockTag")
    override fun onResume() {
        super.onResume()
        LapTimeRecorder.loadTimes(this)
        @Suppress("DEPRECATION")
        mWakeLock = mPowerMan!!.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, WAKE_LOCK_KEY)
        mWakeLock?.apply { acquire(10 * 60 * 1000L /*10 minutes*/) }
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).apply {
            SoundManager.setAudioState(getBoolean(KEY_AUDIO_STATE, true))
            SettingsActivity.loadSettings(this)
            //jump straight to countdown if it was only item left running
            val jumpToPage = getInt(KEY_JUMP_TO_PAGE, -1)
            if (jumpToPage != -1) {
                binding.viewpager.setCurrentItem(2, false)
            }
        }

        mMenu?.findItem(R.id.menu_audio_toggle)?.apply { updateAudioMenuItemIcon(this) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        val inflater = menuInflater
        when (binding.tabLayout.selectedTabPosition) {
            0 -> inflater.inflate(R.menu.menu_stopwatch, menu)
            1 -> inflater.inflate(R.menu.menu_laptimes, menu)
            2 -> {
                inflater.inflate(R.menu.menu_countdown, menu)
                if (mFlashResetIcon) { // icon hint for set countdown time
                    val item = menu.findItem(R.id.menu_reset_time)
                    item.setActionView(R.layout.toolbar_set_time_animation)
                    item.actionView?.findViewById<ImageView>(R.id.set_time_imageview)?.let {
                        (it.drawable as AnimationDrawable).start()
                    }

                    MainScope().launch {
                        withContext(Dispatchers.Default) {
                            delay(1000)
                        }
                        item.actionView = null
                        mFlashResetIcon = false
                    }
                }
            }
        }

        //get audio icon and set correct variant
        updateAudioMenuItemIcon(menu.findItem(R.id.menu_audio_toggle))
        mMenu = menu
        return true
    }

    private fun updateAudioMenuItemIcon(menuItem: MenuItem) {
        menuItem.apply {
            setIcon(
                if (SoundManager.isAudioOn)
                    R.drawable.ic_baseline_volume_up_24
                else
                    R.drawable.ic_baseline_volume_off_24
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_rate_app) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(getString(R.string.play_store_uri))
            startActivity(intent)
        } else if (item.itemId == R.id.menu_clear_laps) {
            LapTimeRecorder.reset(this)
        } else if (item.itemId == R.id.menu_reset_time) {
            //get hold of countdown fragment and call reset, call back to here?
            if (mCountdownFragment != null) {
                mCountdownFragment!!.requestTimeDialog()
            }
        } else if (item.itemId == R.id.menu_audio_toggle) {
            SoundManager.setAudioState(!SoundManager.isAudioOn)
            updateAudioMenuItemIcon(item)
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
        private const val WAKE_LOCK_KEY = "stopwatch"
        const val PREFS_NAME = "USW_PREFS"
    }
}