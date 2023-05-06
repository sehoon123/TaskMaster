package com.example.taskmaster

<<<<<<< HEAD
import android.graphics.Paint
=======
<<<<<<< HEAD
<<<<<<< HEAD
import android.graphics.Paint
=======
>>>>>>> be589b7526c160a37f82af28ba58ae165c568ff6
>>>>>>> d720d07f826272aa16771263cfb67b6ea7179549
=======
>>>>>>> be589b7526c160a37f82af28ba58ae165c568ff6
>>>>>>> f17bec60d8abc83782de6e50e2a84ae844b04915
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
<<<<<<< HEAD
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> d720d07f826272aa16771263cfb67b6ea7179549
=======
>>>>>>> f17bec60d8abc83782de6e50e2a84ae844b04915
import com.example.taskmaster.databinding.ItemTodoBinding


class TodoAdapter(
    private val todos: MutableList<Todo>,
    private val onItemLongClickListener: (position: Int) -> Unit,
    private val onCheckedChangeListener: ((position: Int, isChecked: Boolean) -> Unit)? = null
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val binding =
            ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodoViewHolder(binding)
<<<<<<< HEAD
<<<<<<< HEAD
=======
=======
>>>>>>> f17bec60d8abc83782de6e50e2a84ae844b04915
=======

class TodoAdapter(private val todos: List<String>) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return TodoViewHolder(itemView)
>>>>>>> be589b7526c160a37f82af28ba58ae165c568ff6
<<<<<<< HEAD
>>>>>>> d720d07f826272aa16771263cfb67b6ea7179549
=======
>>>>>>> f17bec60d8abc83782de6e50e2a84ae844b04915
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todo = todos[position]
<<<<<<< HEAD
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> d720d07f826272aa16771263cfb67b6ea7179549
=======
>>>>>>> f17bec60d8abc83782de6e50e2a84ae844b04915
        holder.bind(todo)
    }

    override fun getItemCount(): Int = todos.size

    inner class TodoViewHolder(private val binding: ItemTodoBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnLongClickListener {

        init {
            binding.root.setOnLongClickListener(this)
            binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCheckedChangeListener(position, isChecked)
                }
            }
        }

        fun bind(todo: Todo) {
            binding.tvTodoTitle.text = todo.title
            binding.checkBox.isChecked = todo.isChecked
            if (todo.isChecked) {
                binding.tvTodoTitle.strikeThrough()
            } else {
                binding.tvTodoTitle.removeStrikeThrough()
            }
        }

        override fun onLongClick(v: View?): Boolean {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onItemLongClickListener(position)
                return true
            }
            return false
        }
    }

    private fun onCheckedChangeListener(position: Int, isChecked: Boolean) {
        val todo = todos[position]
        val updatedTodo = todo.copy(isChecked = isChecked)
        todos.removeAt(position)
        todos.add(updatedTodo)
        notifyItemMoved(position, todos.size - 1)
    }

    private fun View.strikeThrough() {
        this.alpha = 0.5f
        if (this is TextView) {
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }
    }

    private fun View.removeStrikeThrough() {
        this.alpha = 1.0f
<<<<<<< HEAD
<<<<<<< HEAD
=======
=======
>>>>>>> f17bec60d8abc83782de6e50e2a84ae844b04915
=======
        holder.todoText.text = todo
    }

    override fun getItemCount(): Int {
        return todos.size
    }

    class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val todoText: TextView = itemView.findViewById(android.R.id.text1)
>>>>>>> be589b7526c160a37f82af28ba58ae165c568ff6
<<<<<<< HEAD
>>>>>>> d720d07f826272aa16771263cfb67b6ea7179549
=======
>>>>>>> f17bec60d8abc83782de6e50e2a84ae844b04915
    }
}
