package com.khaldiAbadrhmane.messenger.model

import java.util.*

interface Messge {

    val senderId:String
    val recipientId:String
    val date: Date
    val type:String
}