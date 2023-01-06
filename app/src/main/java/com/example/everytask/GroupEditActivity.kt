package com.example.everytask

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.everytask.adapters.MemberAdapter
import com.example.everytask.databinding.ActivityGroupEditBinding
import com.example.everytask.databinding.DialogEditUserBinding
import com.example.everytask.models.call.GroupInfo
import com.example.everytask.models.response.Default
import com.example.everytask.models.response.groups.Group
import com.example.everytask.models.response.groups.GroupUser
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class GroupEditActivity : AppCompatActivity() {


    private lateinit var TOKEN: String

    private lateinit var group: Group
    private lateinit var userId: String
    private var encodedPicture: String? = null

    private lateinit var binding: ActivityGroupEditBinding

    private lateinit var startForResult: ActivityResultLauncher<Intent>

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupEditBinding.inflate(layoutInflater)

        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
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

        val actionbar = supportActionBar!!
        actionbar.title = "Edit Group"
        actionbar.setDisplayHomeAsUpEnabled(true)

        group = getSerializable(this, "GROUP", Group::class.java)

        //if the user is not an admin, disable all the fields
        val user = group.users.find { it.id == userId.toInt() }
        Log.d("TAG", "user: $user")
        if (!user!!.is_admin) {
            binding.editGroup.etName.isEnabled = false
            binding.editGroup.etDescription.isEnabled = false
            binding.btnGenerate.isEnabled = false
            binding.btnLock.visibility = View.GONE
            binding.btnSave.visibility = View.GONE
        }else{
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
        }

        //set all the values
        binding.editGroup.etName.setText(group.name)
        binding.editGroup.etDescription.setText(group.description)
        val sorted = group.users.sortedWith(compareBy({ it.id != userId.toInt() }, { !it.is_admin }, { it.username }))
        val adapter = MemberAdapter(sorted, user.is_admin, this)
        binding.rvMembers.adapter = adapter
        binding.etInviteLink.inputType = 0

        if(group.picture != "") {
            val decodedString: ByteArray = Base64.decode(group.picture, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            binding.editGroup.ivGroupIcon.setImageBitmap(decodedByte)
        }
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

    fun updateGroup(view: View) {
        val name = binding.editGroup.etName.text.toString()
        val description = binding.editGroup.etDescription.text.toString()

        if (name.isEmpty()) {
            binding.editGroup.etName.error = "Name is required"
            return
        }
        if (name.length > 32) {
            binding.editGroup.etName.error = "Name is too long"
            return
        }
        if (description.length > 300) {
            binding.editGroup.etDescription.error = "Description is too long"
            return
        }

        val groupInfo = GroupInfo(name, description, encodedPicture)
        val call = retrofitBuilder.updateGroup(TOKEN, group.id, groupInfo)
        call.enqueue(object : Callback<Default> {
            override fun onResponse(call: Call<Default>, response: Response<Default>) {
                if (response.isSuccessful) {
                    Log.d("TAG", "onResponse: ${response.body()}")
                    finish()
                } else {
                    Log.d("TAG", "onResponse: ${response.errorBody()}")
                    Toast.makeText(this@GroupEditActivity, "You are not Admin of this Group", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Default>, t: Throwable) {
                Toast.makeText(this@GroupEditActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        })
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

    fun lockGroup(view: View){
        val call = retrofitBuilder.lockGroup(TOKEN, group.id)
        call.enqueue(object : Callback<Default> {
            override fun onResponse(
                call: Call<Default>,
                response: Response<Default>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(this@GroupEditActivity, "Group Locked", Toast.LENGTH_SHORT).show() }
            }

            override fun onFailure(
                call: Call<Default>,
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

    fun showEditUserDialog(user: GroupUser) {
        val dialog = BottomSheetDialog(this)
        val binding = DialogEditUserBinding.inflate(layoutInflater)
        binding.tvKickMember.setOnClickListener {
            //TODO kick member
            dialog.dismiss()
        }
        binding.tvMakeAdmin.setOnClickListener {
            //TODO make admin
            dialog.dismiss()
        }
        dialog.setContentView(binding.root)
        dialog.show()
    }

}