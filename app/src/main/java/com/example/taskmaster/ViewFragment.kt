package com.example.taskmaster

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.taskmaster.databinding.FragmentViewBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ViewFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentViewBinding
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap

    private lateinit var database: DatabaseReference
    private var todoKey: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve todo key from arguments
        todoKey = arguments?.getString("todoKey")

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Initialize Firebase Realtime Database reference
        database = FirebaseDatabase.getInstance().reference.child("todos").child(FirebaseAuth.getInstance().currentUser!!.uid).child(getCurrentDateString())
        Log.d("ViewFragment", "Database reference: $database")
    }

    private fun getCurrentDateString(): String {
        val sdf : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Retrieve location data from Firebase Realtime Database
        if (todoKey != null) {
            val todoRef = database.child(todoKey!!)
            todoRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val todo = dataSnapshot.getValue(Todo::class.java)
                    Log.d("ViewFragment", "Snapshot: $dataSnapshot")
                    binding.titleText.setText(todo?.title)

                    if (todo != null && todo.latitude != null && todo.longitude != null) {
                        // Customize the map and add markers, polylines, etc.
                        // Here's an example of adding a marker at a specific location
                        val markerOptions = MarkerOptions()
                            .position(LatLng(todo.latitude, todo.longitude))
                            .title("Todo Location")
                        googleMap.addMarker(markerOptions)

                        // Move the camera to the marker's position
                        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                            LatLng(todo.latitude, todo.longitude),
                            12f
                        )
                        googleMap.animateCamera(cameraUpdate)
                    }

                    setupUpdateLogic(todo)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle database error
                    Toast.makeText(requireContext(), "Failed to retrieve todo data", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }


    private fun setupUpdateLogic(todo: Todo?) {
        binding.btnSave.setOnClickListener { updateTodoInDatabase(todo) }
        binding.btnCancel.setOnClickListener { requireActivity().onBackPressed() }
    }
    private fun updateTodoInDatabase(todo: Todo?) {
        val title = binding.titleText.text.toString()
        if (todo != null) {
            val updatedTodo = todo.copy(title = title)
            todo.key?.let { key ->
                database.child(key).setValue(updatedTodo)
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            R.string.todo_updated_successfully,
                            Toast.LENGTH_SHORT
                        ).show()
                        requireActivity().onBackPressed()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "Error updating todo: ${e.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        } else {
            Toast.makeText(requireContext(), R.string.todo_key_not_found, Toast.LENGTH_SHORT).show()
        }
    }
}
