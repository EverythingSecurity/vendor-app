package com.example.telechat.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** In-memory store. Swap for Room / a network backend without touching the UI. */
class ChatRepository {
    private val _chats = MutableStateFlow(seed())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private var nextId = 1000L
    private val palette = listOf(0xFFE17076, 0xFFFAA774, 0xFF7BC862, 0xFF65AADD, 0xFFA695E7, 0xFFEE7AAE)

    fun send(chatId: Long, text: String) {
        val body = text.trim().take(4096)
        if (body.isEmpty()) return
        append(chatId, Message(nextId++, body, now(), outgoing = true))
    }

    fun receive(chatId: Long, text: String) {
        append(chatId, Message(nextId++, text, now(), outgoing = false), incoming = true)
    }

    fun markRead(chatId: Long) = updateChat(chatId) { chat ->
        chat.copy(
            unread = 0,
            messages = chat.messages.map { if (it.outgoing) it.copy(status = MessageStatus.READ) else it },
        )
    }

    fun setTyping(chatId: Long, typing: Boolean) = updateChat(chatId) { it.copy(typing = typing) }

    fun togglePin(chatId: Long) = updateChat(chatId) { it.copy(pinned = !it.pinned) }

    fun delete(chatId: Long) = _chats.update { list -> list.filterNot { it.id == chatId } }

    fun createChat(name: String): Long {
        val clean = name.trim().take(64)
        val id = nextId++
        if (clean.isNotEmpty()) {
            val color = palette[(id % palette.size).toInt()]
            _chats.update { it + Chat(id, clean, color) }
        }
        return id
    }

    private fun append(chatId: Long, msg: Message, incoming: Boolean = false) = updateChat(chatId) {
        it.copy(messages = it.messages + msg, unread = if (incoming) it.unread + 1 else it.unread)
    }

    private fun updateChat(chatId: Long, f: (Chat) -> Chat) =
        _chats.update { list -> list.map { if (it.id == chatId) f(it) else it } }

    private fun now() = System.currentTimeMillis()

    private fun seed(): List<Chat> {
        val t = System.currentTimeMillis()
        val min = 60_000L
        return listOf(
            Chat(
                1, "Saved Messages", 0xFF65AADD, pinned = true,
                messages = listOf(Message(1, "Notes to self live here.", t - 300 * min, true, MessageStatus.READ)),
            ),
            Chat(
                2, "Echo Bot", 0xFF7BC862,
                messages = listOf(Message(2, "Hi! Send me anything and I'll answer.", t - 60 * min, false)),
                unread = 1,
            ),
            Chat(
                3, "Alex", 0xFFFAA774,
                messages = listOf(
                    Message(3, "Are we still on for tomorrow?", t - 30 * min, false),
                    Message(4, "Yes, 10am works.", t - 25 * min, true, MessageStatus.READ),
                ),
            ),
            Chat(
                4, "Design Team", 0xFFA695E7,
                messages = listOf(Message(5, "New mockups are in the shared folder.", t - 1000 * min, false)),
                unread = 3,
            ),
        )
    }
}
