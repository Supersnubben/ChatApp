package com.example.chatapp.activities


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatapp.adapters.ChatAdapter
import com.example.chatapp.databinding.ActivityChatBinding
import com.example.chatapp.models.ChatMessage
import com.example.chatapp.models.User
import com.example.chatapp.utilities.Constants
import com.example.chatapp.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Base64
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.EventListener
import java.util.HashMap
import java.util.Locale
import java.util.Objects

class ChatActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityChatBinding
    private var receiverUser: User? = null
    private lateinit var chatMessages: List<ChatMessage>
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var database: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
        loadReceiverDetails()
        init()
    }

    private fun init()
     {
        preferenceManager = PreferenceManager(applicationContext)
        chatMessages = ArrayList<ChatMessage>()
        chatAdapter = ChatAdapter(chatMessages,
            getBitmapFromEncodedString(receiverUser!!.image),
            preferenceManager.getString(Constants.KEY_USER_ID)!!
        )
        binding.chatRecyclerView.adapter = chatAdapter
        database = FirebaseFirestore.getInstance()
    }

    private fun sendMessage()
    {
        val message = HashMap<String, Any>()
        message[Constants.KEY_SENDER_ID] = preferenceManager.getString(Constants.KEY_USER_ID)!!
        message[Constants.KEY_RECEIVER_ID] = receiverUser!!.id
        message[Constants.KEY_MESSAGE] = binding.inputMessage.text.toString()
        message[Constants.KEY_TIMESTAMP] = Date()
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message)
        binding.inputMessage.text = null

    }

    private val eventListener = object : EventListener
    {
        fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?)
        {
            if (error != null)
            {
                return
            }
            if (value != null)
            {
                val count = chatMessages.size
                for (documentChange in value.documentChanges)
                {
                    val chatMessage = ChatMessage()
                    chatMessage.senderId =
                        documentChange.document.getString(Constants.KEY_SENDER_ID).toString()
                    chatMessage.receiverId =
                        documentChange.document.getString(Constants.KEY_RECEIVER_ID).toString()
                    chatMessage.message =
                        documentChange.document.getString(Constants.KEY_MESSAGE).toString()
                    chatMessage.dateTime =
                        getReadableDateTime(documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!).toString()
                    chatMessage.dateObject =
                        documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!
                    chatMessages.add(chatMessage)
                }
            }
        }
    }
    private fun getBitmapFromEncodedString(encodedImage: String): Bitmap
    {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun loadReceiverDetails()
    {
        receiverUser = intent.getSerializableExtra(Constants.KEY_USER) as? User
        binding.textName.text = receiverUser?.name
    }

    private fun setListeners()
    {
        binding.imageBack.setOnClickListener { onBackPressed() }
        binding.layoutSend.setOnClickListener { sendMessage() }
    }

    private fun getReadableDateTime(date: Date): SimpleDateFormat {
        return SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault())
    }
}