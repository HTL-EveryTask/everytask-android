package com.example.everytask

import android.R.attr.label
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.everytask.databinding.ActivityGroupAddBinding
import com.example.everytask.models.call.GroupInfo
import com.example.everytask.models.response.Default
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class GroupAddActivity : AppCompatActivity() {

    private lateinit var TOKEN: String

    private lateinit var binding: ActivityGroupAddBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupAddBinding.inflate(layoutInflater)

        TOKEN = sharedPreferences.getString("TOKEN", null)!!

        binding.etInviteLink.inputType = 0

        setContentView(binding.root)

        val actionbar = supportActionBar!!
        actionbar.title = "New Group"
        actionbar.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        //TODO implement custom animation here
        return true
    }

    fun copyInviteLink(view: View) {
        val clipboard: ClipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Invite Link", binding.etInviteLink.text)
        clipboard.setPrimaryClip(clip)
    }

    fun addGroup(view: View) {
        finish()
    }

    fun generateInviteLink(view: View) {
        val name = binding.editGroup.etName.text.toString()
        val description = binding.editGroup.etDescription.text.toString()

        if (name.isEmpty()) {
            binding.editGroup.etName.error = "Name cannot be empty"
            return
        }

        val call = retrofitBuilder.addGroup(TOKEN, GroupInfo(name, description))
        call.enqueue(object : Callback<Default> {
            override fun onResponse(call: Call<Default>, response: Response<Default>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val groupId = body?.group?.id

                    val call = retrofitBuilder.createInvite(TOKEN,groupId!!)
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

            override fun onFailure(call: Call<Default>, t: Throwable) {
                Toast.makeText(this@GroupAddActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}