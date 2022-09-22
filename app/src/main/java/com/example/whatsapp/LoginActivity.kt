package com.example.whatsapp

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    // ref to firebase
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val toolbar: Toolbar = findViewById(R.id.toolbar_login)
        setSupportActionBar(toolbar)
        supportActionBar!!.title="Login"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this@LoginActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        mAuth = FirebaseAuth.getInstance()

        login_btn.setOnClickListener {
            logInUser()
        }

        btn_forgot_password.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Forgot password")
            val view = layoutInflater.inflate(R.layout.dialog_forgot_password, null)
            val username = view.findViewById<EditText>(R.id.et_username)
            builder.setView(view)
            builder.setPositiveButton("Reset", DialogInterface.OnClickListener { dialogInterface, i ->

                forgotPassword(username)
            })
            builder.setNegativeButton("close", DialogInterface.OnClickListener { dialogInterface, i ->  })
            builder.show()


        }


    }

    private fun forgotPassword(username: EditText)
    {
        mAuth.sendPasswordResetEmail(username.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this,"Email sent", Toast.LENGTH_SHORT).show()
                }

            }
    }



    private fun logInUser()
    {
        val email:  String= email_login.text.toString()
        val password:  String= password_login.text.toString()
        // to null check the elements
        if(email == "")
        {
            Toast.makeText(this@LoginActivity, "Enter your email.", Toast.LENGTH_LONG).show()
        }else if(password == "")
        {
            Toast.makeText(this@LoginActivity, "Enter the password \n  you dumbass!!", Toast.LENGTH_LONG).show()
        }else
        {
            //after registering an account
            // login data will be checked with the firebaseDB
            mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)

                        // from main activity you wont be addressed back to welcome or login activity unless you click on sign out
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()

                    }else{
                        Toast.makeText(this@LoginActivity, "Error Message"+ task.exception!!
                            .message.toString(), Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}