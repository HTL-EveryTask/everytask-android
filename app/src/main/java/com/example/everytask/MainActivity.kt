package com.example.everytask

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.everytask.databinding.ActivityLoginBinding
import com.example.everytask.databinding.ActivityMainBinding
import com.example.everytask.databinding.ActivityRegisterBinding
import com.example.everytask.fragments.ConnectionsFragment
import com.example.everytask.fragments.GroupsFragment
import com.example.everytask.fragments.HomeFragment
import com.example.everytask.fragments.SettingsFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var loginBinding: ActivityLoginBinding
    private lateinit var registerBinding: ActivityRegisterBinding

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
    }

    fun register(view: View) {
        val validEmail = validEmail(registerBinding.etEmail.text.toString()) == null
        val validPassword = validPassword(registerBinding.etPassword.text.toString()) == null
        val validConfirmPassword =
            registerBinding.etPassword.text.toString() == registerBinding.etConfirmPassword.text.toString()

        if(validEmail && validPassword && validConfirmPassword) {
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
            registerBinding.tilConfirmPasswordContainer
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
        tilConfirmPasswordContainer: TextInputLayout? = null
    ) {
        // TODO : ERROR MAYBE?? statt helperText
        etPassword.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                tilPasswordContainer.helperText = validPassword(etPassword.text.toString())
                    tilConfirmPasswordContainer?.helperText =
                        "Passwords do not match"
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