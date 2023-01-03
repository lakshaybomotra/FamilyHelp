package com.lbdev.familyhelp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    lateinit var database: MyFamilyDatabase

    val db = Firebase.firestore
    val permissionCode = 47
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askForPermission()

        database = MyFamilyDatabase.getDatabase(applicationContext)
        val bottomBar = findViewById<BottomNavigationView>(R.id.bottom_bar)


        val currentUser = FirebaseAuth.getInstance().currentUser
        val name = currentUser?.displayName.toString()
        val email = currentUser?.email.toString()
        val phoneNum = currentUser?.phoneNumber.toString()
        val imageUrl = currentUser?.photoUrl.toString()
        val userUid = currentUser?.uid.toString()
        Log.d("TAG", "onCreate: $email this is current user")

        val user = hashMapOf(
            "name" to name,
            "email" to email,
            "phoneNumber" to phoneNum,
            "imageUrl" to imageUrl,
            "live" to false,
            "lat" to 10.0,
            "long" to 10.0,
            "userUid" to userUid
        )

        db.collection("users").get().addOnSuccessListener { users ->
            var totalUsers = users.size()
            var flag = 0
            while (totalUsers > 0) {
                if (users.documents[totalUsers - 1].data?.get("email").toString() == email) {
                    flag = 1
                    setUserData(email)
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

        bottomBar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_guard -> {
                    inflateFragment(GuardFragment.newInstance())
                }
                R.id.nav_home -> {
                    inflateFragment(HomeFragment.newInstance())
                }
                R.id.nav_dashboard -> {
                    inflateFragment(MapsFragment())
                }
                R.id.nav_profile -> {
                    inflateFragment(ProfileFragment.newInstance())
                }
            }

            true
        }

        bottomBar.selectedItemId = R.id.nav_home

    }

    private fun setUserData(email: String) {
        db.collection("users").document(email).get().addOnSuccessListener {
            var userModel = UserModel()
            userModel.name = it.data?.get("name").toString()
            CoroutineScope(Dispatchers.IO).launch {
                database.userDao().saveUserName(userModel)
            }
        }
    }

    private fun askForPermission() {
        ActivityCompat.requestPermissions(this, permissions, permissionCode)
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
        for (item in permissions) {
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