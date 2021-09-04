package com.khaldiAbadrhmane.messenger.recyclerview

import android.content.Context
import com.google.firebase.storage.FirebaseStorage
import com.khaldiAbadrhmane.messenger.R
import com.khaldiAbadrhmane.messenger.glide.GlideApp
import com.khaldiAbadrhmane.messenger.model.User
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.recycler_view_item.*

class ChatItem(
              val uid:String,
              val user:User,
              val context: Context
               ): Item() {
    private val storageInstance by lazy {

        FirebaseStorage.getInstance()

    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.item_textView_person.text=user.name
        viewHolder.item_textView_caht_last.text="laste message ............"
        viewHolder.item_textView_date.text="12:21"
        if (user.pathimage.isNotEmpty())
        {
            GlideApp.with(context)
                    .load(storageInstance.getReference(user.pathimage))
                    .placeholder(R.drawable.ic_baseline_fingerprint_24)
                    .into(viewHolder.item_imageView_person)
        }else{
            viewHolder.item_imageView_person.setImageResource(R.drawable.ic_baseline_fingerprint_24)
        }
    }

    override fun getLayout(): Int {
        return R.layout.recycler_view_item
    }
}