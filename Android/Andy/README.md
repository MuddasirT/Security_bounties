# Andy Android

Native Android starter for Andy, built with Kotlin and Jetpack Compose.

## Implemented in this starter
- Compose chat interface.
- Push-to-talk speech recognition through Android's system recognizer.
- Spoken replies through Android TextToSpeech.
- Typed and recognized messages sent to the Andy FastAPI gateway.
- HTTPS gateway URL configurable at build time.
- Gateway routes to a configured local OpenAI-compatible model first, then a configured cloud model.

## Not implemented yet
- Production user sign-in and secure session provisioning.
- Always-on wake phrase / background microphone service.
- Conversation persistence and sync.
- GitHub tools and user-confirmed agent actions.
- Packaged, signed release APK.

These are deliberate next milestones. This starter is not yet a finished Siri replacement or production-ready assistant.

## Build
Open this directory in Android Studio (JDK 17 recommended), allow Gradle sync, then run on an Android 8.0+ device. A physical device is recommended for speech-recognition testing.

Set the gateway URL in the Gradle property `andyApiBaseUrl`. Example:
```bash
./gradlew :app:assembleDebug -PandyApiBaseUrl=https://your-private-gateway.example
```

Package: `com.andy.assistant`
Minimum SDK: 26
Target SDK: 35

## Privacy and security
- The app requests microphone permission only after the user taps Speak.
- It does not continuously record audio.
- Speech recognition uses Android's installed speech-recognition service; depending on device settings/provider, speech processing may use that provider's service.
- Provider API keys must remain on the gateway, never in the APK.
- Do not expose the gateway publicly until real user authentication, HTTPS, rate limiting, request limits, secret management, and audit logging are configured.
