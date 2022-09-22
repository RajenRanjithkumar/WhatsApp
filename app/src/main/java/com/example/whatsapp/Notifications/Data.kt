package com.example.whatsapp.Notifications

class Data {

    // use the same variable name
    private var user: String = ""
    private var icon = 0
    // body displays message in notification
    private var body: String = ""
    // title displays sender name
    private var title: String = ""
    private var sented: String = ""

    constructor(){}

    constructor(user: String, icon: Int, body: String, title: String, sented: String) {
        this.user = user
        this.icon = icon
        this.body = body
        this.title = title
        this.sented = sented
    }

    // to get the user from the DB
    fun getUser():String?{
        return user
    }

    //to assign it here
    fun setUser(user: String){
        this.user=user
    }

    // to get the user from the DB
    fun getIcon(): Int?{
        return icon
    }

    //to assign it here
    fun setIcon(icon: Int){
        this.icon = icon
    }

    // to get the user from the DB
    fun getBody():String?{
        return body
    }

    //to assign it here
    fun setBody(body: String){
        this.body = body
    }

    // to get the user from the DB
    fun getTitle():String?{
        return title
    }

    //to assign it here
    fun setTitle(title: String){
        this.title = title
    }

    // to get the user from the DB
    fun getSented() : String?{
        return sented
    }

    //to assign it here
    fun setSented(sented: String){
        this.sented = sented
    }


}