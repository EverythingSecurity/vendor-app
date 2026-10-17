package com.example.telechat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun Avatar(name: String, color: Long, size: Dp = 52.dp) {
    Box(
        Modifier.size(size).clip(CircleShape).background(Color(color)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            name.trim().split(" ").take(2).joinToString("") { it.take(1).uppercase() },
            color = Color.White, fontSize = (size.value * 0.38f).sp,
        )
    }
}

fun formatTime(millis: Long): String {
    val day = 24 * 60 * 60 * 1000L
    val pattern = if (System.currentTimeMillis() - millis < day) "HH:mm" else "MMM d"
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))
}
