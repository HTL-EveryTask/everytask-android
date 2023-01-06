package com.example.everytask.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.Instrumentation
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.marginLeft
import com.example.everytask.LoginActivity
import com.example.everytask.databinding.FragmentSettingsBinding
import com.example.everytask.models.call.PasswordInfo
import com.example.everytask.retrofitBuilder
import com.example.everytask.sharedPreferences
import com.example.everytask.models.response.Default
import com.example.everytask.models.response.User
import com.example.everytask.validPassword
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Base64
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream

class SettingsFragment : Fragment() {

    private lateinit var TOKEN: String

    private lateinit var user: User

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var startForResult: ActivityResultLauncher<Intent>

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        TOKEN = sharedPreferences.getString("TOKEN", null)!!

        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val uri = result.data?.data
                try {
                    var bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                    //TODO: resize image if greater than 2MB
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    val byteArray = stream.toByteArray()
                    val encoded = Base64.encodeToString(byteArray, Base64.DEFAULT)
                    Log.d("TAG", "byteCount: ${bitmap.byteCount}")
                    Log.d("TAG", "encoded: $encoded")
                    binding.ivProfilePicture.setImageBitmap(bitmap)
                    changeProfilePicture(encoded)
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
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        getUser()

        binding.btnLogout.setOnClickListener {
            sharedPreferences.edit().remove("TOKEN").apply()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        binding.btnEditUsername.setOnClickListener() {
            editUsername()
        }

        binding.btnEditPassword.setOnClickListener() {
            changePassword()
        }

        binding.btnDeleteAccount.setOnClickListener() {
            deleteAccount()
        }

        binding.btnChangeProfilePicture.setOnClickListener() {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                selectImage()
            }
        }

        return binding.root
    }

    private fun changeProfilePicture(encoded: String) {
        //remove all spaces and new lines
        val encodedWithoutSpaces = encoded.replace("\\s".toRegex(), "")
        val retrofitData = retrofitBuilder.changeProfilePicture(TOKEN, mapOf("picture" to encodedWithoutSpaces))
        retrofitData.enqueue(object : Callback<Default> {
            override fun onResponse(call: Call<Default>, response: Response<Default>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Profile picture changed", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Default>, t: Throwable) {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startForResult.launch(Intent.createChooser(intent, "Select Image"))
    }

    private fun changePassword() {
        val currentPassword = binding.etCurrentPassword.text.toString()
        val newPassword = binding.etPassword.text.toString()
        val validPassword = validPassword(newPassword)
        val call = retrofitBuilder.changePassword(TOKEN, PasswordInfo(newPassword, currentPassword))

        if (validPassword == null) {
            binding.tilPasswordContainer.error = null
            call.enqueue(object : Callback<Default> {
                override fun onResponse(call: Call<Default>, response: Response<Default>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Password changed", Toast.LENGTH_SHORT)
                            .show()
                        response.body()?.token?.let {
                            sharedPreferences.edit().putString("TOKEN", it).apply()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Wrong Password", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<Default>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            binding.tilPasswordContainer.error = validPassword
        }
    }

    private fun deleteAccount() {
        val builder = AlertDialog.Builder(requireContext())

        val llPassword = LinearLayout(requireContext())
        val etPassword = EditText(requireContext())
        etPassword.inputType =
            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD or android.text.InputType.TYPE_CLASS_TEXT
        llPassword.addView(etPassword)

        //set margin params for edit text
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(50, 0, 50, 0)
        etPassword.layoutParams = params

        builder.setTitle("Delete account")
        builder.setMessage("Enter your password to delete your account:")
        builder.setView(llPassword)
        builder.setPositiveButton("Yes") { dialog, which ->
            val password = etPassword.text.toString()
            val call = retrofitBuilder.deleteAccount(TOKEN, mapOf("password" to password))
            call.enqueue(object : Callback<Default> {
                override fun onResponse(call: Call<Default>, response: Response<Default>) {
                    if (response.isSuccessful) {
                        sharedPreferences.edit().remove("TOKEN").apply()
                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    } else {
                        Toast.makeText(requireContext(), "Wrong password", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<Default>, t: Throwable) {
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT)
                        .show()
                }
            })

        }
        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun getUser() {
        val call = retrofitBuilder.getUserData(TOKEN)
        call.enqueue(object : Callback<Default> {
            override fun onResponse(call: Call<Default>, response: Response<Default>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.user != null) {
                        user = body.user

                        binding.etUsername.setText(user.username)

                        //set profile picture from user as base64
                        val imageBytes = Base64.decode(user.profile_picture, Base64.DEFAULT)
                        val decodedImage =
                            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        binding.ivProfilePicture.setImageBitmap(decodedImage)
                    }
                }
            }

            override fun onFailure(call: Call<Default>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun editUsername() {
        if (binding.etUsername.text!!.isEmpty()) {
            binding.etUsername.error = "Username cannot be empty"
            return
        }
        if (binding.etUsername.text!!.length > 32) {
            binding.etUsername.error = "Username is too long"
            return
        }
        var call = retrofitBuilder.changeUsername(
            TOKEN,
            mapOf("username" to binding.etUsername.text.toString())
        )
        call.enqueue(object : Callback<Default> {
            override fun onResponse(call: Call<Default>, response: Response<Default>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body!!.message != null) {
                        Toast.makeText(requireContext(), body.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<Default>, t: Throwable) {
                Toast.makeText(requireContext(), "No connection to server", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
}