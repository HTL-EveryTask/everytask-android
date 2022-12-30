package com.example.everytask.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.example.everytask.createChip
import com.example.everytask.databinding.RowAssigneeBinding
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip
import retrofit2.http.Query

class AssigneeAdapter(
    val activity: Activity,
    val assigneeList: List<String>,
    val filteredList: MutableList<String>,
    val search: EditText,
    val chipGroup: FlexboxLayout,
) : RecyclerView.Adapter<AssigneeAdapter.AssigneeViewHolder>() {

    class AssigneeViewHolder(val assigneeBinding: RowAssigneeBinding) :
        RecyclerView.ViewHolder(assigneeBinding.root) {
        fun bind(assignee: String) {
            assigneeBinding.tvUsername.text = assignee
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssigneeViewHolder {
        filteredList.sort()
        search.addTextChangedListener(object : android.text.TextWatcher {
            @SuppressLint("NotifyDataSetChanged")
            override fun afterTextChanged(s: android.text.Editable?) {
                filteredList.clear()
                assigneeList.forEach {
                    if (it.contains(s.toString(), true)) {
                        filteredList.add(it)
                    }
                }
                val chips = chipGroup.children
                chips.forEach { chip ->
                    if (chip is Chip && filteredList.contains(chip.text)) {
                        filteredList.remove(chip.text)
                    }
                }

                filteredList.sort()
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
            createChip(activity, assignee, chipGroup, this)
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
            filteredList.add(assignee)
            filteredList.sort()
            notifyDataSetChanged()
        }
    }
}