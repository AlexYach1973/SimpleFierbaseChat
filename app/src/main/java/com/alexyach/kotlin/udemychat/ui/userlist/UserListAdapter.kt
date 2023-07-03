package com.alexyach.kotlin.udemychat.ui.userlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alexyach.kotlin.udemychat.databinding.UserListItemBinding
import com.alexyach.kotlin.udemychat.domain.UserModel

class UserListAdapter(
    private val dataList: List<UserModel>,
    private val listener: OnUserListItemClickListener
) : RecyclerView.Adapter<UserListAdapter.UserListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListViewHolder {
        val binding = UserListItemBinding.inflate(LayoutInflater.from(parent.context))
        return UserListViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: UserListViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int {
        return dataList.size
    }


    // ViewHolder
    inner class UserListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = UserListItemBinding.bind(itemView)

        fun bind(item: UserModel) {
            binding.nameListUserItem.text = item.name
            binding.avatarImageView.setImageResource(item.avatar)

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onUserListItemClick(position)
                }
            }
        }
    }
}

interface OnUserListItemClickListener {
    fun onUserListItemClick(position: Int)
}