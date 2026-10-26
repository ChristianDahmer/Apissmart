package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.local.entities.ApiaryEntity
import com.example.data.local.entities.HiveEntity
import com.example.data.local.entities.InspectionEntity
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.MeadowGreenSecondary
import com.example.ui.theme.WarningAlertRed

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickInspectionDialog(
    apiaries: List<ApiaryEntity>,
    hives: List<HiveEntity>,
    isAiAnalyzing: Boolean,
    onDismiss: () -> Unit,
    onSubmitAiFieldNote: (apiaryId: Long, hiveId: Long, rawNotes: String, inspectorName: String) -> Unit,
    onSubmitManualInspection: (InspectionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedApiaryId by remember { mutableStateOf(apiaries.firstOrNull()?.id ?: 0L) }
    var selectedHiveId by remember { mutableStateOf(hives.firstOrNull { it.apiaryId == selectedApiaryId }?.id ?: (hives.firstOrNull()?.id ?: 0L)) }
    var inspectorName by remember { mutableStateOf("Apicultor Responsável") }

    // Tab 1 (AI Free-text / Voice Note)
    var rawNotesText by remember {
        mutableStateOf("")
    }

    // Voice Mode Recognition State
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var recognitionErrorState by remember { mutableStateOf<String?>(null) }
    var partialTranscription by remember { mutableStateOf("") }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasAudioPermission = granted
        }
    )

    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            try {
                SpeechRecognizer.createSpeechRecognizer(context)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val recognizerIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "pt-BR")
            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("pt-BR"))
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
    }

    val startRecording = {
        if (!hasAudioPermission) {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else if (speechRecognizer != null) {
            isRecording = true
            recognitionErrorState = null
            partialTranscription = ""
            try {
                speechRecognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onError(error: Int) {
                        isRecording = false
                        val message = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Erro de captura do microfone"
                            SpeechRecognizer.ERROR_CLIENT -> "Erro do cliente de voz"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permissão de áudio não concedida"
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Sem conexão com serviço de voz"
                            SpeechRecognizer.ERROR_NO_MATCH -> "Nenhuma voz identificada. Tente falar mais próximo ao microfone."
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Reconhecedor de voz ocupado"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Silêncio prolongado detectado"
                            else -> "Erro no reconhecimento de voz ($error)"
                        }
                        if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                            recognitionErrorState = message
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        isRecording = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim()
                        if (!text.isNullOrBlank()) {
                            rawNotesText = if (rawNotesText.isBlank()) text else "$rawNotesText. $text"
                        }
                        partialTranscription = ""
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull()?.trim()
                        if (!partial.isNullOrBlank()) {
                            partialTranscription = partial
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
                speechRecognizer.startListening(recognizerIntent)
            } catch (e: Exception) {
                isRecording = false
                recognitionErrorState = "Falha ao iniciar microfone: ${e.message}"
            }
        } else {
            recognitionErrorState = "Reconhecimento de voz não suportado neste aparelho."
        }
    }

    val stopRecording = {
        isRecording = false
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Tab 2 (Manual Structured form fields)
    var queenSeen by remember { mutableStateOf(true) }
    var broodPattern by remember { mutableStateOf("Excelente uniforme") }
    var pestDetected by remember { mutableStateOf("Nenhuma") }
    var honeyStores by remember { mutableStateOf("Suficiente") }
    var feedCondition by remember { mutableStateOf("Não instalado") }
    var actionsTaken by remember { mutableStateOf("") }

    val currentHives = hives.filter { it.apiaryId == selectedApiaryId }
    if (selectedHiveId == 0L && currentHives.isNotEmpty()) {
        selectedHiveId = currentHives.first().id
    }

    val sampleTemplates = listOf(
        "🚨 Postura falhada + Traça nos quadros + Alimentador vazio",
        "✅ Enxame muito forte, postura compacta, melgueira 80% cheia",
        "🔬 Suspeita de Varroa nos opérculos, abelhas com asas deformadas",
        "⚠️ Enxame zanganeiro, sem postura nova, enxameação iminente"
    )

    Dialog(
        onDismissRequest = { if (!isAiAnalyzing) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier.fillMaxSize().testTag("quick_inspection_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                Surface(
                    tonalElevation = 3.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Nova Inspeção",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (selectedTabIndex == 0) {
                            Button(
                                onClick = {
                                    if (rawNotesText.isNotBlank() && selectedApiaryId != 0L && selectedHiveId != 0L) {
                                        onSubmitAiFieldNote(selectedApiaryId, selectedHiveId, rawNotesText, inspectorName)
                                    }
                                },
                                enabled = !isAiAnalyzing && rawNotesText.isNotBlank() && selectedApiaryId != 0L && selectedHiveId != 0L,
                                modifier = Modifier.testTag("submit_ai_inspection_btn")
                            ) {
                                if (isAiAnalyzing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Diagnosticar")
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (selectedApiaryId != 0L && selectedHiveId != 0L) {
                                        val entity = InspectionEntity(
                                            apiaryId = selectedApiaryId,
                                            hiveId = selectedHiveId,
                                            inspectorName = inspectorName,
                                            queenSeen = queenSeen,
                                            broodPattern = broodPattern,
                                            eggsSeen = true,
                                            larvaeSeen = true,
                                            cappedBroodSeen = true,
                                            honeyStores = honeyStores,
                                            pollenStores = "Suficiente",
                                            pestDetected = pestDetected,
                                            pestSeverity = if (pestDetected != "Nenhuma") "Moderada" else "Nenhuma",
                                            feedCondition = feedCondition,
                                            notesRaw = "Inspeção manual via formulário estruturado.",
                                            actionsTaken = actionsTaken,
                                            aiSanitaryAlert = pestDetected != "Nenhuma" || broodPattern.contains("Falhada")
                                        )
                                        onSubmitManualInspection(entity)
                                        onDismiss()
                                    }
                                },
                                enabled = selectedApiaryId != 0L && selectedHiveId != 0L,
                                modifier = Modifier.testTag("submit_manual_inspection_btn")
                            ) {
                                Text("Salvar")
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onDismiss,
                            enabled = !isAiAnalyzing,
                            modifier = Modifier
                                .size(34.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), CircleShape)
                                .testTag("inspection_dialog_close_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Scrollable Form Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Apiary & Hive Selectors
                Text("Apiário", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                var apiaryExpanded by remember { mutableStateOf(false) }
                val selectedApiaryObj = apiaries.find { it.id == selectedApiaryId }

                ExposedDropdownMenuBox(
                    expanded = apiaryExpanded,
                    onExpandedChange = { apiaryExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedApiaryObj?.name ?: "Selecione o Apiário",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = apiaryExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = apiaryExpanded,
                        onDismissRequest = { apiaryExpanded = false }
                    ) {
                        apiaries.forEach { apiary ->
                            DropdownMenuItem(
                                text = { Text(apiary.name) },
                                onClick = {
                                    selectedApiaryId = apiary.id
                                    selectedHiveId = hives.firstOrNull { it.apiaryId == apiary.id }?.id ?: 0L
                                    apiaryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Colmeia", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                var hiveExpanded by remember { mutableStateOf(false) }
                val selectedHiveObj = hives.find { it.id == selectedHiveId }

                ExposedDropdownMenuBox(
                    expanded = hiveExpanded,
                    onExpandedChange = { hiveExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedHiveObj?.hiveNumber ?: "Selecione a Colmeia",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = hiveExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = hiveExpanded,
                        onDismissRequest = { hiveExpanded = false }
                    ) {
                        currentHives.forEach { hive ->
                            DropdownMenuItem(
                                text = { Text("${hive.hiveNumber} (${hive.strengthLevel} - ${hive.healthStatus})") },
                                onClick = {
                                    selectedHiveId = hive.id
                                    hiveExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs
                TabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("IA Relato de Campo", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Formulário Manual", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTabIndex == 0) {
                    // Mode A: AI Free-form field notes with Voice Mode & Auto Transcription
                    Text(
                        text = "Fale ou digite o relato bruto da colmeia. A IA analisará o texto para estruturar os dados e emitir o diagnóstico sanitário automaticamente.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Botão e Painel de Modo Voz com Transcrição Automática
                    if (!isRecording) {
                        Button(
                            onClick = { startRecording() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("ai_field_notes_voice_mode_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HoneyGoldPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Iniciar Modo Voz",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🎙️ Modo Voz (Ditado Automático)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        // Painel Ativo de Gravação e Transcrição em Tempo Real
                        val infiniteTransition = rememberInfiniteTransition(label = "voice_pulse")
                        val pulseScale by infiniteTransition.animateFloat(
                            initialValue = 0.95f,
                            targetValue = 1.15f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(650, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse_scale"
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_voice_recording_card"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            border = BorderStroke(1.dp, WarningAlertRed.copy(alpha = 0.6f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .scale(pulseScale)
                                                .background(WarningAlertRed, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Mic,
                                                contentDescription = "Gravando voz",
                                                tint = Color.White,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "🔴 Gravando e Transcrevendo...",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF991B1B)
                                            )
                                            Text(
                                                text = "Fale sobre postura, rainha, alimento ou pragas",
                                                fontSize = 10.sp,
                                                color = Color(0xFF7F1D1D)
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { stopRecording() },
                                        colors = ButtonDefaults.buttonColors(containerColor = WarningAlertRed),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("ai_voice_stop_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Stop,
                                            contentDescription = "Parar Gravação",
                                            modifier = Modifier.size(15.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Parar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (partialTranscription.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFFEE2E2),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Transcrição ao vivo: \"$partialTranscription...\"",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF991B1B),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Alerta de Erro de Reconhecimento
                    if (recognitionErrorState != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = recognitionErrorState ?: "",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(
                                    onClick = { recognitionErrorState = null },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                ) {
                                    Text("OK", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = rawNotesText,
                        onValueChange = { rawNotesText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("ai_field_notes_input"),
                        label = { Text("Relato da Inspeção (Voz ou Texto)") },
                        placeholder = { Text("Ex: Vi a rainha com postura compacta, melgueira 80% cheia, sem sinais de varroa...") },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (rawNotesText.isNotBlank()) {
                                    IconButton(onClick = { rawNotesText = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Limpar texto",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        if (isRecording) stopRecording() else startRecording()
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(Color(0xFFDC2626), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                            contentDescription = if (isRecording) "Parar Gravação" else "Modo Voz",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Modelos rápidos de relato:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        sampleTemplates.forEach { template ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        rawNotesText = if (rawNotesText.isBlank()) template else "$rawNotesText. $template"
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = template.take(35) + "...",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    // Mode B: Manual form
                    Text("Padrão de Postura", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    val broodOptions = listOf("Excelente uniforme", "Regular / Falhada", "Ausente", "Cria Zanganeira")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        broodOptions.forEach { opt ->
                            FilledTonalButton(
                                onClick = { broodPattern = opt },
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                if (broodPattern == opt) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text(opt, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Presença de Pragas/Doenças", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    val pestOptions = listOf(
                        "Nenhuma",
                        "Varroa Destructor",
                        "Ácaro das Traqueias",
                        "Traça da Cera",
                        "Besouro Aethina",
                        "Formigas/Cupins",
                        "Loque Americana",
                        "Loque Europeia",
                        "Cria Giz",
                        "Nosemose"
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        pestOptions.forEach { p ->
                            FilledTonalButton(
                                onClick = { pestDetected = p },
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                if (pestDetected == p) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text(p, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = actionsTaken,
                        onValueChange = { actionsTaken = it },
                        label = { Text("Ações tomadas no manejo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
}
