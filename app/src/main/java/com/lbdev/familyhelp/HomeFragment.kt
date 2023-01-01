package com.lbdev.familyhelp

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.log
import kotlin.math.roundToInt

class HomeFragment : Fragment() {

    lateinit var mContext: Context
    lateinit var adapter: MemberAdapter
    lateinit var recycler: RecyclerView
    lateinit var emptyViewHome: TextView
    lateinit var loadingView: ProgressBar

    private lateinit var geocoder: Geocoder
    lateinit var swipeToRefreshLV: SwipeRefreshLayout
    private val listMembers: ArrayList<MemberModel> = ArrayList()

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
        emptyViewHome = requireView().findViewById(R.id.empty_view_home)
        loadingView = requireView().findViewById(R.id.loading_view)
        swipeToRefreshLV = requireView().findViewById(R.id.idSwipeToRefresh)
        swipeToRefreshLV.setOnRefreshListener {
            swipeToRefreshLV.isRefreshing = false
            val transaction = activity?.supportFragmentManager?.beginTransaction()
            if (transaction != null) {
                transaction.replace(R.id.container, HomeFragment())
                transaction.disallowAddToBackStack()
                transaction.commit()
            }
        }

        adapter = MemberAdapter(listMembers)

        getMembers()
        fetchDatabaseMembers()

        recycler = requireView().findViewById(R.id.rv_members)
        recycler.layoutManager = LinearLayoutManager(mContext)
        recycler.adapter = adapter
    }

    private fun fetchDatabaseMembers() {
        val database = MyFamilyDatabase.getDatabase(mContext)
        database.memberDao().getAllMembers().observe(viewLifecycleOwner){
            listMembers.clear()
            listMembers.addAll(it)

            adapter.notifyDataSetChanged()
        }
    }

    private fun getMembers() {
        val recyclerView = requireView().findViewById<RecyclerView>(R.id.rv_members)
        val listMembersCheck: ArrayList<String> = ArrayList()

        val firestore = Firebase.firestore
        val currentUser = FirebaseAuth.getInstance().currentUser
        val email = currentUser?.email.toString()

        var cuLat = 10.0
        var cuLong = 10.0

        val fireData = firestore.collection("users")

        fireData.document(email).get().addOnSuccessListener {
            cuLat = it.data?.get("lat") as Double
            cuLong = it.data?.get("long") as Double
        }

        fireData.document(email)
            .collection("members").get().addOnCompleteListener {
                if (it.isSuccessful) {
                    for (item in it.result) {
                        if (item.get("invite_status") == 1L) {
                            listMembersCheck.add(item.id)
                        }
                    }

                    var i = 1
                    listMembersCheck.forEach {
                        val dis = FloatArray(1)
                        fireData.document(it).get().addOnSuccessListener { document ->
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
                                                document.data?.get("battery").toString() + "%",
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
                                                document.data?.get("battery").toString() + "%",
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
                                            "552KM",
                                            "No",
                                            document.data?.get("imageUrl").toString()
                                        )
                                    )
                                }
                            } else {
                                Log.d("TAG", "No such document")
                            }
                        }.addOnCompleteListener {
                            val coroutineExceptionHandler = CoroutineExceptionHandler{_, throwable ->
                                throwable.printStackTrace()
                            }
                            CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {
                                insertDatabaseMembers(listMembers)
                                fetchDatabaseMembers()
                            }
                        }
                            .addOnFailureListener {
                                Log.d("TAG", "get failed with ")
                            }
                        i++
                    }
                    if (listMembersCheck.isEmpty()) {
                        emptyViewHome.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        loadingView.visibility = View.GONE
                    } else {
                        recyclerView.visibility = View.VISIBLE
                        emptyViewHome.visibility = View.GONE
                        loadingView.visibility = View.GONE
                    }
                }
            }
    }

    private suspend fun insertDatabaseMembers(listMembers: ArrayList<MemberModel>) {
        val database = MyFamilyDatabase.getDatabase(mContext)
        database.memberDao().insertAll(listMembers)
    }

    private fun getAddress(lat: Any?, long: Any?): String {
        val address = geocoder.getFromLocation(lat as Double, long as Double, 1)
        return if (address[0].subLocality == null) {
            address[0].getAddressLine(0)
        } else {
            address[0].subLocality
        }
    }

    companion object {

        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}