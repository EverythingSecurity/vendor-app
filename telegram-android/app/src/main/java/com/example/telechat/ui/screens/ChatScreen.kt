package com.example.telechat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.telechat.data.Chat
import com.example.telechat.data.Message
import com.example.telechat.data.MessageStatus
import com.example.telechat.ui.theme.Bubble

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(chat: Chat, onBack: () -> Unit, onSend: (String) -> Unit) {
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // reverseLayout: index 0 is the newest message, pinned to the bottom.
    LaunchedEffect(chat.messages.size) { listState.animateScrollToItem(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar(chat.name, chat.avatarColor, 40.dp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(chat.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (chat.typing) "typing…" else "online",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        containerColor = Bubble.wallpaper(),
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().imePadding()) {
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                state = listState,
                reverseLayout = true,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(chat.messages.asReversed(), key = { it.id }) { MessageBubble(it) }
            }
            Row(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = draft,
                    onValueChange = { draft = it.take(4096) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message") },
                    maxLines = 5,
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = { onSend(draft); draft = "" },
                    enabled = draft.isNotBlank(),
                ) { Icon(Icons.AutoMirrored.Filled.Send, "Send") }
            }
        }
    }
}

@Composable
private fun MessageBubble(m: Message) {
    val textColor = if (isDark()) Color.White else Color.Black
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (m.outgoing) Arrangement.End else Arrangement.Start) {
        Row(
            Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(
                    topStart = 16.dp, topEnd = 16.dp,
                    bottomStart = if (m.outgoing) 16.dp else 4.dp,
                    bottomEnd = if (m.outgoing) 4.dp else 16.dp,
                ))
                .background(if (m.outgoing) Bubble.outgoing() else Bubble.incoming())
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(m.text, color = textColor, modifier = Modifier.weight(1f, fill = false))
            Spacer(Modifier.width(8.dp))
            Text(formatTime(m.timeMillis), style = MaterialTheme.typography.labelSmall, color = textColor.copy(alpha = 0.6f))
            if (m.outgoing) {
                Spacer(Modifier.width(2.dp))
                Icon(
                    if (m.status == MessageStatus.READ) Icons.Default.DoneAll else Icons.Default.Done,
                    null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun isDark() = androidx.compose.foundation.isSystemInDarkTheme()
