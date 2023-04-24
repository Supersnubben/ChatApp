package com.example.chatapp.adapters


import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.databinding.ItemContainerRecievedMessageBinding
import com.example.chatapp.databinding.ItemContainerSentMessageBinding
import com.example.chatapp.models.ChatMessage

class ChatAdapter(
    private var chatMessage: List<ChatMessage>,
    private var receiverProfileImage: Bitmap,
    private var senderId: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    init
    {
        this.chatMessage = chatMessage
        this.receiverProfileImage = receiverProfileImage
        this.senderId = senderId
    }

    var VIEW_TYPE_SENT = 1
    var VIEW_TYPE_RECEIVED = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_SENT)
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
        if(getItemViewType(position) == VIEW_TYPE_SENT)
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
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    class SentMessageViewHolder(private val binding: ItemContainerSentMessageBinding) : RecyclerView.ViewHolder(binding.root)
    {

        fun setData(chatMessage: ChatMessage)
        {
            binding.textMessage.text = chatMessage.message
            binding.textMessage.text = chatMessage.dateTime
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