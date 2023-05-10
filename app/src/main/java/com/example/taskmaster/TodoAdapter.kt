package com.example.taskmaster

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmaster.databinding.TodoListItemBinding

class TodoAdapter(
    private val todoList: MutableList<Todo>,
    private val updateDeleteClickCallback: (Int) -> Unit,
    private val updateTodoInDatabase: (String?, Todo) -> Unit,
    private val recyclerView: RecyclerView
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TodoListItemBinding.inflate(inflater, parent, false)
        return TodoViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return todoList.size
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        Log.d("TodoAdapter", "onBindViewHolder called for position $position")
        if (holder.adapterPosition != RecyclerView.NO_POSITION) {
            val todo = todoList[holder.adapterPosition]
            holder.update(todo)
            holder.itemView.setOnClickListener {
                updateDeleteClickCallback(holder.adapterPosition)
            }
        }
    }

    inner class TodoViewHolder(val binding: TodoListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun update(todo: Todo) {
            Log.d("TodoViewHolder", "bind called for todo ${todo.title}")
            binding.tvTodoTitle.text = todo.title
            binding.checkBox.setOnCheckedChangeListener(null) // remove previous listener to avoid side-effects
            binding.checkBox.isChecked = todo.checked
            Log.d("TodoViewHolder", "Todo checked: ${todo.checked}")
            binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked != todo.checked) { // Check if the state has changed
                    val updatedTodo = todo.copy(checked = isChecked)
                    updateTodoInDatabase(todo.key, updatedTodo)
                    todoList[adapterPosition] = updatedTodo // update the todo in the list
                } else {
                    Log.d("TodoViewHolder", "Todo checked state not changed")
                }
            }
            binding.btnDelete.setOnClickListener {
                todoList.removeAt(adapterPosition)
                notifyItemRemoved(adapterPosition)
                updateTodoInDatabase(null, todo)
                Log.d("TodoViewHolder", "Todo deleted")
            }
        }
    }

}
