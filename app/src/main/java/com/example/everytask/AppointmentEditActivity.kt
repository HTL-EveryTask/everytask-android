package com.example.everytask

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.everytask.adapters.AssigneeAdapter
import com.example.everytask.adapters.GroupAdapter
import com.example.everytask.databinding.ActivityAppointmentEditBinding
import com.example.everytask.databinding.DialogSearchableSpinnerBinding
import com.example.everytask.fragments.GroupsFragment
import com.example.everytask.models.call.AppointmentInfo
import com.example.everytask.models.response.Default
import com.example.everytask.models.response.User
import com.example.everytask.models.response.groups.Group
import com.example.everytask.models.response.groups.GroupUser
import com.example.everytask.models.response.tasks.AssignedGroup
import com.example.everytask.models.response.tasks.AssignedUser
import com.example.everytask.models.response.appointments.Appointment
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.dialog_searchable_spinner.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList


class AppointmentEditActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private lateinit var TOKEN: String

    private lateinit var appointment: Appointment

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

    private lateinit var binding: ActivityAppointmentEditBinding
    private lateinit var dialogBinding: DialogSearchableSpinnerBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentEditBinding.inflate(layoutInflater)
        dialogBinding = DialogSearchableSpinnerBinding.inflate(layoutInflater)

        TOKEN = sharedPreferences.getString("TOKEN", null)!!

        setContentView(binding.root)

        val actionbar = supportActionBar!!
        actionbar.title = "Edit Appointment"
        actionbar.setDisplayHomeAsUpEnabled(true)

        pickDate()

        //disable textinputlayout
        binding.editAppointment.etAssignees.inputType = 0

        //set all the values
        appointment = getSerializable(this, "APPOINTMENT", Appointment::class.java)
        binding.editAppointment.etTitle.setText(appointment.title)
        binding.editAppointment.etDescription.setText(appointment.description)
        binding.editAppointment.etStartTime.setText(appointment.start_time)
        binding.editAppointment.etEndTime.setText(appointment.end_time)

        //set tags
        for (tag in appointment.tags) {
            val chip = Chip(this)
            chip.text = tag
            chip.isCloseIconVisible = true
            chip.isClickable = false
            chip.setOnCloseIconClickListener {
                binding.editAppointment.lvTagContainer.removeView(chip)
                binding.editAppointment.etTag.error = null
            }
            binding.editAppointment.lvTagContainer.addView(chip)
        }

        //set saved values
        savedDay = appointment.start_time.split("-")[2].split(" ")[0].toInt()
        savedMonth = appointment.start_time.split("-")[1].toInt()
        savedYear = appointment.start_time.split("-")[0].toInt()
        savedHour = appointment.start_time.split(" ")[1].split(":")[0].toInt()
        savedMinute = appointment.start_time.split(":")[1].toInt()

        //set on enter listener
        binding.editAppointment.etTag.setImeActionLabel("Add", EditorInfo.IME_ACTION_NEXT)
        binding.editAppointment.etTag.setOnEditorActionListener { _, actionId, _ ->
            if(binding.editAppointment.lvTagContainer.childCount < 5){
                if (actionId == EditorInfo.IME_ACTION_DONE|| actionId == EditorInfo.IME_ACTION_NEXT) {
                    val chip = Chip(this)
                    chip.text = binding.editAppointment.etTag.text.toString()
                    chip.isCloseIconVisible = true
                    chip.isClickable = false
                    chip.setOnCloseIconClickListener {
                        binding.editAppointment.lvTagContainer.removeView(chip)
                        binding.editAppointment.etTag.error = null
                    }
                    binding.editAppointment.lvTagContainer.addView(chip)
                    binding.editAppointment.etTag.text?.clear()
                }
            }else{
                binding.editAppointment.etTag.error = "You can only add 5 tags"
            }
            true
        }

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
        binding.editAppointment.etStartTime.setOnClickListener {
            getDateTimeCalendar()
            DatePickerDialog(this, this, year, month, day).show()
        }
        binding.editAppointment.etEndTime.setOnClickListener {
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
        binding.editAppointment.etStartTime.setText("$savedDay/$savedMonth/$savedYear $savedHour:$savedMinute")
    }

    fun updateAppointment(view: View) {
        val title = binding.editAppointment.etTitle.text.toString()
        val description = binding.editAppointment.etDescription.text.toString()
        val dueDate = "$savedYear-$savedMonth-$savedDay $savedHour:$savedMinute:00"
        val tags = mutableListOf<String>()
        for (i in 0 until binding.editAppointment.lvTagContainer.childCount) {
            tags.add((binding.editAppointment.lvTagContainer.getChildAt(i) as Chip).text.toString())
        }
        Log.d("TAG", "updateAppointment: $tags")

        if (title.isEmpty()) {
            binding.editAppointment.etTitle.error = "Title cannot be empty"
            return
        }
        if (title.length > 32) {
            binding.editAppointment.etTitle.error = "Title cannot be longer than 32 characters"
            return
        }
        if (description.length > 300) {
            binding.editAppointment.etDescription.error =
                "Description cannot be longer than 300 characters"
            return
        }
        //get assignedUsers and assignedGroups from dialogBinding.flAssigneeContainer
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
        } else {
            //add all already assigned users
            appointment.assigned_users.forEach {
                assignedUsers.add(it.id)
            }
            //add all already assigned groups
            appointment.assigned_groups.forEach {
                assignedGroups.add(it.id)
            }
        }
        Log.d("TAG", "updateAppointment Users: $assignedUsers")
        Log.d("TAG", "updateAppointment Groups: $assignedGroups")

        Log.d(
            "TAG",
            "editAppointment: ${AppointmentInfo(title, description, dueDate, dueDate, assignedUsers, assignedGroups, tags)}"
        )
        val call = retrofitBuilder.updateAppointment(
            TOKEN,
            appointment.id, AppointmentInfo(title, description, dueDate, dueDate, assignedUsers, assignedGroups, tags)
        )
        call.enqueue(object : Callback<Default> {
            override fun onResponse(call: Call<Default>, response: Response<Default>) {
                if (response.isSuccessful) {
                    Log.d("TAG", "onResponse: ${response.body()}")
                    finish()
                } else {
                    Log.d("TAG", "onResponse: ${response.errorBody()}")
                    Toast.makeText(
                        this@AppointmentEditActivity,
                        "You are not the creator of this Appointment",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Default>, t: Throwable) {
                Toast.makeText(this@AppointmentEditActivity, "No connection to server", Toast.LENGTH_SHORT)
                    .show()
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
                Toast.makeText(this@AppointmentEditActivity, "No connection to server", Toast.LENGTH_SHORT)
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

    fun cancelAppointment(view: View) {
        finish()
    }

    fun initializeAssignables() {
        getAssignees(groups)

        val filteredList = assignees.sortedWith(compareBy { it !is AssignedGroup }).toMutableList()
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

        //create chips for every currently assigned if flAssigneeContainer contains only one child
        appointment.assigned_groups.forEach {
            //get the group from the list of groups
            val group = groups.find { group -> group.id == it.id }
            if (group != null) {
                createChip(
                    this,
                    group,
                    binding.editAppointment.flAssigneeContainer,
                    null
                )
                createChip(
                    this,
                    group,
                    dialogBinding.flAssigneeContainer,
                    adapter
                )
                filteredList.remove(group)
                for (user in group.users) {
                    filteredList.removeAll(filteredList.filter { it is GroupUser && it.id == user.id })
                }
            }
        }
        appointment.assigned_users.forEach {
            //get the user from the list of users
            val user = assignees.find { user -> user is GroupUser && user.id == it.id }
            if (user != null) {
                createChip(
                    this,
                    user,
                    binding.editAppointment.flAssigneeContainer,
                    null
                )
                createChip(
                    this,
                    user,
                    dialogBinding.flAssigneeContainer,
                    adapter
                )
            }
            filteredList.remove(user)
        }

        Log.d("TAG", filteredList.toString())

        binding.editAppointment.etAssignees.setOnClickListener(View.OnClickListener {
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

            //when the dialog is dismissed, update the assignee chips
            dialog.setOnDismissListener {
                binding.editAppointment.flAssigneeContainer.removeViews(
                    0,
                    binding.editAppointment.flAssigneeContainer.childCount - 1
                )
                //add the chips to the assignee container
                for (i in 0 until dialogBinding.flAssigneeContainer.childCount - 1) {
                    val assignee = dialogBinding.flAssigneeContainer.getChildAt(i) as Chip
                    createChip(
                        this,
                        assignee.tag as Serializable,
                        binding.editAppointment.flAssigneeContainer,
                        null
                    )
                }
            }
        })
    }
}