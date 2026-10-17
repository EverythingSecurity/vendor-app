package com.example.telechat.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.telechat.data.Chat
import com.example.telechat.data.MessageStatus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatListScreen(
    chats: List<Chat>,
    query: String,
    onQuery: (String) -> Unit,
    onOpen: (Long) -> Unit,
    onPin: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onCreate: (String) -> Unit,
) {
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var searching by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var menuFor by remember { mutableStateOf<Long?>(null) }

    ModalNavigationDrawer(
        drawerState = drawer,
        drawerContent = {
            ModalDrawerSheet {
                Column(Modifier.padding(24.dp)) {
                    Avatar("Me", 0xFF2AABEE, 64.dp)
                    Spacer(Modifier.height(12.dp))
                    Text("My Account", style = MaterialTheme.typography.titleMedium)
                }
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("New Chat") },
                    icon = { Icon(Icons.Default.Edit, null) },
                    selected = false,
                    onClick = { scope.launch { drawer.close() }; showNew = true },
                )
                NavigationDrawerItem(
                    label = { Text("Saved Messages") },
                    icon = { Icon(Icons.Default.Bookmark, null) },
                    selected = false,
                    onClick = { scope.launch { drawer.close() }; onOpen(1L) },
                )
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        if (searching) {
                            TextField(
                                value = query, onValueChange = onQuery, singleLine = true,
                                placeholder = { Text("Search") },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(),
                            )
                        } else Text("TeleChat")
                    },
                    navigationIcon = {
                        if (searching) IconButton(onClick = { searching = false; onQuery("") }) {
                            Icon(Icons.Default.ArrowBack, "Close search")
                        } else IconButton(onClick = { scope.launch { drawer.open() } }) {
                            Icon(Icons.Default.Menu, "Menu")
                        }
                    },
                    actions = {
                        if (!searching) IconButton(onClick = { searching = true }) {
                            Icon(Icons.Default.Search, "Search")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showNew = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                ) { Icon(Icons.Default.Edit, "New chat") }
            },
        ) { padding ->
            if (chats.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Text("No chats", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else LazyColumn(Modifier.padding(padding)) {
                items(chats, key = { it.id }) { chat ->
                    Box {
                        ChatRow(chat, Modifier.combinedClickable(
                            onClick = { onOpen(chat.id) },
                            onLongClick = { menuFor = chat.id },
                        ))
                        DropdownMenu(expanded = menuFor == chat.id, onDismissRequest = { menuFor = null }) {
                            DropdownMenuItem(
                                text = { Text(if (chat.pinned) "Unpin" else "Pin") },
                                onClick = { onPin(chat.id); menuFor = null },
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = { onDelete(chat.id); menuFor = null },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showNew) NewChatDialog(onDismiss = { showNew = false }, onCreate = { showNew = false; onCreate(it) })
}

@Composable
private fun ChatRow(chat: Chat, modifier: Modifier) {
    Row(
        modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(chat.name, chat.avatarColor)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    chat.name, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
                )
                val last = chat.last
                if (last != null) {
                    if (last.outgoing) Icon(
                        if (last.status == MessageStatus.READ) Icons.Default.DoneAll else Icons.Default.Done,
                        null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(formatTime(last.timeMillis), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (chat.typing) "typing…" else chat.last?.text ?: "No messages yet",
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
                    color = if (chat.typing) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (chat.unread > 0) {
                    Box(
                        Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.primary)
                            .defaultMinSize(minWidth = 22.dp, minHeight = 22.dp).padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(chat.unread.toString(), color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall)
                    }
                } else if (chat.pinned) {
                    Icon(Icons.Default.PushPin, "Pinned", Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun NewChatDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New chat") },
        text = {
            OutlinedTextField(name, { name = it.take(64) }, singleLine = true, label = { Text("Name") })
        },
        confirmButton = { TextButton(onClick = { onCreate(name) }, enabled = name.isNotBlank()) { Text("Create") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
