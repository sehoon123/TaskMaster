package com.example.taskmaster

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.taskmaster.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private lateinit var todoAdapter: TodoAdapter
    private val todoList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeDatabaseReference()

        setupRecyclerView()

        setupAddButton()

        setupCameraButton()

        readTodosFromDatabase()
    }

    private fun initializeDatabaseReference() {
        // Check if the user is signed in
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // If the user is not signed in, start LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // User is signed in, initialize the database reference for the current user
            val userId = user.uid
            database = FirebaseDatabase.getInstance()
            myRef = database.getReference("todos").child(userId)
        }
    }

    private fun setupRecyclerView() {
        todoAdapter = TodoAdapter(todoList)
        binding.rvTodos.adapter = todoAdapter
        binding.rvTodos.layoutManager = LinearLayoutManager(this)
    }

    private fun setupAddButton() {
        binding.btnAdd.setOnClickListener {
            val newTodo = binding.etTodo.text.toString().trim()
            if (newTodo.isNotEmpty()) {
                addNewTodoToDatabase(newTodo)
            }
        }
    }

    private fun setupCameraButton() {
        binding.btnCamera.setOnClickListener {
            launchCameraActivity()
        }
    }

    private fun launchCameraActivity() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
    }

    private fun addNewTodoToDatabase(newTodo: String) {
        val key = myRef.push().key
        key?.let {
            myRef.child(it).setValue(newTodo)
            binding.etTodo.setText("")
        }
    }

    private fun readTodosFromDatabase() {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                todoList.clear()
                dataSnapshot.children.forEach { dataSnapshot ->
                    val todo = dataSnapshot.getValue(String::class.java)
                    todo?.let { todoList.add(it) }
                }
                todoAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("MainActivity", "Failed to read value.", databaseError.toException())
            }
        })
    }
}
