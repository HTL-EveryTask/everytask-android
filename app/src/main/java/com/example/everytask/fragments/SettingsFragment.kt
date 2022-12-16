package com.example.everytask.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
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
import com.example.everytask.validPassword
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsFragment : Fragment() {

    private lateinit var TOKEN: String

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        TOKEN = sharedPreferences.getString("TOKEN", null)!!

        getUsername()

        binding.btnLogout.setOnClickListener {
            sharedPreferences.edit().remove("TOKEN").apply()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        binding.btnEditUsername.setOnClickListener() {
            editUsername()
        }

        binding.btnEditPassword?.setOnClickListener() {
            changePassword()
        }

        binding.btnDeleteAccount?.setOnClickListener() {
            deleteAccount()
        }

        return binding.root
    }

    private fun changePassword() {
        val currentPassword = binding.etCurrentPassword.text.toString()
        val newPassword = binding.etPassword.text.toString()
        val validPassword = validPassword(newPassword)
        val call = retrofitBuilder.changePassword(TOKEN, PasswordInfo(newPassword,currentPassword))

        if(validPassword == null) {
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
        }else{
            binding.tilPasswordContainer.error = validPassword
        }
    }

    private fun deleteAccount() {
        val builder = AlertDialog.Builder(requireContext())

        val llPassword = LinearLayout(requireContext())
        val etPassword = EditText(requireContext())
        etPassword.inputType = android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD or android.text.InputType.TYPE_CLASS_TEXT
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

    private fun getUsername() {
        var call = retrofitBuilder.getUserData(TOKEN)
        call.enqueue(object : Callback<Default> {
            override fun onResponse(call: Call<Default>, response: Response<Default>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val user = body?.user
                    if (user != null) {
                        binding.etUsername.setText(user.username)
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
        var call = retrofitBuilder.changeUsername(TOKEN, mapOf("username" to binding.etUsername.text.toString()))
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
                Toast.makeText(requireContext(), "No connection to server", Toast.LENGTH_SHORT).show()
            }
        })
    }
}