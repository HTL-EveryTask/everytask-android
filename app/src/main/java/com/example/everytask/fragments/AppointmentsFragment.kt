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
import com.example.everytask.adapters.AppointmentAdapter
import com.example.everytask.databinding.FragmentAppointmentsBinding
import com.example.everytask.models.response.Default
import com.example.everytask.models.response.appointments.Appointment
import com.example.everytask.sharedPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AppointmentsFragment : Fragment() {

    private lateinit var TOKEN: String

    private var _binding: FragmentAppointmentsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppointmentsBinding.inflate(inflater, container, false)

        TOKEN = sharedPreferences.getString("TOKEN", null)!!

        Log.d("TAG", TOKEN)

        getAppointments()

        binding.swipeContainer.setOnRefreshListener {
            getAppointments()
            binding.swipeContainer.isRefreshing = false
        }

        binding.fabAddAppointment.setOnClickListener {
            val intent = Intent(requireContext(), AppointmentAddActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    fun getAppointments(appointmentsFragment: AppointmentsFragment = this) {
        val call = retrofitBuilder.getAppointments(TOKEN)
        call.enqueue(object : Callback<Default> {
            override fun onResponse(call: Call<Default>, response: Response<Default>) {
                if (response.isSuccessful) {
                    val default = response.body()
                    val appointments = default?.appointments
                    Log.d("TAG", "onResponse: ${response.body()}")
                    if (appointments != null) {
                        val adapter = AppointmentAdapter(appointments, appointmentsFragment)
                        try {
                            binding.rvAppointments.adapter = adapter
                        } catch (e: Exception) {
                            Log.d("TAG", e.toString())
                        }
                    } else {
                        val adapter = AppointmentAdapter(arrayListOf(), appointmentsFragment)
                        binding.rvAppointments.adapter = adapter
                    }

                }
            }

            override fun onFailure(call: Call<Default>, t: Throwable) {
                Toast.makeText(context, "No connection to server", Toast.LENGTH_SHORT).show()
                Log.d("TAG", "onFailure: ${t.message}")
            }
        })
    }

    private fun deleteAppointment(appointment: Appointment) {
        val call = retrofitBuilder.deleteAppointment(TOKEN, appointment.id!!)
        Log.d("TAG", "deleteAppointment: $TOKEN ${appointment.id}")
        call.enqueue(object : Callback<Default> {
            override fun onResponse(call: Call<Default>, response: Response<Default>) {
                if (response.isSuccessful) {
                    Log.d("TAG", "onResponse: ${response.body()}")
                    getAppointments()
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

    fun showDeleteAlert(appointment: Appointment) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete appointment")
        builder.setMessage("Are you sure you want to delete this appointment?")
        builder.setPositiveButton("Confirm") { dialog, which ->
            deleteAppointment(appointment)
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
        getAppointments()
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