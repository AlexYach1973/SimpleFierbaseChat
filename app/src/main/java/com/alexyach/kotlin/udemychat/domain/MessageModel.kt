package com.alexyach.kotlin.udemychat.domain

data class MessageModel(
    val name: String = "Default",
    val message: String? = null,
    val senderId: String? = null,
    val recipientId: String? = null,
    val imageUrl: String? = null,
    var isMine: Boolean = false
)
