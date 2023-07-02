package com.alexyach.kotlin.udemychat.domain

data class MessageModel(
    val name: String = "Default",
    val message: String? = null,
    val imageUrl: String? = null
)
