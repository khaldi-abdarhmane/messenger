package com.khaldiAbadrhmane.messenger.recyclerview

import android.content.Context
import com.google.firebase.storage.FirebaseStorage

import com.khaldiAbadrhmane.messenger.R
import com.khaldiAbadrhmane.messenger.glide.GlideApp
import com.khaldiAbadrhmane.messenger.model.ImageMessage
import com.khaldiAbadrhmane.messenger.model.TextMessage
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.item_text_message.*
import kotlinx.android.synthetic.main.sendr_item_image_message.*



class SenderImageMessageItem(val imageMessage: ImageMessage,
                             val messageId:String,
                             val context: Context) : Item() {

    private val storageInstance by lazy {
        FirebaseStorage.getInstance()
    }
    override fun bind(viewHolder: ViewHolder, position: Int) {

      //  viewHolder.imageView_message_image_send=

        if (imageMessage.imagePath.isNotEmpty()) {
            GlideApp.with(context)
                    .load(storageInstance.getReference(imageMessage.imagePath))
                    .placeholder(R.drawable.ic_baseline_fingerprint_24)
                    .into(viewHolder.imageView_message_image_send)
        }else{
            viewHolder.imageView_message_image_send.setImageResource(R.drawable.ic_icons8_facebook_messenger)
        }

        viewHolder.text_view_time_image_send.text=android.text.format.DateFormat.format("hh:mm a",imageMessage.date).toString()
    }

    override fun getLayout()= R.layout.sendr_item_image_message
}