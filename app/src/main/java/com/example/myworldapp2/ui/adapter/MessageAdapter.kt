package com.example.myworldapp2.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myworldapp2.R
import com.example.myworldapp2.data.model.Message
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Адаптер для отображения сообщений в чате
 */
class MessageAdapter : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).sender) {
            Message.SenderType.USER -> VIEW_TYPE_SENT
            Message.SenderType.BOT -> VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_sent, parent, false)
                SentMessageViewHolder(view)
            }
            VIEW_TYPE_RECEIVED -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_received, parent, false)
                ReceivedMessageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
        }
    }

    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewMessage: TextView = itemView.findViewById(R.id.textViewMessage)
        private val textViewTime: TextView = itemView.findViewById(R.id.textViewTime)
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(message: Message) {
            textViewMessage.text = message.text
            textViewTime.text = timeFormat.format(message.timestamp)
        }
    }

    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewMessage: TextView = itemView.findViewById(R.id.textViewMessage)
        private val textViewTime: TextView = itemView.findViewById(R.id.textViewTime)
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(message: Message) {
            textViewMessage.text = message.text
            textViewTime.text = timeFormat.format(message.timestamp)
        }
    }

    private class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
} 