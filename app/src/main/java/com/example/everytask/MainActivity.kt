package com.example.everytask

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.everytask.databinding.ActivityLoginBinding
import com.example.everytask.databinding.ActivityMainBinding
import com.example.everytask.databinding.ActivityRegisterBinding
import com.example.everytask.fragments.ConnectionsFragment
import com.example.everytask.fragments.GroupsFragment
import com.example.everytask.fragments.HomeFragment
import com.example.everytask.fragments.SettingsFragment
import com.example.everytask.models.Login
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {

    var BASE_URL = "http://192.168.0.68:8000/api/"

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var loginBinding: ActivityLoginBinding
    private lateinit var registerBinding: ActivityRegisterBinding

    private lateinit var retrofitBuilder: ApiInterface

    private val homeFragment = HomeFragment()
    private val groupsFragment = GroupsFragment()
    private val connectionsFragment = ConnectionsFragment()
    private val settingsFragment = SettingsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        registerBinding = ActivityRegisterBinding.inflate(layoutInflater)

        retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(ApiInterface::class.java)

        toLogin(loginBinding.root)
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fl_fragment_container, fragment)
        transaction.commit()
    }

    fun login(view: View) {
        val validEmail = validEmail(loginBinding.etEmail.text.toString()) == null

        if (validEmail) {
            val retrofitData = retrofitBuilder.loginUser(
                loginBinding.etEmail.text.toString(),
                loginBinding.etPassword.text.toString()
            )

            retrofitData.enqueue(object : Callback<Login> {
                override fun onResponse(call: Call<Login>, response: Response<Login>) {
                    if (response.isSuccessful) {
                        Log.d("Login", "onResponse: ${response.body()}")

                        if(response.body()?.type == "Success") {
                            loginRedirect(response.body()?.token)
                        }else {
                            loginBinding.tilEmailContainer.error = "Invalid email or password"
                            loginBinding.tilPasswordContainer.error = "Invalid email or password"
                        }
                    }
                }

                override fun onFailure(call: Call<Login>, t: Throwable) {
                    Log.d("Login", t.message.toString())
                }
            })

        }
    }

    fun register(view: View) {
        val validEmail = validEmail(registerBinding.etEmail.text.toString()) == null
        val validPassword = validPassword(registerBinding.etPassword.text.toString()) == null
        val validConfirmPassword =
            registerBinding.etPassword.text.toString() == registerBinding.etConfirmPassword.text.toString()

        if(validEmail && validPassword && validConfirmPassword) {
            val retrofitData = retrofitBuilder.registerUser(
                registerBinding.etUsername.text.toString(),
                registerBinding.etEmail.text.toString(),
                registerBinding.etPassword.text.toString(),
                false
            )

            retrofitData.enqueue(object : Callback<Login> {
                override fun onResponse(
                    call: Call<Login>,
                    response: Response<Login>
                ) {
                    if (response.isSuccessful) {
                        Log.d("Register", "onResponse: ${response.body()}")

                        if(response.body()?.type == "Success") {
                            loginRedirect(response.body()?.token)
                        }else {
                            registerBinding.tilEmailContainer.error = "Email already exists"
                        }
                    }
                }

                override fun onFailure(call: Call<Login>, t: Throwable) {
                    Log.d("Register", "onFailure: ${t.message}")
                }
            })
        }
    }

    fun loginRedirect(token: String? = null) {
        val sharedPreferences: SharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.apply {
            putString("TOKEN", token)
        }.apply()

        //log token
        Log.d("Token", "loginRedirect: $token")

        setContentView(mainBinding.root)
        replaceFragment(homeFragment)
        mainBinding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> replaceFragment(homeFragment)
                R.id.groups -> replaceFragment(groupsFragment)
                R.id.connections -> replaceFragment(connectionsFragment)
                R.id.settings -> replaceFragment(settingsFragment)
            }
            true
        }
    }

    fun toLogin(view: View) {
        setContentView(loginBinding.root)
        emailFocusListener(loginBinding.etEmail, loginBinding.tilEmailContainer)
        // TODO add password focus listener? also add password validation in login()
    }

    fun toRegister(view: View) {
        setContentView(registerBinding.root)
        emailFocusListener(registerBinding.etEmail, registerBinding.tilEmailContainer)
        passwordFocusListener(
            registerBinding.etPassword,
            registerBinding.tilPasswordContainer,
            registerBinding.tilConfirmPasswordContainer,
            registerBinding.etConfirmPassword
        )
        confirmPasswordFocusListener()
    }

    private fun confirmPasswordFocusListener() {
        // TODO : ERROR MAYBE?? statt helperText
        registerBinding.etConfirmPassword.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                if (registerBinding.etPassword.text.toString() != registerBinding.etConfirmPassword.text.toString()) {
                    registerBinding.tilConfirmPasswordContainer.helperText =
                        "Passwords do not match"
                } else {
                    registerBinding.tilConfirmPasswordContainer.helperText = null
                }
            }
        }
    }

    private fun emailFocusListener(etEmail: TextInputEditText, tilEmailContainer: TextInputLayout) {
        // TODO : ERROR MAYBE?? statt helperText
        etEmail.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                tilEmailContainer.helperText = validEmail(etEmail.text.toString())
            }
        }
    }

    private fun passwordFocusListener(
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
        }
    }

    private fun validPassword(password: String): String? {
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

    private fun validEmail(email: String): String? {
        return when {
            email.isEmpty() -> "Email is required"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid Email Address"
            else -> null
        }
    }
}