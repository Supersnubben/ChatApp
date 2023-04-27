package com.example.chatapp.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.databinding.ItemContainerRecentConversationsBinding
import com.example.chatapp.listeners.ConversationListener
import com.example.chatapp.models.ChatMessage
import com.example.chatapp.models.User

class RecentConversationsAdapter(private val chatMessages: List<ChatMessage>, private val conversationListener: ConversationListener) :
    RecyclerView.Adapter<RecentConversationsAdapter.ConversationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val binding = ItemContainerRecentConversationsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ConversationViewHolder(binding, conversationListener)
    }
    override fun onBindViewHolder(holder: RecentConversationsAdapter.ConversationViewHolder, position: Int) {
        holder.setData(chatMessages[position])
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    inner class ConversationViewHolder(
        private val binding: ItemContainerRecentConversationsBinding,
        private val conversationListener: ConversationListener) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(chatMessage: ChatMessage) {
            binding.imageProfile.setImageBitmap(getConversationImage(chatMessage.conversationImage))
            binding.textName.text = chatMessage.conversationName
            binding.textRecentMessage.text = chatMessage.message
            binding.root.setOnClickListener{
                val user = User()
                user.id = chatMessage.conversationId
                user.name = chatMessage.conversationName
                user.image = chatMessage.conversationImage
                conversationListener.onConversationClicked(user)
            }
        }
    }

    private fun getConversationImage(encodedImage: String): Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}