package com.example.everytask.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.example.everytask.createChip
import com.example.everytask.databinding.RowAssigneeBinding
import com.example.everytask.models.response.groups.Group
import com.example.everytask.models.response.groups.GroupUser
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip
import java.io.Serializable

class AssigneeAdapter(
    val activity: Activity,
    val assigneeList: List<Serializable>,
    val filteredList: MutableList<Serializable>,
    val search: EditText,
    val chipGroup: FlexboxLayout,
) : RecyclerView.Adapter<AssigneeAdapter.AssigneeViewHolder>() {

    val chips = chipGroup.children

    class AssigneeViewHolder(val assigneeBinding: RowAssigneeBinding) :
        RecyclerView.ViewHolder(assigneeBinding.root) {
        fun bind(assignee: Serializable) {
            if (assignee is GroupUser) {
                assigneeBinding.tvAssignee.text = assignee.username
            } else if (assignee is Group) {
                assigneeBinding.tvAssignee.text = assignee.name
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssigneeViewHolder {
        search.addTextChangedListener(object : android.text.TextWatcher {
            @SuppressLint("NotifyDataSetChanged")
            override fun afterTextChanged(s: android.text.Editable?) {
                filteredList.clear()
                assigneeList.forEach {
                    //if it is a group and the group name contains the search text or if it is a user and the user name contains the search text
                    if ((it is Group && it.name.contains(
                            s.toString(),
                            true
                        )) || (it is GroupUser && it.username.contains(s.toString(), true))
                    ) {
                        filteredList.add(it)
                    }
                }
                removeSelected()

                //TODO sort
                notifyDataSetChanged()
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        return AssigneeViewHolder(
            RowAssigneeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: AssigneeViewHolder, position: Int) {
        val assignee = filteredList[position]
        holder.bind(assignee)
        holder.assigneeBinding.clAsigneeContainer.setOnClickListener {
            val assigneeName =
                if (assignee is GroupUser) assignee.username else (assignee as Group).name
            createChip(activity, assigneeName, chipGroup, this)
            //remove assignee from list
            filteredList.remove(assignee)
            //notify adapter
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addAssignee(assignee: String) {
        //if assignee contains search
        if (assignee.contains(search.text.toString(), true)) {
            //get the assignee from the list
            val assigneeToAdd =
                assigneeList.first { it is Group && it.name == assignee || it is GroupUser && it.username == assignee }
            //add assignee to filtered list
            filteredList.add(assigneeToAdd)
            //TODO sort
            notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeSelected() {
        chips.forEach { chip ->
            //if the filteredList users name or group name contains the chip text
            if (chip is Chip && filteredList.any { it is Group && it.name == chip.text || it is GroupUser && it.username == chip.text }) {
                //get the assignee from the list
                val assigneeToRemove =
                    filteredList.first { it is Group && it.name == chip.text || it is GroupUser && it.username == chip.text }
                //remove assignee from filtered list
                filteredList.remove(assigneeToRemove)
            }
        }
        Log.d("AssigneeAdapter", "removeSelected: $filteredList")
        notifyDataSetChanged()
    }
}