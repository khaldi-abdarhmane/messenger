package com.khaldiAbadrhmane.messenger.model

import java.util.*

data class ImageMessage

    (
     val imagePath :String,
     override  val senderId:String,
     override  val recipientId:String,
     override  val date: Date,
     override val type: String=MessageType.IMAGE
    ): Messge{

        constructor():this("","","",Date())

    }

