package com.example.everytask

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.example.everytask.databinding.ActivityRegisterBinding
import com.example.everytask.models.Default
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var registerBinding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        registerBinding = ActivityRegisterBinding.inflate(layoutInflater)

        setContentView(registerBinding.root)
        emailFocusListener(registerBinding.etEmail, registerBinding.tilEmailContainer)
        passwordFocusListener(
            registerBinding.etPassword,
            registerBinding.tilPasswordContainer,
            registerBinding.tilConfirmPasswordContainer,
            registerBinding.etConfirmPassword
        )
        confirmPasswordFocusListener(
            registerBinding.tilPasswordContainer,
            registerBinding.etConfirmPassword,
            registerBinding.etPassword
        )
        usernameFocusListener(registerBinding.etUsername, registerBinding.tilUsernameContainer)
    }

    fun toLogin(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        this.overridePendingTransition(0, 0)
        finish()
    }

    fun register(view: View) {
        registerBinding.btnRegister.isEnabled = false
        registerBinding.btnRegister.text = ""
        registerBinding.pbRegister.visibility = View.VISIBLE

        val username = registerBinding.etUsername.text.toString()
        val validEmail = validEmail(registerBinding.etEmail.text.toString()) == null
        val validPassword = validPassword(registerBinding.etPassword.text.toString()) == null
        val validConfirmPassword =
            registerBinding.etPassword.text.toString() == registerBinding.etConfirmPassword.text.toString()

        if(username.isEmpty()) {
            registerBinding.tilUsernameContainer.error = "Username cannot be empty"
        } else {
            registerBinding.tilUsernameContainer.error = null
        }

        if (validEmail && validPassword && validConfirmPassword && username.isNotEmpty()) {
            val retrofitData = retrofitBuilder.registerUser(
                registerBinding.etUsername.text.toString(),
                registerBinding.etEmail.text.toString(),
                registerBinding.etPassword.text.toString(),
                false
            )

            Log.d("TAG", "register: $retrofitBuilder")

            retrofitData.enqueue(object : Callback<Default> {
                override fun onResponse(
                    call: Call<Default>,
                    response: Response<Default>
                ) {
                    if (response.isSuccessful) {
                        Log.d("TAG", "onResponse: ${response.body()}")
                        Log.d("TAG", "loginRedirect: ${response.body()?.token}")
                        editor.apply {
                            putString("TOKEN", response.body()?.token)
                        }.apply()
                        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                        startActivity(intent)
                        this@RegisterActivity.overridePendingTransition(0, 0)
                    } else {
                        Log.d("TAG", "onResponse: ${response.errorBody()}")
                        registerBinding.tilEmailContainer.error = "Email already exists"
                    }
                }

                override fun onFailure(call: Call<Default>, t: Throwable) {
                    Toast.makeText(this@RegisterActivity, "No connection to server", Toast.LENGTH_SHORT).show()
                    registerBinding.btnRegister.isEnabled = true
                    registerBinding.btnRegister.text = getString(R.string.register)
                    registerBinding.pbRegister.visibility = View.GONE
                    Log.d("TAG", "onFailure: ${t.message}")
                }
            })
        }
    }
}