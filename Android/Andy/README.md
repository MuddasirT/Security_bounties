# Andy Android

Native Android starter for Andy, built with Kotlin and Jetpack Compose.

## Current starter scope
- Compose app shell.
- Android speech recognition through the system recognizer (push-to-talk).
- Android TextToSpeech output.
- Microphone permission requested only when the user taps Speak.

## Not implemented yet
- Cloud/local model request from the Android client.
- User sign-in and secure session provisioning.
- Always-on wake phrase.
- Background microphone service.
- Conversation persistence.
- GitHub tools.

These are deliberate next milestones. Do not describe this starter as a finished Siri replacement or as a production-ready assistant.

## Build
Open this directory in Android Studio (JDK 17 recommended), allow Gradle sync, then run the app on an Android 8.0+ device. A physical device is recommended for speech-recognition testing.

Package: `com.andy.assistant`
Minimum SDK: 26
Target SDK: 35

## Privacy
The app requests microphone permission only after the user taps Speak. It does not continuously record or upload audio. The current starter uses Android's installed speech-recognition service and speaks a local placeholder response.
