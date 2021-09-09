package com.khaldiAbadrhmane.messenger.model

import java.util.*

data class TextMessage(
                       val text:String,
                       val senderId:String,
                       val recipientId:String,
                       val date: Date

                       ){

    constructor():this("","","",Date())

}
