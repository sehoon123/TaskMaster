package com.example.taskmaster

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.Ringtone
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class RingtonePlayingService : Service() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "default"

            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("알람시작")
                .setContentText("알람음이 재생됩니다.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build()

            startForeground(1, notification)
        }
        // 포그라운드 서비스 동작 수행

        // 알림음 재생
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm)
        mediaPlayer?.start()


    }

    override fun onDestroy() {
        super.onDestroy()

        // 알람음 재생 중지
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

//        Log.d("onDestroy() 실행", "서비스 파괴")
    }
}