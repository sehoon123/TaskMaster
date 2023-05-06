package com.example.taskmaster

<<<<<<< HEAD
=======
import android.graphics.Paint
>>>>>>> d98425475cd79fdf1325e3f434a91a2b8c79bfca
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
<<<<<<< HEAD

class TodoAdapter(private val todos: List<String>) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return TodoViewHolder(itemView)
=======
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
>>>>>>> d98425475cd79fdf1325e3f434a91a2b8c79bfca
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todo = todos[position]
<<<<<<< HEAD
        holder.todoText.text = todo
    }

    override fun getItemCount(): Int {
        return todos.size
    }

    class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val todoText: TextView = itemView.findViewById(android.R.id.text1)
=======
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
>>>>>>> d98425475cd79fdf1325e3f434a91a2b8c79bfca
    }
}
