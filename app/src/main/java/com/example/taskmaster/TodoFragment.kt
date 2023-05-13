package com.example.taskmaster

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskmaster.databinding.FragmentTodoBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TodoFragment : Fragment() {

    private lateinit var binding: FragmentTodoBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private lateinit var todoAdapter: TodoAdapter
    private val todoList = mutableListOf<Todo>()

    private var selectedDate: Calendar? = null
    private lateinit var model: SharedViewModel

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentUserLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        model.selectedDate.observe(this, { selectedDate ->
            this.selectedDate = selectedDate
        })

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTodoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeDatabaseReference()

        setupRecyclerView()

        setupAddButton()

        // Observe changes to the selectedDate LiveData
        model.selectedDate.observe(viewLifecycleOwner, { selectedDate ->
            this.selectedDate = selectedDate
            Log.d("TodoFragment", "Selected date: $selectedDate")

            // Update the database reference and read the todos again every time the selectedDate changes
            initializeDatabaseReference()
            readTodosFromDatabase()
        })

        // Request location permission
        requestLocationPermission()
    }

    private fun getDateString(calendar: Calendar): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun initializeDatabaseReference() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        } else {
            val userId = user.uid
            // Use selectedDate if it's not null, else use the current date
            val dateString = if (selectedDate != null) {
                getDateString(selectedDate!!)
            } else {
                getCurrentDateString()
            }
            database = FirebaseDatabase.getInstance()
            myRef = database.getReference("todos").child(userId).child(dateString)
            Log.d("TodoFragment", "Database reference initialized")
        }
    }

    private fun getCurrentDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun setupRecyclerView() {
        todoAdapter = TodoAdapter(todoList, { position ->
            Log.d("TodoFragment", "Todo clicked at position $position")
            val viewFragment = ViewFragment()
            val bundle = Bundle()
            bundle.putString("action", "update")
            bundle.putString("todoKey", todoList[position].key)
            bundle.putString("todoTitle", todoList[position].title)
            viewFragment.arguments = bundle
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, viewFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }, { key, todo ->
            if (key == null) {
                deleteTodoFromDatabase(todo.key)
                Log.d("TodoFragment", "Todo deleted from database")
            } else {
                updateTodoInDatabase(key, todo)
                Log.d("TodoFragment", "Todo updated in database")
            }
        }, binding.rvTodos)
        binding.rvTodos.adapter = todoAdapter
        binding.rvTodos.layoutManager = LinearLayoutManager(requireContext())
        Log.d("TodoFragment", "RecyclerView setup")
    }


    private fun setupAddButton() {
        binding.btnAdd.setOnClickListener {
            val newTodo = binding.etTodo.text.toString().trim()
            if (newTodo.isNotEmpty()) {
                addNewTodoToDatabase(newTodo)
                Log.d("TodoFragment", "New todo added to database")
            }
        }
    }

    private fun addNewTodoToDatabase(newTodo: String) {
        val key = myRef.push().key
        key?.let {
            val latitude = currentUserLocation?.latitude
            val longitude = currentUserLocation?.longitude
            val todo = Todo(newTodo, false, it, latitude, longitude)
            myRef.child(it).setValue(todo)
            todoList.add(todo) // Add the todo to the todoList
            todoAdapter.notifyDataSetChanged()
            binding.etTodo.setText("")
            Log.d("TodoFragment", "Todo added to database key: $key")
        }
    }

    private fun readTodosFromDatabase() {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("TodoFragment", "Data received from db: $dataSnapshot")
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

                Log.d("TodoFragment", "Todo list updated")

                // Set the checked state of the todos
                for (todo in todoList) {
                    val index = todoList.indexOf(todo)
                    val viewHolder = binding.rvTodos.findViewHolderForAdapterPosition(index)
                    if (viewHolder != null && viewHolder is TodoAdapter.TodoViewHolder) {
                        viewHolder.binding.checkBox.isChecked = todo.checked
                        Log.d("TodoFragment", "Todo checked state updated")
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("TodoFragment", "Failed to read value.", databaseError.toException())
            }
        })
    }

    private fun updateTodoInDatabase(key: String?, updatedTodo: Todo) {
        key?.let {
            myRef.child(it).setValue(updatedTodo).addOnSuccessListener {
                // Update the checked property of the Todo object
                Log.d("TodoAdapter", "updatedTodo: $updatedTodo")
                val updatedTodoWithCheck = updatedTodo.copy(checked = updatedTodo.checked)
                Log.d("TodoAdapter", "Todo updated in database: $updatedTodoWithCheck")

                // Remove the updated todo from the list and add it back at the correct index
                todoList.removeAt(todoList.indexOfFirst { it.key == key })
                if (updatedTodoWithCheck.checked) {
                    todoList.add(updatedTodoWithCheck)
                } else {
                    todoList.add(0, updatedTodoWithCheck)
                }
                todoAdapter.notifyDataSetChanged()
                Log.d("TodoAdapter", "Todo updated in list: $updatedTodoWithCheck")
            }.addOnFailureListener { e ->
                Log.e("TodoAdapter", "Error updating todo: ${e.localizedMessage}")
            }
        }
    }

    private fun deleteTodoFromDatabase(key: String?) {
        key?.let {
            myRef.child(it).removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("TodoFragment", "Todo deleted from database")
                } else {
                    Log.w("TodoFragment", "Failed to delete todo from database")
                }
            }
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getCurrentLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                currentUserLocation = location
                Log.d("TodoFragment", "Current location: $currentUserLocation")
            }
            .addOnFailureListener { e ->
                Log.e("TodoFragment", "Failed to get current location: ${e.message}")
            }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}