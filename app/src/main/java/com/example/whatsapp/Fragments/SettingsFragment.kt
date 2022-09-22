package com.example.whatsapp.Fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.whatsapp.ModelClases.Users
import com.example.whatsapp.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_settings.view.*


class SettingsFragment : Fragment() {
    //firebase ref to fetch cover and pro pic
    var firebaseUser: FirebaseUser?= null
    var usersReference : DatabaseReference?= null
    private val RequestCode = 438
    private var imageUri: Uri?=null
    // ref to firebase Storage
    private var storageRef: StorageReference?=null
    private var coverChecker: String = ""
    private var socialChecker: String = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_settings, container, false)
        //firebase ref to fetch cover and pro pic
        // to get the current user id
        firebaseUser = FirebaseAuth.getInstance().currentUser
        usersReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        // this creates a folder named User Images in FB storage
        storageRef = FirebaseStorage.getInstance().reference.child("User Images")

        usersReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    val user: Users?= p0.getValue(Users::class.java)
                    if(context!=null)
                    {
                        view.username_settings.text = user!!.getUserName()
                        view.status_display.text = user!!.getUserstatus()
                        Picasso.get().load(user.getProfile()).into(view.profile_image_settings)
                        Picasso.get().load(user.getCover()).into(view.cover_image_settings)
                    }

                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        //to update/upload profile pic
        view.profile_image_settings.setOnClickListener{
            pickImage()    
        }

        //to update/upload cover pic
        view.cover_image_settings.setOnClickListener{
            coverChecker="cover"
            pickImage()
        }

        view.set_facebook.setOnClickListener{
            socialChecker="facebook"
            setSocialLinks()
        }

        view.set_instagram.setOnClickListener{
            socialChecker="instagram"
            setSocialLinks()
        }

        view.set_website.setOnClickListener{
            socialChecker="website"
            setSocialLinks()
        }
        view.status_edit.setOnClickListener {
            socialChecker = "status"
            setSocialLinks()
        }
        return view
    }

    private fun setSocialLinks() {
        // to display a dilogbox
        val builder: AlertDialog.Builder=
            AlertDialog.Builder(context!!,R.style.Theme_AppCompat_DayNight_Dialog_Alert)
        val editText = EditText(context)
        if(socialChecker == "website")
        {
            builder.setTitle("Enter the Url")
        }else if(socialChecker == "status")
        {
            builder.setTitle("Enter your status")

        }else
        {
            builder.setTitle("Enter your Username")
        }

        //to define dialog box hint

        if(socialChecker == "website")
        {
            editText.hint = "like www.google.com"
        }
        else if (socialChecker == "status")
        {
            editText.hint = "Anything you like"
        }
        else
        {
            editText.hint = "like ranjithbing"
        }
        builder.setView(editText)
        builder.setPositiveButton("Update",DialogInterface.OnClickListener{
            dialog, which ->
            val str = editText.text.toString()
            if (str == "")
            {
                Toast.makeText(context,"Enter Url/Username...",Toast.LENGTH_LONG).show()
            }else
            {
                // this is where we save all the social links like we did for profile image
                saveSocialLink(str)
            }
        })
        // to include a cancel button for the created dialog
        builder.setNegativeButton("Cancel",DialogInterface.OnClickListener{
                dialog, which ->
            dialog.cancel()
        })
        builder.show()
    }

    private fun saveSocialLink(str: String) {

        // to update social links in reapective child in the firebase db
        val mapSocial = HashMap<String, Any>()

        when(socialChecker)
        {
            "facebook"->
            {
                mapSocial["facebook"]="https://m.facebook.com/$str"
            }
            "instagram"->
            {
                mapSocial["instagram"]="https://m.instagram.com/$str"
            }
            "website"->
            {
                mapSocial["website"]="https://$str"
            }
            "status"->
            {
                mapSocial["userstatus"]=str
            }
        }
        usersReference!!.updateChildren(mapSocial).addOnCompleteListener { task ->
            if (task.isSuccessful)
            {
                Toast.makeText(context,"Updated Successfully",Toast.LENGTH_LONG).show()


            }
        }

    }

    private fun pickImage() {
        // request which opes the gallery
        val intent = Intent()
        intent.type="image/*"
        intent.action=Intent.ACTION_GET_CONTENT
        startActivityForResult(intent,RequestCode)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode && resultCode == Activity.RESULT_OK && data!!.data!=null){

            imageUri=data.data
            Toast.makeText(context,"uploading...",Toast.LENGTH_LONG).show()
            uploadImageToDatabase()

        }
    }

    private fun uploadImageToDatabase() {
        //to display a progress bar
        val progressBar = ProgressDialog(context)
        progressBar.setMessage("Uploading image, please wait...")
        progressBar.show()
        if(imageUri!=null){
            // we upload an image to firebase storage and we assign the url of the new image to profile and cover child of the db
            // so that the new image can be fetched
            // we save it as a jpg file
            val fileRef = storageRef!!.child(System.currentTimeMillis().toString()+".jpg")
            var uploadTask : StorageTask<*>
            uploadTask = fileRef.putFile(imageUri!!)
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot,Task<Uri>> {task ->
                // not
                if (!task.isSuccessful)
                {
                   task.exception?.let {
                       throw it
                   }
                }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener{task ->
                if(task.isSuccessful)
                {
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()
                    //to differentiate cover from prof image
                    if(coverChecker == "cover")
                    {
                        // to update the cover url
                        val mapCoverImg = HashMap<String, Any>()
                        //cover as we defined in  Users class under modelclass directory
                        // updating the cover child with new image url in Db
                        mapCoverImg["cover"]=url
                        usersReference!!.updateChildren(mapCoverImg)
                        coverChecker=""

                    }else
                    {
                        //to update the profile url
                        val mapProfileImg = HashMap<String, Any>()
                        //profile as we defined in  Users class under modelclass directory
                        // updating the profile child with new image url in Db
                        mapProfileImg["profile"]=url
                        usersReference!!.updateChildren(mapProfileImg)
                        coverChecker=""
                    }
                    progressBar.dismiss()
                }
            }

        }
    }


}