package com.example.taskmaster

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.taskmaster.databinding.ItemPhotoBinding

class PhotoAdapter(private val onItemClick: (Photo) -> Unit) :
    ListAdapter<Photo, PhotoAdapter.PhotoViewHolder>(PhotoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPhotoBinding.inflate(inflater, parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = getItem(position)
        holder.bind(photo)
    }

    inner class PhotoViewHolder(private val binding: ItemPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(photo: Photo) {
            // Load the image using Glide
            Glide.with(binding.imageView)
                .load(photo.filePath)
                .centerCrop()
                .into(binding.imageView)

            // Set click listener to handle item click
            binding.root.setOnClickListener {
                onItemClick(photo)
            }

            // Apply grayscale filter if the photo is in grayscale
            val matrix = ColorMatrix().apply {
                setSaturation(if (photo.isGrayscale) 0f else 1f)
            }
            val filter = ColorMatrixColorFilter(matrix)
            binding.imageView.colorFilter = filter

            // Set the photo object to the binding variable
            binding.photo = photo
            binding.executePendingBindings()
        }
    }

    private class PhotoDiffCallback : DiffUtil.ItemCallback<Photo>() {
        override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem == newItem
        }
    }
}
