package com.example.taskmaster

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.Global.getString
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.taskmaster.databinding.FragmentViewBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*
import android.location.Geocoder
import android.location.Location
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class ViewFragment : Fragment() {

    private lateinit var binding: FragmentViewBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        initializeMapView(savedInstanceState)
        initializeDatabaseReference()
        handleTodoLogic()
        return binding.root
    }

    private fun initializeMapView(savedInstanceState: Bundle?) {
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { map ->
            googleMap = map.apply {
                uiSettings.run {
                    isZoomControlsEnabled = true
                    isZoomGesturesEnabled = true
                    isScrollGesturesEnabled = true
                    isTiltGesturesEnabled = true
                }
            }
            showTodoLocationOnMap()
        }
    }

    private fun handleTodoLogic() {
        val action = arguments?.getString("action")
        val todoKey = arguments?.getString("todoKey")

        if (todoKey != null) {
            myRef.child(todoKey).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val todo = snapshot.getValue(Todo::class.java)
                    Log.d("ViewFragment", "Snapshot: $snapshot")
                    binding.titleText.setText(todo?.title)

                    todo?.let { showTodoLocation(it) }

                    when (action) {
                        "update" -> setupUpdateLogic(todo)
                        "delete" -> setupDeleteLogic(todo)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        requireContext(),
                        "Error reading todo data: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    requireActivity().onBackPressed()
                }
            })
        } else {
            Toast.makeText(requireContext(), R.string.todo_key_not_found, Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
        }
    }

    private fun showTodoLocationOnMap() {
        val latitude = arguments?.getDouble("latitude")
        val longitude = arguments?.getDouble("longitude")

        if (latitude != null && longitude != null && googleMap != null) {
            val location = LatLng(latitude, longitude)
            googleMap?.addMarker(MarkerOptions().position(location))
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }
    }

    private fun initializeDatabaseReference() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "User is not signed in", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
            return
        }

        val userId = user.uid
        val currentDate = getCurrentDateString()
        database = FirebaseDatabase.getInstance()
        myRef = database.getReference("todos").child(userId).child(currentDate)
    }

    private fun getCurrentDateString(): String {
        val sdf: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
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
                myRef.child(key).setValue(updatedTodo)
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

    private fun setupDeleteLogic(todo: Todo?) {
        binding.btnSave.text = getString(R.string.delete)
        binding.btnSave.setOnClickListener { deleteTodoFromDatabase(todo) }
        binding.btnCancel.setOnClickListener { requireActivity().onBackPressed() }
    }

    private fun deleteTodoFromDatabase(todo: Todo?) {
        if (todo != null) {
            todo.key?.let { key ->
                myRef.child(key).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            R.string.todo_deleted_successfully,
                            Toast.LENGTH_SHORT
                        ).show()
                        requireActivity().onBackPressed()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "Error deleting todo: ${e.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        } else {
            Toast.makeText(requireContext(), R.string.todo_key_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showTodoLocation(todo: Todo) {
        val latitude = todo.latitude
        val longitude = todo.longitude
        Log.d("ViewFragment", "showTodoLocation: $latitude, $longitude")

        if (latitude != null && longitude != null && googleMap != null) {
            val location = LatLng(latitude, longitude)
            googleMap?.addMarker(MarkerOptions().position(location))
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        } else {
            Toast.makeText(requireContext(), "Location is not available", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun requestCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        } else {
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        } else {
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            saveLocationToTodo(location)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Failed to get current location",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "Error getting current location: ${e.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } catch (e: SecurityException) {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun saveLocationToTodo(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude
        val todoKey = arguments?.getString("todoKey")
        if (todoKey != null) {
            myRef.child(todoKey).child("latitude").setValue(latitude)
            myRef.child(todoKey).child("longitude").setValue(longitude)
            Toast.makeText(requireContext(), "Location saved to todo", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }

}