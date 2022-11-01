package com.example.everytask

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.everytask.databinding.ActivityLoginBinding
import com.example.everytask.models.Default
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var loginBinding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        loginBinding = ActivityLoginBinding.inflate(layoutInflater)

        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE)
        editor = sharedPreferences.edit()

        setContentView(loginBinding.root)

        emailFocusListener(loginBinding.etEmail, loginBinding.tilEmailContainer)

        if (sharedPreferences.getString("TOKEN", null) != null) {
            val token = sharedPreferences.getString("TOKEN", null)
            verifyToken(token!!)
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
                    val errorResponse: Default? =
                        gson.fromJson(response.errorBody()!!.charStream(), type)
                    Log.d("TAG", "onResponse: $errorResponse")
                }
            }
            override fun onFailure(call: Call<Default>, t: Throwable) {
                Log.d("verifyToken", "token not verified")
            }
        })
    }

    fun toRegister(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        this.overridePendingTransition(0, 0)
    }

    fun login(view: View) {
        val validEmail = validEmail(loginBinding.etEmail.text.toString()) == null

        Log.d("LoginActivity", "login: $validEmail")
        if (validEmail) {
            val retrofitData = retrofitBuilder.loginUser(
                loginBinding.etEmail.text.toString(),
                loginBinding.etPassword.text.toString()
            )

            retrofitData.enqueue(object : Callback<Default> {
                override fun onResponse(call: Call<Default>, response: Response<Default>) {
                    if (response.isSuccessful) {
                        Log.d("TAG", "onResponse: ${response.body()}")
                        Log.d("TAG", "loginRedirect: ${response.body()?.token}")
                        editor.apply {
                            putString("TOKEN", response.body()?.token)
                        }.apply()
                        loginRedirect()
                    } else {
                        val errorResponse: Default? =
                            gson.fromJson(response.errorBody()!!.charStream(), type)
                        Log.d("TAG", "onResponse: $errorResponse")
                        loginBinding.tilEmailContainer.error = "Invalid email or password"
                        loginBinding.tilPasswordContainer.error = "Invalid email or password"
                    }
                }

                override fun onFailure(call: Call<Default>, t: Throwable) {
                    Log.d("TAG", t.message.toString())
                }
            })

        }
    }

    private fun loginRedirect(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        this.overridePendingTransition(0, 0)
        finish()
    }
}