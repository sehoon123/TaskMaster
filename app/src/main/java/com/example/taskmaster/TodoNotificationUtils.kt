package com.example.taskmaster

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.taskmaster.R
import com.example.taskmaster.TodoFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TodoNotificationUtils : BroadcastReceiver() {

    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference

    override fun onReceive(context: Context, intent: Intent) {
        // Initialize Firebase
        database = FirebaseDatabase.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return
        val dateString = getCurrentDateString()
        Log.d("TodoNotificationUtils", "dateString: $dateString")
        myRef = database.getReference("todos").child(userId).child(dateString)
        Log.d("TodoNotificationUtils", "Database reference: $myRef")

        // Retrieve uncompleted tasks from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val uncompletedTasks = mutableListOf<Todo>()

                for (childSnapshot in dataSnapshot.children) {
                    val todo = childSnapshot.getValue(Todo::class.java)
                    if (todo != null && !todo.checked) {
                        uncompletedTasks.add(todo)
                    }
                }

                Log.d("TodoNotificationUtils", "Uncompleted tasks: $uncompletedTasks")

                if (uncompletedTasks.isNotEmpty()) {
                    // Build and display notification
                    if (!isAppInForeground(context)) {
                        val notificationBuilder = buildNotification(context, uncompletedTasks.size)

                        val notificationManager = NotificationManagerCompat.from(context)
                        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun getCurrentDateString(): String {
        // Implement your logic here to get the current date string in the desired format
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun buildNotification(
        context: Context,
        uncompletedTaskCount: Int
    ): NotificationCompat.Builder {
        val channelId = "uncompleted_tasks_channel"
        val channelName = "Uncompleted Tasks"

        // Create the notification channel (required for Android 8.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open TodoFragment when notification is clicked
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("notification_target", "TodoFragment")
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        // Build the notification
        return NotificationCompat.Builder(context, channelId)
            .setContentTitle("Uncompleted Tasks")
            .setContentText("You have $uncompletedTaskCount uncompleted tasks.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
    }

    private fun isAppInForeground(context: Context): Boolean {
        val appProcessInfo = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(appProcessInfo)
        return appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
    }

    companion object {
        private const val NOTIFICATION_ID = 123
    }
}
