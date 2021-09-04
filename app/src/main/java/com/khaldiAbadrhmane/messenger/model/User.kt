package com.khaldiAbadrhmane.messenger.model

data class User(val name:String,val pathimage:String){

    constructor():this("","")
    constructor(nam: String) : this(nam,"")
}