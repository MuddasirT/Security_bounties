package com.andy.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

private data class ChatMessage(val fromUser: Boolean, val text: String)

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private val engine by lazy { LocalLlmEngine() }
    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var speechReady = false
    private var pendingRecognition: ((String) -> Unit)? = null
    private var onRecognition: ((String) -> Unit)? = null

    private val modelPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { selected ->
            setModelImportHandler?.invoke(selected)
        }
    }
    private var setModelImportHandler: ((Uri) -> Unit)? = null

    private val micPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) listen(pendingRecognition)
        else speak("Microphone permission was not granted.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        }

        setContent {
            val scope = rememberCoroutineScope()
            val messages = remember { mutableStateListOf(ChatMessage(false, "Hello. I'm Andy. Import a GGUF model to start chatting privately on this phone.")) }
            var status by remember { mutableStateOf("NO MODEL LOADED") }
            var modelFile by remember { mutableStateOf(LocalModelStore.installed(this@MainActivity)) }
            var modelLoaded by remember { mutableStateOf(false) }
            var busy by remember { mutableStateOf(false) }
            var error by remember { mutableStateOf<String?>(null) }

            setModelImportHandler = { uri ->
                scope.launch {
                    busy = true
                    status = "IMPORTING MODEL"
                    error = null
                    try {
                        val imported = withContext(Dispatchers.IO) { LocalModelStore.import(this@MainActivity, uri) }
                        modelFile = imported
                        status = "MODEL IMPORTED"
                        messages.add(ChatMessage(false, "Model imported. Tap Load model when you're ready. The file stays in Andy's private app storage."))
                    } catch (t: Throwable) {
                        error = "Import failed: ${t.message ?: "Unable to copy model"}"
                        status = "IMPORT FAILED"
                    } finally { busy = false }
                }
            }

            AndyScreen(
                messages = messages,
                status = status,
                modelName = modelFile?.name ?: "No GGUF model selected",
                modelLoaded = modelLoaded,
                busy = busy,
                error = error,
                onImport = { modelPicker.launch(arrayOf("application/octet-stream", "*/*")) },
                onLoad = {
                    val file = modelFile
                    if (file == null) {
                        error = "Import a .gguf model file first."
                    } else {
                        scope.launch {
                            busy = true
                            status = "LOADING MODEL"
                            error = null
                            val loaded = withContext(Dispatchers.IO) { engine.loadModel(file.absolutePath) }
                            modelLoaded = loaded
                            status = if (loaded) "LOCAL MODEL READY" else "MODEL LOAD FAILED"
                            if (loaded) messages.add(ChatMessage(false, "I'm loaded and ready. Your prompts and replies are processed on this device."))
                            else error = "The model could not be loaded. Check that it is a valid, supported GGUF file and that your phone has enough free memory."
                            busy = false
                        }
                    }
                },
                onUnload = {
                    scope.launch(Dispatchers.IO) { engine.unloadModel() }
                    modelLoaded = false
                    status = "MODEL UNLOADED"
                },
                onSend = { prompt ->
                    if (!modelLoaded) {
                        messages.add(ChatMessage(false, "Please import and load a GGUF model first. No cloud gateway is configured."))
                    } else {
                        messages.add(ChatMessage(true, prompt))
                        busy = true
                        status = "THINKING LOCALLY"
                        scope.launch {
                            val reply = withContext(Dispatchers.IO) {
                                val fullPrompt = "You are Andy, a helpful personal assistant. Be accurate, concise, and friendly.\n\nUser: $prompt\nAssistant:"
                                engine.generate(fullPrompt, 256).trim()
                            }
                            messages.add(ChatMessage(false, reply.ifBlank { "I couldn't produce a reply. Try a shorter message." }))
                            busy = false
                            status = "LOCAL MODEL READY"
                            if (reply.isNotBlank()) speak(reply)
                        }
                    }
                },
                onListen = {
                    requestMicAndListen { recognized ->
                        // Send recognized text through the same local inference path.
                        scope.launch {
                            if (!modelLoaded) {
                                messages.add(ChatMessage(false, "Load a GGUF model before starting a voice conversation."))
                            } else {
                                messages.add(ChatMessage(true, recognized))
                                busy = true
                                status = "THINKING LOCALLY"
                                val reply = withContext(Dispatchers.IO) {
                                    engine.generate("You are Andy, a helpful personal assistant.\n\nUser: $recognized\nAssistant:", 256).trim()
                                }
                                messages.add(ChatMessage(false, reply))
                                busy = false
                                status = "LOCAL MODEL READY"
                                if (reply.isNotBlank()) speak(reply)
                            }
                        }
                    }
                }
            )
        }
    }

    override fun onInit(status: Int) { speechReady = status == TextToSpeech.SUCCESS }

    private fun speak(text: String) {
        if (speechReady) tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "andy-response")
    }

    private fun requestMicAndListen(callback: (String) -> Unit) {
        pendingRecognition = callback
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            listen(callback)
        } else {
            micPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun listen(callback: ((String) -> Unit)?) {
        pendingRecognition = null
        val recognizer = speechRecognizer ?: run {
            speak("Speech recognition is not available on this device.")
            return
        }
        recognizer.setRecognitionListener(object : android.speech.RecognitionListener {
            override fun onResults(results: Bundle?) {
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let { callback?.invoke(it) }
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
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        recognizer.startListening(intent)
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        if (isFinishing) engine.unloadModel()
        tts?.shutdown()
        super.onDestroy()
    }
}

@Composable
private fun AndyScreen(
    messages: List<ChatMessage>,
    status: String,
    modelName: String,
    modelLoaded: Boolean,
    busy: Boolean,
    error: String?,
    onImport: () -> Unit,
    onLoad: () -> Unit,
    onUnload: () -> Unit,
    onSend: (String) -> Unit,
    onListen: () -> Unit
) {
    var message by remember { mutableStateOf("") }
    val accent = Color(0xFF9AA9FF)
    MaterialTheme(colorScheme = darkColorScheme(
        primary = accent, onPrimary = Color(0xFF10152B), secondary = Color(0xFF72D8C8),
        background = Color(0xFF080D18), surface = Color(0xFF111A2A),
        onBackground = Color(0xFFF1F3FA), onSurface = Color(0xFFF1F3FA)
    )) {
        Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF17243A), Color(0xFF080D18)))).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).background(Brush.linearGradient(listOf(Color(0xFF8B9CFF), Color(0xFF54D8C5))), CircleShape), contentAlignment = Alignment.Center) {
                    Text("A", color = Color(0xFF08111E), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Andy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("YOUR PERSONAL AI", color = Color(0xFF9BAAC4), style = MaterialTheme.typography.labelSmall)
                }
                Surface(color = if (modelLoaded) Color(0xFF183A35) else Color(0xFF382D20), shape = RoundedCornerShape(50)) {
                    Text(if (modelLoaded) "ON-DEVICE" else "LOCAL MODE", Modifier.padding(horizontal = 11.dp, vertical = 8.dp), color = if (modelLoaded) Color(0xFF8CE5CF) else Color(0xFFFFD69A), style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(Modifier.height(16.dp))
            Surface(color = Color(0xFF17243A), shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Color(0xFF293B59)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(15.dp)) {
                    Text("Your AI. Your device.", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(modelName, color = Color(0xFFB3C0D6), style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onImport, enabled = !busy, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 8.dp)) { Text("Import GGUF", maxLines = 1) }
                        Button(onClick = if (modelLoaded) onUnload else onLoad, enabled = !busy && (modelLoaded || modelName.endsWith(".gguf")), modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 8.dp)) {
                            Text(if (modelLoaded) "Unload" else "Load model", maxLines = 1)
                        }
                    }
                    Spacer(Modifier.height(7.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (busy) CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = Color(0xFF8CE5CF))
                        Spacer(Modifier.width(7.dp))
                        Text(status, color = Color(0xFF8CE5CF), style = MaterialTheme.typography.labelSmall)
                    }
                    if (error != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(error, color = Color(0xFFFFB4AB), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("CONVERSATION", color = Color(0xFF91A0B9), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text(if (busy) "WORKING" else "PRIVATE CHAT", color = Color(0xFF7FDCCB), style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
                items(messages) { item ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (item.fromUser) Arrangement.End else Arrangement.Start) {
                        Surface(
                            color = if (item.fromUser) Color(0xFF28365B) else Color(0xFF141F31),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.dp, if (item.fromUser) Color(0xFF3A4C79) else Color(0xFF26354C)),
                            modifier = Modifier.widthIn(max = 310.dp)
                        ) {
                            Column(Modifier.padding(horizontal = 14.dp, vertical = 11.dp)) {
                                Text(if (item.fromUser) "YOU" else "ANDY", color = if (item.fromUser) Color(0xFFB8C8FF) else Color(0xFF82DCCB), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text(item.text, color = Color(0xFFEAF0FA), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
            Surface(color = Color(0xFF131E30), shape = RoundedCornerShape(22.dp), border = BorderStroke(1.dp, Color(0xFF30415D)), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(9.dp)) {
                    OutlinedTextField(value = message, onValueChange = { message = it }, placeholder = { Text("Message Andy...") }, modifier = Modifier.fillMaxWidth(), minLines = 1, maxLines = 4, shape = RoundedCornerShape(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = onListen, enabled = !busy, shape = RoundedCornerShape(14.dp)) { Text("Speak") }
                        Button(onClick = { val text = message.trim(); if (text.isNotEmpty()) { onSend(text); message = "" } }, enabled = !busy && message.isNotBlank(), modifier = Modifier.weight(1f).height(46.dp), shape = RoundedCornerShape(14.dp)) { Text("Send  ➤", fontWeight = FontWeight.Bold) }
                    }
                }
            }
            Text("NO BACKEND  •  MODEL RUNS ON THIS PHONE", Modifier.align(Alignment.CenterHorizontally).padding(vertical = 10.dp), color = Color(0xFF64738D), style = MaterialTheme.typography.labelSmall)
        }
    }
}
