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
import com.khaldiAbadrhmane.messenger.model.TextMessage
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
    private fun sendMessage(channelId:String,message: TextMessage) {

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

        val query = chatChannelsCollectionRef.document(channelId).collection("messages").orderBy("date",Query.Direction.DESCENDING)
        query.addSnapshotListener { value, error ->
           messageAdapter.clear()
            value!!.documents.forEach {document->

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



            }
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ChatActivity.RC_SELECT_IMAGE && resultCode== Activity.RESULT_OK  && data?.data !=null ){


            val selectedImagePath = data.data
            val selectedImageBmp= MediaStore.Images.Media.getBitmap(this.contentResolver,selectedImagePath)
            val outputStorage= ByteArrayOutputStream()
            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG,20,outputStorage)
            val seletedImageBytes=outputStorage.toByteArray()
            uploadImage(seletedImageBytes){

                path ->
                val userFieldMap= mutableMapOf<String,Any>()
                userFieldMap["name"]=userName
                userFieldMap["pathimage"]=path
                currentUserDocRef.update(userFieldMap)




            }



        }

    }

    private fun uploadImage(seletedImageBytes: ByteArray, OnSuccess:(imagePath:String)->Unit ) {

        val ref= currentImageRef.child("${mAuth.currentUser!!.uid}/image/send")
        ref.putBytes(seletedImageBytes).addOnCompleteListener {
            task ->
            if (task.isSuccessful){

                OnSuccess(ref.path)
                progressBarprofile.visibility=View.GONE


            }else{
                Toast.makeText(this,"ERROR : ${task.exception?.message }",Toast.LENGTH_LONG).show()
            }

        }
    }
}