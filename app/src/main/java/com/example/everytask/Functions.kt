package com.example.everytask

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.everytask.adapters.AssigneeAdapter
import com.example.everytask.models.response.Default
import com.example.everytask.models.response.User
import com.example.everytask.models.response.groups.Group
import com.example.everytask.models.response.groups.GroupUser
import com.example.everytask.models.response.tasks.AssignedGroup
import com.example.everytask.models.response.tasks.AssignedUser
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.Serializable

internal var BASE_URL = "http://192.168.0.68:8000/api/"

internal var retrofitBuilder = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(ApiInterface::class.java)

val gson = Gson()
val type = object : TypeToken<Default>() {}.type

internal lateinit var sharedPreferences: SharedPreferences
internal lateinit var editor: SharedPreferences.Editor

internal fun confirmPasswordFocusListener(
    tilConfirmPasswordContainer: TextInputLayout,
    etConfirmPassword: TextInputEditText,
    etPassword: TextInputEditText
) {
    etConfirmPassword.setOnFocusChangeListener { _, focused ->
        if (!focused) {
            if (etPassword.text.toString() != etConfirmPassword.text.toString()) {
                tilConfirmPasswordContainer.helperText =
                    "Passwords do not match"
            } else {
                tilConfirmPasswordContainer.helperText = null
            }
        }
    }
}

internal fun emailFocusListener(etEmail: TextInputEditText, tilEmailContainer: TextInputLayout) {
    etEmail.setOnFocusChangeListener { _, focused ->
        if (!focused) {
            tilEmailContainer.helperText = validEmail(etEmail.text.toString())
        }
    }
}

internal fun passwordFocusListener(
    etPassword: TextInputEditText,
    tilPasswordContainer: TextInputLayout,
    tilConfirmPasswordContainer: TextInputLayout? = null,
    etConfirmPassword: TextInputEditText? = null
) {
    etPassword.setOnFocusChangeListener { _, focused ->
        if (!focused) {
            tilPasswordContainer.helperText = validPassword(etPassword.text.toString())
            if (etConfirmPassword != null && tilConfirmPasswordContainer != null) {
                if (etPassword.text.toString() != etConfirmPassword.text.toString()) {
                    tilConfirmPasswordContainer.helperText =
                        "Passwords do not match"
                } else {
                    tilConfirmPasswordContainer.helperText = null
                }
            }
        }
        Log.d("TAG", "passwordFocusListener: ${validPassword(etPassword.text.toString())}")
    }
}

internal fun usernameFocusListener(
    etUsername: TextInputEditText,
    tilUsernameContainer: TextInputLayout
) {
    etUsername.setOnFocusChangeListener { _, focused ->
        if (!focused) {
            tilUsernameContainer.helperText = validUsername(etUsername.text.toString())
        }
    }
}

fun validUsername(username: String): String? {
    return if (username.isEmpty()) {
        "Username cannot be empty"
    } else {
        null
    }
}

internal fun validPassword(password: String): String? {
    return when {
        password.isEmpty() -> "Password is required"
        password.length < 8 -> "Password must be at least 8 characters"
        //TODO Matching regex checken
        !password.matches(".*[a-z].*".toRegex()) -> "Password must contain at least one lowercase letter"
        !password.matches(".*[0-9].*".toRegex()) -> "Password must contain at least one number"
        else -> null
    }
}

internal fun validEmail(email: String): String? {
    return when {
        email.isEmpty() -> "Email is required"
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid Email Address"
        else -> null
    }
}

internal fun createChip(context: Context, assignee: Serializable, chipGroup: ViewGroup, adapter: AssigneeAdapter?) {
    val chip = Chip(context)
    chip.tag = assignee
    if(assignee is GroupUser) {
        chip.text = assignee.username
        chip.chipIcon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_person_outline_24)
    } else if(assignee is Group) {
        chip.text = assignee.name
        chip.chipIcon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_groups_24)
    }
    chip.isClickable = false
    chip.isCheckable = false
    chip.isCloseIconVisible = true
    chipGroup.addView(chip as View, chipGroup.childCount - 1)
    if(adapter != null) {
        chip.setOnCloseIconClickListener {
            chipGroup.removeView(chip as View)
            adapter.addAssignee(assignee)
        }
    }else{
        chip.isCloseIconVisible = false
    }
}

fun <T : Serializable?> getSerializable(activity: Activity, name: String, clazz: Class<T>): T {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        activity.intent.getSerializableExtra(name, clazz)!!
    else
        activity.intent.getSerializableExtra(name) as T
}

internal fun Int.toBoolean() = this == 1
