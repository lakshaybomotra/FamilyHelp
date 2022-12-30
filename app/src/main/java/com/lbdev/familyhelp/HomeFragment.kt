package com.lbdev.familyhelp

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.log
import kotlin.math.roundToInt

class HomeFragment : Fragment() {

    lateinit var mContext: Context
    val listMembers = mutableListOf<MemberModel>()
    private lateinit var geocoder: Geocoder
    lateinit var swipeToRefreshLV: SwipeRefreshLayout

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
        geocoder = Geocoder(mContext, Locale.getDefault())
        swipeToRefreshLV = requireView().findViewById(R.id.idSwipeToRefresh)
        swipeToRefreshLV.setOnRefreshListener {
            swipeToRefreshLV.isRefreshing = false
//            getMembers()
            val transaction = activity?.supportFragmentManager?.beginTransaction()
            if (transaction != null) {
                transaction.replace(R.id.container, HomeFragment())
                transaction.disallowAddToBackStack()
                transaction.commit()
            }
        }

        getMembers()

        val threeDots = requireView().findViewById<ImageView>(R.id.icon_three_dots)
        threeDots.setOnClickListener {
            SharedPref.putBoolean(PrefConstants.IS_USER_LOGGED_IN, false)
            FirebaseAuth.getInstance().signOut()

            Intent(mContext, LocationService::class.java).apply {
                action = LocationService.ACTION_STOP
                mContext.startService(this)
            }
        }
    }

    private fun getMembers() {
        val recycler = requireView().findViewById<RecyclerView>(R.id.rv_members)
        val emptyViewHome = requireView().findViewById<TextView>(R.id.empty_view_home)
        val loadingView = requireView().findViewById<TextView>(R.id.loading_view)

        val listMembersCheck: ArrayList<String> = ArrayList()

        val firestore = Firebase.firestore
        val currentUser = FirebaseAuth.getInstance().currentUser
        val email = currentUser?.email.toString()

        var cuLat = 10.0
        var cuLong = 10.0

        firestore.collection("users")
            .document(email).get().addOnSuccessListener {
                cuLat = it.data?.get("lat") as Double
                cuLong = it.data?.get("long") as Double
            }

//        var lastLat: Double = 10.0
//        var lastLong: Double = 10.0

//        firestore.collection("users").document(email).collection("location").document("lastLoc").get().addOnSuccessListener { document ->
//            if (document.data != null) {
//                lastLat = document.data?.get("lat") as Double
//                lastLong = document.data?.get("long") as Double
//            }
//        }.addOnSuccessListener {
//            val loc = hashMapOf(
//                "lat" to lastLat,
//                "long" to lastLong
//            )
//            firestore.collection("users").document(email).update(loc as Map<String, Any>)
//        }

        firestore.collection("users")
            .document(email)
            .collection("members").get().addOnCompleteListener {
                if (it.isSuccessful) {
                    for (item in it.result) {
                        if (item.get("invite_status") == 1L) {
                            listMembersCheck.add(item.id)
                        }
                    }

                    var i = 1
                    val length = listMembersCheck.size
                    listMembersCheck.forEach {
                        val dis = FloatArray(1)
                        firestore.collection("users")
                            .document(it).get().addOnSuccessListener { document ->
                                if (document != null) {
                                    if (document.data?.get("lat") != null) {
                                        Location.distanceBetween(
                                            cuLat,
                                            cuLong,
                                            document.data?.get("lat") as Double,
                                            document.data?.get("long") as Double,
                                            dis
                                        )
                                        if (document.data?.get("live") == true) {
                                            listMembers.add(
                                                MemberModel(
                                                    document.data?.get("name").toString(),
                                                    "Live: " + getAddress(
                                                        document.data?.get("lat"),
                                                        document.data?.get("long")
                                                    ),
                                                    document.data?.get("battery").toString()+"%",
                                                    ((dis[0] / 1000).roundToInt()).toString() + "KM",
                                                    document.data?.get("deviceNetwork").toString(),
                                                    document.data?.get("imageUrl").toString()
                                                )
                                            )
                                        } else {
                                            listMembers.add(
                                                MemberModel(
                                                    document.data?.get("name").toString(),
                                                    "Last At: " + getAddress(
                                                        document.data?.get("lat"),
                                                        document.data?.get("long")
                                                    ),
                                                    document.data?.get("battery").toString()+"%",
                                                    ((dis[0] / 1000).roundToInt()).toString() + "KM",
                                                    document.data?.get("deviceNetwork").toString(),
                                                    document.data?.get("imageUrl").toString()
                                                )
                                            )
                                        }

                                    } else {
                                        listMembers.add(
                                            MemberModel(
                                                document.data?.get("name").toString(),
                                                "User Not Sharing Location",
                                                "88%",
                                                "552M",
                                                "No",
                                                document.data?.get("imageUrl").toString()
                                            )
                                        )
                                    }
                                } else {
                                    Log.d("TAG", "No such document")
                                }
                                if (i == length + 1) {
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
                    } else {
                        recycler.visibility = View.VISIBLE
                        emptyViewHome.visibility = View.GONE
                        loadingView.visibility = View.GONE
                        Log.d("TAG", "getMembers: inside else")
                    }
                }
            }
    }

    private fun getAddress(lat: Any?, long: Any?): String {
        val address = geocoder.getFromLocation(lat as Double, long as Double, 1)
        if (address[0].subLocality == null) {
            return address[0].getAddressLine(0)
        } else {
            return address[0].subLocality
        }
    }

    companion object {

        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}