package com.example.whatsapp.ModelClases

class Chatlist {
    // we are using the same string name as we defined in MessageChatActivity
    private var id: String = ""

    constructor()

    constructor(id: String) {
        this.id = id
    }

    fun getId():String
    {
        return id
    }

    fun setId(id: String?)
    {
        this.id= id!!
    }


}