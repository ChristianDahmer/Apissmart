package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.AiConsultationEntity
import com.example.data.local.entities.ApiaryEntity
import com.example.data.local.entities.HiveEntity
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.WarningAlertRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiConsultantScreen(
    consultations: List<AiConsultationEntity>,
    apiaries: List<ApiaryEntity>,
    hives: List<HiveEntity>,
    isLoading: Boolean,
    onSendMessage: (query: String, context: String) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val quickQuestions = listOf(
        "🔬 Protocolo de tratamento contra Varroa Destructor",
        "🍯 Cálculo de xarope 1:1 e bife proteico de incentivo",
        "👑 Sinais de enxameação e manejo preventivo",
        "🚨 O que fazer em caso de suspeita de Loque Americana?",
        "📊 Gerar Resumo Executivo e Projeção de Safra dos apiários"
    )

    // Build context summary string
    val contextInfo = buildString {
        appendLine("Apiários cadastrados: ${apiaries.joinToString { "${it.name} (${it.location})" }}")
        appendLine("Total de colmeias: ${hives.size}")
        val alertHives = hives.filter { it.healthStatus == "Alerta Sanitário" }
        if (alertHives.isNotEmpty()) {
            appendLine("Colmeias em alerta sanitário: ${alertHives.joinToString { "${it.hiveNumber} (${it.notes})" }}")
        }
    }

    LaunchedEffect(consultations.size, isLoading) {
        if (consultations.isNotEmpty()) {
            listState.animateScrollToItem(consultations.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.testTag("ai_consultant_screen"),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .padding(bottom = 80.dp) // accommodate bottom nav bar
            ) {
                // Input Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_chat_input"),
                        placeholder = { Text("Tire dúvidas de sanidade, manejo, florada...", fontSize = 13.sp) },
                        maxLines = 3,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HoneyGoldPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (inputText.isNotBlank() && !isLoading) HoneyGoldPrimary else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable(enabled = inputText.isNotBlank() && !isLoading) {
                                val msg = inputText
                                inputText = ""
                                onSendMessage(msg, contextInfo)
                            }
                            .testTag("ai_send_message_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = HoneyGoldPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Enviar",
                                tint = if (inputText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(HoneyGoldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "CONSULTORIA ESPECIALIZADA",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Engenharia Agronômica e Sanidade Apícola",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (consultations.isNotEmpty()) {
                        IconButton(onClick = onClearHistory) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Limpar Conversa", modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // Quick Prompt Chips
            item {
                Column {
                    Text(
                        text = "Consultas técnicas frequentes:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickQuestions.forEach { q ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                                    .clickable {
                                        onSendMessage(q, contextInfo)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = q,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Message Bubbles
            items(consultations, key = { it.id }) { item ->
                ConsultationBubble(item = item)
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = HoneyGoldPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ApisSmart analisando parâmetros técnicos...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConsultationBubble(item: AiConsultationEntity) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // User Message (Right-aligned)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(12.dp)
            ) {
                Text(
                    text = item.userPrompt,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Assistant Response (Left-aligned)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(0.95f),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (item.isSanitaryAlert) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🐝", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ApisSmart",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = HoneyGoldPrimary
                            )
                        }
                        Text(
                            text = timeFormat.format(Date(item.timestamp)),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (item.isSanitaryAlert) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAlertRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ALERTA SANITÁRIO TÉCNICO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarningAlertRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item.assistantResponse,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
