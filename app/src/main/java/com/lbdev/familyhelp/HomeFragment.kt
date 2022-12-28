package com.lbdev.familyhelp

import android.app.ProgressDialog.show
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

    lateinit var mContext: Context
    val listMembers = mutableListOf<MemberModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getMembers()

//        Log.d("TAG", "onViewCreated: ${listMembers}")
//        Log.d("getMembers21", "getMembers: recycler view te ageya")
//        val adapter = MemberAdapter(listMembers)
//        val recycler = requireView().findViewById<RecyclerView>(R.id.rv_members)
//        recycler.layoutManager = LinearLayoutManager(mContext)
//        recycler.adapter = adapter
//        Log.d("getMembers21", "getMembers: recycler view khatam")


        val threeDots = requireView().findViewById<ImageView>(R.id.icon_three_dots)
        val icon = requireView().findViewById<ImageView>(R.id.icon_location)
        threeDots.setOnClickListener {
            SharedPref.putBoolean(PrefConstants.IS_USER_LOGGED_IN, false)
            FirebaseAuth.getInstance().signOut()
        }
    }

    private fun getMembers() {
        val recycler = requireView().findViewById<RecyclerView>(R.id.rv_members)
        val emptyViewHome = requireView().findViewById<TextView>(R.id.empty_view_home)
        val loadingView = requireView().findViewById<TextView>(R.id.loading_view)

        val listMembersCheck: ArrayList<String> = ArrayList()

        val firestore = Firebase.firestore

        Log.d("getMembers21", "getMembers: inside getmembers")
        firestore.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.email.toString())
            .collection("invites").get().addOnCompleteListener {
                Log.d("getMembers21", "getMembers: onCompleteListener de andar")
                if (it.isSuccessful) {
                    Log.d("getMembers21", "getMembers: 101 line")
                    for (item in it.result) {
                        if (item.get("invite_status") == 1L) {
                            listMembersCheck.add(item.id)
                        }
                    }

                    var i =1
                    val length = listMembersCheck.size
                    listMembersCheck.forEach{
                        firestore.collection("users")
                            .document(it).get().addOnSuccessListener { document ->
                                if (document != null) {
                                    listMembers.add(
                                        MemberModel(
                                            document.data?.get("name").toString(),
                                            document.data?.get("email").toString(),
                                            "58%",
                                            "552M"
                                        )
                                    )
                                } else {
                                    Log.d("TAG", "No such document")
                                }
                                if (i==length+1){
                                    val adapter = MemberAdapter(listMembers)
                                    recycler.layoutManager = LinearLayoutManager(mContext)
                                    recycler.adapter = adapter
                                }
                            }
                            .addOnFailureListener {
                                Log.d("TAG", "get failed with ")
                            }
                        i++
                    }

                    if (listMembersCheck.isEmpty()) {
                        emptyViewHome.visibility = View.VISIBLE
                        recycler.visibility = View.GONE
                        loadingView.visibility = View.GONE
                        Log.d("TAG", "getMembers: inside isempty")
                    }
                    else {
                        recycler.visibility = View.VISIBLE
                        emptyViewHome.visibility = View.GONE
                        loadingView.visibility = View.GONE
                        Log.d("TAG", "getMembers: inside else")
                    }
                }
            }
    }

    companion object {

        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}