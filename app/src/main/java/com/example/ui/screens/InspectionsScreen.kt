package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ApiaryEntity
import com.example.data.local.entities.HiveEntity
import com.example.data.local.entities.InspectionEntity
import com.example.ui.components.EditInspectionDialog
import com.example.ui.components.QueenColorTableDialog
import com.example.ui.components.WirelessCameraViewerDialog
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.WarningAlertRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InspectionsScreen(
    inspections: List<InspectionEntity>,
    apiaries: List<ApiaryEntity>,
    hives: List<HiveEntity>,
    onOpenNewInspection: () -> Unit,
    onOpenCombImageAnalysis: () -> Unit,
    onOpenHandsFreeMode: () -> Unit,
    onOpenQrScanner: () -> Unit = {},
    onOpenMaintenanceTips: () -> Unit = {},
    onUpdateInspection: (InspectionEntity) -> Unit = {},
    onDeleteInspection: (InspectionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    var showWirelessCameraDialog by remember { mutableStateOf(false) }
    var showQueenColorTableDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.testTag("inspections_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenNewInspection,
                containerColor = HoneyGoldPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .testTag("add_inspection_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Nova Inspeção")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Action Bar for Quick Field Modes
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onOpenQrScanner,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("inspections_open_qr_scanner_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Scanner QR",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }

                            Button(
                                onClick = onOpenCombImageAnalysis,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("inspections_open_comb_analysis_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Foto Quadro",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Button(
                                onClick = onOpenHandsFreeMode,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("inspections_open_hands_free_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Modo Voz",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showWirelessCameraDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("inspections_wireless_camera_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Câmera Sem Fio",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Button(
                                onClick = { showQueenColorTableDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("inspections_queen_colors_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Cores Rainha 👑",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Guia Técnico & Dicas de Manejo
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenMaintenanceTips() }
                            .testTag("inspections_maintenance_tips_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(HoneyGoldPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TipsAndUpdates,
                                    contentDescription = null,
                                    tint = HoneyGoldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Dicas de Manejo & Manutenção",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = HoneyGoldPrimary.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "GUIA TÉCNICO",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = HoneyGoldPrimary,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Boas práticas de fumegador, controle de varroa, rotação de cera e profilaxia.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedButton(
                                onClick = onOpenMaintenanceTips,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Ver Dicas", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HISTÓRICO DE INSPEÇÕES E SANIDADE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${inspections.size} registros",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (inspections.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "🔍", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Nenhuma inspeção realizada ainda.",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Use o botão '+' ou as ferramentas acima para registrar anotações por voz, foto ou texto.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(inspections, key = { it.id }) { inspection ->
                        val apiary = apiaries.find { it.id == inspection.apiaryId }
                        val hive = hives.find { it.id == inspection.hiveId }
                        InspectionCardItem(
                            inspection = inspection,
                            apiaryName = apiary?.name ?: "Apiário Principal",
                            hiveNumber = hive?.hiveNumber ?: "Colmeia",
                            dateFormat = dateFormat,
                            onUpdate = onUpdateInspection,
                            onDelete = { onDeleteInspection(inspection) }
                        )
                    }
                }
            }

            if (showWirelessCameraDialog) {
                WirelessCameraViewerDialog(
                    hiveNumber = "01",
                    onDismiss = { showWirelessCameraDialog = false },
                    onPhotoCaptured = { _, _, _ ->
                        showWirelessCameraDialog = false
                    }
                )
            }

            if (showQueenColorTableDialog) {
                QueenColorTableDialog(
                    onDismiss = { showQueenColorTableDialog = false }
                )
            }
        }
    }
}

@Composable
fun InspectionCardItem(
    inspection: InspectionEntity,
    apiaryName: String,
    hiveNumber: String,
    dateFormat: SimpleDateFormat,
    onUpdate: (InspectionEntity) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("inspection_card_${inspection.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (inspection.aiSanitaryAlert) Color(0xFFFFF1F2) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (inspection.aiSanitaryAlert) WarningAlertRed else HoneyGoldPrimary
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (inspection.aiSanitaryAlert) "🚨" else "📋",
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "$hiveNumber • $apiaryName",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (inspection.aiSanitaryAlert) WarningAlertRed else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${dateFormat.format(Date(inspection.date))} • ${inspection.inspectorName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expandir"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Inspection Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricPill(
                    label = "Rainha",
                    value = if (inspection.queenSeen) "Vista" else "Não vista",
                    color = if (inspection.queenSeen) Color(0xFF16A34A) else Color(0xFFEAB308)
                )
                MetricPill(
                    label = "Postura",
                    value = inspection.broodPattern,
                    color = if (inspection.broodPattern.contains("Excelente")) Color(0xFF16A34A) else Color(0xFFDC2626)
                )
                MetricPill(
                    label = "Pragas",
                    value = inspection.pestDetected,
                    color = if (inspection.pestDetected == "Nenhuma") Color(0xFF16A34A) else Color(0xFFDC2626)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    if (!inspection.notesRaw.isNullOrBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Relato de Campo Original:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "\"${inspection.notesRaw}\"",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!inspection.aiDiagnosticMarkdown.isNullOrBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (inspection.aiSanitaryAlert) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (inspection.aiSanitaryAlert) WarningAlertRed else HoneyGoldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Laudo & Diagnóstico IA ApisSmart:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (inspection.aiSanitaryAlert) WarningAlertRed else HoneyGoldPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = inspection.aiDiagnosticMarkdown.orEmpty(),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar inspeção",
                                tint = HoneyGoldPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Excluir inspeção",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        EditInspectionDialog(
            inspection = inspection,
            hiveNumber = hiveNumber,
            apiaryName = apiaryName,
            onDismiss = { showEditDialog = false },
            onSaveInspection = { updated ->
                showEditDialog = false
                onUpdate(updated)
            }
        )
    }
}

@Composable
fun MetricPill(
    label: String,
    value: String,
    color: Color
) {
    Column(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
