package com.example.telechat.data

enum class MessageStatus { SENT, READ }

data class Message(
    val id: Long,
    val text: String,
    val timeMillis: Long,
    val outgoing: Boolean,
    val status: MessageStatus = MessageStatus.SENT,
)

data class Chat(
    val id: Long,
    val name: String,
    val avatarColor: Long,
    val messages: List<Message> = emptyList(),
    val unread: Int = 0,
    val pinned: Boolean = false,
    val typing: Boolean = false,
) {
    val last: Message? get() = messages.lastOrNull()
}
