package com.example.chatapp.adapters


import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.databinding.ItemContainerRecievedMessageBinding
import com.example.chatapp.databinding.ItemContainerSentMessageBinding
import com.example.chatapp.models.ChatMessage

class ChatAdapter(
    private val chatMessage: List<ChatMessage>,
    private val receiverProfileImage: Bitmap,
    private val senderId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var viewTypeSent = 1
    private var viewTypeReceived = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == viewTypeSent)
        {
            return SentMessageViewHolder(
                ItemContainerSentMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false))
        }
        else
        {
            return ReceivedMessageViewHolder(
                ItemContainerRecievedMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int)
    {
        if(getItemViewType(position) == viewTypeSent)
        {
            (holder as SentMessageViewHolder).setData(chatMessage[position])
        }
        else
        {
            (holder as ReceivedMessageViewHolder).setData(chatMessage[position], receiverProfileImage)
        }
    }

    override fun getItemCount(): Int
    {
        return chatMessage.size
    }

    override fun getItemViewType(position: Int): Int
    {
        return if(chatMessage[position].senderId == senderId) {
            viewTypeSent
        } else {
            viewTypeReceived
        }
    }

    class SentMessageViewHolder(private val binding: ItemContainerSentMessageBinding) : RecyclerView.ViewHolder(binding.root)
    {

        fun setData(chatMessage: ChatMessage)
        {
            binding.textMessage.text = chatMessage.message
            binding.textDateTime.text = chatMessage.dateTime
        }
    }

    class ReceivedMessageViewHolder(private val binding: ItemContainerRecievedMessageBinding) : RecyclerView.ViewHolder(binding.root)
    {

        fun setData(chatMessage: ChatMessage, receiverProfileImage: Bitmap)
        {
            binding.textMessage.text = chatMessage.message
            binding.textDateTime.text = chatMessage.dateTime
            binding.imageProfile.setImageBitmap(receiverProfileImage)
        }
    }
}