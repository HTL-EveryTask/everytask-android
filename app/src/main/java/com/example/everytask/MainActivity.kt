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

    fun register(view: View) {
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
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
        emailFocusListener()
    }

    fun toRegister(view: View) {
        setContentView(registerBinding.root)
    }

    private fun emailFocusListener() {
        loginBinding.etEmail.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                loginBinding.tilEmailContainer.helperText = validEmail()
            }
        }
    }

    private fun validEmail(): String? {
        val email = loginBinding.etEmail.text.toString()
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Invalid Email Address"
        }
        return null
    }
}