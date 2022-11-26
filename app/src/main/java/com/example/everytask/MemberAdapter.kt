package com.example.everytask

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.everytask.databinding.RowMembersBinding
import com.example.everytask.models.response.User

class MemberAdapter(val userList: List<User>, val groupEditActivity: GroupEditActivity): RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    class MemberViewHolder(
        val rowMembersBinding: RowMembersBinding,
        val groupEditActivity: GroupEditActivity,
    ): RecyclerView.ViewHolder(rowMembersBinding.root){
        fun bind(user: User){
            rowMembersBinding.tvUsername.text = user.username

            // TODO if user is admin, show admin icon
            // HOW DOES DIS WORK
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        return MemberViewHolder(
            RowMembersBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            groupEditActivity
        )
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val group = userList[position]
        holder.bind(group)
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}