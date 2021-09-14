package com.khaldiAbadrhmane.messenger

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity.apply
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

import com.khaldiAbadrhmane.messenger.glide.GlideApp
import com.khaldiAbadrhmane.messenger.model.ImageMessage
import com.khaldiAbadrhmane.messenger.model.MessageType
import com.khaldiAbadrhmane.messenger.model.Messge
import com.khaldiAbadrhmane.messenger.model.TextMessage
import com.khaldiAbadrhmane.messenger.recyclerview.SenderImageMessageItem
import com.khaldiAbadrhmane.messenger.recyclerview.SenderImageMessageItem2
import com.khaldiAbadrhmane.messenger.recyclerview.TextMessageitem
import com.khaldiAbadrhmane.messenger.recyclerview.TextMessageitem2
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.ByteArrayOutputStream
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var  mCurrentChatChannelId:String
    private val currentImageRef: StorageReference
        get() = storageInstance.reference
    companion object{
        val RC_SELECT_IMAGE =2
    }
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
    private var muidRecipient  =""
    private val messageAdapter by lazy {
        GroupAdapter<ViewHolder>()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        textView12.visibility=View.GONE
        imageView_send_message.setOnClickListener {
            val myIntentimage= Intent().apply {
                    type="image/*"
                    action=Intent.ACTION_GET_CONTENT
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg","image/png"))
            }
                startActivityForResult(Intent.createChooser(myIntentimage,"select Image"), ChatActivity.RC_SELECT_IMAGE)
        }

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
        muidRecipient= intent.getStringExtra("uid")!!
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
            mCurrentChatChannelId=channelId
            getMessages(channelId)
            send_message_imageView.setOnClickListener {
                val text=send_message_edittext.text.toString()
                if (text.isNotEmpty()){
                    val messageSend=TextMessage( text ,
                            mcurrentUserId,
                            muidRecipient,
                            Calendar.getInstance().time)
                    sendMessage(channelId,messageSend)
                    send_message_edittext.setText("")
                }else{
                    Toast.makeText(this,"Empty",Toast.LENGTH_SHORT).show()
                }
            }
        }


///////////////////////////////////////////////////////////////
        chat_recycle_activity.apply {
            adapter=messageAdapter
        }
////////////////////////////////////////////////////////////

    }

    private fun sendMessage(channelId:String,message: Messge) {

        chatChannelsCollectionRef.document(channelId).collection("messages").add(message)



    }
    private fun createChatChannel(onComplete:(channelId:String)-> Unit){

        firestoreInstance.collection("users")
                .document("user:"+mcurrentUserId)
                .collection("chatChannelSet")
                .document(muidRecipient)
                .get()
                .addOnSuccessListener { document ->

                    if (document.exists()) {
                        onComplete(document["channelId"] as String)
                        return@addOnSuccessListener
                    }


                    val newChatChannel = firestoreInstance.collection("users").document()
                    firestoreInstance.collection("users")
                            .document("user:" + muidRecipient)
                            .collection("chatChannelSet")
                            .document(mcurrentUserId)
                            .set(mapOf("channelId" to newChatChannel.id))

                    firestoreInstance.collection("users")
                            .document("user:" + mcurrentUserId)
                            .collection("chatChannelSet")
                            .document(muidRecipient)
                            .set(mapOf("channelId" to newChatChannel.id))
                    onComplete(newChatChannel.id)
                }
    }
    private fun getMessages(channelId: String){

        val query = chatChannelsCollectionRef.document(channelId).collection("messages").orderBy("date", Query.Direction.DESCENDING)
        query.addSnapshotListener { value, error ->
           messageAdapter.clear()
            value!!.documents.forEach {document->


                if(document["type"]==MessageType.TEXT){

                    val textMessage=document.toObject(TextMessage::class.java)
                    if (textMessage?.senderId==mcurrentUserId){
                        messageAdapter.add(
                                TextMessageitem(
                                        document.toObject(TextMessage::class.java)!!,
                                        document.id,
                                        this
                                ))

                    }else{
                        messageAdapter.add(
                                TextMessageitem2(
                                        document.toObject(TextMessage::class.java)!!,
                                        document.id,
                                        this
                                ))

                    }

                }else{

                    val imageMessage=document.toObject(ImageMessage::class.java)
                    if (imageMessage?.senderId==mcurrentUserId){
                        messageAdapter.add(
                                SenderImageMessageItem(
                                        document.toObject(ImageMessage::class.java)!!,
                                        document.id,
                                        this
                                ))

                    }

                    else{
                        messageAdapter.add(
                                SenderImageMessageItem2(
                                        document.toObject(ImageMessage::class.java)!!,
                                        document.id,
                                        this
                                ))

                    }


                    }





            }
        }


    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ChatActivity.RC_SELECT_IMAGE && resultCode== Activity.RESULT_OK  && data?.data !=null ){


            val selectedImagePath = data.data
            val selectedImageBmp= MediaStore.Images.Media.getBitmap(this.contentResolver,selectedImagePath)
            val outputStorage= ByteArrayOutputStream()
            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG,25,outputStorage)
            val seletedImageBytes=outputStorage.toByteArray()

            uploadImage(seletedImageBytes){

               path ->

                val imageMessage = ImageMessage(path ,
                        mcurrentUserId,
                        muidRecipient,
                        Calendar.getInstance().time)

              //   chatChannelsCollectionRef.document(mCurrentChatChannelId).collection("messages").add(imageMessage)
                sendMessage(mCurrentChatChannelId,imageMessage)




            }



        }

    }
    private fun uploadImage(seletedImageBytes: ByteArray, OnSuccess:(imagePath:String)->Unit ) {
            progressBar12.visibility=View.VISIBLE
        val ref= currentImageRef.child("${mAuth.currentUser!!.uid}/image/send/${UUID.nameUUIDFromBytes(seletedImageBytes)}")
        ref.putBytes(seletedImageBytes).addOnCompleteListener {
            task ->
            if (task.isSuccessful){
                progressBar12.visibility=View.GONE
                OnSuccess(ref.path)



            }else{
                progressBar12.visibility=View.GONE
                textView12.visibility=View.VISIBLE
                textView12.text="ERROR : ${task.exception?.message }"
            }

        }
    }
}