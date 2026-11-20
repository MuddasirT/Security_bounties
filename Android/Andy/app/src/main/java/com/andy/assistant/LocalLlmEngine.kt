package com.andy.assistant

/**
 * JNI bridge to llama.cpp. GGUF files are imported into app-private storage.
 * The model is never uploaded to a server.
 */
class LocalLlmEngine {
    companion object {
        init { System.loadLibrary("andy-llama") }
    }

    external fun loadModel(path: String): Boolean
    external fun generate(prompt: String, maxTokens: Int = 256): String
    external fun unloadModel()
}
