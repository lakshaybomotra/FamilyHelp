package com.lbdev.familyhelp

import android.app.ProgressDialog.show
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listMembers = listOf<MemberModel>(
            MemberModel("Lakshay","9th building, 2nd floor, maldiv road, manali 9th building, 2nd floor 9th building, 2nd floor","67%","455M"),
            MemberModel("Harry","10th building, 3rd floor, maldiv road, manali","89%","605M"),
            MemberModel("Sakshi","2nd floor 9th building, 2nd floor, manali 19th building, ","79%","210M"),
            MemberModel("Ganesh","manali 19th building, 2nd floor 9th building, 2nd floor","44%","302M"),
            MemberModel("Rajni","2nd floor, maldiv road, manali 9th building, 2nd floor 9th building, 2nd floor","95%","667M")
        )

        val adapter = MemberAdapter(listMembers)
        val recycler = requireView().findViewById<RecyclerView>(R.id.rv_members)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        val threeDots = requireView().findViewById<ImageView>(R.id.icon_three_dots)
        threeDots.setOnClickListener {
            SharedPref.putBoolean(PrefConstants.IS_USER_LOGGED_IN,false)
            FirebaseAuth.getInstance().signOut()
        }
    }

    companion object {

        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}