package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.OutlinedButton
import java.io.File
import java.io.FileOutputStream
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.collectAsState
import com.example.util.BluetoothMicHelper
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.fillMaxHeight
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import com.example.data.local.entities.ApiaryEntity
import com.example.data.local.entities.HiveEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandsFreeFieldDialog(
    apiaries: List<ApiaryEntity>,
    hives: List<HiveEntity>,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onSubmitVoiceDictation: (apiaryId: Long, hiveId: Long, rawTranscription: String) -> Unit,
    onTakePhotoVoiceCommand: (() -> Unit)? = null,
    onPhotoCaptured: ((hiveId: Long, photoUri: String) -> Unit)? = null
) {
    var selectedApiaryId by remember { mutableStateOf(apiaries.firstOrNull()?.id ?: 0L) }
    val filteredHives = hives.filter { it.apiaryId == selectedApiaryId }
    var selectedHiveId by remember { mutableStateOf(filteredHives.firstOrNull()?.id ?: 0L) }

    val context = LocalContext.current
    val bluetoothMicHelper = remember { BluetoothMicHelper(context) }
    val btAudioState by bluetoothMicHelper.state.collectAsState()

    // Photo capture state for voice mode
    var capturedPhotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var capturedPhotoUriString by remember { mutableStateOf<String?>(null) }
    var showEnlargedPhotoDialog by remember { mutableStateOf(false) }

    var isHandsFreeActive by remember { mutableStateOf(false) }
    var isDictationActive by remember { mutableStateOf(false) }
    var transcriptionText by remember { mutableStateOf("") }
    var recognitionErrorState by remember { mutableStateOf<String?>(null) }
    var lastVoiceCommandMessage by remember { mutableStateOf<String?>(null) }
    var currentRmsDb by remember { mutableStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val quickPresets = listOf(
        "Postura regular com ovos e larvas visíveis, rainha em atividade plena.",
        "Presença de cria operculada saudável, sem sinais de loque ou deformidades.",
        "Reservas abundantes de mel e pólen nos favos periféricos.",
        "Sinais de puxada de realeira, possível preparação para enxameação.",
        "Colmeia fraca, baixa população de operárias, necessita alimentação de reforço."
    )

    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedPhotoBitmap = bitmap
            try {
                val file = File(context.cacheDir, "voice_field_photo_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                capturedPhotoUriString = file.absolutePath
                onPhotoCaptured?.invoke(selectedHiveId, file.absolutePath)
                lastVoiceCommandMessage = "📸 Foto capturada e anexada à colmeia!"
            } catch (e: Exception) {
                Log.e("HandsFree", "Erro ao salvar foto: ${e.message}")
            }
        }
    }

    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    fun startListening() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            recognitionErrorState = "Permissão de microfone necessária para escuta de voz."
            return
        }
        try {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                recognitionErrorState = "Reconhecimento de voz não suportado neste aparelho."
                return
            }
            speechRecognizer?.destroy()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            if (recognizer == null) {
                recognitionErrorState = "Serviço de voz indisponível."
                return
            }
            speechRecognizer = recognizer
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isHandsFreeActive = true
                    recognitionErrorState = null
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {
                    currentRmsDb = rmsdB
                }
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    recognitionErrorState = "Aguardando áudio..."
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    if (text.isNotBlank()) {
                        val lower = text.lowercase()
                        when {
                            lower.contains("tirar foto") || lower.contains("foto do favo") || lower.contains("capturar imagem") -> {
                                takePhotoLauncher.launch(null)
                                lastVoiceCommandMessage = "📸 Comando: Tirar foto executado"
                            }
                            lower.contains("próxima colmeia") || lower.contains("proxima") -> {
                                val currentIdx = filteredHives.indexOfFirst { it.id == selectedHiveId }
                                if (currentIdx in 0 until filteredHives.size - 1) {
                                    selectedHiveId = filteredHives[currentIdx + 1].id
                                    lastVoiceCommandMessage = "➡️ Avançou para Colmeia ${filteredHives[currentIdx + 1].hiveNumber}"
                                }
                            }
                            lower.contains("colmeia anterior") || lower.contains("anterior") -> {
                                val currentIdx = filteredHives.indexOfFirst { it.id == selectedHiveId }
                                if (currentIdx > 0) {
                                    selectedHiveId = filteredHives[currentIdx - 1].id
                                    lastVoiceCommandMessage = "⬅️ Voltou para Colmeia ${filteredHives[currentIdx - 1].hiveNumber}"
                                }
                            }
                            lower.contains("limpar") -> {
                                transcriptionText = ""
                                lastVoiceCommandMessage = "🧹 Transcrição limpa"
                            }
                            lower.contains("salvar") -> {
                                if (transcriptionText.isNotBlank()) {
                                    onSubmitVoiceDictation(selectedApiaryId, selectedHiveId, transcriptionText)
                                    lastVoiceCommandMessage = "💾 Inspeção salva com sucesso!"
                                }
                            }
                            lower.contains("fechar") -> {
                                onDismiss()
                            }
                            else -> {
                                if (isDictationActive) {
                                    transcriptionText = if (transcriptionText.isBlank()) text else "$transcriptionText. $text"
                                }
                            }
                        }
                    }
                    if (isHandsFreeActive) {
                        try {
                            recognizer.startListening(intent)
                        } catch (_: Exception) {}
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            recognizer.startListening(intent)
        } catch (e: Exception) {
            recognitionErrorState = "Erro ao iniciar escuta: ${e.message}"
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.destroy()
            bluetoothMicHelper.stopBluetoothSco()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().testTag("hands_free_dialog"),
            color = Color(0xFF0F172A), // High contrast dark field mode
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // High contrast Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🎙️ Modo Luvas & Voz",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24)
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFF334155), CircleShape)
                            .testTag("hands_free_dialog_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                    }
                }

                Text(
                    text = "Dite o que está observando com o ninho aberto sem precisar tirar as luvas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Bluetooth Microphone Status Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (btAudioState.isScoAudioConnected) Color(0xFF065F46)
                        else if (btAudioState.isHeadsetConnected) Color(0xFF78350F)
                        else Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = if (btAudioState.isScoAudioConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                                contentDescription = null,
                                tint = if (btAudioState.isScoAudioConnected) Color(0xFF34D399) else Color(0xFFFBBF24),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (btAudioState.isHeadsetConnected) "Microfone Sem Fio: ${btAudioState.activeDeviceName ?: "Conectado"}"
                                    else "Microfone Bluetooth / Lapela",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (btAudioState.isScoAudioConnected) "Canal SCO Ativo (Escuta sem fio no apiário)"
                                    else if (btAudioState.isHeadsetConnected) "Pareado. Toque para direcionar áudio."
                                    else "Nenhum fone/lapela conectado. Usando mic do celular.",
                                    fontSize = 9.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }

                        if (btAudioState.isHeadsetConnected) {
                            Button(
                                onClick = {
                                    if (btAudioState.isScoAudioConnected) {
                                        bluetoothMicHelper.stopBluetoothSco()
                                    } else {
                                        bluetoothMicHelper.startBluetoothSco()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (btAudioState.isScoAudioConnected) Color(0xFFDC2626) else Color(0xFF10B981)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(if (btAudioState.isScoAudioConnected) "Desconectar" else "Ativar Mic Sem Fio", fontSize = 10.sp)
                            }
                        }
                    }
                }

                // Voice Command Feedback Banner
                AnimatedVisibility(visible = lastVoiceCommandMessage != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = lastVoiceCommandMessage ?: "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFDE047)
                            )
                        }
                    }
                }

                // Hive Navigation Bar (Glove-Friendly + Voice Commands Hints)
                val currentHiveIndex = filteredHives.indexOfFirst { it.id == selectedHiveId }
                val currentHive = filteredHives.getOrNull(if (currentHiveIndex >= 0) currentHiveIndex else 0)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Hive Button
                    Button(
                        onClick = {
                            if (filteredHives.isNotEmpty()) {
                                val prevIdx = if (currentHiveIndex > 0) currentHiveIndex - 1 else filteredHives.size - 1
                                selectedHiveId = filteredHives[prevIdx].id
                                lastVoiceCommandMessage = "🔄 Voltou para Colmeia ${filteredHives[prevIdx].hiveNumber}"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(56.dp)
                            .weight(0.32f),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Anterior", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Text("\"Colmeia anterior\"", fontSize = 8.sp, color = Color(0xFF94A3B8))
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Center Active Hive Badge
                    Surface(
                        modifier = Modifier
                            .weight(0.48f)
                            .height(56.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF59E0B)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Colmeia ${currentHive?.hiveNumber ?: "01"}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "${currentHive?.beeSpecies ?: "Apis"} • ${currentHiveIndex + 1}/${filteredHives.size}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A).copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Next Hive Button
                    Button(
                        onClick = {
                            if (filteredHives.isNotEmpty()) {
                                val nextIdx = if (currentHiveIndex in 0 until filteredHives.size - 1) currentHiveIndex + 1 else 0
                                selectedHiveId = filteredHives[nextIdx].id
                                lastVoiceCommandMessage = "🔄 Avançou para Colmeia ${filteredHives[nextIdx].hiveNumber}"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(56.dp)
                            .weight(0.32f),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Próxima", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            Text("\"Próxima colmeia\"", fontSize = 8.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable Hive Chips (all hives in apiary)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredHives) { hive ->
                        val isSelected = selectedHiveId == hive.id
                        Box(
                            modifier = Modifier
                                .height(34.dp)
                                .background(
                                    if (isSelected) Color(0xFFF59E0B) else Color(0xFF1E293B),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedHiveId = hive.id
                                    lastVoiceCommandMessage = "🎯 Selecionou Colmeia ${hive.hiveNumber}"
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Colmeia ${hive.hiveNumber}",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                color = if (isSelected) Color(0xFF0F172A) else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Voice Commands Quick Reference Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Comandos de Voz Rápidos (Mãos Livres):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFBBF24)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Iniciar Ditado: \"Iniciar ditado\", \"Começar ditado\", \"Gravar relato\"",
                            fontSize = 10.sp,
                            color = Color(0xFFCBD5E1)
                        )
                        Text(
                            text = "• Pausar Ditado: \"Parar ditado\", \"Pausar ditado\", \"Pausar\"",
                            fontSize = 10.sp,
                            color = Color(0xFFCBD5E1)
                        )
                        Text(
                            text = "• Alternar Colmeia: \"Próxima colmeia\", \"Colmeia anterior\" ou \"Colmeia 02\"",
                            fontSize = 10.sp,
                            color = Color(0xFFCBD5E1)
                        )
                        Text(
                            text = "• Tirar Foto por Voz: \"Tirar foto\", \"Foto do favo\", \"Capturar imagem\"",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFDE047)
                        )
                        Text(
                            text = "• Ações Rápidas: \"Limpar texto\", \"Salvar inspeção\", \"Fechar\"",
                            fontSize = 10.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Minimalist Waveform Voice Control (Vermelho-Goiaba no ditado / Verde Esmeralda na escuta de comandos)
                MinimalistWaveformVoiceControl(
                    isHandsFreeActive = isHandsFreeActive,
                    isDictating = isDictationActive,
                    rmsDb = currentRmsDb,
                    pulseScale = pulseScale,
                    onToggleRecording = {
                        if (isHandsFreeActive) {
                            if (isDictationActive) {
                                isDictationActive = false
                                lastVoiceCommandMessage = "🛑 Ditado pausado manualmente"
                            } else {
                                isDictationActive = true
                                lastVoiceCommandMessage = "🎙️ Ditado iniciado manualmente"
                            }
                        } else {
                            startListening()
                        }
                    }
                )

                recognitionErrorState?.let { err ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ℹ️ $err",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFBBF24),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Photo Capture Button & Visualizer
                OutlinedButton(
                    onClick = { takePhotoLauncher.launch(null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFBBF24)),
                    border = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFFFBBF24))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📸 Tirar Foto do Favo (Toque ou diga \"Tirar foto\")", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }

                if (capturedPhotoBitmap != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Image(
                                    bitmap = capturedPhotoBitmap!!.asImageBitmap(),
                                    contentDescription = "Foto capturada do favo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showEnlargedPhotoDialog = true }
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("📸 Foto Registrada", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                                    Text("Toque para ampliar e inspecionar", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                }
                            }
                            Row {
                                IconButton(onClick = { showEnlargedPhotoDialog = true }) {
                                    Icon(Icons.Default.Visibility, contentDescription = "Visualizar em tamanho real", tint = Color(0xFFFBBF24))
                                }
                                IconButton(onClick = {
                                    capturedPhotoBitmap = null
                                    capturedPhotoUriString = null
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir foto", tint = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Transcription Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Transcrição Automática:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (transcriptionText.isBlank()) "Dite seu relato de campo acima ou escolha um modelo rápido abaixo..." else transcriptionText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (transcriptionText.isBlank()) Color(0xFF64748B) else Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick presets for convenience
                Text(
                    text = "Ditações Rápidas de Campo:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))
                quickPresets.forEach { preset ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .clickable { transcriptionText = preset }
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "💬 \"$preset\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Submit Button
                Button(
                    onClick = {
                        onSubmitVoiceDictation(selectedApiaryId, selectedHiveId, transcriptionText)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    enabled = !isProcessing && transcriptionText.isNotBlank() && selectedHiveId != 0L
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color(0xFF0F172A),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Processando Diagnóstico...", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                    } else {
                        Text("Salvar & Diagnosticar com IA", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showEnlargedPhotoDialog && capturedPhotoBitmap != null) {
        Dialog(onDismissRequest = { showEnlargedPhotoDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📸 Foto do Favo em Detalhes",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24),
                            fontSize = 14.sp
                        )
                        IconButton(onClick = { showEnlargedPhotoDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Image(
                        bitmap = capturedPhotoBitmap!!.asImageBitmap(),
                        contentDescription = "Foto ampliada do favo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showEnlargedPhotoDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Fechar Visualização", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MinimalistWaveformVoiceControl(
    isHandsFreeActive: Boolean,
    isDictating: Boolean,
    rmsDb: Float,
    pulseScale: Float,
    onToggleRecording: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numBarsPerSide = 12
    var sideAmplitudes by remember { mutableStateOf(List(numBarsPerSide) { 0.2f }) }

    LaunchedEffect(isHandsFreeActive, isDictating, rmsDb) {
        if (isHandsFreeActive) {
            val random = java.util.Random()
            while (isActive) {
                val normalizedDb = ((rmsDb + 10f) / 40f).coerceIn(0.2f, 1f)
                val baseFactor = if (isDictating) 1f else 0.5f
                sideAmplitudes = List(numBarsPerSide) { index ->
                    val factor = 1f - (index * 0.05f)
                    val jitter = (random.nextFloat() - 0.5f) * 0.3f
                    (normalizedDb * factor * baseFactor + jitter).coerceIn(0.12f, 1.0f)
                }
                kotlinx.coroutines.delay(50)
            }
        } else {
            sideAmplitudes = List(numBarsPerSide) { index ->
                when (index % 4) {
                    0 -> 0.2f
                    1 -> 0.35f
                    2 -> 0.15f
                    else -> 0.25f
                }
            }
        }
    }

    val guavaRed = Color(0xFFE11D48) // Vermelho-goiaba vibrante para ditado ativo
    val emeraldGreen = Color(0xFF059669) // Verde esmeralda para escuta de comandos ativa
    val inactiveGray = Color(0xFF475569) // Cinza quando pausado
    val darkGrayBg = Color(0xFF1E293B) // Fundo cinza-escuro elegante

    val buttonColor = when {
        isDictating -> guavaRed
        isHandsFreeActive -> emeraldGreen
        else -> inactiveGray
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = darkGrayBg,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Forma de onda de áudio dinâmica e simétrica
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Esquerda: Barras verticais arredondadas
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(70.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    sideAmplitudes.reversed().forEach { amp ->
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight(amp)
                                .background(
                                    color = if (isHandsFreeActive) {
                                        if (isDictating) Color.White else Color(0xFF6EE7B7)
                                    } else Color.White.copy(alpha = 0.25f),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Centro: Botão circular de gravação/comando
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(if (isDictating) pulseScale else 1f)
                        .background(buttonColor, CircleShape)
                        .border(
                            width = 2.5.dp,
                            color = if (isHandsFreeActive) Color.White.copy(alpha = 0.85f) else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onToggleRecording() }
                        .testTag("guava_red_voice_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            isDictating -> Icons.Default.Stop
                            isHandsFreeActive -> Icons.Default.Mic
                            else -> Icons.Default.MicOff
                        },
                        contentDescription = "Gravação por Voz",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Direita: Barras verticais espelhadas
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(70.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    sideAmplitudes.forEach { amp ->
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight(amp)
                                .background(
                                    color = if (isHandsFreeActive) {
                                        if (isDictating) Color.White else Color(0xFF6EE7B7)
                                    } else Color.White.copy(alpha = 0.25f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when {
                isDictating -> {
                    Text(
                        text = "🔴 DITANDO RELATO • Microfone Aberto",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF87171)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Fale seu relato • Diga \"Parar ditado\" para pausar ou \"Próxima colmeia\"",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFCBD5E1)
                    )
                }
                isHandsFreeActive -> {
                    Text(
                        text = "🟢 ESCUTA DE COMANDOS ATIVA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Diga \"Iniciar ditado\", \"Próxima colmeia\" ou \"Salvar\"",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFCBD5E1)
                    )
                }
                else -> {
                    Text(
                        text = "⏸️ Escuta Pausada",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Toque no botão central para religar a escuta por voz",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}

private sealed class HandsFreeVoiceCommand {
    data class SwitchToHive(val hive: HiveEntity) : HandsFreeVoiceCommand()
    object NextHive : HandsFreeVoiceCommand()
    object PreviousHive : HandsFreeVoiceCommand()
    object StartDictation : HandsFreeVoiceCommand()
    object StopDictation : HandsFreeVoiceCommand()
    object ClearText : HandsFreeVoiceCommand()
    object TakePhoto : HandsFreeVoiceCommand()
    object SubmitInspection : HandsFreeVoiceCommand()
    object CloseDialog : HandsFreeVoiceCommand()
}

private fun findHiveBySpokenText(spoken: String, hives: List<HiveEntity>): HiveEntity? {
    val lower = spoken.lowercase().trim()
    val numberWordMap = mapOf(
        "um" to "1", "uma" to "1", "primeira" to "1",
        "dois" to "2", "duas" to "2", "segunda" to "2",
        "três" to "3", "tres" to "3", "terceira" to "3",
        "quatro" to "4", "quarta" to "4",
        "cinco" to "5", "quinta" to "5",
        "seis" to "6", "sexta" to "6",
        "sete" to "7", "sétima" to "7", "setima" to "7",
        "oito" to "8", "oitava" to "8",
        "nove" to "9", "nona" to "9",
        "dez" to "10", "décima" to "10", "decima" to "10"
    )

    for (hive in hives) {
        val hNum = hive.hiveNumber.lowercase().trim()
        val hDigits = hNum.filter { it.isDigit() }
        if (lower.contains("colmeia $hNum") || lower.contains("caixa $hNum")) {
            return hive
        }
        if (hDigits.isNotBlank()) {
            if (lower.contains("colmeia $hDigits") || lower.contains("caixa $hDigits") ||
                lower.contains("colmeia número $hDigits") || lower.contains("colmeia numero $hDigits")) {
                return hive
            }
            for ((word, digit) in numberWordMap) {
                if (digit == hDigits || digit.padStart(2, '0') == hNum) {
                    if (lower.contains("colmeia $word") || lower.contains("caixa $word") ||
                        lower.contains("colmeia número $word") || lower.contains("colmeia numero $word")) {
                        return hive
                    }
                }
            }
        }
    }
    return null
}

private fun parseVoiceCommand(text: String, hives: List<HiveEntity>): Pair<HandsFreeVoiceCommand?, String> {
    val lower = text.lowercase().trim()

    // 1. Stop / Pause dictation command
    val stopKeywords = listOf(
        "parar ditado", "parar o ditado", "desativar ditado", "pausar ditado",
        "pausar o ditado", "interromper ditado", "parar gravação", "parar gravacao",
        "pausar gravação", "pausar gravacao", "finalizar ditado", "concluir ditado",
        "desligar microfone", "desativar voz", "parar escuta", "parar de gravar",
        "pausar relato", "pausar", "parar", "silêncio", "silencio"
    )
    for (kw in stopKeywords) {
        if (lower.contains(kw)) {
            val cleaned = text.replace(Regex("(?i)$kw"), "").trim()
            return Pair(HandsFreeVoiceCommand.StopDictation, cleaned)
        }
    }

    // 2. Start / Activate dictation command
    val startKeywords = listOf(
        "iniciar ditado", "iniciar o ditado", "iniciar ditação", "iniciar ditacao",
        "ativar ditado", "ativar o ditado", "começar ditado", "comecar ditado",
        "começar o ditado", "comecar o ditado", "iniciar gravação", "iniciar gravacao",
        "ativar gravação", "ativar gravacao", "ativar microfone", "ligar microfone",
        "ativar voz", "iniciar voz", "abrir ditado", "retomar ditado", "continuar ditado",
        "começar a gravar", "comecar a gravar", "gravar relato", "começar relato",
        "comecar relato", "ditar relato", "gravar", "ditar", "iniciar", "começar", "comecar"
    )
    for (kw in startKeywords) {
        if (lower.contains(kw)) {
            val cleaned = text.replace(Regex("(?i)$kw"), "").trim()
            return Pair(HandsFreeVoiceCommand.StartDictation, cleaned)
        }
    }

    // 3. Next hive command
    val nextKeywords = listOf(
        "próxima colmeia", "proxima colmeia", "avançar colmeia", "avancar colmeia",
        "colmeia seguinte", "seguinte colmeia", "próxima caixa", "proxima caixa",
        "avançar caixa", "avancar caixa", "ir para próxima", "ir para proxima",
        "trocar colmeia", "mudar colmeia", "próxima", "proxima", "avançar", "avancar"
    )
    for (kw in nextKeywords) {
        if (lower.contains(kw)) {
            val cleaned = text.replace(Regex("(?i)$kw"), "").trim()
            return Pair(HandsFreeVoiceCommand.NextHive, cleaned)
        }
    }

    // 4. Previous hive command
    val prevKeywords = listOf(
        "colmeia anterior", "caixa anterior", "voltar colmeia", "retornar colmeia",
        "colmeia passada", "voltar caixa", "retornar caixa", "ir para anterior",
        "anterior", "voltar", "retornar"
    )
    for (kw in prevKeywords) {
        if (lower.contains(kw)) {
            val cleaned = text.replace(Regex("(?i)$kw"), "").trim()
            return Pair(HandsFreeVoiceCommand.PreviousHive, cleaned)
        }
    }

    // 5. Specific hive command
    val specific = findHiveBySpokenText(lower, hives)
    if (specific != null) {
        val cleaned = text.replace(Regex("(?i)(?:ir para a?|selecionar|mudar para a?)?\\s*(?:colmeia|caixa)\\s+[0-9a-zA-Z\\-]+"), "").trim()
        return Pair(HandsFreeVoiceCommand.SwitchToHive(specific), cleaned)
    }

    // 6. Clear text
    val clearKeywords = listOf(
        "limpar texto", "apagar texto", "limpar ditado", "apagar relato",
        "limpar tudo", "apagar tudo", "limpar relato", "limpar", "apagar"
    )
    for (kw in clearKeywords) {
        if (lower.contains(kw)) {
            return Pair(HandsFreeVoiceCommand.ClearText, "")
        }
    }

    // 6.5. Take photo by voice command
    val photoKeywords = listOf(
        "tirar foto", "bater foto", "capturar foto", "tirar fotografia",
        "bater fotografia", "capturar imagem", "foto do favo", "tirar foto do favo",
        "fotografar", "fotografe", "foto agora", "capturar quadro", "foto quadro"
    )
    for (kw in photoKeywords) {
        if (lower.contains(kw)) {
            val cleaned = text.replace(Regex("(?i)$kw"), "").trim()
            return Pair(HandsFreeVoiceCommand.TakePhoto, cleaned)
        }
    }

    // 7. Submit
    val submitKeywords = listOf(
        "salvar inspeção", "salvar inspecao", "salvar relato", "enviar inspeção",
        "enviar inspecao", "confirmar inspeção", "confirmar inspecao", "finalizar inspeção",
        "concluir inspeção", "enviar diagnóstico", "salvar", "enviar", "finalizar", "concluir"
    )
    for (kw in submitKeywords) {
        if (lower.contains(kw)) {
            val cleaned = text.replace(Regex("(?i)$kw"), "").trim()
            return Pair(HandsFreeVoiceCommand.SubmitInspection, cleaned)
        }
    }

    // 8. Close dialog
    val closeKeywords = listOf(
        "fechar modo luvas", "fechar tela", "fechar janela", "fechar diálogo",
        "fechar", "sair", "cancelar"
    )
    for (kw in closeKeywords) {
        if (lower.contains(kw)) {
            return Pair(HandsFreeVoiceCommand.CloseDialog, "")
        }
    }

    return Pair(null, text)
}
