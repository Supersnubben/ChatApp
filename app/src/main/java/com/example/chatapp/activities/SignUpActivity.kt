package com.example.chatapp.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatapp.R
import com.example.chatapp.databinding.ActivitySignInBinding
import com.example.chatapp.databinding.ActivitySignUpBinding
import android.content.Intent
import androidx.activity.OnBackPressedDispatcher

@SuppressLint("StaticFieldLeak")
private lateinit var binding: ActivitySignUpBinding
@Suppress("DEPRECATION")
class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
    }

    private fun setListeners() {
        binding.textSignIn.setOnClickListener { onBackPressed() }
    }
}