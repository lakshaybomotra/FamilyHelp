package com.lbdev.familyhelp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager

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
            MemberModel("Lakshay"),
            MemberModel("Harry"),
            MemberModel("Sakshi"),
            MemberModel("Ganesh"),
            MemberModel("Rajni")
        )

        val adapter = MemberAdapter(listMembers)

        val recycler = requireView().findViewById<RecyclerView>(R.id.rv_members)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter
    }

    companion object {

        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}