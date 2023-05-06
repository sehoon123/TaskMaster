package com.example.taskmaster

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.taskmaster.databinding.ActivityViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class ViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeDatabaseReference()

        val action = intent.getStringExtra("action")
        val todoKey = intent.getStringExtra("todoKey")

        if (todoKey != null) {
            myRef.child(todoKey).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val todo = snapshot.getValue(Todo::class.java)
                    binding.titleText.setText(todo?.title)

                    when (action) {
                        "update" -> {
                            setupUpdateLogic(todo)
                        }
                        "delete" -> {
                            setupDeleteLogic(todo)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ViewActivity, "Error reading todo data: ${error.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            })
        } else {
            Toast.makeText(this, R.string.todo_key_not_found, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initializeDatabaseReference() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid
            val currentDate = getCurrentDateString()
            database = FirebaseDatabase.getInstance()
            myRef = database.getReference("todos").child(userId).child(currentDate)
        }
    }

    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun setupUpdateLogic(todo: Todo?) {
        binding.btnSave.setOnClickListener {
            updateTodoInDatabase(todo)
        }
        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun updateTodoInDatabase(todo: Todo?) {
        val title = binding.titleText.text.toString()
        if (todo != null) {
            val updatedTodo = todo.copy(title = title)
            todo.key?.let {
                myRef.child(it).setValue(updatedTodo).addOnSuccessListener {
                    Toast.makeText(this, R.string.todo_updated_successfully, Toast.LENGTH_SHORT).show()
                    finish()
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Error updating todo: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, R.string.todo_key_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupDeleteLogic(todo: Todo?) {
        binding.btnSave.text = getString(R.string.delete)
        binding.btnSave.setOnClickListener {
            deleteTodoFromDatabase(todo)
        }
        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun deleteTodoFromDatabase(todo: Todo?) {
        if (todo != null) {
            todo.key?.let {
                myRef.child(it).removeValue().addOnSuccessListener {
                    Toast.makeText(this, R.string.todo_deleted_successfully, Toast.LENGTH_SHORT).show()
                    finish()
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Error deleting todo: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, R.string.todo_key_not_found, Toast.LENGTH_SHORT).show()
        }
    }
}
