package com.example.whatsapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*
import kotlin.collections.HashMap

class RegisterActivity : AppCompatActivity() {


    // ref to firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var refUsers: DatabaseReference
    private var firebaseUserID: String=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val toolbar: Toolbar = findViewById(R.id.toolbar_register)
        setSupportActionBar(toolbar)
        supportActionBar!!.title="Register"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@RegisterActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()

        register_btn.setOnClickListener {
            registerUser()
        }

    }

    private fun registerUser()
    {
        val username:  String= username_register.text.toString()
        val email:  String= email_register.text.toString()
        val password:  String= password_register.text.toString()
        // to null check the elements
        if (username == ""){
            Toast.makeText(this@RegisterActivity, "Please enter an User name.",Toast.LENGTH_LONG).show()
        }else if(email == ""){
            Toast.makeText(this@RegisterActivity, "Enter an email.",Toast.LENGTH_LONG).show()
        }else if(password == ""){
            Toast.makeText(this@RegisterActivity, "Enter a password dumbass!!.",Toast.LENGTH_LONG).show()
        }else{

            mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                    {
                        //feed the user details to firebaseDB to get it stored.
                        firebaseUserID = mAuth.currentUser!!.uid
                        //this creates a folder named Users in FB database
                        refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUserID)

                        val userHashMap = HashMap<String, Any>()
                        userHashMap["uid"]=firebaseUserID
                        userHashMap["username"]=username
                        userHashMap["profile"]="https://firebasestorage.googleapis.com/v0/b/whatsapp-a7b0b.appspot.com/o/profie.png?alt=media&token=613ccbf7-1289-4b33-a1f3-b21217fcc538"
                        userHashMap["cover"]="https://firebasestorage.googleapis.com/v0/b/whatsapp-a7b0b.appspot.com/o/cover.png?alt=media&token=9df97a9d-8e91-48b7-b636-6f3ee18a1520"
                        userHashMap["status"]="offline"
                        userHashMap["search"]=username.toLowerCase(Locale.ROOT)
                        userHashMap["facebook"]="https://m.facebook.com"
                        userHashMap["instagram"]="https://m.instagram.com"
                        userHashMap["website"]="https://www.google.com"
                        userHashMap["userstatus"]="I am a bad ass"

                        refUsers.updateChildren(userHashMap)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful){
                                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)

                                    // from main activity you wont be addressed back to welcome or login activity unless you click on sign out
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()
                                }
                            }

                    }
                    else
                    {
                        Toast.makeText(this@RegisterActivity, "Error Message"+ task.exception!!
                            .message.toString(),Toast.LENGTH_LONG).show()
                    }
                }
        }
    }






}