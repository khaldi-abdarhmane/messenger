package com.khaldiAbadrhmane.messenger

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

import com.khaldiAbadrhmane.messenger.glide.GlideApp
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.ByteArrayOutputStream
import java.util.*

class ProfileActivity : AppCompatActivity() {


    private val mAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    val firestoreInstance: FirebaseFirestore by lazy {

        FirebaseFirestore.getInstance()
    }
    private val storageInstance by lazy {

        FirebaseStorage.getInstance()

    }
    private val currentUserDocRef: DocumentReference
    get() = firestoreInstance.document("users/user:${mAuth.currentUser?.uid.toString() }")
    private val currentUserStorageRef: StorageReference
    get() = storageInstance.reference.child("profile:"+mAuth.currentUser?.uid.toString())
    companion object{
        val RC_SELECT_IMAGE =2
    }
    private lateinit var userName:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setSupportActionBar(profile_toolbar)
        supportActionBar?.title="Me"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        imageViewProfile.setImageResource(R.drawable.ic_account_circle_24)
        imageViewProfile.setOnClickListener {

            val myIntentimage= Intent().apply {
                type="image/*"
                action=Intent.ACTION_GET_CONTENT
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg","image/png"))

            }
            startActivityForResult(Intent.createChooser(myIntentimage,"select Image"), RC_SELECT_IMAGE)


        }
        getUserInfo {
            user ->
            userName= user.name
            textViewProfile.text=user.name
            if (user.pathimage.isNotEmpty()) {
                GlideApp.with(this)
                        .load(storageInstance.getReference(user.pathimage))
                        .placeholder(R.drawable.ic_baseline_fingerprint_24)
                        .into(imageViewProfile)
            }else{
                imageViewProfile.setImageResource(R.drawable.ic_baseline_fingerprint_24)
            }

        }

        btn.setOnClickListener {


            mAuth.signOut()
            MainActivity().finish()
            
            val intentsignInActivity= Intent(this,SignInActivity::class.java)
            intentsignInActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intentsignInActivity)

        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                finish()
                return true
            }
        }
    return false
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SELECT_IMAGE && resultCode==Activity.RESULT_OK  && data?.data !=null ){
            progressBarprofile.visibility=View.VISIBLE
            imageViewProfile.setImageURI(data.data)
            val selectedImagePath = data.data
            val selectedImageBmp=MediaStore.Images.Media.getBitmap(this.contentResolver,selectedImagePath)
            val outputStorage= ByteArrayOutputStream()
            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG,20,outputStorage)
            val seletedImageBytes=outputStorage.toByteArray()
            uploadProfileImage(seletedImageBytes){

               path ->
                val userFieldMap= mutableMapOf<String,Any>()
                userFieldMap["name"]=userName
                userFieldMap["pathimage"]=path
                currentUserDocRef.update(userFieldMap)




           }
          
            

        }

    }
    private fun uploadProfileImage(seletedImageBytes: ByteArray, OnSuccess:(imagePath:String)->Unit ) {

        val ref= currentUserStorageRef.child("profilePictures/${UUID.nameUUIDFromBytes(seletedImageBytes)}")
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
    private fun getUserInfo(onComplete:(com.khaldiAbadrhmane.messenger.model.User) -> Unit ){
    currentUserDocRef.get().addOnSuccessListener {

        onComplete(it.toObject(com.khaldiAbadrhmane.messenger.model.User::class.java)!!)
    }

}


}