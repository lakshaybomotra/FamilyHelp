package com.lbdev.familyhelp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors


class ProfileFragment : Fragment() {
    lateinit var database: MyFamilyDatabase
    lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        database = MyFamilyDatabase.getDatabase(mContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = Firebase.firestore

        val userProfile = requireView().findViewById<ImageView>(R.id.ivProfile)
//        val editProfile = requireView().findViewById<ImageView>(R.id.ivEditProfile)
        val userName = requireView().findViewById<TextView>(R.id.tvUserName)
        val logout = requireView().findViewById<LinearLayout>(R.id.llLogout)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val email = currentUser?.email.toString()
        var uImage: String = ""
        CoroutineScope(Dispatchers.IO).launch {
            getUserData(userName)
        }
        db.collection("users").document(email).get().addOnSuccessListener { uData ->
            uImage = uData.data?.get("imageUrl").toString()
        }.addOnCompleteListener {
            val executor = Executors.newSingleThreadExecutor()
            val handler = Handler(Looper.getMainLooper())
            var image: Bitmap? = null

            executor.execute {
                val imageURL = uImage
                try {
                    val `in` = java.net.URL(imageURL).openStream()
                    image = BitmapFactory.decodeStream(`in`)

                    handler.post {
                        userProfile.setImageBitmap(image)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

//        editProfile.setOnClickListener {
//            Toast.makeText(context, "Change profile image", Toast.LENGTH_SHORT).show()
//        }


        logout.setOnClickListener {
            Toast.makeText(context, "Logged Out", Toast.LENGTH_SHORT).show()
            Intent(context, LocationService::class.java).apply {
                action = LocationService.ACTION_STOP
                context?.startService(this)
            }
            CoroutineScope(Dispatchers.IO).launch {
                SharedPref.putBoolean(PrefConstants.IS_USER_LOGGED_IN, false)
                FirebaseAuth.getInstance().signOut()
                insertUserData()
                val intent = Intent(activity, SplashScreen::class.java).apply {
                    action = LocationService.ACTION_STOP
                }
                startActivity(intent)
                activity?.finish()
            }
        }

    }

    private fun getUserData(userName: TextView?) {
        val name = database.userDao().getLiveStatus()
        userName?.text = name[0].name
    }

    private fun insertUserData() {
        val userModel = UserModel()
        userModel.liveStatus = false
        userModel.live = "liveStatus"
        database.userDao().saveLiveStatus(userModel)
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }
}