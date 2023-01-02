package com.lbdev.familyhelp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.math.cos
import kotlin.math.ln

class MapsFragment : Fragment() {

    var hashMap: HashMap<String, LatLng> = HashMap<String, LatLng>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getMembers()
    }

    private fun getMembers() {
        val listMembersCheck: ArrayList<String> = ArrayList()

        val firestore = Firebase.firestore
        val currentUser = FirebaseAuth.getInstance().currentUser
        val email = currentUser?.email.toString()

        val fireData = firestore.collection("users")

        fireData.document(email)
            .collection("members").get().addOnSuccessListener {
                for (item in it.documents) {
                    if (item.get("invite_status") == 1L) {
                        listMembersCheck.add(item.id)
                    }
                }

                var i = 1
                listMembersCheck.forEach {
                    fireData.document(it).get().addOnSuccessListener { document ->
                        if (document != null) {
                            if (document.data?.get("lat") != null) {
                                if (document.data?.get("live") == true) {
                                    hashMap[document.data?.get("email").toString()] = LatLng(
                                        document.data!!["lat"] as Double,
                                        document.data!!["long"] as Double
                                    )
                                } else {
                                    hashMap[document.data?.get("email").toString()] = LatLng(
                                        document.data!!["lat"] as Double,
                                        document.data!!["long"] as Double
                                    )
                                }
                            } else {
//                                val locs: ArrayList<Double> = ArrayList()
//                                locs.add(0.0)
//                                locs.add(0.0)
//                                hashMap[document.data?.get("email").toString()] = LatLng(0.0,0.0)
                            }
                        } else {
                            Log.d("TAG", "No such document")
                        }
                    }.addOnCompleteListener {
                        @SuppressLint("MissingPermission")
                        val callback = OnMapReadyCallback { googleMap ->

                            for (keys in hashMap.keys) {
                                println("Element at key $keys : ${hashMap[keys]}")
                                val locat = hashMap[keys]
                                if (it.result.data?.get("email")==keys){
                                    googleMap.addMarker(
                                        MarkerOptions().position(locat!!).title("${it.result.data?.get("name")}")
                                    )
                                }
                                fireData.document(email).get().addOnSuccessListener { currentUserData ->
                                    val current = LatLng(currentUserData.data!!["lat"] as Double, currentUserData.data!!["long"] as Double)
                                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(current))
                                    getZoom(googleMap,current)
                                    googleMap.uiSettings.isZoomControlsEnabled = true;
                                    googleMap.uiSettings.isRotateGesturesEnabled = true;
                                    googleMap.uiSettings.isScrollGesturesEnabled = true;
                                    googleMap.uiSettings.isTiltGesturesEnabled = true;
                                }
                            }
                            if (ContextCompat.checkSelfPermission(
                                    requireContext(),
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                googleMap.isMyLocationEnabled = true
                            }
                        }

                        val mapFragment =
                            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
                        mapFragment?.getMapAsync(callback)
                    }
                        .addOnFailureListener {
                            Log.d("TAG", "get failed with ")
                        }
                    i++
                }
            }.addOnCompleteListener { }
    }
    private fun getZoom(googleMap: GoogleMap, latLngPoint: LatLng) {
        val metrics: DisplayMetrics = context?.resources?.displayMetrics!!
        val mapWidth: Float = metrics.widthPixels / metrics.density

        val equatorLength = 40075004
        val timeAnimationMilis = 1500
        val latitudinalAdjustment: Double = cos(Math.PI * latLngPoint.latitude / 180.0)
        val arg: Double = equatorLength * mapWidth * latitudinalAdjustment / (10000 * 256.0)
        val valToZoom: Double = ln(arg) / ln(2.0)
        Log.d("TAG", "getZoom: ${valToZoom.toFloat()}")

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngPoint, valToZoom.toFloat()), timeAnimationMilis , null)
    }
}