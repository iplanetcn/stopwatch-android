@file:SuppressLint("UnspecifiedImmutableFlag")
package com.cherry.android.stopwatch.utils

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cherry.android.stopwatch.MainActivity
import com.cherry.android.stopwatch.R


object AlarmUpdater {
    const val INTENT_EXTRA_LAUNCH_COUNTDOWN = "launch_countdown"
    const val CHANNEL_ID = "STOPWATCH_NOTIFICATIONS"
    const val NOTIFICATION_ID = 9527

    const val NAME = "stopwatch_notify"


    fun cancelCountdownAlarm(context: Context?) {
        try {
            val alarmMan = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val defineIntent = Intent(context, UpdateService::class.java)
            defineIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            val piWakeUp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.getService(
                    context,
                    0,
                    defineIntent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getService(context, 0, defineIntent, PendingIntent.FLAG_NO_CREATE)
            }
            if (piWakeUp != null) {
                alarmMan.cancel(piWakeUp)
            }
        } catch (ignored: Exception) {
        }
        try {
            (context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(
                R.layout.activity_main
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.getService(
                    context,
                    0,
                    defineIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getService(
                    context,
                    0,
                    defineIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
        alarmMan.cancel(piWakeUp)
        if (inMillis != -1L) {
            alarmMan[AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + inMillis] = piWakeUp
        }
    }

    fun showChronometerNotification(context: Context, startTime: Long) {
        // The PendingIntent to launch our activity if the user selects this notification
        val launcher = Intent(context, MainActivity::class.java)
        launcher.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val contentIntent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.getActivity(
                    context,
                    0,
                    launcher,
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getActivity(context, 0, launcher, PendingIntent.FLAG_ONE_SHOT)
            }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.app_name))
            .setWhen(System.currentTimeMillis() - startTime)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentIntent(contentIntent)
            .setUsesChronometer(true)
            .build()
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // We use a layout id because it is a unique number.  We use it later to cancel.
        notificationManager.notify(R.layout.fragment_stopwatch, notification)
    }

    fun cancelChronometerNotification(context: Context?) {
        try {
            (context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(
                R.layout.fragment_stopwatch
            )
            //((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(R.layout.fragment_countdown);
        } catch (ignored: Exception) {
        }
    }

    class UpdateService : Service() {
        override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
            cancelCountdownAlarm(this)
            notifyStatusBar()
            stopSelf()
            return START_NOT_STICKY
        }

        //show Countdown Complete notification
        private fun notifyStatusBar() {
            // The PendingIntent to launch our activity if the user selects this notification
            val launcher = Intent(this, MainActivity::class.java)
            launcher.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            launcher.putExtra(INTENT_EXTRA_LAUNCH_COUNTDOWN, true)
            val contentIntent =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.getActivity(
                        this,
                        0,
                        launcher,
                        PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                    )
                } else {
                    PendingIntent.getActivity(this, 0, launcher, PendingIntent.FLAG_ONE_SHOT)
                }

            // Set the icon, scrolling text and timestamp
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.countdown_complete)) //.setSubText(getString(R.string.countdown_complete))
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentIntent(contentIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .build()

            val notificationChannel: NotificationChannel
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationChannel =
                    NotificationChannel(CHANNEL_ID, NAME, NotificationManager.IMPORTANCE_DEFAULT)
                notificationChannel.enableLights(true)
                notificationChannel.enableVibration(true)
                notificationChannel.vibrationPattern = longArrayOf(1000)
                notificationChannel.lightColor = -0x7f7f80
                notificationChannel.lockscreenVisibility
                notificationChannel.setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .build()
                )
                NotificationManagerCompat.from(this).createNotificationChannel(notificationChannel)
            }

            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
        }

        override fun onBind(arg0: Intent): IBinder? {
            return null
        }
    }
}