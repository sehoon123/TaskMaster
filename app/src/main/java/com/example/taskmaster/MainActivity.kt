package com.example.taskmaster

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.taskmaster.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private lateinit var todoAdapter: TodoAdapter
    private val todoList = mutableListOf<Todo>()

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
                todoAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("MainActivity", "Failed to read value.", databaseError.toException())
            }
        })
    }

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

}