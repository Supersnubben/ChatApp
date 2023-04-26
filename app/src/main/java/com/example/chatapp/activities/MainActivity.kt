package com.example.chatapp.activities

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatapp.R
import com.example.chatapp.databinding.ActivityMainBinding
import com.example.chatapp.utilities.Constants
import com.example.chatapp.utilities.PreferenceManager
import android.util.Base64
import android.widget.Toast
import com.example.chatapp.adapters.RecentConversationsAdapter
import com.example.chatapp.models.ChatMessage
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity()
{

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var conversations: List<ChatMessage>
    private lateinit var conversationsAdapter: RecentConversationsAdapter
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        init()
        loadUserDetails()
        getToken()
        setListeners()
    }

    private fun init()
    {
        conversations = ArrayList()
        conversationsAdapter = RecentConversationsAdapter(conversations)
        binding.conversationsRecyclerView.adapter = conversationsAdapter
        database = FirebaseFirestore.getInstance()
    }

    private fun setListeners()
    {
        binding.imageSignOut.setOnClickListener {
            signOut()
        }
        binding.fabNewChat.setOnClickListener {
            startActivity(Intent(applicationContext, UsersActivity::class.java))
        }
    }

    private fun loadUserDetails()
    {
        binding.textName.text = preferenceManager.getString(Constants.KEY_NAME)
        val bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.size)
        binding.imageProfile.setImageBitmap(bitmap)
    }

    private fun showToast(message: String)
    {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun getToken()
    {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token -> updateToken(token) }
    }

    private fun updateToken(token: String)
    {
        val database = FirebaseFirestore.getInstance()
        val documentReference = preferenceManager.getString(Constants.KEY_USER_ID)?.let {
            database.collection(Constants.KEY_COLLECTION_USERS)
                .document(it)
        }
        documentReference?.update(Constants.KEY_FCM_TOKEN, token)
            ?.addOnFailureListener { showToast("Unable to update token") }
    }

    private fun signOut()
    {
        showToast("Signing out...")
        val database = FirebaseFirestore.getInstance()
        val documentReference = preferenceManager.getString(Constants.KEY_USER_ID)?.let {
            database.collection(Constants.KEY_COLLECTION_USERS)
                .document(it)
        }
        val updates = hashMapOf<String, Any>(
            Constants.KEY_FCM_TOKEN to FieldValue.delete())

        if (documentReference != null)
        {
            documentReference.update(updates)
                .addOnSuccessListener {
                    preferenceManager.clear()
                    val intent = Intent(this, SignInActivity::class.java)
                    startActivity(intent)
                    finish();
                }
                .addOnFailureListener { showToast("unable to sign out") }
        }
    }

}