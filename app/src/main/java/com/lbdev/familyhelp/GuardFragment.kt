package com.lbdev.familyhelp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class GuardFragment : Fragment(), InvitiesAdapter.OnActionClick {

    lateinit var mContext: Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sendInviteButton = requireView().findViewById<Button>(R.id.btnSendInvite)
        sendInviteButton.setOnClickListener {
            sendInvite()
        }

        getInvites()
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
                    val recycler = requireView().findViewById<RecyclerView>(R.id.invite_check_rv)
                    val emptyView = requireView().findViewById<TextView>(R.id.empty_view)
                    recycler.layoutManager = LinearLayoutManager(mContext)
                    recycler.adapter = adapter

                    if (list.isEmpty()) {
                        recycler.visibility = View.GONE;
                        emptyView.visibility = View.VISIBLE;
                        Log.d("TAG", "getMembers: inside isempty")
                    }
                    else {
                        recycler.visibility = View.VISIBLE;
                        emptyView.visibility = View.GONE;
                        Log.d("TAG", "getMembers: inside else")
                    }
                }
            }
    }

    private fun sendInvite() {

        val mail = requireView().findViewById<EditText>(R.id.inviteEmailId).text.toString()

        val firestore = Firebase.firestore

        val data = hashMapOf(
            "invite_status" to 0
        )

        val senderMail = FirebaseAuth.getInstance().currentUser?.email.toString()

        firestore.collection("users")
            .document(mail)
            .collection("invites")
            .document("sham@gmail.com").set(data)
            .addOnSuccessListener {

            }
            .addOnFailureListener {

            }
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

            }
            .addOnFailureListener {

            }
    }
}