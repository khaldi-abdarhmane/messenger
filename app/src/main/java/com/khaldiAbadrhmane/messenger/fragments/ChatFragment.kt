package com.khaldiAbadrhmane.messenger.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.khaldiAbadrhmane.messenger.ChatActivity
import com.khaldiAbadrhmane.messenger.ProfileActivity
import com.khaldiAbadrhmane.messenger.R
import com.khaldiAbadrhmane.messenger.model.User
import com.khaldiAbadrhmane.messenger.recyclerview.ChatItem
import com.xwray.groupie.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_chat.*


class ChatFragment : Fragment() {


    private val mAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    val firestoreInstance: FirebaseFirestore by lazy {

        FirebaseFirestore.getInstance()
    }
    private lateinit var chatSection: Section




    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val textViewTitle= activity?.findViewById(R.id.Toolbar_main_text) as TextView
        textViewTitle.text="Chats"

        val circleImageView_profile_image = activity?.findViewById(R.id.id_circleImageView_profile_image) as ImageView

        circleImageView_profile_image.setOnClickListener {

            startActivity(Intent(activity, ProfileActivity::class.java))
            requireActivity().finish()

        }

        // Inflate the layout for this fragment

        //listening of chats ...........
        addChatListener(::initRecyclerView)
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    private fun addChatListener( onListen:(List<com.xwray.groupie.kotlinandroidextensions.Item>) -> Unit ):ListenerRegistration {

        return  firestoreInstance.collection("users")
                .document("user:"+ mAuth.currentUser!!.uid)
                .collection("chatChannelSet")
                .addSnapshotListener {
            value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            val items= mutableListOf<com.xwray.groupie.kotlinandroidextensions.Item>()
            value!!.documents.forEach  {document ->
                val s=document.id

                if (document.exists()) {
                    items.add(ChatItem(s, document.toObject(User::class.java)!!, requireActivity()))
                }

            }
            onListen(items)


        }

    }

    private fun initRecyclerView(items:List<com.xwray.groupie.kotlinandroidextensions.Item>){
        chat_recyclerView.apply {
            layoutManager=LinearLayoutManager(activity)
            adapter=GroupAdapter<ViewHolder>().apply {
                chatSection = Section(items)
                add(chatSection)
                setOnItemClickListener(onItem)
            }



        }
    }
    private val onItem =OnItemClickListener{ item,view ->

        if(item is ChatItem){


            val intentChatActivity=Intent(activity, ChatActivity::class.java)
            intentChatActivity.putExtra("username", item.user.name)
            intentChatActivity.putExtra("profile_image", item.user.pathimage)
            intentChatActivity.putExtra("uid",item.uid)
            requireActivity().startActivity(intentChatActivity)

        }


    }

}