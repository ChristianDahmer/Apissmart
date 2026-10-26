package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.StructuredInspectionResult

@Composable
fun DiagnosticResultDialog(
    result: StructuredInspectionResult,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("diagnostic_dialog"),
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.testTag("diagnostic_dialog_confirm_btn")
            ) {
                Text("Entendido / Aplicar Manejos")
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (result.isSanitaryAlert) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alerta Sanitário",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Alerta Sanitário",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Diagnóstico Saudável",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Diagnóstico Técnico",
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Structured quick summary pills
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "DADOS ESTRUTURADOS PELA IA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "👑 Rainha: ${result.queenStatusAssessment} (Vista: ${if (result.queenSeen) "Sim" else "Não"})", fontSize = 13.sp)
                        Text(text = "🐣 Padrão de Cria: ${result.broodPattern}", fontSize = 13.sp)
                        Text(text = "🔬 Praga Detectada: ${result.pestDetected} (${result.pestSeverity})", fontSize = 13.sp)
                        Text(text = "🍯 Reservas de Mel: ${result.honeyStores} | Alimentador: ${result.feedCondition}", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Markdown Diagnostic Content
                Text(
                    text = result.diagnosticMarkdown,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (result.recommendedTasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "📅 TAREFAS AUTOMATICAMENTE AGENDADAS:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            result.recommendedTasks.forEach { task ->
                                Text(
                                    text = "• $task",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}
