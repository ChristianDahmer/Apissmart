package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object ApisNotificationHelper {
    private const val CHANNEL_ID = "apis_smart_tasks_channel"
    private const val CHANNEL_NAME = "Manejos e Prazos Apícolas"

    fun showTaskDueNotification(context: Context, taskId: Long, taskTitle: String, dueDateFormatted: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações de prazos de manejos e colmeias alcançados"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🐝 Manejo Vencendo / Alcançado: $taskTitle")
            .setContentText("O prazo para realizar este manejo venceu hoje ($dueDateFormatted). Verifique no ApisSmart.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            notificationManager.notify(taskId.toInt(), builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
