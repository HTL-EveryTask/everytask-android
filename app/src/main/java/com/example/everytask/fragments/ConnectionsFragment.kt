package com.example.everytask.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.everytask.LoginActivity
import com.example.everytask.R
import com.example.everytask.databinding.FragmentConnectionsBinding
import com.example.everytask.databinding.FragmentSettingsBinding
import com.example.everytask.models.call.UntisInfo
import com.example.everytask.retrofitBuilder
import com.example.everytask.sharedPreferences

class ConnectionsFragment : Fragment() {
    private lateinit var TOKEN: String

    private var _binding: FragmentConnectionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConnectionsBinding.inflate(inflater, container, false)

        TOKEN = sharedPreferences.getString("TOKEN", null)!!

        binding.btnSaveUntis.setOnClickListener {
            saveUntisData()
        }

        binding.btnRemoveUntis.setOnClickListener {
            removeUntisData()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun saveUntisData() {
        if (binding.etUntisUsername.text.toString()
                .isNotEmpty() && binding.etUntisPassword.text.toString()
                .isNotEmpty() && binding.etUntisSchool.text.toString()
                .isNotEmpty() && binding.etUntisServer.text.toString().isNotEmpty()
        ) {
            val call = retrofitBuilder.loginUntis(TOKEN, UntisInfo(
                binding.etUntisUsername.text.toString(),
                binding.etUntisPassword.text.toString(),
                binding.etUntisSchool.text.toString(),
                binding.etUntisServer.text.toString()
            ))
            call.enqueue(object : retrofit2.Callback<com.example.everytask.models.response.Default> {
                override fun onResponse(
                    call: retrofit2.Call<com.example.everytask.models.response.Default>,
                    response: retrofit2.Response<com.example.everytask.models.response.Default>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            Toast.makeText(requireContext(), body.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(
                    call: retrofit2.Call<com.example.everytask.models.response.Default>,
                    t: Throwable
                ) {
                    Toast.makeText(requireContext(), t.message, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun removeUntisData() {
        TODO("Not yet implemented")
    }
}
