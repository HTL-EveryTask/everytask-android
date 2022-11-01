package com.example.everytask.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.everytask.ApiInterface
import com.example.everytask.TaskAdapter
import com.example.everytask.databinding.FragmentHomeBinding
import com.example.everytask.models.Default
import com.example.everytask.models.Task
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeFragment : Fragment() {

    private lateinit var BASE_URL: String
    private lateinit var TOKEN: String

    private lateinit var retrofitBuilder: ApiInterface

    private lateinit var sharedPreferences: SharedPreferences
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        sharedPreferences =
            requireActivity().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)

        TOKEN = sharedPreferences.getString("TOKEN", null)!!
        BASE_URL = sharedPreferences.getString("BASE_URL", null).toString()

        Log.d("TAG", TOKEN)

        retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(ApiInterface::class.java)

        getTasks()

        //TODO add add task button
        //TODO edit task
        //TODO implement back gesture

        binding.swipeContainer.setOnRefreshListener {
            getTasks()
            binding.swipeContainer.isRefreshing = false
        }

        return binding.root
    }

    fun getTasks(homeFragment: HomeFragment = this) {
        val call = retrofitBuilder.getTasks(TOKEN)
        call.enqueue(object : Callback<Default> {
            override fun onResponse(call: Call<Default>, response: Response<Default>) {

                Log.d("TAG", "onResponse: ${response.body()}")

                if (response.isSuccessful) {
                    val default = response.body()
                    val tasks = default?.data

                    val taskAdapter = TaskAdapter(tasks!!, homeFragment)
                    binding.rvTasks.adapter = taskAdapter

                }
            }

            override fun onFailure(call: Call<Default>, t: Throwable) {
                Log.d("TAG", "onFailure: ${t.message}")
            }
        })
    }

    fun deleteTask(task: Task) {
        val call = retrofitBuilder.deleteTask(TOKEN, task.pk_task_id)
        call.enqueue(object : Callback<Default> {
            override fun onResponse(call: Call<Default>, response: Response<Default>) {

                Log.d("TAG", "onResponse: ${response.body()}")

                if (response.isSuccessful) {
                    getTasks()
                }
            }

            override fun onFailure(call: Call<Default>, t: Throwable) {
                Log.d("TAG", "onFailure: ${t.message}")
            }
        })
    }

    fun showDeleteAlert(task: Task){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Task")
        builder.setMessage("Are you sure you want to delete this task?")
        builder.setPositiveButton("Confirm"){dialog, which ->
            deleteTask(task)
        }
        builder.setNegativeButton("Cancel"){dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}