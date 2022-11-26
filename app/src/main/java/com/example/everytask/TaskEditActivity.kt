package com.example.everytask

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import com.example.everytask.databinding.ActivityTaskEditBinding
import com.example.everytask.models.Task
import com.example.everytask.models.response.Default
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable
import java.util.*

class TaskEditActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private lateinit var TOKEN: String

    private lateinit var task: Task

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

    private lateinit var editBinding: ActivityTaskEditBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editBinding = ActivityTaskEditBinding.inflate(layoutInflater)

        TOKEN = sharedPreferences.getString("TOKEN", null)!!

        setContentView(editBinding.root)

        val actionbar = supportActionBar!!
        actionbar.title = "Edit Task"
        actionbar.setDisplayHomeAsUpEnabled(true)

        pickDate()

        //set all the values
        task = getSerializable(this, "TASK",Task::class.java)
        editBinding.editTask.etTitle.setText(task.title)
        editBinding.editTask.etDescription.setText(task.description)
        editBinding.editTask.etDueDate.setText(task.due_time)
    }

    fun <T : Serializable?> getSerializable(activity: Activity, name: String, clazz: Class<T>): T
    {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            activity.intent.getSerializableExtra(name, clazz)!!
        else
            activity.intent.getSerializableExtra(name) as T
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
        editBinding.editTask.etDueDate.setOnClickListener {
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
        editBinding.editTask.etDueDate.setText("$savedDay/$savedMonth/$savedYear $savedHour:$savedMinute")
    }

    fun updateTask(view: View) {
        val title = editBinding.editTask.etTitle.text.toString()
        val description = editBinding.editTask.etDescription.text.toString()
        val dueDate = "$savedYear-$savedMonth-$savedDay $savedHour:$savedMinute:00"

        if (title.isEmpty()) {
            editBinding.editTask.etTitle.error = "Title cannot be empty"
            return
        }
        val call = retrofitBuilder.updateTask(TOKEN,
            task.id!!,Task(title, description, dueDate, task.id))
        Log.d("TAG", "editTask: ${Task(title, description, dueDate)}")
        call.enqueue(object : Callback<Default> {
            override fun onResponse(call: Call<Default>, response: Response<Default>) {
                if (response.isSuccessful) {
                    Log.d("TAG", "onResponse: ${response.body()}")
                    finish()
                } else {
                    Log.d("TAG", "onResponse: ${response.errorBody()}")
                    Toast.makeText(this@TaskEditActivity, "You are not the creator of this Task", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Default>, t: Throwable) {
                Toast.makeText(this@TaskEditActivity, "No connection to server", Toast.LENGTH_SHORT)
                    .show()
                Log.d("TAG", t.message.toString())
            }
        })
    }

    fun cancelTask(view: View) {
        finish()
    }
}