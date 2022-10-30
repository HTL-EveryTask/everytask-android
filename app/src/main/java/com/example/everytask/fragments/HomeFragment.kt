package com.example.everytask.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.everytask.ApiInterface
import com.example.everytask.models.TestItem
import com.example.everytask.databinding.FragmentHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeFragment : Fragment() {

    var BASE_URL = "https://jsonplaceholder.typicode.com/"

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val sharedPref = activity?.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val token = sharedPref?.getString("TOKEN", null)

        Log.d("token", token.toString())

        binding.tvHome.text = token

        return binding.root
    }

    private fun getData() {
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(ApiInterface::class.java)

        val retrofitData = retrofitBuilder.getData()

        retrofitData.enqueue(object : Callback<List<TestItem>> {
            override fun onResponse(call: Call<List<TestItem>>, response: Response<List<TestItem>>) {
                val responseBody = response.body()!!

                val myStringBuilder = StringBuilder()
                for(myData in responseBody) {
                    myStringBuilder.append(myData.id)
                    myStringBuilder.append("\n")
                }
                binding.tvHome.text = myStringBuilder
            }
            override fun onFailure(call: Call<List<TestItem>>, t: Throwable) {
                Log.d("MainActivity", "onFailure: ${t.message}")
            }
        })
    }
}