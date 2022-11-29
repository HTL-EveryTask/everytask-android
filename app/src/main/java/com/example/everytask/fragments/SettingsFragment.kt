package com.example.everytask.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.everytask.LoginActivity
import com.example.everytask.databinding.FragmentSettingsBinding
import com.example.everytask.retrofitBuilder
import com.example.everytask.sharedPreferences
import com.example.everytask.models.response.Default
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable
import java.util.*

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

        return binding.root
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