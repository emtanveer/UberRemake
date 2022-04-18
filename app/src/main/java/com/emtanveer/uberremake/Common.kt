package com.emtanveer.uberremake

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.emtanveer.uberremake.model.DriverInfoModel
import java.lang.StringBuilder

object Common {
    fun buildWelcomeMessage(): String {
        return StringBuilder("Welcome, ")
            .append(currentUser!!.firstName)
            .append(" ")
            .append(currentUser!!.lastName)
            .toString()
    }

    fun showNotification(context: Context, id: Int, title: String?, body: String?, intent: Intent?) {
        var pendingIntent: PendingIntent? = null
        if (intent != null)
            pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val NOTIFICATION_CHANNEL_ID = "kama_uber_remake"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Uber Remake",
            NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Uber Remake"
                enableLights(true)
                lightColor = Color.RED
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID).apply {
            setContentTitle(title)
            setContentText(body)
            setAutoCancel(false)
            priority = NotificationCompat.PRIORITY_HIGH
            setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            setSmallIcon(R.drawable.ic_notification_icon)
            setLargeIcon(BitmapFactory.decodeResource(context.resources,R.drawable.ic_notification_icon))
        }


        if(pendingIntent != null)
            builder.setContentIntent(pendingIntent)

        val notification = builder.build()
        notificationManager.notify(id, notification)

    }

    const val NOTI_BODY: String = "body"
    const val NOTI_TITLE: String = "title"

    const val TOKEN_REFERENCE: String = "Token"
    const val DRIVERS_LOCATION_REFERENCE: String = "DriversLocation"
    const val DRIVER_INFO_REFERENCE:String = "DriverInfo"
    var currentUser: DriverInfoModel? = null
}