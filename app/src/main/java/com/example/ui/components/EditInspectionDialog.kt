package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entities.InspectionEntity
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.MeadowGreenSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditInspectionDialog(
    inspection: InspectionEntity,
    hiveNumber: String,
    apiaryName: String,
    onDismiss: () -> Unit,
    onSaveInspection: (InspectionEntity) -> Unit
) {
    var inspectorName by remember { mutableStateOf(inspection.inspectorName) }
    var notesRaw by remember { mutableStateOf(inspection.notesRaw) }
    var queenSeen by remember { mutableStateOf(inspection.queenSeen) }
    var broodPattern by remember { mutableStateOf(inspection.broodPattern) }
    var honeyStores by remember { mutableStateOf(inspection.honeyStores) }
    var pestDetected by remember { mutableStateOf(inspection.pestDetected) }
    var temperament by remember { mutableStateOf(inspection.temperament) }
    var aiDiagnostic by remember { mutableStateOf(inspection.aiDiagnosticMarkdown ?: "") }
    var isSanitaryAlert by remember { mutableStateOf(inspection.aiSanitaryAlert) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.925f)
                .testTag("edit_inspection_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = HoneyGoldPrimary.copy(alpha = 0.2f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = HoneyGoldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Editar Inspeção",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$hiveNumber • $apiaryName",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Inspector Name
                OutlinedTextField(
                    value = inspectorName,
                    onValueChange = { inspectorName = it },
                    label = { Text("Apicultor / Inspetor") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Raw Notes / Relato de Campo
                OutlinedTextField(
                    value = notesRaw,
                    onValueChange = { notesRaw = it },
                    label = { Text("Relato de Campo / Notas") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Metrics grid / selects (simplified text fields or status selectors)
                OutlinedTextField(
                    value = broodPattern,
                    onValueChange = { broodPattern = it },
                    label = { Text("Padrão de Postura (ex: Excelente uniforme)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = honeyStores,
                        onValueChange = { honeyStores = it },
                        label = { Text("Reservas de Mel") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = pestDetected,
                        onValueChange = { pestDetected = it },
                        label = { Text("Pragas / Sanidade") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Queen seen toggle & Sanitary alert toggle
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Rainha Vista?", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Switch(
                                checked = queenSeen,
                                onCheckedChange = { queenSeen = it }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Alerta Sanitário 🚨", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = if (isSanitaryAlert) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface)
                            Switch(
                                checked = isSanitaryAlert,
                                onCheckedChange = { isSanitaryAlert = it }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // AI Diagnostic Markdown
                OutlinedTextField(
                    value = aiDiagnostic,
                    onValueChange = { aiDiagnostic = it },
                    label = { Text("Laudo / Diagnóstico Técnico") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Save button
                Button(
                    onClick = {
                        val updated = inspection.copy(
                            inspectorName = inspectorName.ifBlank { "Apicultor Responsável" },
                            notesRaw = notesRaw,
                            queenSeen = queenSeen,
                            broodPattern = broodPattern,
                            honeyStores = honeyStores,
                            pestDetected = pestDetected,
                            aiDiagnosticMarkdown = aiDiagnostic.ifBlank { null },
                            aiSanitaryAlert = isSanitaryAlert
                        )
                        onSaveInspection(updated)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_inspection_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MeadowGreenSecondary)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Salvar Alterações", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
