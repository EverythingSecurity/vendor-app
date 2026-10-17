# TeleChat

A Telegram-style messenger UI for Android, built with Kotlin and Jetpack Compose (Material 3).

## Features
- Chat list: avatars, last message, time, unread badges, read ticks, pinned chats, live search
- Long-press a chat to pin or delete it
- Conversation view: sent and received bubbles, ✓ / ✓✓ status, "typing…" indicator, bottom-anchored list
- New chat dialog and navigation drawer
- Light and dark themes in Telegram colours
- Offline demo: an in-memory repository with a simple auto-reply bot (`ChatRepository`, `ChatViewModel`)

## Build
Open the `telegram-android` folder in Android Studio (Koala or newer) and run the `app` configuration.
From the command line (needs the Android SDK and a Gradle install): `gradle :app:assembleDebug`.
Requires JDK 17, minSdk 24.

## Next steps for a real messenger
- Persist with Room and replace `ChatRepository` with a network-backed implementation
- Use TLS (and pin certificates) for transport, and end-to-end encryption for secret chats
- Store credentials and keys in the Android Keystore
- Add the `INTERNET` permission only once a backend exists; `allowBackup` is already disabled so chats stay out of cloud backups
