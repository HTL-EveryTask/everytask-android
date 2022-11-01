package com.example.everytask

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.everytask.databinding.RowTasksBinding
import com.example.everytask.models.Task

class TaskAdapter(val taskList: List<Task>): RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    //make checkbox array to save which tasks are done
    public var checked = BooleanArray(taskList.size)

    class TaskViewHolder(val itemBinding: RowTasksBinding, val checked: BooleanArray): RecyclerView.ViewHolder(itemBinding.root){
        fun bind(task: Task){
            itemBinding.tvTaskTitle.text = task.title
            itemBinding.tvTaskDescription.text = task.description
            itemBinding.cbDone.isChecked = checked[adapterPosition]
            itemBinding.cbDone.setOnCheckedChangeListener { buttonView, isChecked ->
                checked[adapterPosition] = isChecked
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(RowTasksBinding.inflate(LayoutInflater.from(parent.context), parent, false),checked)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int {
        return taskList.size
    }
}