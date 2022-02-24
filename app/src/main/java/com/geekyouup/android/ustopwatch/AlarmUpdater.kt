package com.geekyouup.android.ustopwatch

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.IBinder
import androidx.core.app.NotificationCompat

object AlarmUpdater {
    const val INTENT_EXTRA_LAUNCH_COUNTDOWN = "launch_countdown"
    fun cancelCountdownAlarm(context: Context?) {
        try {
            val alarmMan = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val defineIntent = Intent(context, UpdateService::class.java)
            defineIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            val piWakeUp =
                PendingIntent.getService(context, 0, defineIntent, PendingIntent.FLAG_NO_CREATE)
            if (piWakeUp != null) {
                alarmMan.cancel(piWakeUp)
            }
        } catch (ignored: Exception) {
        }
        try {
            (context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(
                R.layout.main
            )
        } catch (ignored: Exception) {
        }
    }

    //cancels alarm then sets new one
    fun setCountdownAlarm(context: Context, inMillis: Long) {
        val alarmMan = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val defineIntent = Intent(context, UpdateService::class.java)
        defineIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val piWakeUp =
            PendingIntent.getService(context, 0, defineIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        //alarmMan.cancel(piWakeUp);
        if (inMillis != -1L) {
            alarmMan[AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + inMillis] = piWakeUp
        }
    }

    fun showChronometerNotification(context: Context?, startTime: Long) {
        // The PendingIntent to launch our activity if the user selects this notification
        val launcher = Intent(context, UltimateStopwatchActivity::class.java)
        launcher.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val contentIntent =
            PendingIntent.getActivity(context, 0, launcher, PendingIntent.FLAG_ONE_SHOT)
        val notification = NotificationCompat.Builder(context!!)
            .setContentTitle(context.getString(R.string.app_name))
            .setWhen(System.currentTimeMillis() - startTime)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentIntent(contentIntent)
            .setUsesChronometer(true)
            .build()
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // We use a layout id because it is a unique number.  We use it later to cancel.
        notificationManager.notify(R.layout.stopwatch_fragment, notification)
    }

    /*public static void showCountdownChronometerNotification(Context context, long endTime)
    {
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            // The PendingIntent to launch our activity if the user selects this notification
            Intent launcher = new Intent(context,UltimateStopwatchActivity.class);
            launcher.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,launcher,PendingIntent.FLAG_ONE_SHOT);

            Notification notification = new Notification.Builder(context)
                    .setContentTitle("Ultimate Stopwatch")
                    .setUsesChronometer(true)
                    .setWhen(System.currentTimeMillis() + endTime)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentIntent(contentIntent)
                    .build();

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // We use a layout id because it is a unique number.  We use it later to cancel.
            notificationManager.notify(R.layout.countdown_fragment, notification);
        }
    }  */
    fun cancelChronometerNotification(context: Context?) {
        try {
            (context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(
                R.layout.stopwatch_fragment
            )
            //((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(R.layout.countdown_fragment);
        } catch (ignored: Exception) {
        }
    }

    class UpdateService : Service() {
        override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
            // Build the widget update for today
            //no need for a screen, this just has to refresh all content in the background
            //cancelCountdownAlarm(this);
            notifyStatusBar()
            stopSelf()
            return START_NOT_STICKY
        }

        //show Countdown Complete notification
        private fun notifyStatusBar() {
            // The PendingIntent to launch our activity if the user selects this notification
            val launcher = Intent(this, UltimateStopwatchActivity::class.java)
            launcher.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            launcher.putExtra(INTENT_EXTRA_LAUNCH_COUNTDOWN, true)
            val contentIntent =
                PendingIntent.getActivity(this, 0, launcher, PendingIntent.FLAG_ONE_SHOT)

            // Set the icon, scrolling text and timestamp
            val notification = NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.countdown_complete)) //.setSubText(getString(R.string.countdown_complete))
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(contentIntent)
                .build()
            try {
                notification.ledARGB = -0x7f7f80
                notification.ledOnMS = 500
                notification.ledOffMS = 1000
                if (SettingsActivity.isVibrate) {
                    notification.vibrate = longArrayOf(1000)
                }
                notification.flags = notification.flags or Notification.FLAG_SHOW_LIGHTS
                notification.audioStreamType = AudioManager.STREAM_NOTIFICATION
//                notification.sound= Uri.parse("android.resource://com.geekyouup.android.ustopwatch/" + R.raw.alarm)
            } catch (ignored: Exception) {
            }
            notification.defaults = notification.defaults or Notification.DEFAULT_ALL
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            // We use a layout id because it is a unique number.  We use it later to cancel.
            notificationManager.notify(R.layout.main, notification)
        }

        override fun onBind(arg0: Intent): IBinder? {
            return null
        }
    }
}