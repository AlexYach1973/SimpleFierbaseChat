package com.alexyach.kotlin.udemychat.ui.listmessages.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.alexyach.kotlin.udemychat.databinding.MyMessageItemBinding
import com.alexyach.kotlin.udemychat.databinding.YourMessageItemBinding
import com.alexyach.kotlin.udemychat.domain.MessageModel

class ListMessageAdapter(
    private val dataList: List<MessageModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == WhoseMessage.MY_MESSAGE.message) {
            val binding = MyMessageItemBinding.inflate(LayoutInflater.from(parent.context))
            return MyMessageViewHolder(binding.root)
        } else {
            val binding = YourMessageItemBinding.inflate(LayoutInflater.from(parent.context))
            return YourMessageViewHolder(binding.root)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataList[position].isMine) {
            WhoseMessage.MY_MESSAGE.message
        } else {
            WhoseMessage.YOUR_MESSAGE.message
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == WhoseMessage.MY_MESSAGE.message) {
            (holder as MyMessageViewHolder).bind(dataList[position])
        } else {
            (holder as YourMessageViewHolder).bind(dataList[position])
        }
    }


    // ViewHolders
    inner class MyMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = MyMessageItemBinding.bind(itemView)

        fun bind(message: MessageModel) {

//            binding.messageItemName.text = message.name
            binding.messageItemText.text = message.message

            if (message.imageUrl != null) {
                binding.messageItemPhoto.visibility = View.VISIBLE
                binding.messageItemPhoto.load(message.imageUrl) {
                    placeholder(android.R.drawable.stat_sys_download)
                    error(android.R.drawable.stat_notify_error)
                }
            } else {
                binding.messageItemPhoto.visibility = View.GONE
            }
        }
    }
    inner class YourMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = YourMessageItemBinding.bind(itemView)

        fun bind(message: MessageModel) {
            binding.messageItemText.text = message.message

            if (message.imageUrl != null) {
                binding.messageItemPhoto.visibility = View.VISIBLE
                binding.messageItemPhoto.load(message.imageUrl) {
                    placeholder(android.R.drawable.stat_sys_download)
                    error(android.R.drawable.stat_notify_error)
                }
            } else {
                binding.messageItemPhoto.visibility = View.GONE
            }
        }
    }
}
enum class WhoseMessage(val message: Int) {
    MY_MESSAGE(0),
    YOUR_MESSAGE(1)
}