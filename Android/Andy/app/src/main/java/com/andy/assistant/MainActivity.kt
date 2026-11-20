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
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    val accent = Color(0xFF9AA9FF)
    MaterialTheme(colorScheme = darkColorScheme(
        primary = accent, onPrimary = Color(0xFF10152B),
        secondary = Color(0xFF72D8C8), background = Color(0xFF080D18),
        surface = Color(0xFF111A2A), onBackground = Color(0xFFF1F3FA),
        onSurface = Color(0xFFF1F3FA)
    )) {
        Column(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0xFF17243A), Color(0xFF080D18)))
            ).padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(46.dp).background(
                    Brush.linearGradient(listOf(Color(0xFF8B9CFF), Color(0xFF54D8C5))), CircleShape
                ), contentAlignment = Alignment.Center) {
                    Text("A", color = Color(0xFF08111E), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Andy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("YOUR PERSONAL AI", color = Color(0xFF9BAAC4), style = MaterialTheme.typography.labelSmall)
                }
                Surface(color = Color(0xFF182B2D), shape = RoundedCornerShape(50)) {
                    Text("LOCAL AI", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = Color(0xFF8CE5CF), style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(Modifier.height(22.dp))
            Surface(
                color = Color(0xFF17243A), shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF293B59)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("A little more human.", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(5.dp))
                    Text("Talk naturally. Think freely. Your assistant, on your device.", color = Color(0xFFB3C0D6), style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    Text("●  Local model not installed yet", color = Color(0xFFFFD69A), style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("CONVERSATION", color = Color(0xFF91A0B9), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text(status.uppercase(), color = Color(0xFF7FDCCB), style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(10.dp))
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 14.dp)
            ) {
                items(messages) { item ->
                    val isUser = item.startsWith("You:")
                    val body = item.substringAfter(":").trim()
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start) {
                        Surface(
                            color = if (isUser) Color(0xFF28365B) else Color(0xFF141F31),
                            shape = RoundedCornerShape(
                                topStart = 18.dp, topEnd = 18.dp,
                                bottomStart = if (isUser) 18.dp else 5.dp,
                                bottomEnd = if (isUser) 5.dp else 18.dp
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isUser) Color(0xFF3A4C79) else Color(0xFF26354C)),
                            modifier = Modifier.widthIn(max = 310.dp)
                        ) {
                            Column(Modifier.padding(horizontal = 15.dp, vertical = 12.dp)) {
                                Text(if (isUser) "YOU" else "ANDY", color = if (isUser) Color(0xFFB8C8FF) else Color(0xFF82DCCB), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(5.dp))
                                Text(body, color = Color(0xFFEAF0FA), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
            Surface(
                color = Color(0xFF131E30), shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF30415D)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(10.dp)) {
                    OutlinedTextField(
                        value = message, onValueChange = { message = it },
                        placeholder = { Text("Message Andy...", color = Color(0xFF8291AA)) },
                        modifier = Modifier.fillMaxWidth(), minLines = 1, maxLines = 4,
                        shape = RoundedCornerShape(17.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = accent
                        )
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = onListen, shape = RoundedCornerShape(15.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3A4C68)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB9C7E0))
                        ) { Text("🎙 Speak") }
                        Button(
                            onClick = { val text = message.trim(); if (text.isNotEmpty()) { onSend(text); message = "" } },
                            modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(15.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color(0xFF10152B))
                        ) { Text("Send  ➤", fontWeight = FontWeight.Bold) }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("PRIVATE BY DESIGN  •  LOCAL AI IS BEING BUILT", Modifier.align(Alignment.CenterHorizontally).padding(bottom = 10.dp), color = Color(0xFF64738D), style = MaterialTheme.typography.labelSmall)
        }
    }
}
