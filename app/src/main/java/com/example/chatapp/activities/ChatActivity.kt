package com.example.chatapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatapp.R
import com.example.chatapp.databinding.ActivityChatBinding
import com.example.chatapp.models.User
import com.example.chatapp.utilities.Constants

class ChatActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityChatBinding
    private var receiverUser: User? = null
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
        loadReceiverDetails()
    }

    private fun loadReceiverDetails()
    {
        receiverUser = intent.getSerializableExtra(Constants.KEY_USER) as? User
        binding.textName.text = receiverUser?.name
    }

    private fun setListeners()
    {
        binding.imageBack.setOnClickListener { onBackPressed() }
    }
}