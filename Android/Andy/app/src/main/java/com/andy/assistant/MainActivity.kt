package com.andy.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var speechReady = false
    private val api = AndyApiClient(BuildConfig.ANDY_API_BASE_URL)

    private val permission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) listen()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        }
        setContent {
            val messages = remember { mutableStateListOf("Andy: Hello. I'm ready. Tap Speak or type a message.") }
            var status by remember { mutableStateOf("Ready when you are") }
            MaterialTheme {
                AndyScreen(
                    messages = messages,
                    status = status,
                    onSend = { text ->
                        messages.add("You: $text")
                        status = "Thinking…"
                        api.ask(text) { result ->
                            runOnUiThread {
                                result.onSuccess { reply ->
                                    messages.add("Andy: $reply")
                                    status = "Ready"
                                    speak(reply)
                                }.onFailure {
                                    val note = it.message ?: "Unknown connection error"
                                    messages.add("Andy: I couldn't reach my AI gateway. $note")
                                    status = "Connection problem"
                                }
                            }
                        }
                    },
                    onSpeak = { speak(it) },
                    onListen = { requestMicAndListen { recognized ->
                        messages.add("You: $recognized")
                        status = "Thinking…"
                        api.ask(recognized) { result ->
                            runOnUiThread {
                                result.onSuccess { reply ->
                                    messages.add("Andy: $reply")
                                    status = "Ready"
                                    speak(reply)
                                }.onFailure {
                                    messages.add("Andy: I couldn't reach my AI gateway. Check the server URL and connection.")
                                    status = "Connection problem"
                                }
                            }
                        }
                    } }
                )
            }
        }
    }

    override fun onInit(status: Int) { speechReady = status == TextToSpeech.SUCCESS }

    private fun speak(text: String) {
        if (speechReady) tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "andy-response")
    }

    private fun requestMicAndListen(onText: (String) -> Unit) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            listen(onText)
        } else {
            pendingRecognition = onText
            permission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private var pendingRecognition: ((String) -> Unit)? = null

    private fun listen(onText: ((String) -> Unit)? = pendingRecognition) {
        pendingRecognition = null
        val recognizer = speechRecognizer ?: return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        recognizer.setRecognitionListener(object : android.speech.RecognitionListener {
            override fun onResults(results: Bundle?) {
                val phrase = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!phrase.isNullOrBlank()) onText?.invoke(phrase)
            }
            override fun onError(error: Int) { speak("I couldn't hear that clearly. Please try again.") }
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        recognizer.startListening(intent)
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        tts?.shutdown()
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AndyScreen(
    messages: List<String>,
    status: String,
    onSend: (String) -> Unit,
    onSpeak: (String) -> Unit,
    onListen: () -> Unit
) {
    var message by remember { mutableStateOf("") }
    Scaffold(topBar = { TopAppBar(title = { Text("Andy") }) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.primaryContainer) {
                Text("A", Modifier.padding(horizontal = 42.dp, vertical = 28.dp), style = MaterialTheme.typography.displayLarge)
            }
            Spacer(Modifier.height(12.dp))
            Text(status, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(20.dp))
            LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                items(messages) { Text(it, Modifier.padding(vertical = 8.dp)) }
            }
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Talk to Andy") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { val text = message.trim(); if (text.isNotEmpty()) { onSend(text); message = "" } },
                    modifier = Modifier.weight(1f)
                ) { Text("Send") }
                OutlinedButton(onClick = onListen, modifier = Modifier.weight(1f)) { Text("Speak") }
            }
        }
    }
}
