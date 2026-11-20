# Andy Android

Native Android assistant built with Kotlin, Jetpack Compose, and llama.cpp.

## Local AI implemented
- On-device GGUF inference through llama.cpp JNI.
- ARM64 Android native build, CPU backend.
- Import a GGUF file using Android's system file picker.
- The model is copied to app-private storage and loaded from the phone.
- Chat generation runs on a background dispatcher.
- No backend URL, cloud API key, network permission, or local HTTP server is used for text inference.
- Push-to-talk through Android's speech-recognition service and spoken replies through Android TextToSpeech.

## Getting started
1. Install the debug APK on an ARM64 Android phone.
2. Download a compatible instruct GGUF model from its publisher. Review the license and model card first.
3. Open Andy, choose Import GGUF, and select the model file.
4. Tap Load model. Start with a 3B or 4B Q4 quantized model.
5. Send a message. The model runs on the device.

The model weights are not bundled in the APK. Importing copies the file into Andy's private internal storage, so ensure sufficient free storage. Removing app data removes the imported model.

## Build
Open this directory in Android Studio with JDK 17. The build uses Android NDK 27.2.12479018, CMake 3.22.1, and the llama.cpp tag b4517.

Run: gradle :app:assembleDebug

Package: com.andy.assistant
Minimum SDK: 26
Target SDK: 35
ABI: arm64-v8a

## First milestone limitations
- CPU inference only. Mali GPU acceleration is not enabled.
- 2048-token context; up to 512 generated tokens.
- The first prompt wrapper is generic. Exact model-specific chat templates will be added in a later pass.
- No streaming output or cancel-generation button yet.
- Android's speech-recognition provider may use its own network service. Typed text inference itself does not use the network.
- No always-on microphone or wake phrase.

## Privacy
Model files and typed prompts stay inside the app's local process/storage. The app does not start a server or send chat requests to a backend. Microphone permission is requested only after the user taps Speak.
