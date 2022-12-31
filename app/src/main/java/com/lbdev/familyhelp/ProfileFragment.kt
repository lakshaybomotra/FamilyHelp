package com.lbdev.familyhelp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import java.util.concurrent.Executors


class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        val editProfile = requireView().findViewById<ImageView>(R.id.ivEditProfile)
        val userName = requireView().findViewById<TextView>(R.id.tvUserName)
        val logout = requireView().findViewById<LinearLayout>(R.id.llLogout)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val email = currentUser?.email.toString()
        var uImage: String = ""
        var uName: String = "test"
        db.collection("users").document(email).get().addOnSuccessListener { uData ->
            uImage = uData.data?.get("imageUrl").toString()
            uName = uData.data?.get("name").toString()
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
                }
                catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            userName.text = uName
        }
        
        editProfile.setOnClickListener {
            Toast.makeText(context, "Change profile image", Toast.LENGTH_SHORT).show()
        }

        
        logout.setOnClickListener{
            SharedPref.putBoolean(PrefConstants.IS_USER_LOGGED_IN, false)
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(context, "$uName Logged Out", Toast.LENGTH_SHORT).show()
            val intent = Intent(activity, SplashScreen::class.java)
            startActivity(intent)
            activity?.finish()
        }

    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }
}