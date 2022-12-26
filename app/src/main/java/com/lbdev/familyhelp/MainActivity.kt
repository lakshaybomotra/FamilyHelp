package com.lbdev.familyhelp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.selects.SelectInstance

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inflateFragment(HomeFragment.newInstance())

        val bottomBar = findViewById<BottomNavigationView>(R.id.bottom_bar)
        bottomBar.setOnItemSelectedListener {
            if (it.itemId==R.id.nav_guard){
                inflateFragment(GuardFragment.newInstance())
            }
            else if (it.itemId==R.id.nav_home){
                inflateFragment(HomeFragment.newInstance())
            }
            else if (it.itemId==R.id.nav_dashboard){
                inflateFragment(DashboardFragment.newInstance())
            }
            else if (it.itemId==R.id.nav_profile){
                inflateFragment(ProfileFragment.newInstance())
            }

            true
        }
    }

//    private fun inflateHomeFragment() {
//        val transaction = supportFragmentManager.beginTransaction()
//        transaction.replace(R.id.container, HomeFragment.newInstance())
//        transaction.commit()
//    }

    private fun inflateFragment(newInstance: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, newInstance)
        transaction.commit()
    }
}