package com.example.taskmaster

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.taskmaster.databinding.FragmentCameraBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import androidx.room.Room
import androidx.room.RoomDatabase

class CameraFragment : Fragment() {
    private lateinit var binding: FragmentCameraBinding
    private lateinit var imageCapture: ImageCapture
    private lateinit var photoDao: PhotoDao

    private val CAMERA_PERMISSION_REQUEST_CODE = 100

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoDao = AppDatabase.getInstance(requireContext()).photoDao()
        requestCameraPermission()
        setupCamera()
        setupCaptureButton()
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission is already granted, proceed with camera initialization
            setupCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted, proceed with camera initialization
                setupCamera()
            } else {
                // Camera permission denied, handle accordingly (e.g., show an error message)
            }
        }
    }

    private fun setupCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun setupCaptureButton() {
        binding.takePhotoButton.setOnClickListener {
            Log.d("CameraFragment", "Take photo button clicked")
            takePhoto()
        }
    }

    private fun takePhoto() {
        val photoFile = createPhotoFile()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // Photo saved successfully
                    val savedUri = outputFileResults.savedUri ?: photoFile.toUri()
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val numericTimeStamp = timeStamp.replace("_", "").replace(" ", "")

                    // Create a Photo object with relevant details
                    val photo = Photo(
                        filePath = savedUri.toString(),
                        timestamp = numericTimeStamp.toLong()
                    )

                    // Insert the Photo object into the room database
                    lifecycleScope.launch(Dispatchers.IO) {
                        photoDao.insertPhoto(photo)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    // Error occurred while capturing photo
                    exception.printStackTrace()
                }
            }
        )
    }

    private fun createPhotoFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val fileName = "IMG_$timeStamp.jpg"
        val file = File(storageDir, fileName)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).parse(timeStamp)?.time ?: 0L
        file.setLastModified(timestamp)
        return file
    }
}
