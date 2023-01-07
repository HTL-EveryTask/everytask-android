package com.example.everytask

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import com.example.everytask.adapters.AssigneeAdapter
import com.example.everytask.databinding.ActivityTaskAddBinding
import com.example.everytask.databinding.DialogSearchableSpinnerBinding
import com.example.everytask.models.call.TaskInfo
import com.example.everytask.models.response.tasks.Task
import com.example.everytask.models.response.Default
import com.example.everytask.models.response.groups.Group
import com.example.everytask.models.response.groups.GroupUser
import com.example.everytask.models.response.tasks.AssignedGroup
import com.example.everytask.models.response.tasks.AssignedUser
import com.google.android.material.chip.Chip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable
import java.util.*

class TaskAddActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private lateinit var TOKEN: String

    private lateinit var groups: List<Group>

    private lateinit var assignees: List<java.io.Serializable>

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
    private lateinit var dialogBinding: DialogSearchableSpinnerBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskAddBinding.inflate(layoutInflater)
        dialogBinding = DialogSearchableSpinnerBinding.inflate(layoutInflater)

        TOKEN = sharedPreferences.getString("TOKEN", null)!!

        setContentView(binding.root)

        val actionbar = supportActionBar!!
        actionbar.title = "Add Task"
        actionbar.setDisplayHomeAsUpEnabled(true)
        
        binding.editTask.etAssignees.inputType = 0

        pickDate()

        getDateTimeCalendar()
        savedDay = day
        savedMonth = month
        savedYear = year
        savedHour = hour
        savedMinute = minute
        binding.editTask.etDueDate.setText("$savedYear-$savedMonth-$savedDay $savedHour:$savedMinute:00")

        getGroups()
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
        //disable button
        binding.btnSave.isEnabled = false

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

        val assignedUsers = mutableListOf<Int>()
        val assignedGroups = mutableListOf<Int>()
        Log.d("TAG", "assignees: $assignees")
        if (dialogBinding.flAssigneeContainer.childCount > 1) {
            for (i in 0 until dialogBinding.flAssigneeContainer.childCount) {
                val assignee = dialogBinding.flAssigneeContainer.getChildAt(i)
                if (assignee is Chip) {
                    if (assignee.tag is GroupUser) {
                        assignedUsers.add((assignee.tag as GroupUser).id)
                    } else if (assignee.tag is Group) {
                        assignedGroups.add((assignee.tag as Group).id)
                    }
                }
            }
        }

        val call = retrofitBuilder.addTask(
            TOKEN,
            TaskInfo(title, description, dueDate, assignedUsers, assignedGroups)
        )
        Log.d(
            "TAG",
            "addTask: ${TaskInfo(title, description, dueDate, assignedUsers, assignedGroups)}"
        )
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
                //enable button
                binding.btnSave.isEnabled = true
                Log.d("TAG", t.message.toString())
            }
        })
    }

    fun getGroups() {
        val call = retrofitBuilder.getGroups(TOKEN)
        call.enqueue(object : Callback<Default> {
            override fun onResponse(call: Call<Default>, response: Response<Default>) {
                if (response.isSuccessful) {
                    val default = response.body()
                    Log.d("TAG", "affiliated Groups: ${response.body()}")
                    if (default != null) {
                        groups = default.groups!!
                        initializeAssignables()
                    }
                }
            }

            override fun onFailure(call: Call<Default>, t: Throwable) {
                Toast.makeText(this@TaskAddActivity, "No connection to server", Toast.LENGTH_SHORT)
                    .show()
                Log.d("TAG", "onFailure: ${t.message}")
            }
        })
    }

    fun getAssignees(groups: List<Group>) {
        //create a list of all users in the groups
        val assigneeList = mutableListOf<java.io.Serializable>()
        for (group in groups) {
            assigneeList.add(group)
            for (user in group.users) {
                if (assigneeList.find { it is GroupUser && it.id == user.id } == null) {
                    assigneeList.add(user)
                }
            }
        }
        assignees = assigneeList
    }

    fun initializeAssignables() {
        getAssignees(groups)

        val filteredList = assignees.sortedWith(compareBy { it !is AssignedGroup }).toMutableList()

        Log.d("TAG", filteredList.toString())

        binding.editTask.etAssignees.setOnClickListener(View.OnClickListener {
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

            filteredList.sortWith(
                compareBy(
                    { it !is Group },
                    { if (it is Group) it.name else (it as GroupUser).username })
            )

            val adapter = AssigneeAdapter(
                this,
                assignees,
                filteredList,
                dialogBinding.etAssigneeSearch,
                dialogBinding.flAssigneeContainer
            )
            dialogBinding.rvAssignees.adapter = adapter

            //when the dialog is dismissed, update the assignee chips
            dialog.setOnDismissListener {
                binding.editTask.flAssigneeContainer.removeViews(
                    0,
                    binding.editTask.flAssigneeContainer.childCount - 1
                )
                //add the chips to the assignee container
                for (i in 0 until dialogBinding.flAssigneeContainer.childCount - 1) {
                    val assignee = dialogBinding.flAssigneeContainer.getChildAt(i) as Chip
                    createChip(
                        this,
                        assignee.tag as Serializable,
                        binding.editTask.flAssigneeContainer,
                        null
                    )
                }
            }
        })
    }
}