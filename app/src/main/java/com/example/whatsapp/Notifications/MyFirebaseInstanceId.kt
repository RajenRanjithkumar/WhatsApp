package com.example.whatsapp.Notifications

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseInstanceId : FirebaseMessagingService()
{
    override fun onNewToken(p0: String)
    {
        //to get token for each new notification
        super.onNewToken(p0)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val refreshToken = FirebaseInstanceId.getInstance().token

        if (firebaseUser != null)
        {
            updateToken(refreshToken)
        }
    }

    private fun updateToken(refreshToken: String?)
    {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        //creating a folder named Tokens in DB
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val token = Token(refreshToken!!)
        ref.child(firebaseUser!!.uid).setValue(token)

    }
}