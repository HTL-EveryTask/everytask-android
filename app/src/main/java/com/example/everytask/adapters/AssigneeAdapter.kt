package com.example.everytask.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.everytask.createChip
import com.example.everytask.databinding.RowAssigneeBinding
import com.google.android.flexbox.FlexboxLayout
import retrofit2.http.Query

class AssigneeAdapter(
    val activity: Activity,
    val assigneeList: MutableList<String>,
    val flexboxLayout: FlexboxLayout
) : RecyclerView.Adapter<AssigneeAdapter.AssigneeViewHolder>() {

    class AssigneeViewHolder(val assigneeBinding: RowAssigneeBinding) :
        RecyclerView.ViewHolder(assigneeBinding.root) {
        fun bind(assignee: String) {
            assigneeBinding.tvUsername.text = assignee
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssigneeViewHolder {
        assigneeList.sort()
        return AssigneeViewHolder(
            RowAssigneeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: AssigneeViewHolder, position: Int) {
        val assignee = assigneeList[position]
        holder.bind(assignee)
        holder.assigneeBinding.clAsigneeContainer.setOnClickListener {
            createChip(activity, assignee, flexboxLayout, this)
            //remove assignee from list
            assigneeList.remove(assignee)
            //notify adapter
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return assigneeList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(filteredList: MutableList<String>) {
        assigneeList.clear()
        assigneeList.addAll(filteredList)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addAssignee(assignee: String) {
        assigneeList.add(assignee)
        //sort list alphabetically
        assigneeList.sort()
        notifyDataSetChanged()
    }
}