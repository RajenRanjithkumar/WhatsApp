package com.example.whatsapp.AdapterClasses

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.whatsapp.*
import com.example.whatsapp.ModelClases.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.reflect.Array.get
import java.nio.file.Paths.get

class ChatsAdapter(
    mContext: Context,
    mChatList: List<Chat>,
    imageUrl: String
    ) : RecyclerView.Adapter<ChatsAdapter.ViewHolder?>()
{

    private val mContext: Context
    private val mChatList: List<Chat>
    private val imageUrl: String
    var firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    init {
        this.mContext = mContext
        this.mChatList = mChatList
        this.imageUrl = imageUrl
    }


    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder
    {
        // message_item_left  position == 0
        // message_item_Right  position == 1
        return if (position == 1){
            //val view: View = LayoutInflater.from(mContext).inflate(com.example.whatsapp.R.layout.message_item_left,parent,false)
            val view: View = LayoutInflater.from(mContext).inflate(R.layout.message_item_right,parent,false)
            ViewHolder(view)
        }else
        {
            //val view: View = LayoutInflater.from(mContext).inflate(com.example.whatsapp.R.layout.message_item_left,parent,false)
            val view: View = LayoutInflater.from(mContext).inflate(R.layout.message_item_left,parent,false)
            ViewHolder(view)
        }
    }

    override fun getItemCount(): Int {

        //!!  added coz of error
        return mChatList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var profile_image: CircleImageView?= null
        var show_text_message: TextView?=null
        var left_image_view: ImageView?=null
        var text_seen: TextView?=null
        var right_image_view: ImageView?=null

        init
        {
            profile_image = itemView.findViewById(R.id.profile_image)
            show_text_message = itemView.findViewById(R.id.show_text_message)
            left_image_view = itemView.findViewById(R.id.left_image_view)
            text_seen = itemView.findViewById(R.id.text_seen)
            right_image_view = itemView.findViewById(R.id.right_image_view)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val chat : Chat = mChatList[position]

        Picasso.get().load(imageUrl).into(holder.profile_image)
        //Picasso.get().load(chat.getUrl()).placeholder(R.drawable.profie).into(holder.profile_image)
        //condition to check is a message is a text or image
        //image messages

        if (chat.getMessage().equals("sent you an image.") ||!chat.getUrl().equals(""))
        {
            // image message - right side
            // .equals() changed to ==
            if (chat.getSender().equals(firebaseUser!!.uid))
            {

                holder.show_text_message!!.visibility == View.GONE
                holder.right_image_view!!.visibility == View.VISIBLE

                Picasso.get().load(chat.getUrl()).placeholder(R.drawable.profie).into(holder.right_image_view)

                holder.right_image_view!!.setOnClickListener {

                    // to display options
                    val options = arrayOf<CharSequence>(
                        "view Full Image",
                        "Delete Image",
                        "cancel"
                    )
                    var builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("To:")
                    builder.setItems(options, DialogInterface.OnClickListener{
                        dialog, which->
                        if ( which == 0)
                        {
                            val intent = Intent(mContext, ViewFullImageActivity::class.java)
                            intent.putExtra("url", chat.getUrl())
                            mContext.startActivity(intent)

                        }else
                            if (which == 1)
                            {
                                deleteSentMessage(position, holder)
                            }
                    })
                    builder.show()
                }

            }else
            // image message - left side
            // !.equals() changed to !=
                if (!chat.getSender().equals(firebaseUser!!.uid))
                {

                    holder.show_text_message!!.visibility == View.GONE
                    holder.left_image_view!!.visibility == View.VISIBLE
                    Picasso.get().load(chat.getUrl()).placeholder(R.drawable.profie).into(holder.left_image_view)


                    holder.left_image_view!!.setOnClickListener {

                        // to display options
                        val options = arrayOf<CharSequence>(
                            "view Full Image",
                            "Cancel"
                        )
                        var builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                        builder.setTitle("To?")
                        builder.setItems(options, DialogInterface.OnClickListener{
                                dialog, which->
                            if ( which == 0)
                            {
                                val intent = Intent(mContext, ViewFullImageActivity::class.java)
                                intent.putExtra("url", chat.getUrl())
                                mContext.startActivity(intent)
                            }
                        })
                        builder.show()
                    }
                }
        }else
        //text messages
        {
            holder.show_text_message!!.text = chat.getMessage()

            // delete message
            // if you want to delete the receiver message remove the condition
            if (firebaseUser!!.uid == chat.getSender())
            {
                holder.show_text_message!!.setOnClickListener {

                    // to display options
                    val options = arrayOf<CharSequence>(
                        "Delete Text",
                        "Cancel"
                    )
                    var builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("To?")
                    builder.setItems(options, DialogInterface.OnClickListener{
                            dialog, which->
                        if ( which == 0)
                        {
                            deleteSentMessage(position, holder)
                        }
                    })
                    builder.show()
                }
            }


        }


        // sent and seen message

        if(position.equals(mChatList.size-1))
        {
            if (chat.isIsSeen())
            {
                holder.text_seen!!.text = "Seen"
                if (chat.getMessage().equals("sent you an image.") && !chat.getUrl().equals(""))
                {
                    // to move the seen text to bottom of the image
                    val lp: RelativeLayout.LayoutParams? = holder.text_seen!!.layoutParams as RelativeLayout.LayoutParams?
                    lp!!.setMargins(0,245, 10, 0 )
                    holder.text_seen!!.layoutParams = lp
                }
            }else
            {
                holder.text_seen!!.text = "Sent"
                if (chat.getMessage().equals("sent you an image.") && !chat.getUrl().equals(""))
                {
                    // added.
                    // to move the seen text to bottom of the image
                    val lp: RelativeLayout.LayoutParams? = holder.text_seen!!.layoutParams as RelativeLayout.LayoutParams?
                    lp!!.setMargins(0,245, 10, 0 )
                    holder.text_seen!!.layoutParams = lp
                }
            }

        }
        else
        {
            holder.text_seen!!.visibility = View.GONE
        }


    }

    override fun getItemViewType(position: Int): Int
    {



        return if (mChatList[position].getSender().equals(firebaseUser!!.uid))
        {
            // refer onCreateViewHolder method
            // sender = userid then update in message_item_right
            1
        }else
        {
            //else then update in message_item_right(receiver)
            0
        }

    }

    // delete msg feature
    private fun deleteSentMessage(position: Int, holder: ChatsAdapter.ViewHolder)
    {
        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
            .child(mChatList.get(position).getMessageId()!!)
            .removeValue()
            .addOnCompleteListener { task ->
                if(task.isSuccessful)
                {
                    Toast.makeText(holder.itemView.context, "Deleted", Toast.LENGTH_SHORT).show()
                }else
                {
                    Toast.makeText(holder.itemView.context, "Failed, not deleted", Toast.LENGTH_SHORT).show()
                }

            }
    }

}