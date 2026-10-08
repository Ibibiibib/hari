package com.habitquest.app.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    const val CHANNEL_ID = "daily_reminder"
    private const val WORK_NAME = "daily_reminder_work"

    fun schedule(context: Context) {
        val now = LocalDateTime.now()
        var target = now.toLocalDate().atTime(LocalTime.of(21, 0))
        if (!target.isAfter(now)) target = target.plusDays(1)
        val delay = Duration.between(now, target).toMillis()

        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}

class DailyReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val manager = NotificationManagerCompat.from(context)

        val channel = NotificationChannel(
            ReminderScheduler.CHANNEL_ID,
            "Ежедневное напоминание",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: заменить на свою иконку
            .setContentTitle("не забывай про себя")
            .setContentText("ТЫ самое важное что есть у тебя")
            .setAutoCancel(true)
            .build()

        try {
            manager.notify(1, notification)
        } catch (_: SecurityException) {
            // Разрешение на уведомления не выдано — пропускаем
        }
        return Result.success()
    }
}
