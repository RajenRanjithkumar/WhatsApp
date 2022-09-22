package com.example.whatsapp.AdapterClasses

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsapp.MessageChatActivity
import com.example.whatsapp.ModelClases.Chat
import com.example.whatsapp.ModelClases.Users
import com.example.whatsapp.R
import com.example.whatsapp.VisitUserProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

// Adapter for search fragment
class UserAdapter(
    mContext: Context,
    mUsers: List<Users>,
    isChatCheck:Boolean): RecyclerView.Adapter<UserAdapter.ViewHolder?>()
{
    private val mContext: Context
    private val mUsers: List<Users>
    private var isChatCheck:Boolean
    var lastMsg: String =""

    init {
        this.mContext=mContext
        this.mUsers=mUsers
        this.isChatCheck=isChatCheck
    }



    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        var userNameTxt: TextView
        var profileImageView: CircleImageView
        var onlineImageView: CircleImageView
        var offlineImageView: CircleImageView
        var lastMessageTxt: TextView

        init {
            userNameTxt = itemView.findViewById(R.id.username)
            profileImageView = itemView.findViewById(R.id.profile_image)
            onlineImageView = itemView.findViewById(R.id.image_online)
            offlineImageView = itemView.findViewById(R.id.image_offline)
            lastMessageTxt = itemView.findViewById(R.id.message_last)

        }


    }
    // this is where the actual inflating of the layout happens with the no users data
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.user_search_item_layout,viewGroup,false)
        return UserAdapter.ViewHolder(view)

    }
    // to get the no of users
    override fun getItemCount(): Int {
        return mUsers.size

    }
    // used to bind the user data to the user_search_item_layout
    override fun onBindViewHolder(holder: ViewHolder, i: Int)
    {
        // similar to what we did in main activity
        //each row of recycler view is assigned with user   name and pro pic
        //remove ? opt
        val user: Users = mUsers[i]

        holder.userNameTxt.text = user.getUserName()
        //placeholder(R.drawable.profie) fetches the offline image from drawables, if the online one can't be fetched.
        Picasso.get().load(user.getProfile()).placeholder(R.drawable.profie).into(holder.profileImageView)

        //to display last message
        if(isChatCheck)
        {
            retrieveLastmessage(user.getUID() , holder.lastMessageTxt)
        }else
        {
            holder.lastMessageTxt.visibility = View.GONE
        }
        //online/ offline feature
        //if (isChatCheck) {

            if (user.getStatus() == "online")
            {
                holder.onlineImageView.visibility = View.VISIBLE
                holder.offlineImageView.visibility = View.GONE
            }else
            {
                holder.onlineImageView.visibility = View.GONE
                holder.offlineImageView.visibility = View.VISIBLE
            }
        //}else
        //{
            //holder.onlineImageView.visibility = View.GONE
            //holder.offlineImageView.visibility = View.GONE
        //}

        holder.profileImageView.setOnClickListener {

            val intent = Intent(mContext, VisitUserProfileActivity::class.java)
            // ref to receiver user id
            intent.putExtra("visit_id",user.getUID())
            mContext.startActivity(intent)

        }

        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, MessageChatActivity::class.java)
            // ref to receiver user id
            intent.putExtra("visit_id",user.getUID())
            mContext.startActivity(intent)
        }



        /*holder.itemView.setOnClickListener {
            // creating a dialog box with options
            val options= arrayOf<CharSequence>(
                "Send a message",
                "Visit the Profile"
            )

            val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
            builder.setTitle("To")
            builder.setItems(options,   DialogInterface.OnClickListener { dialog, position ->
                // 0= Send a message
                // 1= Visit the Profile
                if(position == 0)
                {
                    val intent = Intent(mContext, MessageChatActivity::class.java)
                    // ref to receiver user id
                    intent.putExtra("visit_id",user.getUID())
                    mContext.startActivity(intent)
                }
                if (position == 1)
                {
                    val intent = Intent(mContext, VisitUserProfileActivity::class.java)
                    // ref to receiver user id
                    intent.putExtra("visit_id",user.getUID())
                    mContext.startActivity(intent)
                }

            })
            builder.show()
        }*/
    }

    private fun retrieveLastmessage(chatUserId: String?, lastMessageTxt: TextView)
    {
        lastMsg = "defaultMsg"

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                for (dataSnapshot in p0.children)
                {
                    val chat: Chat? = dataSnapshot.getValue(Chat::class.java)

                    if(firebaseUser!=null && chat!= null)
                    {
                        if (chat.getReceiver() == firebaseUser.uid &&
                                chat.getSender() == chatUserId ||
                                chat.getReceiver() == chatUserId && chat.getSender() ==firebaseUser.uid)
                        {
                            lastMsg = chat.getMessage()!!
                        }
                    }
                }
                when(lastMsg)
                {
                    "defaultMsg" -> lastMessageTxt.text = "No Message"
                    "sent you an image." -> lastMessageTxt.text = "send an image"
                    else -> lastMessageTxt.text = lastMsg
                }
                lastMsg = "defaultMsg"

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }


}