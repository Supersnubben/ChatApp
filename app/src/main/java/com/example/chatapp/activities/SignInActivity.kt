package com.example.chatapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.example.chatapp.databinding.ActivitySignInBinding
import android.content.Intent
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.chatapp.utilities.Constants
import com.example.chatapp.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class SignInActivity : AppCompatActivity()
{

    private lateinit var binding: ActivitySignInBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        preferenceManager = PreferenceManager(applicationContext)
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN))
        {
            var intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
    }
    private fun setListeners()
    {
        binding.textCreateNewAccount.setOnClickListener {
            startActivity(Intent(applicationContext, SignUpActivity::class.java))
        }
        binding.buttonSignIn.setOnClickListener {
            if (isValidSignInDetails())
            {
                signIn()
            }
        }
    }

    private fun signIn()
    {
        loading(true)
        var database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
            .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.text.toString())
            .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.text.toString())
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null && task.result.documents.size > 0) {
                    var documentSnapshot = task.result.documents[0]
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                    preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.id)
                    preferenceManager.putString(Constants.KEY_NAME,
                        documentSnapshot.get(Constants.KEY_NAME) as String)
                    preferenceManager.putString(Constants.KEY_IMAGE,
                        documentSnapshot.get(Constants.KEY_IMAGE) as String)
                    var intent = Intent(applicationContext, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
                else {
                    loading(false)
                    showToast("Can't find any user with these credentials")
                }
            }
    }

    private fun loading (isLoading: Boolean)
    {
        if (isLoading)
        {
            binding.buttonSignIn.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        }
        else
        {
            binding.progressBar.visibility = View.INVISIBLE
            binding.buttonSignIn.visibility = View.VISIBLE
        }
    }

    private fun showToast (message: String)
    {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun isValidSignInDetails(): Boolean
    {
        return if (binding.inputEmail.text.toString().trim().isEmpty())
        {
            showToast("Enter email")
            false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString()).matches())
        {
            showToast("Enter valid email")
            false
        } else if (binding.inputPassword.text.toString().trim().isEmpty())
        {
            showToast("Enter password")
            false
        } else
        {
            true
        }
    }
}