package com.khaldiAbadrhmane.messenger.recyclerview

import android.content.Context
import com.khaldiAbadrhmane.messenger.R
import com.khaldiAbadrhmane.messenger.model.TextMessage
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_text_message.*
import java.lang.String.format
import java.text.DateFormat
import java.text.MessageFormat.format

class TextMessageitem(val textMessage: TextMessage,
                      val messageId:String,
                      val context: Context  ) :Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.text_view_message.text=textMessage.text

        viewHolder.text_view_time.text=android.text.format.DateFormat.format("hh:mm a",textMessage.date).toString()   //textMessage.date

    }

    override fun getLayout()= R.layout.item_text_message
}