package com.lbdev.familyhelp

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val perissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_CONTACTS
    )

    val permissionCode = 47
    var state = "close"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askForPermission()

        val bottomBar = findViewById<BottomNavigationView>(R.id.bottom_bar)
        val inviteButton = findViewById<ImageView>(R.id.openInvite)
        inviteButton.setOnClickListener {
                if (state=="close")
                {
                    openInvite(inviteButton)
                    state = "open"
                }
                else {
                    closeInvite(inviteButton)
                    state = "close"
                }
        }

        bottomBar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_guard -> {
                    closeInvite(inviteButton)
                    inviteButton.isVisible=false
                    inflateFragment(GuardFragment.newInstance())
                }
                R.id.nav_home -> {
                    inviteButton.isVisible=true
                    inflateFragment(HomeFragment.newInstance())
                }
                R.id.nav_dashboard -> {
                    closeInvite(inviteButton)
                    inviteButton.isVisible=false
                    inflateFragment(MapsFragment())
                }
                R.id.nav_profile -> {
                    closeInvite(inviteButton)
                    inviteButton.isVisible=false
                    inflateFragment(ProfileFragment.newInstance())
                }
            }

            true
        }

        bottomBar.selectedItemId = R.id.nav_home
    }

    private fun closeInvite(inviteButton: ImageView) {
        inviteButton.setImageResource(R.drawable.ic_up)
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        if(fragmentManager.backStackEntryCount >0) {
            fragmentManager.popBackStack();
        }
        fragmentTransaction.commit();
        state = "close"
    }

    private fun openInvite(inviteButton: ImageView) {
        inviteButton.setImageResource(R.drawable.ic_down)
        val fragment = InviteFragment()
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.invite_container, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
        state = "open"
    }

    private fun askForPermission() {
        ActivityCompat.requestPermissions(this, perissions, permissionCode)
    }

    private fun inflateFragment(newInstance: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, newInstance)
        transaction.commit()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == permissionCode) {
            if (allPermissionGranted()) {

            } else {

            }
        }
    }

    private fun allPermissionGranted(): Boolean {
        for (item in perissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    item
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }

        return true
    }
}