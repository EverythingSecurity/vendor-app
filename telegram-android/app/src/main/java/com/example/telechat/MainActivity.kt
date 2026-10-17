package com.example.telechat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.telechat.ui.screens.ChatListScreen
import com.example.telechat.ui.screens.ChatScreen
import com.example.telechat.ui.theme.TeleChatTheme

class MainActivity : ComponentActivity() {
    private val vm: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TeleChatTheme {
                val nav = rememberNavController()
                val chats by vm.chats.collectAsStateWithLifecycle()
                val all by vm.allChats.collectAsStateWithLifecycle()
                val query by vm.query.collectAsStateWithLifecycle()

                NavHost(nav, startDestination = "list") {
                    composable("list") {
                        ChatListScreen(
                            chats = chats,
                            query = query,
                            onQuery = { vm.query.value = it },
                            onOpen = { nav.navigate("chat/$it") },
                            onPin = vm::togglePin,
                            onDelete = vm::delete,
                            onCreate = { name -> vm.createChat(name)?.let { nav.navigate("chat/$it") } },
                        )
                    }
                    composable(
                        "chat/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.LongType }),
                    ) { entry ->
                        val id = entry.arguments?.getLong("id") ?: return@composable
                        // Re-mark as read whenever the open chat gains a message.
                        val chat = all.firstOrNull { it.id == id }
                        androidx.compose.runtime.LaunchedEffect(id, chat?.messages?.size) { vm.open(id) }
                        if (chat != null) {
                            ChatScreen(chat = chat, onBack = { nav.popBackStack() }, onSend = { vm.send(id, it) })
                        }
                    }
                }
            }
        }
    }
}
