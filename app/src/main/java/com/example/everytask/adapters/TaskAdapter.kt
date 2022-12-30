package com.example.everytask.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.everytask.TaskEditActivity
import com.example.everytask.databinding.RowTasksBinding
import com.example.everytask.fragments.HomeFragment
import com.example.everytask.models.response.tasks.Task

class TaskAdapter(val taskList: List<Task>, val homeFragment: HomeFragment): RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    //make checkbox array to save which tasks are done
    var checked = BooleanArray(taskList.size)

    class TaskViewHolder(
        val tasksBinding: RowTasksBinding,
        val checked: BooleanArray,
        val homeFragment: HomeFragment,
    ): RecyclerView.ViewHolder(tasksBinding.root){
        fun bind(task: Task){
            tasksBinding.tvTaskTitle.text = task.title
            tasksBinding.tvTaskDescription.text = task.due_time
            checked[adapterPosition] = task.is_done!!
            tasksBinding.cbDone.isChecked = checked[adapterPosition]
            tasksBinding.cbDone.setOnCheckedChangeListener { buttonView, isChecked ->
                checked[adapterPosition] = isChecked
                homeFragment.toggleDone(task.id!!, isChecked)
            }
            tasksBinding.flBtnDeleteContainer.setOnClickListener {
                homeFragment.showDeleteAlert(task)
            }
            tasksBinding.llTaskTextContainer.setOnClickListener {
                val intent = Intent(homeFragment.requireContext(), TaskEditActivity::class.java)
                intent.putExtra("TASK", task)
                startActivity(homeFragment.requireContext(), intent, null)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(
            RowTasksBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            checked,
            homeFragment
        )
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int {
        return taskList.size
    }
}