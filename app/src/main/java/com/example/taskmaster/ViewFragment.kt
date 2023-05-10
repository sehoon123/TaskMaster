package com.example.taskmaster

import android.os.Bundle
import android.provider.Settings.Global.getString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import com.example.taskmaster.databinding.FragmentViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class ViewFragment : Fragment() {

    private lateinit var binding: FragmentViewBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewBinding.inflate(inflater, container, false)

        initializeDatabaseReference()

        val action = arguments?.getString("action")
        val todoKey = arguments?.getString("todoKey")

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
                    Toast.makeText(requireContext(), "Error reading todo data: ${error.message}", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                }
            })
        } else {
            Toast.makeText(requireContext(), R.string.todo_key_not_found, Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
        }

        return binding.root
    }

    private fun initializeDatabaseReference() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "User is signed in", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
            return
        }

        val userId = user.uid
        val currentDate = getCurrentDateString()
        database = FirebaseDatabase.getInstance()
        myRef = database.getReference("todos").child(userId).child(currentDate)
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
            requireActivity().onBackPressed()
        }
    }

    private fun updateTodoInDatabase(todo: Todo?) {
        val title = binding.titleText.text.toString()
        if (todo != null) {
            val updatedTodo = todo.copy(title = title)
            todo.key?.let {
                myRef.child(it).setValue(updatedTodo).addOnSuccessListener {
                    Toast.makeText(requireContext(), R.string.todo_updated_successfully, Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                }.addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error updating todo: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), R.string.todo_key_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupDeleteLogic(todo: Todo?) {
        binding.btnSave.text = getString(R.string.delete)
        binding.btnSave.setOnClickListener {
            deleteTodoFromDatabase(todo)
        }
        binding.btnCancel.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun deleteTodoFromDatabase(todo: Todo?) {
        if (todo != null) {
            todo.key?.let {
                myRef.child(it).removeValue().addOnSuccessListener {
                    Toast.makeText(requireContext(), R.string.todo_deleted_successfully, Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                }.addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error deleting todo: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), R.string.todo_key_not_found, Toast.LENGTH_SHORT).show()
        }
    }
}
