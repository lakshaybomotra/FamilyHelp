package com.lbdev.familyhelp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

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
            if (state == "close") {
                openInvite(inviteButton)
                state = "open"
            } else {
                closeInvite(inviteButton)
                state = "close"
            }
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        val name = currentUser?.displayName.toString()
        val email = currentUser?.email.toString()
        val phoneNum = currentUser?.phoneNumber.toString()
        val imageUrl = currentUser?.photoUrl.toString()
        Log.d("TAG", "onCreate: $email this is current user")

        val db = Firebase.firestore


        val user = hashMapOf(
            "name" to name,
            "email" to email,
            "phoneNumber" to phoneNum,
            "imageUrl" to imageUrl,
            "live" to false,
            "lat" to 10.0,
            "long" to 10.0
        )

        db.collection("users").get().addOnSuccessListener { users ->
            var totalUsers = users.size()
            var flag = 0
            while (totalUsers > 0) {
                if (users.documents[totalUsers - 1].data?.get("email").toString() == email) {
                    flag = 1
                }
                totalUsers -= 1
            }
            if (flag == 0) {
                db.collection("users").document(email).set(user).addOnSuccessListener {
                    bottomBar.selectedItemId = R.id.nav_home
                }
                    .addOnFailureListener { }
            }
        }

        val userData = db.collection("users").document(email).get()

        bottomBar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_guard -> {
                    closeInvite(inviteButton)
                    inviteButton.isVisible = false
                    inflateFragment(GuardFragment.newInstance())
                }
                R.id.nav_home -> {
                    inviteButton.isVisible = true
                    inflateFragment(HomeFragment.newInstance())
                }
                R.id.nav_dashboard -> {
                    closeInvite(inviteButton)
                    inviteButton.isVisible = false
                    inflateFragment(MapsFragment())
                }
                R.id.nav_profile -> {
                    closeInvite(inviteButton)
                    inviteButton.isVisible = false
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
        if (fragmentManager.backStackEntryCount > 0) {
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