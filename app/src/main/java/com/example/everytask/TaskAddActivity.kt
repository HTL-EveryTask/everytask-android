package com.example.everytask

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import com.example.everytask.databinding.ActivityTaskAddBinding
import com.example.everytask.models.Task
import com.example.everytask.models.response.Default
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class TaskAddActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private lateinit var TOKEN: String

    var day = 0
    var month = 0
    var year = 0
    var hour = 0
    var minute = 0

    var savedDay = 0
    var savedMonth = 0
    var savedYear = 0
    var savedHour = 0
    var savedMinute = 0

    private lateinit var binding: ActivityTaskAddBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskAddBinding.inflate(layoutInflater)

        TOKEN = sharedPreferences.getString("TOKEN", null)!!

        setContentView(binding.root)

        val actionbar = supportActionBar!!
        actionbar.title = "Add Task"
        actionbar.setDisplayHomeAsUpEnabled(true)

        pickDate()

        getDateTimeCalendar()
        savedDay = day
        savedMonth = month
        savedYear = year
        savedHour = hour
        savedMinute = minute
        binding.editTask.etDueDate.setText("$savedYear-$savedMonth-$savedDay $savedHour:$savedMinute:00")
    }

    private fun getDateTimeCalendar() {
        val cal = Calendar.getInstance()
        year = cal.get(Calendar.YEAR)
        month = cal.get(Calendar.MONTH)
        day = cal.get(Calendar.DAY_OF_MONTH)
        hour = cal.get(Calendar.HOUR_OF_DAY)
        minute = cal.get(Calendar.MINUTE)
    }

    private fun pickDate() {
        binding.editTask.etDueDate.setOnClickListener {
            getDateTimeCalendar()
            DatePickerDialog(this, this, year, month, day).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        //TODO implement custom animation here
        return true
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = dayOfMonth
        savedMonth = month
        savedYear = year

        getDateTimeCalendar()

        TimePickerDialog(this, this, hour, minute, true).show()
    }

    @SuppressLint("SetTextI18n")
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        savedHour = hourOfDay
        savedMinute = minute
        binding.editTask.etDueDate.setText("$savedDay/$savedMonth/$savedYear $savedHour:$savedMinute")
    }

    fun addTask(view: View) {
        val title = binding.editTask.etTitle.text.toString()
        val description = binding.editTask.etDescription.text.toString()
        val dueDate = "$savedYear-$savedMonth-$savedDay $savedHour:$savedMinute:00"

        if (title.isEmpty()) {
            binding.editTask.etTitle.error = "Title cannot be empty"
            return
        }
        val call = retrofitBuilder.addTask(TOKEN, Task(title, description, dueDate))
        Log.d("TAG", "addTask: ${Task(title, description, dueDate)}")
        call.enqueue(object : Callback<Default> {
            override fun onResponse(call: Call<Default>, response: Response<Default>) {
                if (response.isSuccessful) {
                    Log.d("TAG", "onResponse: ${response.body()}")
                    finish()
                }
            }

            override fun onFailure(call: Call<Default>, t: Throwable) {
                Toast.makeText(this@TaskAddActivity, "No connection to server", Toast.LENGTH_SHORT)
                    .show()
                Log.d("TAG", t.message.toString())
            }
        })
    }
}