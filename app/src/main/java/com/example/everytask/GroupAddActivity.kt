package com.example.everytask

import android.R.attr.label
import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.everytask.adapters.AssigneeAdapter
import com.example.everytask.databinding.ActivityGroupAddBinding
import com.example.everytask.databinding.DialogSearchableSpinnerBinding
import com.example.everytask.models.call.GroupInfo
import com.example.everytask.models.response.Default
import com.example.everytask.models.response.groups.Group
import com.example.everytask.models.response.groups.GroupUser
import com.example.everytask.models.response.tasks.AssignedGroup
import com.example.everytask.models.response.tasks.AssignedUser
import com.google.android.material.chip.Chip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.Serializable
import java.util.*


class GroupAddActivity : AppCompatActivity() {

    private lateinit var TOKEN: String

    private lateinit var binding: ActivityGroupAddBinding
    private lateinit var dialogBinding: DialogSearchableSpinnerBinding

    private var encodedPicture: String? = null
    private lateinit var userId: String
    private lateinit var groups: List<Group>
    private lateinit var members: List<java.io.Serializable>
    private lateinit var group: Group

    private lateinit var startForResult: ActivityResultLauncher<Intent>

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupAddBinding.inflate(layoutInflater)
        dialogBinding = DialogSearchableSpinnerBinding.inflate(layoutInflater)

        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val uri = result.data?.data
                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                        //TODO: resize image if greater than 2MB
                        val stream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        val byteArray = stream.toByteArray()
                        val encoded = Base64.encodeToString(byteArray, Base64.DEFAULT)
                        encodedPicture = encoded.replace("\\s".toRegex(), "")
                        Log.d("TAG", "byteCount: ${bitmap.byteCount}")
                        Log.d("TAG", "encoded: $encoded")
                        binding.editGroup.ivGroupIcon.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        Log.e("SettingsFragment", e.toString())
                    }
                }
            }

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                selectImage()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        TOKEN = sharedPreferences.getString("TOKEN", null)!!
        userId = sharedPreferences.getString("USER_ID", null)!!

        setContentView(binding.root)

        binding.etMembers.inputType = 0
        binding.editGroup.ivGroupIcon.setOnClickListener() {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                selectImage()
            }
        }
        getGroups()

        val actionbar = supportActionBar!!
        actionbar.title = "New Group"
        actionbar.setDisplayHomeAsUpEnabled(true)
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startForResult.launch(Intent.createChooser(intent, "Select Image"))
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        //TODO implement custom animation here
        return true
    }

    fun getGroups() {
        val call = retrofitBuilder.getGroups(TOKEN)
        call.enqueue(object : Callback<Default> {
            override fun onResponse(call: Call<Default>, response: Response<Default>) {
                if (response.isSuccessful) {
                    val default = response.body()
                    Log.d("TAG", "affiliated Groups: ${response.body()}")
                    if (default != null) {
                        groups = default.groups!!
                        initializeAssignables()
                    }
                }
            }

            override fun onFailure(call: Call<Default>, t: Throwable) {
                Toast.makeText(this@GroupAddActivity, "No connection to server", Toast.LENGTH_SHORT)
                    .show()
                Log.d("TAG", "onFailure: ${t.message}")
            }
        })
    }

    fun getMembers(groups: List<Group>) {
        //create a list of all users in the groups
        val memberList = mutableListOf<java.io.Serializable>()
        for (group in groups) {
            for (user in group.users) {
                //check if user is already in the list
                if (memberList.find { it is GroupUser && it.id == user.id } == null) {
                    if (user.id.toString() != userId) {
                        memberList.add(user)
                    } else {
                        if (binding.flMemberContainer.childCount == 1) {
                            createChip(
                                this,
                                user,
                                binding.flMemberContainer,
                                null
                            )
                            createChip(
                                this,
                                user,
                                dialogBinding.flAssigneeContainer,
                                null
                            )
                        }
                    }
                }
            }
        }
        members = memberList
    }

    fun initializeAssignables() {
        getMembers(groups)

        val filteredList = members.toMutableList()

        Log.d("TAG", filteredList.toString())

        binding.etMembers.setOnClickListener(View.OnClickListener {
            val dialog = Dialog(this)
            //call removeView to remove the view from the parent
            (dialogBinding.root.parent as ViewGroup?)?.removeView(dialogBinding.root)

            dialog.setContentView(dialogBinding.root)
            dialog.getWindow()?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()

            filteredList.sortWith(compareBy { (it as GroupUser).username })

            val adapter = AssigneeAdapter(
                this,
                members,
                filteredList,
                dialogBinding.etAssigneeSearch,
                dialogBinding.flAssigneeContainer
            )
            dialogBinding.rvAssignees.adapter = adapter

            //TODO in EDIT, create chips for all members of the group

            //when the dialog is dismissed, update the member chips
            dialog.setOnDismissListener {
                binding.flMemberContainer.removeViews(
                    0,
                    binding.flMemberContainer.childCount - 1
                )
                //add the chips to the member container
                for (i in 0 until dialogBinding.flAssigneeContainer.childCount - 1) {
                    val member = dialogBinding.flAssigneeContainer.getChildAt(i) as Chip
                    createChip(
                        this,
                        member.tag as Serializable,
                        binding.flMemberContainer,
                        null
                    )
                }
            }
        })
    }

    fun addGroup(view: View) {
        val name = binding.editGroup.etName.text.toString()
        val description = binding.editGroup.etDescription.text.toString()

        if (name.isEmpty()) {
            binding.editGroup.etName.error = "Name cannot be empty"
            return
        }

        val call = retrofitBuilder.addGroup(TOKEN, GroupInfo(name, description, encodedPicture))
        call.enqueue(object : Callback<Default> {
            override fun onResponse(call: Call<Default>, response: Response<Default>) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        group = response.body()!!.group!!
                        setMembers()
                    }
                    finish()
                } else {
                    Toast.makeText(this@GroupAddActivity, "Error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Default>, t: Throwable) {
                Toast.makeText(this@GroupAddActivity, "Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setMembers() {
        //make a list of all the members ids
        val memberIds = mutableListOf<Int>()
        memberIds.add(userId.toInt())
        for (i in 1 until binding.flMemberContainer.childCount) {
            val member = binding.flMemberContainer.getChildAt(i)
            if (member is Chip) {
                if (member.tag is GroupUser) {
                    memberIds.add((member.tag as GroupUser).id)
                }
            }
        }

        val picture = if (encodedPicture == "") null else encodedPicture

        val call = retrofitBuilder.updateGroup(
            TOKEN,
            group.id,
            GroupInfo(group.name, group.description, picture, memberIds)
        )
        call.enqueue(object : Callback<Default> {
            override fun onResponse(call: Call<Default>, response: Response<Default>) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        Log.d("TAG", "onResponse: ${response.body()}")
                    }
                } else {
                    Toast.makeText(this@GroupAddActivity, "Error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Default>, t: Throwable) {
                Toast.makeText(this@GroupAddActivity, "Error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}