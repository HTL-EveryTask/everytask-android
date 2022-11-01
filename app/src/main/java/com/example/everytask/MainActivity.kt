package com.example.everytask

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.everytask.databinding.ActivityMainBinding
import com.example.everytask.fragments.ConnectionsFragment
import com.example.everytask.fragments.GroupsFragment
import com.example.everytask.fragments.HomeFragment
import com.example.everytask.fragments.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding

    private val homeFragment = HomeFragment()
    private val groupsFragment = GroupsFragment()
    private val connectionsFragment = ConnectionsFragment()
    private val settingsFragment = SettingsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        /*TODO check screen orientation with kiki in AndroidManifest.xml
        https://developer.android.com/guide/topics/manifest/activity-element.html#screen
        */
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        mainBinding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(mainBinding.root)
        this.overridePendingTransition(0, 0)

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

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fl_fragment_container, fragment)
        transaction.commit()
    }
}