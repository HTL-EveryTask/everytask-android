package com.example.everytask.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.everytask.*
import com.example.everytask.databinding.FragmentHomeBinding
import com.example.everytask.models.Default
import com.example.everytask.models.Task
import com.example.everytask.sharedPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private lateinit var TOKEN: String

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        TOKEN = sharedPreferences.getString("TOKEN", null)!!

        Log.d("TAG", TOKEN)

        getTasks()

        binding.swipeContainer.setOnRefreshListener {
            getTasks()
            binding.swipeContainer.isRefreshing = false
        }

        binding.fabAddTask.setOnClickListener {
            val intent = Intent(requireContext(), AddActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    fun getTasks(homeFragment: HomeFragment = this) {
        val call = retrofitBuilder.getTasks(TOKEN)
        call.enqueue(object : Callback<Default> {
            override fun onResponse(call: Call<Default>, response: Response<Default>) {
                if (response.isSuccessful) {
                    val default = response.body()
                    val tasks = default?.data

                    if (tasks != null) {
                        val adapter = TaskAdapter(tasks, homeFragment)
                        binding.rvTasks.adapter = adapter
                    } else {
                        val adapter = TaskAdapter(arrayListOf(), homeFragment)
                        binding.rvTasks.adapter = adapter
                    }

                }
            }

            override fun onFailure(call: Call<Default>, t: Throwable) {
                Log.d("TAG", "onFailure: ${t.message}")
            }
        })
    }

    fun deleteTask(task: Task) {
        val call = retrofitBuilder.deleteTask(task.id, TOKEN)
        call.enqueue(object : Callback<Default> {
            override fun onResponse(call: Call<Default>, response: Response<Default>) {
                if (response.isSuccessful) {
                    Log.d("TAG", "onResponse: ${response.body()}")
                    getTasks()
                } else {
                    val errorResponse: Default? =
                        gson.fromJson(response.errorBody()!!.charStream(), type)
                    Log.d("TAG", "onResponse: $errorResponse")
                }
            }

            override fun onFailure(call: Call<Default>, t: Throwable) {
                Log.d("TAG", "onFailure: ${t.message}")
            }
        })
    }

    fun showDeleteAlert(task: Task) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Task")
        builder.setMessage("Are you sure you want to delete this task?")
        builder.setPositiveButton("Confirm") { dialog, which ->
            deleteTask(task)
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        getTasks()
    }
}