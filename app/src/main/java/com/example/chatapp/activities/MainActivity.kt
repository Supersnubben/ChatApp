package com.example.chatapp.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatapp.R
import com.example.chatapp.databinding.ActivityMainBinding
import com.example.chatapp.utilities.Constants
import com.example.chatapp.utilities.PreferenceManager
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.example.chatapp.adapters.RecentConversationsAdapter
import com.example.chatapp.listeners.ConversationListener
import com.example.chatapp.models.ChatMessage
import com.example.chatapp.models.User
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.messaging.FirebaseMessaging
import java.util.Collections

class MainActivity : AppCompatActivity(), ConversationListener {


    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var conversations: MutableList<ChatMessage>
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
        listenConversations()
    }

    private fun init()
    {
        conversations = ArrayList()
        conversationsAdapter = RecentConversationsAdapter(conversations, this)
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

    private fun listenConversations()
    {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
            .addSnapshotListener(eventListener)
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
            .addSnapshotListener(eventListener)
    }

    private val eventListener = object : com.google.firebase.firestore.EventListener<QuerySnapshot>
    {
        @SuppressLint("NotifyDataSetChanged")
        override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?)
        {
            if (error != null)
            {
                return
            }
            if (value != null)
            {
                for (documentChange in value.documentChanges)
                {
                    if (documentChange.type == DocumentChange.Type.ADDED)
                    {
                        val chatMessage = ChatMessage()
                        var senderId: String = documentChange.document.getString(Constants.KEY_SENDER_ID)!!
                        var receiverId: String = documentChange.document.getString(Constants.KEY_RECEIVER_ID)!!
                        chatMessage.senderId = senderId
                        chatMessage.receiverId = receiverId
                        if(preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId))
                        {
                            chatMessage.conversationImage = documentChange.document.getString(Constants.KEY_RECEIVER_IMAGE)!!
                            chatMessage.conversationName = documentChange.document.getString(Constants.KEY_RECEIVER_NAME)!!
                            chatMessage.conversationId = documentChange.document.getString(Constants.KEY_RECEIVER_ID)!!
                        }
                        else
                        {
                            chatMessage.conversationImage = documentChange.document.getString(Constants.KEY_SENDER_IMAGE)!!
                            chatMessage.conversationName = documentChange.document.getString(Constants.KEY_SENDER_NAME)!!
                            chatMessage.conversationId = documentChange.document.getString(Constants.KEY_SENDER_ID)!!
                        }
                        chatMessage.message = documentChange.document.getString(Constants.KEY_LAST_MESSAGE)!!
                        chatMessage.dateObject = documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!
                        conversations.add(chatMessage)
                    }
                    else if(documentChange.type == DocumentChange.Type.MODIFIED)
                    {
                        for(i in 0 until conversations.size)
                        {
                            var senderId: String = documentChange.document.getString(Constants.KEY_SENDER_ID)!!
                            var receiverID: String = documentChange.document.getString(Constants.KEY_RECEIVER_ID)!!
                            if(conversations[i].senderId.equals(senderId) && conversations[i].receiverId.equals(receiverID))
                            {
                                conversations[i].message = documentChange.document.getString(Constants.KEY_LAST_MESSAGE)!!
                                conversations[i].dateObject = documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!
                                break
                            }
                        }
                    }
                }
                conversations.sortedWith(compareBy { it.dateObject } )
                conversationsAdapter.notifyDataSetChanged()
                binding.conversationsRecyclerView.smoothScrollToPosition(0)
                binding.conversationsRecyclerView.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            }
        }
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

    override fun onConversationClicked(user: User)
    {
        var intent = Intent(applicationContext, ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER, user)
        startActivity(intent)
    }
}