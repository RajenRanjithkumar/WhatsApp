package com.example.whatsapp

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsapp.AdapterClasses.ChatsAdapter
import com.example.whatsapp.Fragments.APIService
import com.example.whatsapp.ModelClases.Chat
import com.example.whatsapp.ModelClases.Users
import com.example.whatsapp.Notifications.*
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message_chat.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MessageChatActivity : AppCompatActivity() {

    // userIdVisit is the receiver user id
    var userIdVisit: String=""
    var firebaseUser: FirebaseUser? =null
    var chatsAdapter: ChatsAdapter? = null
    // Chat folder contains all the messages with the id stored in the DB.
    var mChatList : List<Chat>? = null
    private lateinit var recycler_view_chats: RecyclerView
    var reference: DatabaseReference? = null

    var notify = false
    var apiService: APIService? = null

    var user: Users? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)

        // to include a back button and its action
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_message_chat)
        setSupportActionBar(toolbar)
        supportActionBar!!.title=""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //recycler_view_chats.adapter = chatsAdapter
        toolbar.setNavigationOnClickListener {


            // from main activity you wont be addressed back to welcome or login activity unless you click on sign out

            finish()
        }

        apiService = Client.Client.getClient("https://fcm.googleapis.com/")!!.create(APIService::class.java)

        intent=intent
        // to get both the sender id and the receiver id
        // visit is defined in UserAdapter
        userIdVisit = intent.getStringExtra("visit_id")
        firebaseUser = FirebaseAuth.getInstance().currentUser
        // to access the recycler view to put all the messages we stored in mChatList
        recycler_view_chats = findViewById(R.id.recycler_view_chats)
        recycler_view_chats.setHasFixedSize(true)
        // changed from var
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recycler_view_chats.layoutManager = linearLayoutManager
        recycler_view_chats.adapter = chatsAdapter


        // to fetch the receiver name and profile pic
         reference = FirebaseDatabase.getInstance().reference
            .child("Users").child(userIdVisit)
        reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot)
            {
                // to get the dp of receiver
                val user : Users? = p0.getValue(Users::class.java)
                username_mchat.text= user!!.getUserName()
                Picasso.get().load(user.getProfile()).into(profile_image_mchat)
                user_status.text = user.getStatus()
                if (user_status.text == "online")
                {
                    image_online_msg.visibility = View.VISIBLE
                }else
                    image_online_msg.visibility = View.INVISIBLE
                retrieveMessages(firebaseUser!!.uid,userIdVisit, user.getProfile())
            }

            override fun onCancelled(error: DatabaseError)
            {

            }
        })

        send_message_bt.setOnClickListener {

            notify = true
           val message = text_message.text.toString()
                if (message=="")
                {

                    Toast.makeText(this@MessageChatActivity, "Please enter some message...", Toast.LENGTH_LONG).show()
                }else
                {
                    sendMessageToUser(firebaseUser!!.uid,userIdVisit,message)
                }
                // to make the text_message edittext null once we send a text
                text_message.setText("")

        }


        attach_image_file_btn.setOnClickListener {

            notify = true
            // to send the user to mobile phone gallery
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            // to get the image from gallery
            startActivityForResult(Intent.createChooser(intent,"Pick an image"),438)


        }

        seenMessage(userIdVisit)
    }

    private fun sendMessageToUser(senderId: String, receiverId: String?, message: String)
    {
        val reference = FirebaseDatabase.getInstance().reference
        // have to create unique key for each message
        val messageKey = reference.push().key
        //to create a hash map
        val messageHashMap = HashMap<String,Any?>()
        messageHashMap["sender"] = senderId
        messageHashMap["message"] = message
        messageHashMap["receiver"] = receiverId
        messageHashMap["isseen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageId"] = messageKey
        // this creates a Chat folder in DB and places all the child of messageHashMap under it
        reference.child("Chats")
            .child(messageKey!!)
            .setValue(messageHashMap)
            .addOnCompleteListener { task ->
                // to add the chats to Chats fragment for both sender and the receiver
                if(task.isSuccessful)
                {
                    // this is to retrieve the last message for each user (unread message)
                    // for sender add the receiver to his chat fragment
                    val chatListReference = FirebaseDatabase.getInstance()
                        .reference
                        .child("ChatList")
                        .child(firebaseUser!!.uid)
                        .child(userIdVisit)
                    chatListReference.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(p0: DataSnapshot) {

                            if (!p0.exists())
                            {
                                chatListReference.child("id").setValue(userIdVisit)
                            }

                            // for receiver add the sender to his chat fragment
                            val chatListReceiverRef = FirebaseDatabase.getInstance()
                                .reference
                                .child("ChatList")
                                .child(userIdVisit)
                                .child(firebaseUser!!.uid)
                            chatListReceiverRef.child("id").setValue(firebaseUser!!.uid)

                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })

                }
            }
        // to implement the push notifications
        val usersReference = FirebaseDatabase.getInstance().reference
            .child("Users").child(firebaseUser!!.uid)

        usersReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot)
            {
                val user = p0.getValue(Users::class.java)
                if (notify)
                {
                    sendNotification(receiverId, user!!.getUserName(), message)
                }
                notify = false

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }

    private fun sendNotification(receiverId: String?, userName: String?, message: String) {

        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")

        val query = ref.orderByKey().equalTo(receiverId)
        query.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot)
            {
                for (dataSnapshot in p0.children)
                {
                    val token: Token? = dataSnapshot.getValue(Token::class.java)
                    // or ic launcher
                    val data = Data(firebaseUser!!.uid,
                        R.mipmap.ic_launcher_round,
                    "$userName: $message",
                    "New Message",
                    userIdVisit)
                    val sender = Sender(data!!, token!!.getToken().toString())

                    apiService!!.sendNotification(sender)
                        .enqueue(object : Callback<MyResponse>
                        {
                            override fun onResponse(
                                call: Call<MyResponse>,
                                response: Response<MyResponse>
                            )
                            {
                                    if (response.code() == 200)
                                    {
                                        if (response.body()!!.success !=1)
                                        {
                                            Toast.makeText(this@MessageChatActivity, "Failed Nothing happened", Toast.LENGTH_LONG).show()
                                        }
                                    }

                            }

                            override fun onFailure(call: Call<MyResponse>, t: Throwable) {

                            }

                        })
                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode==438 && resultCode == RESULT_OK && data!=null && data!!.data!=null)
        {
            // to display a loading bar
            val progressBar = ProgressDialog(this)
            progressBar.setMessage("sending image...")
            progressBar.show()

            val fileUri = data.data
            // to send the image to a folder Chat Images in DB so that it can be send to the receiver
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            //unique id for each image like we did for text
            val messageId = ref.push().key
            val filePath = storageReference.child("$messageId.jpg")

            var uploadTask : StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot,Task<Uri>> {task ->
                // not
                if (!task.isSuccessful)
                {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener { task ->
                if(task.isSuccessful)
                {
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    val messageHashMap = HashMap<String,Any?>()
                    messageHashMap["sender"] = firebaseUser!!.uid
                    messageHashMap["message"] = "sent you an image."
                    messageHashMap["receiver"] = userIdVisit
                    messageHashMap["isseen"] = false
                    messageHashMap["url"] = url
                    messageHashMap["messageId"] = messageId
                    // all the above are Chats folder children in DB
                    ref.child("Chats").child(messageId!!).setValue(messageHashMap)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful)
                            {
                                progressBar.dismiss()
                                // to implement the push notifications
                                val reference = FirebaseDatabase.getInstance().reference
                                    .child("Users").child(firebaseUser!!.uid)

                                reference.addValueEventListener(object : ValueEventListener{
                                    override fun onDataChange(p0: DataSnapshot)
                                    {
                                        val user = p0.getValue(Users::class.java)
                                        if (notify)
                                        {
                                            sendNotification(userIdVisit, user!!.getUserName(), "sent you an image.")
                                        }
                                        notify = false

                                    }

                                    override fun onCancelled(p0: DatabaseError) {

                                    }
                                })
                            }
                        }

                    //
                    // these info's are fetched using the Chat model class

                }
            }
        }
    }

    private fun retrieveMessages(senderId: String, receiverId: String?, receiverImageUrl: String?)
    {
        // to store all the messages in mChatlist
        mChatList = ArrayList()
        // to retrieve all the messages under Chats folder in DB
        val reference = FirebaseDatabase.getInstance().reference
            .child("Chats")

         reference.addValueEventListener(object : ValueEventListener{
             override fun onDataChange(p0: DataSnapshot)
             {
                 (mChatList as ArrayList<Chat>).clear()
                 for(snapshot in p0.children)
                 {
                    val chat = snapshot.getValue(Chat::class.java)
                     if (chat!!.getReceiver().equals(senderId) && chat.getSender().equals(receiverId)
                         || chat.getReceiver().equals(receiverId)  && chat.getSender().equals(senderId)
                     )
                     {
                         (mChatList as ArrayList<Chat>).add(chat)
                     }
                     chatsAdapter = ChatsAdapter(this@MessageChatActivity,  (mChatList as ArrayList<Chat>), receiverImageUrl!!)
                     // after this we have to display the messages in the recycler view on activity_message_chat
                     recycler_view_chats.adapter = chatsAdapter
                     //chatsAdapter?.notifyDataSetChanged()
                 }
             }

             override fun onCancelled(p0: DatabaseError) {

             }
         })
    }
    // to implement seen message function

    var seenListener : ValueEventListener? = null
    private fun seenMessage(userId: String)
    {
        val reference = FirebaseDatabase.getInstance().reference
            .child("Chats")

        seenListener = reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot)
            {
                for (dataSnapshot in p0.children)
                {
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if (chat!!.getReceiver().equals(firebaseUser!!.uid) && chat!!.getSender().equals(userId))
                    {
                        // updating the Chats hashmap isseen value
                        val hashMap = HashMap<String, Any>()
                        hashMap["isseen"]= true
                        dataSnapshot.ref.updateChildren(hashMap)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    override fun onPause() {
        super.onPause()
        updateStatus("offline")
        reference!!.removeEventListener(seenListener!!)
    }

    //online/offline feature
    private fun updateStatus(status: String)
    {
        val ref = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        val hashMap = HashMap<String , Any>()
        // updating the status value in User node in DB
        hashMap["status"] = status
        ref!!.updateChildren(hashMap)
    }

    override fun onResume() {
        super.onResume()

        updateStatus("online")
    }




}
