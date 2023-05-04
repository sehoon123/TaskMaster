package com.example.taskmaster

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taskmaster.databinding.ActivityCameraBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException

class CameraActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var surfaceHolder: SurfaceHolder
    private lateinit var camera: android.hardware.Camera
    private lateinit var todoList: MutableList<String>
    private lateinit var todoAdapter: TodoAdapter
    private var capturedImage: Bitmap? = null

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        surfaceHolder = binding.surfaceView.holder
        surfaceHolder.addCallback(this)

        binding.captureButton.setOnClickListener {
            takePhoto()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCameraPreview()
        } else {
            requestCameraPermission()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // No implementation needed
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        releaseCamera()
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraPreview()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun startCameraPreview() {
        try {
            camera = android.hardware.Camera.open()
            camera.setDisplayOrientation(getCameraDisplayOrientation())
            camera.setPreviewDisplay(surfaceHolder)
            camera.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun releaseCamera() {
        camera.stopPreview()
        camera.release()
    }

    private fun takePhoto() {
        camera.takePicture(null, null, { data, _ ->
            capturedImage = data?.let {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                rotateBitmap(bitmap)
            }
            showPhotoOptions()
            camera.startPreview()
        })
    }

    private fun showPhotoOptions() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Photo Options")
            .setMessage("What would you like to do with the photo?")
            .setPositiveButton("Proceed") { _, _ ->
                savePhotoToTodoList()
            }
            .setNegativeButton("Retake") { _, _ ->
                capturedImage = null
            }
            .setCancelable(false)
            .create()

        dialog.show()
    }


    private fun savePhotoToTodoList() {
        capturedImage?.let { image ->
            val newTodo = "Photo Task" // Example: Create a new task for the photo
            val imageByteArray: ByteArray = convertImageToByteArray(image)

            // Add the new task with the photo to the todo list
            todoList.add(newTodo)
            todoAdapter.notifyDataSetChanged()

            // Save the image byte array along with the task or perform any necessary actions
            // Example: todoList.savePhotoWithTask(newTodo, imageByteArray)

            // Clear the captured image after saving
            capturedImage = null
        }
    }

    private fun convertImageToByteArray(image: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return outputStream.toByteArray()
    }

    private fun rotateBitmap(bitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(90f)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun getCameraDisplayOrientation(): Int {
        val info = android.hardware.Camera.CameraInfo()
        android.hardware.Camera.getCameraInfo(android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK, info)
        val rotation = windowManager.defaultDisplay.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }

        var result = 0
        if (info.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360  // compensate for the mirror effect
        } else {  // back-facing camera
            result = (info.orientation - degrees + 360) % 360
        }

        return result
    }
}
