package com.example.chatapp.activities


import android.annotation.SuppressLint
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
import android.view.View
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class ChatActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityChatBinding
    private var receiverUser: User? = null
    private lateinit var chatMessages: MutableList<ChatMessage>
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var database: FirebaseFirestore
    private var conversationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
        loadReceiverDetails()
        init()
        listenMessages()
    }

    private fun init()
     {
        preferenceManager = PreferenceManager(applicationContext)
        chatMessages = mutableListOf()
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

        if(conversationId != null)
        {
            updateConversation(binding.inputMessage.text.toString())
        }
        else
        {
            val conversation = HashMap<String, Any>()
            conversation[Constants.KEY_SENDER_ID] = preferenceManager.getString(Constants.KEY_USER_ID)!!
            conversation[Constants.KEY_SENDER_NAME] = preferenceManager.getString(Constants.KEY_NAME)!!
            conversation[Constants.KEY_SENDER_IMAGE] = preferenceManager.getString(Constants.KEY_IMAGE)!!
            conversation[Constants.KEY_RECEIVER_ID] = receiverUser!!.id
            conversation[Constants.KEY_RECEIVER_NAME] = receiverUser!!.name
            conversation[Constants.KEY_RECEIVER_IMAGE] = receiverUser!!.image
            conversation[Constants.KEY_LAST_MESSAGE] = binding.inputMessage.text.toString()
            conversation[Constants.KEY_TIMESTAMP] = Date()
            addConversation(conversation)
        }

        binding.inputMessage.text = null
    }

    private fun listenMessages()
    {
        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser!!.id)
            .addSnapshotListener(eventListener)
        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser!!.id)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
            .addSnapshotListener(eventListener)
    }

    private val eventListener = object : com.google.firebase.firestore.EventListener<QuerySnapshot>
    {
        @SuppressLint("NotifyDataSetChanged")
        override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
            if (error != null) {
                return
            }
            if (value != null) {
                val count = chatMessages.size
                for (documentChange in value.documentChanges) {
                    if (documentChange.type == DocumentChange.Type.ADDED) {
                        val chatMessage = ChatMessage()
                        chatMessage.senderId =
                            documentChange.document.getString(Constants.KEY_SENDER_ID).toString()
                        chatMessage.receiverId =
                            documentChange.document.getString(Constants.KEY_RECEIVER_ID).toString()
                        chatMessage.message =
                            documentChange.document.getString(Constants.KEY_MESSAGE).toString()
                        chatMessage.dateTime =
                            getReadableDateTime(documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!)
                        chatMessage.dateObject =
                            documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!
                        chatMessages.add(chatMessage)
                    }
                }
                chatMessages.sortedWith(compareBy { it.dateObject })
                if (count == 0) {
                    chatAdapter.notifyDataSetChanged()
                } else {
                    chatAdapter.notifyItemRangeInserted(chatMessages.size, chatMessages.size)
                    binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size - 1)
                }
                binding.chatRecyclerView.visibility = View.VISIBLE
            }
            binding.progressBar.visibility = View.GONE

            if (conversationId == null)
            {
                checkForConversation()
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
        receiverUser = intent.getSerializableExtra(Constants.KEY_USER, User::class.java)
        binding.textName.text = receiverUser?.name
    }

    private fun setListeners()
    {
        binding.imageBack.setOnClickListener { finish() }
        binding.layoutSend.setOnClickListener { sendMessage() }
    }

    private fun getReadableDateTime(date: Date): String {
        return SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date)
    }

    private fun addConversation(conversation: HashMap<String, Any>)
    {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .add(conversation)
            .addOnSuccessListener { documentReference -> conversationId = documentReference.id }
    }

    private fun updateConversation(message: String)
    {
        val documentReference: DocumentReference =
            database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversationId!!)
        documentReference.update(
            Constants.KEY_LAST_MESSAGE, message, Constants.KEY_TIMESTAMP, Date()
        )
    }

    private fun checkForConversation()
    {
        if(chatMessages.size != 0)
        {
            checkForConversationRemotely(
                preferenceManager.getString(Constants.KEY_USER_ID)!!,
                receiverUser!!.id
            )
            checkForConversationRemotely(
                receiverUser!!.id,
                preferenceManager.getString(Constants.KEY_USER_ID)!!
            )
        }
    }

    private fun checkForConversationRemotely(senderId: String, receiverId: String)
    {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
            .get()
            .addOnCompleteListener(conversationOnCompleteListener)
    }

    private val conversationOnCompleteListener = OnCompleteListener<QuerySnapshot>
    { task ->
        if (task.isSuccessful && task.result != null && task.result.documents.size > 0)
        {
            val documentSnapshot = task.result.documents[0]
            conversationId = documentSnapshot.id
        }
    }
}
