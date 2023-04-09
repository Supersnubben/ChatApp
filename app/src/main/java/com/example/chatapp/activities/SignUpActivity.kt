package com.example.chatapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatapp.R
import com.example.chatapp.databinding.ActivitySignInBinding
import com.example.chatapp.databinding.ActivitySignUpBinding
import android.content.Intent

private lateinit var binding: ActivitySignUpBinding
class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners();
    }

    private fun setListeners() {
        binding.textSignIn.setOnClickListener { onBackPressed() }
    }
}