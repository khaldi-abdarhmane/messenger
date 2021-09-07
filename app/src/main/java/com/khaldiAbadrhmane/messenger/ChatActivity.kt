package com.khaldiAbadrhmane.messenger

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.khaldiAbadrhmane.messenger.glide.GlideApp
import com.khaldiAbadrhmane.messenger.model.TextMessage
import com.khaldiAbadrhmane.messenger.recyclerview.TextMessageitem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class ChatActivity : AppCompatActivity() {


    private val mAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    val firestoreInstance: FirebaseFirestore by lazy {

        FirebaseFirestore.getInstance()
    }
    private val storageInstance by lazy {

        FirebaseStorage.getInstance()

    }
    private val chatChannelsCollectionRef=firestoreInstance.collection("chatChannels")
    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document("users/user:${mAuth.currentUser?.uid.toString() }")
    private val currentUserStorageRef: StorageReference
        get() = storageInstance.reference.child("profile:"+mAuth.currentUser?.uid.toString())
    val mcurrentUserId= mAuth.currentUser!!.uid
    private var uid =""


    private val messageAdapter by lazy {
        GroupAdapter<ViewHolder>()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)



    /////////////////////////bestbare/////////////////////////
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }else{
            window.statusBarColor= Color.WHITE
        }
        imageView_back.setOnClickListener {

            finish()
        }
      //recycle_view

      //  val username=
        val a=intent.getStringExtra("username")
        val profileImage= intent.getStringExtra("profile_image")
         uid= intent.getStringExtra("uid")!!
        textView_amie.text = a
        if (profileImage!!.isNotEmpty()){
            GlideApp.with(this)
                .load(storageInstance.getReference(profileImage!!))
                .placeholder(R.drawable.ic_baseline_fingerprint_24)
                .into(image_amie_chat)
        }else{
            image_amie_chat.setImageResource(R.drawable.ic_baseline_fingerprint_24)
        }
        createChatChannel{
            channelId ->
            getMessages(channelId)

            send_message_imageView.setOnClickListener {



                val messageSend= TextMessage(send_message_edittext.text.toString(),mcurrentUserId,Calendar.getInstance().time)

                sendMessage(channelId,messageSend)
                send_message_edittext.setText("")


            }


        }


///////////////////////////////////////////////////////////////
        chat_recycle_activity.apply {

            adapter=messageAdapter

        }
////////////////////////////////////////////////////////////

    }

    private fun sendMessage(channelId:String,message: TextMessage) {

        chatChannelsCollectionRef.document(channelId).collection("messages").add(message)



    }

    private fun createChatChannel(onComplete:(channelId:String)-> Unit){







        firestoreInstance.collection("users")
                .document("user:"+mcurrentUserId)
                .collection("chatChannelSet")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->

                    if (document.exists()) {
                        onComplete(document["channelId"] as String)
                        return@addOnSuccessListener
                    }


                    val newChatChannel = firestoreInstance.collection("users").document()
                    firestoreInstance.collection("users")
                            .document("user:" + uid)
                            .collection("chatChannelSet")
                            .document(mcurrentUserId)
                            .set(mapOf("channelId" to newChatChannel.id))

                    firestoreInstance.collection("users")
                            .document("user:" + mcurrentUserId)
                            .collection("chatChannelSet")
                            .document(uid)
                            .set(mapOf("channelId" to newChatChannel.id))
                    onComplete(newChatChannel.id)
                }
    }



    private fun getMessages(channelId: String){

        val query = chatChannelsCollectionRef.document(channelId).collection("messages").orderBy("date",Query.Direction.DESCENDING)
        query.addSnapshotListener { value, error ->
           messageAdapter.clear()
            value!!.documents.forEach {document->
                messageAdapter.add(TextMessageitem(document.toObject(TextMessage::class.java)!!,document.id,this))

            }
        }


    }
}