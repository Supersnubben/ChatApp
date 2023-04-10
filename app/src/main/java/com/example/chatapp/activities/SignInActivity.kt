package com.example.chatapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatapp.R
import com.example.chatapp.databinding.ActivitySignInBinding
import android.content.Intent
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Objects

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
    }
    private fun setListeners() {
        binding.textCreateNewAccount.setOnClickListener {
            startActivity(Intent(applicationContext, SignUpActivity::class.java))
        }
    }




}