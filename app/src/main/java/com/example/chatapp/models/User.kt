package com.example.chatapp.models

import java.io.Serializable

class User: Serializable
{
    var name: String = ""
    var image: String = ""
    var email: String = ""
    var token: String = ""
}