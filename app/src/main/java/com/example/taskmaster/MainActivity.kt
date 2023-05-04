package com.example.taskmaster

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.taskmaster.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference // Changed type to DatabaseReference
    private lateinit var todoAdapter: TodoAdapter
    private val todoList = mutableListOf<Pair<String, ByteArray?>>()

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
            database =
                FirebaseDatabase.getInstance().getReference("users").child(userId).child("todos")
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
        val newTodo = binding.etTodo.text.toString().trim()
        if (newTodo.isEmpty()) {
            Toast.makeText(this, "Please enter a todo description", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(this, CameraActivity::class.java)
        intent.putExtra("newTodo", newTodo)
        startActivityForResult(intent, REQUEST_CODE_CAMERA)
    }

    private fun addNewTodoToDatabase(newTodo: String) {
        val key = database.push().key
        key?.let {
            database.child(it).child("text").setValue(newTodo)
            binding.etTodo.setText("")
        }
    }

    private fun readTodosFromDatabase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val newTodoList = mutableListOf<Pair<String, ByteArray?>>()

                for (childSnapshot in dataSnapshot.children) {
                    val todoText = childSnapshot.child("text").getValue(String::class.java)
                    val todoImageIntList = childSnapshot.child("image").getValue<List<Int>>(object: GenericTypeIndicator<List<Int>>() {})

                    val todoImageByteArray = todoImageIntList?.let { intList ->
                        val byteArray = ByteArray(intList.size)
                        for (i in intList.indices) {
                            byteArray[i] = intList[i].toByte()
                        }
                        byteArray
                    }

                    if (todoText != null) {
                        newTodoList.add(Pair(todoText, todoImageByteArray))
                    } else {
                        Log.w("MainActivity", "Invalid todo data format: $todoText")
                    }
                }

                todoList.clear()
                todoList.addAll(newTodoList)
                todoAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("MainActivity", "Failed to read value.", databaseError.toException())
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_CAMERA && resultCode == RESULT_OK) {
            data?.let {
                val newTodo = it.getStringExtra("newTodo")
                val imageByteArray = it.getByteArrayExtra("imageByteArray")

                if (newTodo != null && imageByteArray != null) {
                    Log.i("MainActivity", "Photo taken successfully")
                    addNewTodoWithImageToDatabase(newTodo, imageByteArray)
                } else {
                    Log.w("MainActivity", "Photo taken, but newTodo or imageByteArray is null")
                }
            }
        } else if (requestCode == REQUEST_CODE_CAMERA && resultCode == RESULT_CANCELED) {
            Log.w("MainActivity", "Photo not taken")
        }
    }

    private fun addNewTodoWithImageToDatabase(todo: String, imageByteArray: ByteArray) {
        val imageList = imageByteArray.toList().map { it.toInt() }
        val todoWithImage = TodoWithImage(todo, imageList)

//        database.child("todos").push().setValue(todoWithImage)
        database.push().setValue(todoWithImage)
            .addOnSuccessListener {
                Log.w("MainActivity", "Todo with image added to database successfully")
            }
            .addOnFailureListener { exception ->
                Log.w("MainActivity", "Error adding todo with image to database", exception)
            }
    }

    companion object {
        private const val REQUEST_CODE_CAMERA = 1001
    }
}
