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
import kotlinx.coroutines.*

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var speechReady = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val permission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) listen()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        if (SpeechRecognizer.isRecognitionAvailable(this)) speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        setContent {
            MaterialTheme {
                AndyScreen(
                    onSpeak = { speak(it) },
                    onListen = { requestMicAndListen() }
                )
            }
        }
    }

    override fun onInit(status: Int) { speechReady = status == TextToSpeech.SUCCESS }

    private fun speak(text: String) {
        if (speechReady) tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "andy-response")
    }

    private fun requestMicAndListen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) listen()
        else permission.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun listen() {
        val recognizer = speechRecognizer ?: return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        recognizer.setRecognitionListener(object : android.speech.RecognitionListener {
            override fun onResults(results: Bundle?) {
                val phrase = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!phrase.isNullOrBlank()) speak("I heard: $phrase. Connect the Andy backend to enable AI replies.")
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
        scope.cancel()
        super.onDestroy()
    }
}

@Composable
private fun AndyScreen(onSpeak: (String) -> Unit, onListen: () -> Unit) {
    var message by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Ready when you are") }
    val history = remember { mutableStateListOf("Andy: Hello. I'm your assistant. Voice input and speech output are ready.") }

    Scaffold(topBar = { TopAppBar(title = { Text("Andy") }) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(28.dp))
            Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.primaryContainer) {
                Text("A", Modifier.padding(horizontal = 42.dp, vertical = 28.dp), style = MaterialTheme.typography.displayLarge)
            }
            Spacer(Modifier.height(12.dp))
            Text(status, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(24.dp))
            LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                items(history) { Text(it, Modifier.padding(vertical = 8.dp)) }
            }
            OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Talk to Andy") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = {
                    if (message.isNotBlank()) {
                        history.add("You: $message")
                        history.add("Andy: Backend connection is the next integration step.")
                        onSpeak("Backend connection is the next integration step.")
                        message = ""
                    }
                }, modifier = Modifier.weight(1f)) { Text("Send") }
                OutlinedButton(onClick = { status = "Listening…"; onListen() }, modifier = Modifier.weight(1f)) { Text("Speak") }
            }
        }
    }
}
