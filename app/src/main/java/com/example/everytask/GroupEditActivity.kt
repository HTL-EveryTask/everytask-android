package com.example.everytask

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.everytask.databinding.ActivityGroupAddBinding
import com.example.everytask.databinding.ActivityGroupEditBinding
import com.example.everytask.models.Task
import com.example.everytask.models.call.GroupInfo
import com.example.everytask.models.response.Default
import com.example.everytask.models.response.groups.Group
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class GroupEditActivity : AppCompatActivity() {


    private lateinit var TOKEN: String

    private lateinit var group: Group

    private lateinit var binding: ActivityGroupEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupEditBinding.inflate(layoutInflater)

        TOKEN = sharedPreferences.getString("TOKEN", null)!!

        setContentView(binding.root)

        val actionbar = supportActionBar!!
        actionbar.title = "Edit Group"
        actionbar.setDisplayHomeAsUpEnabled(true)


        group = getSerializable(this, "GROUP", Group::class.java)
        //set all the values
        binding.editGroup.etName.setText(group.name)
        binding.editGroup.etDescription.setText(group.description)
        val adapter = MemberAdapter(group.users, this)
        binding.rvMembers.adapter = adapter
        binding.etInviteLink.inputType = 0
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        //TODO implement custom animation here
        return true
    }

    fun updateGroup(view: View) {
        //TODO implement update group
    }

    fun showLeaveAlert(view: View) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Leave Group")
        builder.setMessage("Are you sure you want to leave this group?")
        builder.setPositiveButton("Confirm") { dialog, which ->
            leaveGroup()
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

    fun leaveGroup() {
        val call = retrofitBuilder.leaveGroup(TOKEN, group.id)
        call.enqueue(object : Callback<Default> {
            override fun onResponse(
                call: Call<com.example.everytask.models.response.Default>,
                response: Response<com.example.everytask.models.response.Default>
            ) {
                if (response.isSuccessful) {
                    finish()
                }
            }

            override fun onFailure(
                call: Call<com.example.everytask.models.response.Default>,
                t: Throwable
            ) {
                Toast.makeText(
                    this@GroupEditActivity,
                    "No connection to server",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun cancelGroup(view: View) {
        finish()
    }

    fun copyInviteLink(view: View) {
        val clipboard: ClipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Invite Link", binding.etInviteLink.text)
        clipboard.setPrimaryClip(clip)
    }

    fun generateInviteLink(view: View) {
        val call = retrofitBuilder.createInvite(TOKEN, group.id)
        call.enqueue(object : Callback<Default> {
            override fun onResponse(call: Call<Default>, response: Response<Default>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val inviteLink = body?.key
                    binding.etInviteLink.setText(inviteLink)
                } else {
                    Log.d("TAG", "onResponse: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<Default>, t: Throwable) {
                Log.d("TAG", "onFailure: ${t.message}")
            }
        })
    }

}