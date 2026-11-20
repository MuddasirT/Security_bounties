# Andy Local AI: Redmi Note 12 Pro 5G

## Target device

- SoC: MediaTek Dimensity 1080 (2x Cortex-A78 up to 2.6 GHz + 6x Cortex-A55)
- GPU: ARM Mali-G68 MC4
- User-reported RAM: 12 GB
- ABI target: arm64-v8a

Official device specifications: https://www.mi.com/in/product/redmi-note-12-pro-5g/specs/

## Architecture decision

Andy will use on-device inference. A cloud backend is not required for chat.

- Android UI: Kotlin + Jetpack Compose
- Native inference: llama.cpp through Android NDK/JNI
- Model format: GGUF
- Initial model class: 3B-4B instruct, Q4_K_M (benchmark before selecting a fixed model)
- Initial context: 2,048-4,096 tokens, configurable
- Threads: start with 4; benchmark 4/6 and monitor thermals
- Storage: model downloaded once into app-managed storage; show model size and free-space check
- Voice: Android SpeechRecognizer and TextToSpeech. Note: Android speech recognition may use the device's speech service and may require network depending on the installed recognition provider. Fully offline speech recognition is a separate model/integration task.
- Conversation history: Room or a small local database; no cloud sync by default.

## Device constraints and expectations

12 GB physical RAM does not mean all 12 GB is available to one app. Android, MIUI/HyperOS, background apps, model weights, KV cache and native allocations all consume memory. Start with a 3B/4B Q4 model and a modest context. A 7B model is an experiment, not a promised smooth configuration on this chipset.

The Mali GPU must not be assumed to accelerate llama.cpp. Begin with the portable ARM64 CPU backend and benchmark. Add a GPU backend only after validating support and stability on this exact device.

## Model licensing and download

Do not bundle model weights inside the APK. Keep the APK small and let the user download a compatible GGUF model after installation. Display the model's source, license, file size, quantization, and checksum before download. Never silently download a multi-gigabyte file.

A candidate model is not considered selected until its current model card, license, GGUF availability, and on-device benchmark have been checked.

## Security and privacy

- No API key or remote gateway is needed for local inference.
- Do not expose a local inference HTTP server to the LAN by default.
- Keep model files and chat history in app-private storage unless the user explicitly exports them.
- Add a visible delete-chat-history and delete-model action.
- Microphone permission is requested only when the user taps the voice control.
- No always-on microphone or wake-word service in the first local release.

## Implementation sequence

1. Replace the HTTP gateway client with a local inference abstraction.
2. Build llama.cpp for Android arm64-v8a using the Android NDK and JNI.
3. Add model download/import, integrity checks, storage checks, and model loading/unloading.
4. Add streaming token output, cancellation, and lifecycle-safe native resource cleanup.
5. Benchmark prompt processing, generation speed, peak memory, battery drain, and thermal behavior on the Redmi Note 12 Pro 5G.
6. Add local conversation persistence and offline speech recognition as separately testable features.
7. Build and install a debug APK, then test on the physical device.

## Upstream references

- llama.cpp Android documentation: https://github.com/ggml-org/llama.cpp/blob/master/docs/android.md
- llama.cpp build documentation: https://github.com/ggml-org/llama.cpp/blob/master/docs/build.md
- Xiaomi device specifications: https://www.mi.com/in/product/redmi-note-12-pro-5g/specs/

## Current status

This document records the approved architecture. It does not claim that the current APK already contains the native inference engine or a model. The existing gateway-based APK must be replaced by a new local-inference build before Andy can answer without a backend.
