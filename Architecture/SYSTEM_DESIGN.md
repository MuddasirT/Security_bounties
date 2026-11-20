# Andy Android: Hybrid Architecture

## Goal
A native Android personal assistant with push-to-talk voice in the first milestone, cloud/local model routing, and a path to a user-controlled wake phrase.

## Components
- Android client: Kotlin, Jetpack Compose, Android SpeechRecognizer, TextToSpeech.
- Gateway: FastAPI. The Android app calls only the gateway; provider credentials remain server-side.
- Model router: prefer a configured local OpenAI-compatible endpoint, fall back to a configured cloud endpoint.
- Future integrations: explicit, permissioned GitHub tools and user-approved research workflows.

## Request flow
1. User presses Speak or types a message.
2. Android obtains text through the platform speech recognizer.
3. Client sends the text to POST /v1/chat over HTTPS.
4. Gateway selects local first, cloud second.
5. Android displays and speaks the reply.

## Milestones
1. MVP: app shell, push-to-talk, TTS, gateway health and chat.
2. Connect authenticated gateway and render conversation history.
3. Add conversation persistence and user-configurable provider settings.
4. Add local wake-word detection with a foreground service, persistent notification, opt-in and battery controls.
5. Add scoped GitHub integrations with confirmation before writes.

## Android constraints
A third-party app cannot assume unrestricted always-on microphone access or guaranteed wake-word execution while locked. Wake-word support must use a user-visible foreground service, runtime microphone permission, battery-aware operation, and device/OEM compatibility testing. The MVP therefore uses explicit push-to-talk.

## Deployment
- Development: run FastAPI locally or on a free-tier host where available.
- Local LLM: expose an OpenAI-compatible endpoint only on a trusted private network or authenticated tunnel; never expose an unauthenticated model port to the public internet.
- Production: HTTPS, rate limits, request size limits, secret rotation, audit logs, and per-user authentication.
