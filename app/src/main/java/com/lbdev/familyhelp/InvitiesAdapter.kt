package com.lbdev.familyhelp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class InvitiesAdapter(
    private val listInvites: List<String>,
    private val onActionClick: OnActionClick
) :
    RecyclerView.Adapter<InvitiesAdapter.ViewHolder>() {

    class ViewHolder(private val item: View) : RecyclerView.ViewHolder(item) {
        val email = item.findViewById<TextView>(R.id.inviteEmail)
        val accept = item.findViewById<TextView>(R.id.inviteAccept)
        val reject = item.findViewById<TextView>(R.id.inviteReject)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val item = inflater.inflate(R.layout.invities_check, parent, false)
        return ViewHolder(item)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listInvites[position]
        holder.email.text = item
        holder.accept.setOnClickListener {
            onActionClick.onAcceptClick(item)
        }

        holder.reject.setOnClickListener {
            onActionClick.onRejectClick(item)
        }
    }

    override fun getItemCount(): Int {
        return listInvites.size
    }

    interface OnActionClick {

        fun onAcceptClick(email: String)

        fun onRejectClick(email: String)

    }

}