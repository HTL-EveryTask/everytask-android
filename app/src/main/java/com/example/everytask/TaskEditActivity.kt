package com.example.everytask

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.example.everytask.adapters.AssigneeAdapter
import com.example.everytask.databinding.ActivityTaskEditBinding
import com.example.everytask.databinding.DialogSearchableSpinnerBinding
import com.example.everytask.models.call.TaskInfo
import com.example.everytask.models.response.Default
import com.example.everytask.models.response.tasks.Task
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.dialog_searchable_spinner.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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

    private lateinit var binding: ActivityTaskEditBinding
    private lateinit var dialogBinding: DialogSearchableSpinnerBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskEditBinding.inflate(layoutInflater)
        dialogBinding = DialogSearchableSpinnerBinding.inflate(layoutInflater)

        TOKEN = sharedPreferences.getString("TOKEN", null)!!

        setContentView(binding.root)

        val actionbar = supportActionBar!!
        actionbar.title = "Edit Task"
        actionbar.setDisplayHomeAsUpEnabled(true)

        pickDate()

        //set all the values
        task = getSerializable(this, "TASK", Task::class.java)
        binding.editTask.etTitle.setText(task.title)
        binding.editTask.etDescription.setText(task.description)
        binding.editTask.etDueDate.setText(task.due_time)
        //set saved values
        savedDay = task.due_time.split("-")[2].split(" ")[0].toInt()
        savedMonth = task.due_time.split("-")[1].toInt()
        savedYear = task.due_time.split("-")[0].toInt()
        savedHour = task.due_time.split(" ")[1].split(":")[0].toInt()
        savedMinute = task.due_time.split(":")[1].toInt()

        //make a read-only list of assignees with hardcoded values
        val assignees = List(100) { "Assignee $it" }
        var filteredList = assignees.sorted().toMutableList()

        binding.editTask.tvAssignee.setOnClickListener(View.OnClickListener {
            val dialog = Dialog(this)
            //call removeView to remove the view from the parent
            (dialogBinding.root.parent as ViewGroup?)?.removeView(dialogBinding.root)


            dialog.setContentView(dialogBinding.root)
            dialog.getWindow()?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()

            val adapter = AssigneeAdapter(this, filteredList, dialogBinding.flAssigneeContainer)
            dialogBinding.rvAssignees.adapter = adapter

            dialogBinding.etAssigneeSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    filteredList = assignees.filter {
                        it.lowercase(Locale.getDefault()).contains(
                            s.toString().lowercase(Locale.getDefault())
                        )
                    }.toMutableList()
                    var chips = dialogBinding.flAssigneeContainer.children
                    chips = chips.filter { chip ->
                        chip is Chip
                    }
                    chips.forEach { chip ->
                        if (filteredList.contains((chip as Chip).text)) {
                            filteredList.remove(chip.text)
                        }
                    }
                    Log.d("TAG", "assignees: $assignees")
                    Log.d("TAG", "filteredList: $filteredList")
                    Log.d("TAG", "chips: $chips")
                    adapter.setList(filteredList)
                }

                override fun afterTextChanged(s: Editable) {}
            })
        })
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

    fun updateTask(view: View) {
        val title = binding.editTask.etTitle.text.toString()
        val description = binding.editTask.etDescription.text.toString()
        val dueDate = "$savedYear-$savedMonth-$savedDay $savedHour:$savedMinute:00"

        if (title.isEmpty()) {
            binding.editTask.etTitle.error = "Title cannot be empty"
            return
        }
        if (title.length > 32) {
            binding.editTask.etTitle.error = "Title cannot be longer than 32 characters"
            return
        }
        if (description.length > 300) {
            binding.editTask.etDescription.error =
                "Description cannot be longer than 300 characters"
            return
        }
        val call = retrofitBuilder.updateTask(
            TOKEN,
            task.id, TaskInfo(title, description, dueDate)
        )
        Log.d("TAG", "editTask: ${TaskInfo(title, description, dueDate)}")
        call.enqueue(object : Callback<Default> {
            override fun onResponse(call: Call<Default>, response: Response<Default>) {
                if (response.isSuccessful) {
                    Log.d("TAG", "onResponse: ${response.body()}")
                    finish()
                } else {
                    Log.d("TAG", "onResponse: ${response.errorBody()}")
                    Toast.makeText(
                        this@TaskEditActivity,
                        "You are not the creator of this Task",
                        Toast.LENGTH_SHORT
                    ).show()
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