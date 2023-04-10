package com.example.chatapp.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatapp.R
import com.example.chatapp.databinding.ActivitySignInBinding
import com.example.chatapp.databinding.ActivitySignUpBinding
import android.content.Intent
import android.util.Patterns
import android.widget.Toast
import androidx.activity.OnBackPressedDispatcher

@SuppressLint("StaticFieldLeak")
private lateinit var binding: ActivitySignUpBinding
private lateinit var encodedImage: String
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
        binding.buttonSignUp.setOnClickListener {
            if (isValidSignUpDetails()) {
                signUp()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun signUp() {

    }

    private fun isValidSignUpDetails(): Boolean {
        if(encodedImage == null) {
            showToast("Select profile image")
            return false
        }
        else if (binding.inputName.text.toString().trim().isEmpty()) {
            showToast("Enter name")
            return false
        }
        else if (binding.inputEmail.text.toString().trim().isEmpty()) {
            showToast("Enter email")
            return false
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString()).matches()) {
            showToast("Enter valid image")
            return false
        }
        else if (binding.inputPassword.text.toString().trim().isEmpty()) {
            showToast("Enter password")
            return false
        }
        else if (binding.inputConfirmPassword.text.toString().trim().isEmpty()) {
            showToast("Confirm your password")
            return false
        }
        else if (binding.inputPassword.text.toString() != binding.inputConfirmPassword.text.toString()) {
            showToast("Password must be the same in both fields")
            return false
        }
        else {
            return true
        }
    }
}