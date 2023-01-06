package com.example.everytask

import android.R.attr.label
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.everytask.databinding.ActivityGroupAddBinding
import com.example.everytask.models.call.GroupInfo
import com.example.everytask.models.response.Default
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.*


class GroupAddActivity : AppCompatActivity() {

    private lateinit var TOKEN: String

    private lateinit var binding: ActivityGroupAddBinding
    private var encodedPicture: String? = null

    private lateinit var startForResult: ActivityResultLauncher<Intent>

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupAddBinding.inflate(layoutInflater)

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

        setContentView(binding.root)

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
}