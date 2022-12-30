package com.example.everytask.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.everytask.*
import com.example.everytask.adapters.TaskAdapter
import com.example.everytask.databinding.FragmentHomeBinding
import com.example.everytask.models.response.Default
import com.example.everytask.models.response.tasks.Task
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
            val intent = Intent(requireContext(), TaskAddActivity::class.java)
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
                    val tasks = default?.tasks
                    Log.d("TAG", "onResponse: ${response.body()}")
                    if (tasks != null) {
                        val adapter = TaskAdapter(tasks, homeFragment)
                        try {
                            binding.rvTasks.adapter = adapter
                        } catch (e: Exception) {
                            Log.d("TAG", e.toString())
                        }
                    } else {
                        val adapter = TaskAdapter(arrayListOf(), homeFragment)
                        binding.rvTasks.adapter = adapter
                    }

                }
            }

            override fun onFailure(call: Call<Default>, t: Throwable) {
                Toast.makeText(context, "No connection to server", Toast.LENGTH_SHORT).show()
                Log.d("TAG", "onFailure: ${t.message}")
            }
        })
    }

    private fun deleteTask(task: Task) {
        val call = retrofitBuilder.deleteTask(TOKEN, task.id!!)
        Log.d("TAG", "deleteTask: $TOKEN ${task.id}")
        call.enqueue(object : Callback<Default> {
            override fun onResponse(call: Call<Default>, response: Response<Default>) {
                if (response.isSuccessful) {
                    Log.d("TAG", "onResponse: ${response.body()}")
                    getTasks()
                } else {
                    Log.d("TAG", "onResponse: ${response.body()}")
                }
            }

            override fun onFailure(call: Call<Default>, t: Throwable) {
                Toast.makeText(context, "No connection to server", Toast.LENGTH_SHORT).show()
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

    fun toggleDone(id: Int, checked: Boolean) {
        val call = retrofitBuilder.toggleDone(TOKEN, id, mapOf("is_done" to checked))
        call.enqueue(object : Callback<Default> {
            override fun onResponse(call: Call<Default>, response: Response<Default>) {
                if (response.isSuccessful) {
                    Log.d("TAG", "onResponse: ${response.body()}")
                } else {
                    Log.d("TAG", "onResponse: ${response.body()}")
                }
            }

            override fun onFailure(call: Call<Default>, t: Throwable) {
                Toast.makeText(context, "No connection to server", Toast.LENGTH_SHORT).show()
                Log.d("TAG", "onFailure: ${t.message}")
            }
        })
    }
}