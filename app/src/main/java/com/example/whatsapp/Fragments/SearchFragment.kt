package com.example.whatsapp.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsapp.AdapterClasses.UserAdapter
import com.example.whatsapp.ModelClases.Users
import com.example.whatsapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.

 * create an instance of this fragment.
 */
class SearchFragment : Fragment() {
    private var userAdapter: UserAdapter?=null
    private var mUsers: List<Users>? = null
    private var recyclerView: RecyclerView?=null
    private var searchEditText: EditText?=null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View= inflater.inflate(R.layout.fragment_search, container, false)
        // ref to recycler view
        recyclerView=view.findViewById(R.id.searchList)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        searchEditText=view.findViewById(R.id.searchUsersET)


        mUsers = ArrayList()
        retrieveAllUsers()
        //used to search for the user
        // this is a ref to the fragment_search.xml
        searchEditText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            // live search
            override fun onTextChanged(cs: CharSequence?, p1: Int, p2: Int, p3: Int) {

                searchForUsers(cs.toString().toLowerCase(Locale.ROOT))
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        return view
    }

    private fun retrieveAllUsers()
    {
        // ref to DB
        // this is used to authenticate into DB
        val firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
        //ref to all the users in DB
        val refUsers = FirebaseDatabase.getInstance().reference.child("Users")

        refUsers.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot)
            {
                (mUsers as ArrayList<Users>).clear()
                // this get all the users one by one that is what referred as children
                if(searchEditText!!.text.toString() == ""){

                    for (snapshot in p0.children)
                    {
                        val user: Users? =snapshot.getValue(Users::class.java)
                        // not equals
                        // this neglects our profile
                        if (!(user!!.getUID()).equals(firebaseUserID))
                        {
                            (mUsers as ArrayList<Users>).add(user)
                        }
                    }
                    // this was created after UserAdapter.kt was created
                    userAdapter = UserAdapter(context!!,mUsers!!,false)
                    // how we link the UserAdapter data to recyclerView
                    recyclerView!!.adapter=userAdapter
                    // this function displays all the users if edit text is empty else
                    // users returned by method searchForUsers is displayed, savvy?

                }

            }

            override fun onCancelled(p0: DatabaseError)
            {

            }
        })
    }

    private fun searchForUsers(str: String)
    {
        // ref to DB
        // this is used to authenticate into DB
        //changed to val
        val firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
        //modified the query so that this takes the string stored in search keyword for each user which is nothing
        // but the username to do the actual search

        val queryUsers = FirebaseDatabase.getInstance().reference
            .child("Users").orderByChild("search")
            .startAt(str)
            .endAt(str + "\uf8ff")

        queryUsers.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot)
            {
                (mUsers as ArrayList<Users>).clear()
                for (snapshot in p0.children)
                {
                    val user: Users? =snapshot.getValue(Users::class.java)
                    // not equals
                    if (!(user!!.getUID()).equals(firebaseUserID))
                    {
                        (mUsers as ArrayList<Users>).add(user)
                    }
                }
                // closest string to the keywords on search are assigned to mUser
                userAdapter = UserAdapter(context!!,mUsers!!,false)
                // how we link the UserAdapter data to recyclerView
                recyclerView!!.adapter=userAdapter


            }

            override fun onCancelled(p0: DatabaseError)
            {

            }
        })

    }


}