package com.example.everytask.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
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
                assigneeBinding.ivGroupPicture.visibility = View.GONE
                assigneeBinding.ivUserPicture.visibility = View.VISIBLE
            } else if (assignee is Group) {
                assigneeBinding.tvAssignee.text = assignee.name
                assigneeBinding.ivUserPicture.visibility = View.GONE
                assigneeBinding.ivGroupPicture.visibility = View.VISIBLE
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

                sort()
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
        holder.assigneeBinding.clAssigneeContainer.setOnClickListener {

            //if assignee is group remove all chips of the users in the group
            if (assignee is Group) {
                assignee.users.forEach { user ->
                    chips.forEach { chip ->
                        if (chip is Chip && chip.text == user.username) {
                            chipGroup.removeView(chip)
                        }
                    }
                }
            }
            Log.d("TAG", "Class: ${assignee.javaClass.name}")
            createChip(activity, assignee, chipGroup, this)
            //remove assignee from list
            removeSelected()
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addAssignee(assignee: Serializable) {
        //get name
        val assigneeName =
            if (assignee is Group) assignee.name else (assignee as GroupUser).username
        //if assignee contains search
        if (assigneeName.contains(search.text.toString(), true)) {
            if (assignee is GroupUser) {
                if (filteredList.find { i -> i is GroupUser && i.id == assignee.id } == null) {
                    filteredList.add(assignee)
                }
            } else if (assignee is Group) {
                if (!filteredList.contains(assignee)) {
                    filteredList.add(assignee)
                    assignee.users.forEach { user ->
                        if (filteredList.find { i -> i is GroupUser && i.id == user.id } == null) {
                            filteredList.add(user)
                        }
                    }
                }
            }
            removeSelected()
            sort()
            notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeSelected() {
        chips.forEach { chip ->
            //if the filteredList users name or group name contains the chip text
            if (chip is Chip) {
                if (chip.tag is GroupUser) {
                    filteredList.removeAll(
                        filteredList.filter {
                            it is GroupUser && it.id == (chip.tag as GroupUser).id
                        }
                    )
                } else if (chip.tag is Group) {
                    filteredList.remove(chip.tag)
                    (chip.tag as Group).users.forEach { user ->
                        filteredList.removeAll(
                            filteredList.filter {
                                it is GroupUser && it.id == user.id
                            }
                        )
                    }
                }
            }
        }
        Log.d("AssigneeAdapter", "removeSelected: $filteredList")
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun sort() {
        //sort the filtered list first by type (group or user) and then alphabetically
        filteredList.sortWith(
            compareBy(
                { it !is Group },
                { if (it is Group) it.name else (it as GroupUser).username })
        )
        notifyDataSetChanged()
    }
}