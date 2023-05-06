package com.example.taskmaster

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
<<<<<<< HEAD
import androidx.appcompat.app.AlertDialog
=======
<<<<<<< HEAD
<<<<<<< HEAD
import androidx.appcompat.app.AlertDialog
=======
>>>>>>> be589b7526c160a37f82af28ba58ae165c568ff6
>>>>>>> d720d07f826272aa16771263cfb67b6ea7179549
=======
>>>>>>> be589b7526c160a37f82af28ba58ae165c568ff6
>>>>>>> f17bec60d8abc83782de6e50e2a84ae844b04915
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.taskmaster.databinding.ActivityMainBinding
<<<<<<< HEAD
import java.text.SimpleDateFormat
import java.util.*
=======
<<<<<<< HEAD
<<<<<<< HEAD
import java.text.SimpleDateFormat
import java.util.*
=======
>>>>>>> be589b7526c160a37f82af28ba58ae165c568ff6
>>>>>>> d720d07f826272aa16771263cfb67b6ea7179549
=======
>>>>>>> be589b7526c160a37f82af28ba58ae165c568ff6
>>>>>>> f17bec60d8abc83782de6e50e2a84ae844b04915

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private lateinit var todoAdapter: TodoAdapter
<<<<<<< HEAD
    private val todoList = mutableListOf<Todo>()
=======
<<<<<<< HEAD
<<<<<<< HEAD
    private val todoList = mutableListOf<Todo>()
=======
    private val todoList = mutableListOf<String>()
>>>>>>> be589b7526c160a37f82af28ba58ae165c568ff6
>>>>>>> d720d07f826272aa16771263cfb67b6ea7179549
=======
    private val todoList = mutableListOf<String>()
>>>>>>> be589b7526c160a37f82af28ba58ae165c568ff6
>>>>>>> f17bec60d8abc83782de6e50e2a84ae844b04915

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeDatabaseReference()

        setupRecyclerView()

        setupAddButton()

        readTodosFromDatabase()
    }

    private fun initializeDatabaseReference() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
<<<<<<< HEAD
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> d720d07f826272aa16771263cfb67b6ea7179549
=======
>>>>>>> f17bec60d8abc83782de6e50e2a84ae844b04915
            val userId = user.uid
            val currentDate = getCurrentDateString()
            database = FirebaseDatabase.getInstance()
            myRef = database.getReference("todos").child(userId).child(currentDate)
        }
    }

    private fun getCurrentDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }


    private fun setupRecyclerView() {
        todoAdapter = TodoAdapter(todoList, { position ->
            showUpdateDeleteDialog(position)
        }) { position, isChecked ->
            val todo = todoList[position]
            val updatedTodo = todo.copy(isChecked = isChecked as Boolean)
            updateTodoInDatabase(todo.key, updatedTodo)
        }
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

    private fun showUpdateDeleteDialog(position: Int) {
        val options = arrayOf("Update", "Delete")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose an action")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    val intent = Intent(this, ViewActivity::class.java)
                    intent.putExtra("action", "update")
                    intent.putExtra("todoKey", todoList[position].key)
                    intent.putExtra("todoTitle", todoList[position].title)
                    startActivity(intent)
                }

                1 -> {
                    val todo = todoList[position]
                    deleteTodoFromDatabase(todo.key)
                }
            }
        }
        builder.show()
    }


    private fun addNewTodoToDatabase(newTodo: String) {
        val key = myRef.push().key
        key?.let {
            val todo = Todo(newTodo, false, it)
            myRef.child(it).setValue(todo)
            todoList.add(todo)
            todoAdapter.notifyDataSetChanged()
            binding.etTodo.setText("")
        }
    }

    private fun readTodosFromDatabase() {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val newTodoList = mutableListOf<Todo>()

                for (childSnapshot in dataSnapshot.children) {
                    val todoData = childSnapshot.getValue(Todo::class.java)
                    if (todoData != null) {
                        val key = childSnapshot.key
                        val todoWithKey = todoData.copy(key = key)
                        newTodoList.add(todoWithKey)
                    }
                }

                todoList.clear()
                todoList.addAll(newTodoList)
<<<<<<< HEAD
<<<<<<< HEAD
=======
=======
>>>>>>> f17bec60d8abc83782de6e50e2a84ae844b04915
=======
            // User is signed in, initialize the database reference for the current user
            val userId = user.uid
            database = FirebaseDatabase.getInstance()
            myRef = database.getReference("todos").child(userId)
        }

        // Set up the RecyclerView and its adapter
        todoAdapter = TodoAdapter(todoList)
        binding.rvTodos.adapter = todoAdapter
        binding.rvTodos.layoutManager = LinearLayoutManager(this)

        // Listen for add button clicks
        binding.btnAdd.setOnClickListener {
            val newTodo = binding.etTodo.text.toString().trim()
            if (newTodo.isNotEmpty()) {
                addNewTodo(newTodo)
            }
        }

        // Read data from the Realtime Database
        readTodosFromDatabase()
    }

    private fun addNewTodo(newTodo: String) {
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
>>>>>>> be589b7526c160a37f82af28ba58ae165c568ff6
<<<<<<< HEAD
>>>>>>> d720d07f826272aa16771263cfb67b6ea7179549
=======
>>>>>>> f17bec60d8abc83782de6e50e2a84ae844b04915
                todoAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("MainActivity", "Failed to read value.", databaseError.toException())
            }
        })
    }
<<<<<<< HEAD
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> d720d07f826272aa16771263cfb67b6ea7179549
=======
>>>>>>> f17bec60d8abc83782de6e50e2a84ae844b04915

    private fun updateTodoInDatabase(key: String?, updatedTodo: Todo) {
        key?.let {
            myRef.child(it).setValue(updatedTodo)
        }
    }


    private fun deleteTodoFromDatabase(key: String?) {
        key?.let {
            myRef.child(it).removeValue()
        }
    }

<<<<<<< HEAD
}
=======
}
<<<<<<< HEAD
=======
}
>>>>>>> be589b7526c160a37f82af28ba58ae165c568ff6
>>>>>>> d720d07f826272aa16771263cfb67b6ea7179549
=======
>>>>>>> be589b7526c160a37f82af28ba58ae165c568ff6
>>>>>>> f17bec60d8abc83782de6e50e2a84ae844b04915
