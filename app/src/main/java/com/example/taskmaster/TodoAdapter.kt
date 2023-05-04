package com.example.taskmaster

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.taskmaster.R

class TodoAdapter(private val todos: List<Pair<String, ByteArray?>>) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bind(todos[position])
    }

    override fun getItemCount(): Int {
        return todos.size
    }

    class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val todoText: TextView = itemView.findViewById(R.id.txtTodo)
        private val todoImage: ImageView = itemView.findViewById(R.id.imgTodo)

        fun bind(todo: Pair<String, ByteArray?>) {
            todoText.text = todo.first
            todo.second?.let {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                todoImage.setImageBitmap(bitmap)
            } ?: run {
                todoImage.setImageResource(0) // or a placeholder image
            }
        }
    }
}
