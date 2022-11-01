package com.example.everytask

import android.content.SharedPreferences
import android.util.Log
import android.util.Patterns
import com.example.everytask.models.Default
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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

internal fun confirmPasswordFocusListener(tilConfirmPasswordContainer: TextInputLayout, etConfirmPassword: TextInputEditText, etPassword: TextInputEditText) {
    // TODO : ERROR MAYBE?? statt helperText
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
    // TODO : ERROR MAYBE?? statt helperText
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
    // TODO : ERROR MAYBE?? statt helperText
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

internal fun validPassword(password: String): String? {
    return when {
        password.isEmpty() -> "Password is required"
        password.length < 8 -> "Password must be at least 8 characters"
        //TODO Matching regex checken
        !password.matches(".*[a-z].*".toRegex()) -> "Password must contain at least one lowercase letter"
        !password.matches(".*[A-Z].*".toRegex()) -> "Password must contain at least one uppercase letter"
        !password.matches(".*[0-9].*".toRegex()) -> "Password must contain at least one number"
        !password.matches(".*[!@#\$%^&*()_+].*".toRegex()) -> "Password must contain at least one special character"
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