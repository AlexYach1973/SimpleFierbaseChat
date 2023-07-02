package com.alexyach.kotlin.udemychat.ui.listmessages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.alexyach.kotlin.udemychat.R
import com.alexyach.kotlin.udemychat.databinding.MessageItemBinding
import com.alexyach.kotlin.udemychat.domain.MessageModel

class ListMessageAdapter(
    private val dataList: List<MessageModel>
) : RecyclerView.Adapter<ListMessageAdapter.ListMessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListMessageViewHolder {
        val binding = MessageItemBinding
            .inflate(LayoutInflater.from(parent.context))
        return ListMessageViewHolder(binding.root)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ListMessageViewHolder, position: Int) {
        holder.bind(dataList[position])
    }


    // ViewHolder
    inner class ListMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = MessageItemBinding.bind(itemView)

        fun bind(message: MessageModel) {
//            with(binding) {
            binding.messageItemName.text = message.name

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