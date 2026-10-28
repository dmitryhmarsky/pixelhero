package com.pixelhero.widget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager

/**
 * Анимирует виджет, пока экран включён (4 кадра в секунду).
 * Когда экран выключен, сервис ничего не рисует и не тратит батарею: игра догонит время при включении экрана.
 */
class HeroService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var ticking = false
    private var frames = 0

    private val loop = object : Runnable {
        override fun run() {
            if (!ticking) return
            Engine.tick(this@HeroService)
            HeroWidgetProvider.update(this@HeroService, full = false)
            syncDeathNotification(Engine.get(this@HeroService).dead)
            frames++
            if (frames % 120 == 0) Engine.save(this@HeroService)
            handler.postDelayed(this, FRAME_MS)
        }
    }

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> stopTicking()
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> startTicking()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "Пиксель-герой", NotificationManager.IMPORTANCE_MIN),
        )
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val n = Notification.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.star_on)
            .setContentTitle("Пиксель-герой в пути")
            .setContentText("Виджет оживает, пока экран включён")
            .setContentIntent(open)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIF_ID, n)
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(screenReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(screenReceiver, filter)
        }
        if (getSystemService(PowerManager::class.java).isInteractive) startTicking()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopTicking()
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            // уже отписан
        }
        super.onDestroy()
    }

    private var deathShown = false

    /** Уведомление «Герой погиб!» показывается один раз за смерть и пропадает после воскрешения. */
    private fun syncDeathNotification(dead: Boolean) {
        if (dead == deathShown) return
        deathShown = dead
        val nm = getSystemService(NotificationManager::class.java)
        if (!dead) {
            nm.cancel(DEATH_ID)
            return
        }
        nm.createNotificationChannel(
            NotificationChannel(DEATH_CHANNEL, "Герой погиб", NotificationManager.IMPORTANCE_DEFAULT),
        )
        val open = PendingIntent.getActivity(
            this, 1, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val n = Notification.Builder(this, DEATH_CHANNEL)
            .setSmallIcon(android.R.drawable.star_off)
            .setContentTitle("Герой погиб!")
            .setContentText("Нажмите, чтобы воскресить его за рекламу")
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        try {
            nm.notify(DEATH_ID, n)
        } catch (e: Exception) {
            // нет разрешения на уведомления
        }
    }

    private fun startTicking() {
        if (ticking) return
        ticking = true
        handler.post(loop)
    }

    private fun stopTicking() {
        ticking = false
        handler.removeCallbacks(loop)
        Engine.save(this)
    }

    companion object {
        private const val CHANNEL = "hero"
        private const val NOTIF_ID = 1
        private const val DEATH_ID = 2
        private const val DEATH_CHANNEL = "death"
        private const val FRAME_MS = 250L

        /** Запускает сервис; в фоне Android 12+ может отказать, это не ошибка. */
        fun startSafely(context: Context) {
            try {
                context.startForegroundService(Intent(context, HeroService::class.java))
            } catch (e: Exception) {
                // Фон: система не разрешила. Анимация включится при открытии приложения.
            }
        }
    }
}
