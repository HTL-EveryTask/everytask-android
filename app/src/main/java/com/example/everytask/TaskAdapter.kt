package com.example.everytask

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.everytask.databinding.RowTasksBinding
import com.example.everytask.models.Task

class TaskAdapter(val taskList: List<Task>): RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(val itemBinding: RowTasksBinding): RecyclerView.ViewHolder(itemBinding.root){
        fun bind(task: Task){
            itemBinding.tvTaskTitle.text = task.title
            itemBinding.tvTaskDescription.text = task.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(RowTasksBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int {
        return taskList.size
    }
}