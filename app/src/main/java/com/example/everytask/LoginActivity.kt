package com.example.everytask

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.everytask.databinding.ActivityForgotPasswordBinding
import com.example.everytask.databinding.ActivityLoadingBinding
import com.example.everytask.databinding.ActivityLoginBinding
import com.example.everytask.models.call.LoginInfo
import com.example.everytask.models.response.Default
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity() {

    private lateinit var loginBinding: ActivityLoginBinding
    private lateinit var loadingBinding: ActivityLoadingBinding
    private lateinit var forgotPasswordBinding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        loadingBinding = ActivityLoadingBinding.inflate(layoutInflater)

        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE)
        editor = sharedPreferences.edit()

        setContentView(loadingBinding.root)

        if (sharedPreferences.getString("TOKEN", null) != null) {
            //TODO show loading animation
            val token = sharedPreferences.getString("TOKEN", null)
            verifyToken(token!!)
        } else {
            setContentView(loginBinding.root)
            emailFocusListener(loginBinding.etEmail, loginBinding.tilEmailContainer)
        }


    }

    private fun verifyToken(token: String) {
        val call = retrofitBuilder.verifyToken(token)
        call.enqueue(object : Callback<Default> {
            override fun onResponse(call: Call<Default>, response: Response<Default>) {
                if (response.isSuccessful) {
                    Log.d("TAG", "onResponse: ${response.body()}")
                    loginRedirect()
                } else {
                    sharedPreferences.edit().remove("TOKEN").apply()
                    setContentView(loginBinding.root)
                }
            }

            override fun onFailure(call: Call<Default>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "No connection to server", Toast.LENGTH_SHORT)
                    .show()
                Log.d("TAG", "no response")
                setContentView(loginBinding.root)
                emailFocusListener(loginBinding.etEmail, loginBinding.tilEmailContainer)
            }
        })
    }

    fun toRegister(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        this.overridePendingTransition(0, 0)
    }

    fun login(view: View) {
        loginBinding.btnLogin.isEnabled = false
        loginBinding.btnLogin.text = ""
        loginBinding.pbLogin.visibility = View.VISIBLE

        val validEmail = validEmail(loginBinding.etEmail.text.toString()) == null

        Log.d("LoginActivity", "login: $validEmail")
        if (validEmail) {
            val retrofitData = retrofitBuilder.loginUser(
                LoginInfo(
                    loginBinding.etEmail.text.toString(),
                    loginBinding.etPassword.text.toString()
                )
            )

            retrofitData.enqueue(object : Callback<Default> {
                override fun onResponse(call: Call<Default>, response: Response<Default>) {
                    if (response.isSuccessful) {
                        Log.d("TAG", "onResponse: ${response.body()}")
                        Log.d("TAG", "loginRedirect: ${response.body()?.token}")
                        editor.apply {
                            putString("TOKEN", response.body()?.token)
                            putString("USER_ID", response.body()?.user?.id.toString())
                        }.apply()
                        loginRedirect()
                    } else {
                        Log.d("TAG", "onResponse:${response.errorBody().toString()}")
                        loginBinding.tilEmailContainer.error = "Invalid email or password"
                        loginBinding.tilPasswordContainer.error = "Invalid email or password"
                        loginBinding.btnLogin.isEnabled = true
                        loginBinding.btnLogin.text = getString(R.string.login)
                        loginBinding.pbLogin.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<Default>, t: Throwable) {
                    Toast.makeText(
                        this@LoginActivity,
                        "No connection to server",
                        Toast.LENGTH_SHORT
                    ).show()
                    loginBinding.btnLogin.isEnabled = true
                    loginBinding.btnLogin.text = getString(R.string.login)
                    loginBinding.pbLogin.visibility = View.GONE
                    Log.d("TAG", t.message.toString())
                }
            })

        }
    }

    private fun loginRedirect() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        this.overridePendingTransition(0, 0)
        finish()
    }

    fun toForgotPassword(view: View) {
        forgotPasswordBinding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(forgotPasswordBinding.root)
    }

    fun toLogin(view: View) {
        forgotPasswordBinding.etEmail.text = null
        forgotPasswordBinding.tilEmailContainer.error = null
        setContentView(loginBinding.root)
    }

    fun sendResetMail(view: View) {
        val email = forgotPasswordBinding.etEmail.text.toString()
        val validEmail = validEmail(email)
        forgotPasswordBinding.tilEmailContainer.error = null
        Log.d("TAG", "sendResetMail: $validEmail")
        if (validEmail == null) {
            forgotPasswordBinding.btnSend.isEnabled = false
            forgotPasswordBinding.btnSend.text = ""
            forgotPasswordBinding.pbSend.visibility = View.VISIBLE
            val retrofitData = retrofitBuilder.sendPasswordResetMail(mapOf("email" to email))
            retrofitData.enqueue(object : Callback<Default> {
                override fun onResponse(call: Call<Default>, response: Response<Default>) {
                    if (response.isSuccessful) {
                        Log.d("TAG", "onResponse: ${response.body()}")
                        forgotPasswordBinding.btnSend.visibility = View.GONE
                        forgotPasswordBinding.pbSend.visibility = View.GONE
                        forgotPasswordBinding.llEmailContainer.visibility = View.GONE
                        forgotPasswordBinding.llSuccessContainer.visibility = View.VISIBLE
                    } else {
                        Log.d("TAG", "onResponse:${response.errorBody().toString()}")
                        forgotPasswordBinding.tilEmailContainer.error = "Invalid email"
                        forgotPasswordBinding.btnSend.isEnabled = true
                        forgotPasswordBinding.btnSend.text = "Send Reset Mail"
                        forgotPasswordBinding.pbSend.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<Default>, t: Throwable) {
                    Toast.makeText(
                        this@LoginActivity,
                        "No connection to server",
                        Toast.LENGTH_SHORT
                    ).show()
                    forgotPasswordBinding.btnSend.isEnabled = true
                    forgotPasswordBinding.btnSend.text = "Send Reset Mail"
                    forgotPasswordBinding.pbSend.visibility = View.GONE
                    Log.d("TAG", t.message.toString())
                }
            })
        } else {
            forgotPasswordBinding.tilEmailContainer.error = validEmail
        }
    }
}