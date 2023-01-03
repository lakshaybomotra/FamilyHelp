package com.lbdev.familyhelp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GuardFragment : Fragment(), InvitiesAdapter.OnActionClick {

    fun View.hideKeyboard() {
        val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(windowToken, 0)
    }

    lateinit var mContext: Context
    lateinit var mail: String
    lateinit var email: EditText
    lateinit var database: MyFamilyDatabase
    lateinit var recycler: RecyclerView
    lateinit var emptyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        database = MyFamilyDatabase.getDatabase(mContext)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler = requireView().findViewById(R.id.invite_check_rv)
        emptyView = requireView().findViewById(R.id.empty_view)

        val firestore = Firebase.firestore

        val sendInviteButton = requireView().findViewById<Button>(R.id.btnSendInvite)
        val greenCard = requireView().findViewById<CardView>(R.id.green_card)
        val pinkCard = requireView().findViewById<CardView>(R.id.pink_card)
        val locStatus = requireView().findViewById<TextView>(R.id.text_location_status)
        val locDes = requireView().findViewById<TextView>(R.id.text_location_des)
        val locStatusImg = requireView().findViewById<ImageView>(R.id.img_location_status)
        email = requireView().findViewById(R.id.inviteEmailId)

        CoroutineScope(Dispatchers.IO).launch {
            database.userDao().getLiveStatus().forEach {
                if (it.liveStatus){
                    locStatus.text = "LOCATION ON"
                    locDes.text = "Turn off when you want to stop sharing location with your members."
                    locStatusImg.setImageResource(R.drawable.icon_loc_on)
                }
            }
        }

        sendInviteButton.setOnClickListener {
            mail= email.text.toString()
            sendInvite(mail)
            it.hideKeyboard()
            email.isEnabled = false
        }

        pinkCard.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            Toast.makeText(context, "$uid", Toast.LENGTH_SHORT).show()
        }

        greenCard.setOnClickListener {
            if (locStatus.text.equals("LOCATION OFF")){
                CoroutineScope(Dispatchers.IO).launch {
                    insertUserData(true)
                }
                locStatus.text = "LOCATION ON"
                locDes.text = "Turn off when you want to stop sharing location with your members."
                locStatusImg.setImageResource(R.drawable.icon_loc_on)
                if (ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    val intent = Intent(mContext, LocationService::class.java).apply {
                        action = LocationService.ACTION_START
                    }
                    mContext.startForegroundService(intent)
                } else {

                    val intent = Intent(mContext, LocationService::class.java).apply {
                        action = LocationService.ACTION_START
                    }
                    mContext.startForegroundService(intent)
                }
            }
            else{
                CoroutineScope(Dispatchers.IO).launch {
                    insertUserData(false)
                }
                locStatus.text = "LOCATION OFF"
                locDes.text = "Turn on when you need to share location with your members."
                locStatusImg.setImageResource(R.drawable.icon_loc_off)
                Intent(mContext, LocationService::class.java).apply {
                    action = LocationService.ACTION_STOP
                    mContext.startService(this)
                }
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            getInvites()
        }
    }

    private suspend fun insertUserData(live: Boolean) {
        var userModel = UserModel()
        userModel.liveStatus = live
        userModel.live = "liveStatus"
        userModel.name = database.userDao().getLiveStatus()[0].name
        database.userDao().saveLiveStatus(userModel)
    }

    private fun getInvites() {
        val firestore = Firebase.firestore

        firestore.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.email.toString())
            .collection("invites").get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val list: ArrayList<String> = ArrayList()
                    for (item in it.result) {
                        if (item.get("invite_status") == 0L) {
                            list.add(item.id)
                        }
                    }

                    val adapter = InvitiesAdapter(list,this)
                    recycler.layoutManager = LinearLayoutManager(mContext)
                    recycler.adapter = adapter

                    if (list.isEmpty()) {
                        recycler.visibility = View.GONE;
                        emptyView.visibility = View.VISIBLE;
                    }
                    else {
                        recycler.visibility = View.VISIBLE;
                        emptyView.visibility = View.GONE;
                        Log.d("TAG", "getMembers: inside else")
                    }
                }
            }
    }

    private fun sendInvite(mail: String) {

//        val mail = requireView().findViewById<EditText>(R.id.inviteEmailId).text.toString()

        val firestore = Firebase.firestore

        val data = hashMapOf(
            "invite_status" to 0
        )

        val senderMail = FirebaseAuth.getInstance().currentUser?.email.toString()

        firestore.collection("users")
            .document(mail)
            .collection("invites")
            .document(senderMail).set(data)
            .addOnSuccessListener {
                firestore.collection("users")
                    .document(senderMail)
                    .collection("members")
                    .document(mail).set(data)
                email.isEnabled = true
            }
            .addOnFailureListener {

            }
        email.setText("")
        Toast.makeText(mContext, "Invite Sent", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_guard, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() = GuardFragment()
    }

    override fun onAcceptClick(email: String) {

        val firestore = Firebase.firestore

        val data = hashMapOf(
            "invite_status" to 1
        )

        val senderMail = FirebaseAuth.getInstance().currentUser?.email.toString()

        firestore.collection("users")
            .document(senderMail)
            .collection("invites")
            .document(email).set(data)
            .addOnSuccessListener {
                firestore.collection("users")
                    .document(email)
                    .collection("members")
                    .document(senderMail).set(data)
            }
            .addOnFailureListener {

            }

    }

    override fun onRejectClick(email: String) {

        val firestore = Firebase.firestore

        val data = hashMapOf(
            "invite_status" to -1
        )

        val senderMail = FirebaseAuth.getInstance().currentUser?.email.toString()

        firestore.collection("users")
            .document(senderMail)
            .collection("invites")
            .document(email).set(data)
            .addOnSuccessListener {
                firestore.collection("users")
                    .document(email)
                    .collection("members")
                    .document(senderMail).set(data)
            }
            .addOnFailureListener {

            }
    }
}