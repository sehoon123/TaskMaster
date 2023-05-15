package com.example.taskmaster

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Iterate over all widget instances
        for (appWidgetId in appWidgetIds) {
            // Create RemoteViews object and set the layout for the widget
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            // Set the click listener for the update button
            views.setOnClickPendingIntent(R.id.widgetUpdateButton, getUpdatePendingIntent(context, appWidgetId))

            // Get the uncompleted to-do list items from the database
            getUncompletedTasksFromDatabase(context) { uncompletedTasks ->
                // Create a string representation of the uncompleted tasks
                val tasksText = buildTasksText(uncompletedTasks)

                // Update the text in the widget
                views.setTextViewText(R.id.widgetText, tasksText)

                // Update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    private fun getUncompletedTasksFromDatabase(context: Context, callback: (List<String>) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return

        // Get the current date
        val currentDate = getCurrentDate()

        // Construct the database reference
        val myRef = database.getReference("todos").child(userId).child(currentDate)
        Log.d("MyWidgetProvider", "myRef: $myRef")

        // Retrieve uncompleted tasks from the database
        val uncompletedTasks = mutableListOf<String>()
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (childSnapshot in dataSnapshot.children) {
                    val todo = childSnapshot.getValue(Todo::class.java)
                    if (todo != null && !todo.checked) {
                        uncompletedTasks.add(todo.title)
                        Log.d("MyWidgetProvider", "uncompletedTasks: $uncompletedTasks")
                    }
                }
                callback(uncompletedTasks)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("MyWidgetProvider", "Failed to read value.", databaseError.toException())
                callback(emptyList())
            }
        })
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun buildTasksText(uncompletedTasks: List<String>): String {
        return if (uncompletedTasks.isNotEmpty()) {
            uncompletedTasks.joinToString(separator = "\n")
        } else {
            "No uncompleted tasks"
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                // Update the widget
                onUpdate(context, appWidgetManager, intArrayOf(appWidgetId))
            }
        }
    }

    private fun getUpdatePendingIntent(context: Context, appWidgetId: Int): PendingIntent {
        val intent = Intent(context, MyWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))

        // Add FLAG_IMMUTABLE to the PendingIntent flags
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, appWidgetId, intent, flags)
    }

}
