#include <jni.h>
#include <android/log.h>
#include <algorithm>
#include <mutex>
#include <string>
#include <vector>
#include "llama.h"

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "AndyLlama", __VA_ARGS__)

static llama_model *g_model = nullptr;
static llama_context *g_context = nullptr;
static const llama_vocab *g_vocab = nullptr;
static std::mutex g_mutex;

static void unload_locked() {
    if (g_context) { llama_free(g_context); g_context = nullptr; }
    if (g_model) { llama_model_free(g_model); g_model = nullptr; }
    g_vocab = nullptr;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_andy_assistant_LocalLlmEngine_loadModel(JNIEnv *env, jobject, jstring path) {
    std::lock_guard<std::mutex> lock(g_mutex);
    unload_locked();
    const char *file = env->GetStringUTFChars(path, nullptr);
    llama_model_params model_params = llama_model_default_params();
    model_params.use_mmap = true;
    g_model = llama_model_load_from_file(file, model_params);
    env->ReleaseStringUTFChars(path, file);
    if (!g_model) { LOGE("Could not load model file"); return JNI_FALSE; }

    llama_context_params context_params = llama_context_default_params();
    context_params.n_ctx = 2048;
    context_params.n_batch = 512;
    context_params.n_threads = std::max(2, (int)std::thread::hardware_concurrency() - 2);
    context_params.n_threads_batch = context_params.n_threads;
    g_context = llama_init_from_model(g_model, context_params);
    if (!g_context) { unload_locked(); LOGE("Could not create llama context"); return JNI_FALSE; }
    g_vocab = llama_model_get_vocab(g_model);
    return JNI_TRUE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_andy_assistant_LocalLlmEngine_generate(JNIEnv *env, jobject, jstring input, jint max_tokens) {
    std::lock_guard<std::mutex> lock(g_mutex);
    if (!g_model || !g_context || !g_vocab) return env->NewStringUTF("Please import and load a GGUF model first.");
    const char *raw = env->GetStringUTFChars(input, nullptr);
    std::string prompt(raw);
    env->ReleaseStringUTFChars(input, raw);

    llama_memory_clear(llama_get_memory(g_context), true);
    const int capacity = std::max(256, (int)prompt.size() * 2 + 16);
    std::vector<llama_token> tokens((size_t)capacity);
    int32_t token_count = llama_tokenize(g_vocab, prompt.c_str(), (int32_t)prompt.size(),
                                         tokens.data(), (int32_t)tokens.size(), true, true);
    if (token_count < 0) {
        tokens.resize((size_t)(-token_count));
        token_count = llama_tokenize(g_vocab, prompt.c_str(), (int32_t)prompt.size(),
                                     tokens.data(), (int32_t)tokens.size(), true, true);
    }
    if (token_count <= 0 || token_count > (int)tokens.size()) return env->NewStringUTF("I couldn't tokenize that prompt.");
    tokens.resize((size_t)token_count);

    llama_batch batch = llama_batch_get_one(tokens.data(), (int32_t)tokens.size());
    if (llama_decode(g_context, batch) != 0) return env->NewStringUTF("The model ran out of context. Try a shorter message.");

    llama_sampler *sampler = llama_sampler_chain_init(llama_sampler_chain_default_params());
    llama_sampler_chain_add(sampler, llama_sampler_init_top_k(40));
    llama_sampler_chain_add(sampler, llama_sampler_init_top_p(0.90f, 1));
    llama_sampler_chain_add(sampler, llama_sampler_init_temp(0.75f));
    llama_sampler_chain_add(sampler, llama_sampler_init_dist(LLAMA_DEFAULT_SEED));

    std::string answer;
    std::vector<char> piece(4096);
    const int limit = std::max(1, std::min((int)max_tokens, 512));
    for (int i = 0; i < limit; ++i) {
        llama_token token = llama_sampler_sample(sampler, g_context, -1);
        if (llama_vocab_is_eog(g_vocab, token)) break;
        int n = llama_token_to_piece(g_vocab, token, piece.data(), (int)piece.size(), 0, true);
        if (n > 0) answer.append(piece.data(), (size_t)n);
        llama_sampler_accept(sampler, token);
        llama_token next = token;
        llama_batch next_batch = llama_batch_get_one(&next, 1);
        if (llama_decode(g_context, next_batch) != 0) break;
    }
    llama_sampler_free(sampler);
    return env->NewStringUTF(answer.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_andy_assistant_LocalLlmEngine_unloadModel(JNIEnv *, jobject) {
    std::lock_guard<std::mutex> lock(g_mutex);
    unload_locked();
}
