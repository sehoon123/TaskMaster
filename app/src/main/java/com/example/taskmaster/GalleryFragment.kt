package com.example.taskmaster

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskmaster.databinding.FragmentGalleryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GalleryFragment : Fragment() {
    private lateinit var binding: FragmentGalleryBinding
    private lateinit var photoAdapter: PhotoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoAdapter = PhotoAdapter { clickedPhoto ->
            toggleGrayscale(clickedPhoto)
        }

        binding.rvPhotos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = photoAdapter
        }

        loadPhotos()
    }

    private fun loadPhotos() {
        lifecycleScope.launch(Dispatchers.IO) {
            val photos = retrievePhotosFromDatabase()
            withContext(Dispatchers.Main) {
                photoAdapter.submitList(photos)
            }
        }
    }

    private suspend fun retrievePhotosFromDatabase(): List<Photo> {
        return AppDatabase.getInstance(requireContext()).photoDao().getAllPhotos()
    }

    private fun toggleGrayscale(photo: Photo) {
        val newStatus = !photo.isGrayscale
        lifecycleScope.launch(Dispatchers.IO) {
            photo.isGrayscale = newStatus
            AppDatabase.getInstance(requireContext()).photoDao().updatePhoto(photo)
        }
        photoAdapter.notifyItemChanged(photoAdapter.currentList.indexOf(photo))
    }
}
