package com.example.whatsapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.whatsapp.ModelClases.Users
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message_chat.*
import kotlinx.android.synthetic.main.activity_visit_user_profile.*

class VisitUserProfileActivity : AppCompatActivity() {

    var userIdVisit: String = ""
    var user: Users? = null
    var reference: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_user_profile)

        // to get the receiver info
        userIdVisit = intent.getStringExtra("visit_id")

        reference = FirebaseDatabase.getInstance().reference
            .child("Users").child(userIdVisit)
        reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot)
            {
                // to get the dp of receiver
                val user : Users? = p0.getValue(Users::class.java)
                username_display.text= user!!.getUserName()
                check_fb.text = user!!.getFacebook()
                check_insta.text = user!!.getInstagram()
                check_web.text = user!!.getWebsite()
                status_display.text = user!!.getUserstatus()
                Picasso.get().load(user.getProfile()).into(profile_display)
                Picasso.get().load(user.getCover()).into(cover_display)

            }

            override fun onCancelled(error: DatabaseError)
            {

            }
        })


        facebook_display.setOnClickListener{
            val uri = Uri.parse(check_fb.text.toString())

            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)


        }

        instagram_display.setOnClickListener{
            val uri = Uri.parse(check_insta.text.toString())

            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)


        }

        website_display.setOnClickListener{
            val uri = Uri.parse(check_web.text.toString())

            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)


        }

        send_msg_bt.setOnClickListener {
            val intent = Intent(this@VisitUserProfileActivity, MessageChatActivity::class.java)
            // ref to receiver user id
            intent.putExtra("visit_id",user?.getUID())
            startActivity(intent)


        }

    }
}