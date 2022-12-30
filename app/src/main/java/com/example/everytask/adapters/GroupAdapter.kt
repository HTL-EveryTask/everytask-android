package com.example.everytask.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.everytask.GroupEditActivity
import com.example.everytask.databinding.RowGroupsBinding
import com.example.everytask.fragments.GroupsFragment
import com.example.everytask.models.response.groups.Group

class GroupAdapter(val groupList: List<Group>, val groupsFragment: GroupsFragment): RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    class GroupViewHolder(
        val groupsBinding: RowGroupsBinding,
        val groupsFragment: GroupsFragment,
    ): RecyclerView.ViewHolder(groupsBinding.root){
        fun bind(group: Group){
            groupsBinding.tvGroupName.text = group.name
            groupsBinding.tvGroupDescription.text = group.description
            groupsBinding.tvMemberCount.text = group.stats.total_users.toString()
            groupsBinding.llGroupTextContainer.setOnClickListener {
                val intent = Intent(groupsFragment.requireContext(), GroupEditActivity::class.java)
                intent.putExtra("GROUP", group)
                startActivity(groupsFragment.requireContext(), intent, null)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        return GroupViewHolder(
            RowGroupsBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            groupsFragment
        )
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groupList[position]
        holder.bind(group)
    }

    override fun getItemCount(): Int {
        return groupList.size
    }
}