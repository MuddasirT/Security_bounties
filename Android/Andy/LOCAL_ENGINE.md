# Andy local inference engine

Andy now uses llama.cpp through JNI. The app targets **arm64-v8a** and builds the native engine from the pinned llama.cpp tag `b12556`.

## Install a model
1. Build and install the debug APK.
2. Obtain a compatible GGUF instruct model from its publisher. Check its model card, license, quantization, and file checksum.
3. In Andy, tap **Import GGUF model** and select the downloaded `.gguf` file.
4. Tap **Load model**. The file is copied to Andy's private app storage and remains on the phone.
5. Chat locally. No gateway URL, account, API key, or backend is used.

A 3B to 4B Q4 model is the suggested starting point for a 12 GB Android phone. Actual speed and memory use depend on the model and available device RAM. Close other heavy apps before loading. The initial engine uses CPU inference; it does not claim Mali GPU acceleration.

## Limitations in this first native-engine milestone
- Context is 2048 tokens and each reply is capped at 512 generated tokens.
- The prompt wrapper is a generic instruction format. Some model families may need their exact chat template for best results.
- Generation is synchronous on a worker thread. There is no streaming token display or generation-cancel control yet.
- Android system speech recognition may use the device's speech provider and may require network access. Text-to-speech uses the installed Android TTS engine.
- Import requires enough free storage for a second copy of the GGUF file.
- The app supports arm64-v8a only in this milestone.

## Privacy
The selected model is stored under app-private internal storage. It is not uploaded. Android app data removal deletes the imported model. No local HTTP server is opened.
