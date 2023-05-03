package com.example.taskmaster

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class MainActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if the user is signed in
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // If the user is not signed in, start LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        database = FirebaseDatabase.getInstance()
        myRef = database.getReference("message")

        // Write data to the Realtime Database
        myRef.setValue("Hello, World!")

        // Read data from the Realtime Database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(String::class.java)
                Log.d("MainActivity", "Value is: $value")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("MainActivity", "Failed to read value.", databaseError.toException())
            }
        })
    }
}
