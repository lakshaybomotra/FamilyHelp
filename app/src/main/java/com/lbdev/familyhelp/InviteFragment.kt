package com.lbdev.familyhelp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class InviteFragment : BottomSheetDialogFragment() {

    lateinit var adapter: InviteAdapter
    lateinit var mContext: Context
    private val listContacts: ArrayList<ContactModel> = ArrayList()

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

        return inflater.inflate(R.layout.fragment_invite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = InviteAdapter(listContacts)
        fetchDatabaseContacts()

        CoroutineScope(Dispatchers.IO).launch {
            Log.d("fetchContacts70", "onViewCreated: insert start hoi")
            insertDatabaseContacts(fetchContacts())
            Log.d("fetchContacts70", "onViewCreated: insert khatam hoi")
        }

        val recycler = requireView().findViewById<RecyclerView>(R.id.invite_members)
        recycler.layoutManager = LinearLayoutManager(mContext,LinearLayoutManager.HORIZONTAL,false)
        recycler.adapter = adapter
    }

    private fun fetchDatabaseContacts() {
        val database = MyFamilyDatabase.getDatabase(mContext)
        database.contactDao().getAllContacts().observe(viewLifecycleOwner){

            listContacts.clear()
            listContacts.addAll(it)

            adapter.notifyDataSetChanged()

        }
    }

    private suspend fun insertDatabaseContacts(listContacts: ArrayList<ContactModel>) {
//        if (context!=null)
//        {
            val database = MyFamilyDatabase.getDatabase(mContext)
            database.contactDao().insertAll(listContacts)
//        }
    }

    @SuppressLint("Range")
    private fun fetchContacts(): ArrayList<ContactModel> {
        val cr = requireActivity().contentResolver
        val cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)

        val listContacts: ArrayList<ContactModel> = ArrayList()

        if (cursor != null && cursor.count > 0) {
            while (cursor != null && cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val name =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val hasPhoneNumber =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))

                if (hasPhoneNumber > 0.toString()) {
                    val pCur = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID+" = ?",
                        arrayOf(id),
                        ""
                    )
                    if (pCur!=null && pCur.count>0){
                        while (pCur!=null && pCur.moveToNext()){
                            val phoneNum = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            if (!listContacts.contains(ContactModel(name,phoneNum))){
                                listContacts.add(ContactModel(name,phoneNum))
                            }
                        }
                        pCur.close()
                    }
                }
            }

            if (cursor!=null){
                cursor.close()
            }
        }

        return listContacts
    }

}