package com.khaldiAbadrhmane.messenger

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.storage.FirebaseStorage
import com.khaldiAbadrhmane.messenger.glide.GlideApp
import com.khaldiAbadrhmane.messenger.model.User
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_main.*

class ChatActivity : AppCompatActivity() {

    private val storageInstance by lazy {

        FirebaseStorage.getInstance()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }else{
            window.statusBarColor= Color.WHITE
        }
        imageView_back.setOnClickListener {

            finish()
        }

      //  val username=
        val a=intent.getStringExtra("username")
        val profileImage= intent.getStringExtra("profile_image")

        textView_amie.text = a
        if (profileImage!!.isNotEmpty()){
            GlideApp.with(this)
                .load(storageInstance.getReference(profileImage!!))
                .placeholder(R.drawable.ic_baseline_fingerprint_24)
                .into(image_amie_chat)
        }else{
            image_amie_chat.setImageResource(R.drawable.ic_baseline_fingerprint_24)
        }



    }


}