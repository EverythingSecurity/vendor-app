package com.example.telechat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.telechat.data.Chat
import com.example.telechat.data.ChatRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class ChatViewModel(private val repo: ChatRepository = ChatRepository()) : ViewModel() {

    val query = MutableStateFlow("")

    /** Pinned first, then newest activity; filtered by the search query. */
    val chats: StateFlow<List<Chat>> = combine(repo.chats, query) { list, q ->
        list.filter { q.isBlank() || it.name.contains(q.trim(), ignoreCase = true) }
            .sortedWith(
                compareByDescending<Chat> { it.pinned }
                    .thenByDescending { it.last?.timeMillis ?: 0L }
            )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allChats: StateFlow<List<Chat>> get() = repo.chats

    fun open(chatId: Long) = repo.markRead(chatId)

    fun send(chatId: Long, text: String) {
        repo.send(chatId, text)
        if (chatId == SAVED_MESSAGES_ID) return
        viewModelScope.launch {
            delay(800)
            repo.markRead(chatId)
            repo.setTyping(chatId, true)
            delay(1_200)
            repo.setTyping(chatId, false)
            repo.receive(chatId, reply(text))
            repo.markRead(chatId) // user is looking at the open chat
        }
    }

    fun togglePin(id: Long) = repo.togglePin(id)
    fun delete(id: Long) = repo.delete(id)
    fun createChat(name: String): Long? =
        repo.createChat(name).takeIf { name.isNotBlank() }

    private fun reply(text: String): String = when {
        text.contains("hello", true) || text.contains("hi", true) -> "Hello there! 👋"
        text.endsWith("?") -> "Good question — let me think about that."
        else -> "You said: \"${text.take(200)}\""
    }

    private companion object {
        const val SAVED_MESSAGES_ID = 1L
    }
}
