package com.example.everytask.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.everytask.*
import com.example.everytask.adapters.GroupAdapter
import com.example.everytask.databinding.FragmentGroupsBinding
import com.example.everytask.models.response.Default
import com.example.everytask.retrofitBuilder
import com.example.everytask.sharedPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GroupsFragment : Fragment() {

    private lateinit var TOKEN: String

    private var _binding: FragmentGroupsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupsBinding.inflate(inflater, container, false)

        TOKEN = sharedPreferences.getString("TOKEN", null)!!

        Log.d("TAG", TOKEN)

        getGroups()

        binding.swipeContainer.setOnRefreshListener {
            getGroups()
            binding.swipeContainer.isRefreshing = false
        }

        binding.fabAddGroup.setOnClickListener {
            val intent = Intent(requireContext(), GroupAddActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    fun getGroups(groupsFragment: GroupsFragment = this) {
        val call = retrofitBuilder.getGroups(TOKEN)
        call.enqueue(object : Callback<Default> {
            override fun onResponse(call: Call<Default>, response: Response<Default>) {
                if (response.isSuccessful) {
                    val default = response.body()
                    val groups = default?.groups
                    Log.d("TAG", "onResponse: ${response.body()}")
                    if (groups != null) {
                        val adapter = GroupAdapter(groups, groupsFragment)
                        try {
                            binding.rvGroups.adapter = adapter
                        } catch (e: Exception) {
                            Log.d("TAG", e.toString())
                        }
                    } else {
                        val adapter = GroupAdapter(arrayListOf(), groupsFragment)
                        binding.rvGroups.adapter = adapter
                    }

                }
            }

            override fun onFailure(call: Call<Default>, t: Throwable) {
                Toast.makeText(context, "No connection to server", Toast.LENGTH_SHORT).show()
                Log.d("TAG", "onFailure: ${t.message}")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        getGroups()
    }
}