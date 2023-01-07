package com.example.everytask.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.everytask.GroupEditActivity
import com.example.everytask.databinding.RowMembersBinding
import com.example.everytask.models.response.groups.GroupUser

class MemberAdapter(val userList: List<GroupUser>, val currentUser: GroupUser,val groupEditActivity: GroupEditActivity): RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    class MemberViewHolder(
        val rowMembersBinding: RowMembersBinding,
        val groupEditActivity: GroupEditActivity,
        val currentUser: GroupUser
    ): RecyclerView.ViewHolder(rowMembersBinding.root){
        fun bind(user: GroupUser){
            rowMembersBinding.tvUsername.text = user.username
            if(user.is_admin){
                rowMembersBinding.ivAdmin.visibility = View.VISIBLE
            }
            else{
                rowMembersBinding.ivAdmin.visibility = View.GONE
            }
            if(currentUser.is_admin && user.id != currentUser.id){
                //on long press, show the option to remove the user
                rowMembersBinding.root.setOnLongClickListener{
                    groupEditActivity.showEditUserDialog(user)
                    true
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        return MemberViewHolder(
            RowMembersBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            groupEditActivity,
            currentUser
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