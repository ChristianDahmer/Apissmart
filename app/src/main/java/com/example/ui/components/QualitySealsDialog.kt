package com.example.ui.components

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entities.QualitySealGoalEntity
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.MeadowGreenSecondary
import com.example.ui.theme.WarningAlertRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SealInfo(
    val type: String,
    val name: String,
    val acronym: String,
    val officialOrg: String,
    val summary: String,
    val valueAddText: String,
    val badgeIcon: String,
    val primaryColor: Color
)

val QUALITY_SEALS_INFO = listOf(
    SealInfo(
        type = "ARTE",
        name = "Selo ARTE (Produto Artesanal)",
        acronym = "Selo ARTE",
        officialOrg = "MAPA • Lei 13.680 / Decreto 11.099",
        summary = "Certifica o mel como produto artesanal, produzido com técnicas tradicionais. Permite a venda intermunicipal e interestadual de produtos artesanais ligados ao SIM ou SIE.",
        valueAddText = "Libera a comercialização em todo o Brasil a partir do SIM local e agrega 30% a 50% no preço de venda.",
        badgeIcon = "🎨",
        primaryColor = HoneyGoldPrimary
    ),
    SealInfo(
        type = "ORGANICO",
        name = "Selo de Produto Orgânico",
        acronym = "Orgânico Brasil",
        officialOrg = "MAPA • Lei 10.831 (Certificadoras / OCS)",
        summary = "Concedido por certificadoras credenciadas ao MAPA para méis produzidos sem insumos químicos e em áreas sem contaminantes.",
        valueAddText = "Valorização média de 40% a 70% no mercado nacional e habilitação para canais premium e exportação.",
        badgeIcon = "🌿",
        primaryColor = MeadowGreenSecondary
    ),
    SealInfo(
        type = "IG",
        name = "Selo de Indicação Geográfica (IG)",
        acronym = "Indicação Geográfica / D.O.",
        officialOrg = "INPI • Lei 9.279 (Propriedade Industrial)",
        summary = "Identifica méis de regiões com notoriedade ou características únicas atribuídas à sua origem geográfica.",
        valueAddText = "Proteção contra falsificações, reputação territorial inigualável e agregação de até 100% no valor por frasco.",
        badgeIcon = "📍",
        primaryColor = Color(0xFF64B5F6)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QualitySealsDialog(
    goals: List<QualitySealGoalEntity>,
    onToggleGoal: (QualitySealGoalEntity) -> Unit,
    onSaveGoal: (sealType: String, title: String, description: String, category: String, evidence: String, mandatory: Boolean) -> Unit,
    onDeleteGoal: (QualitySealGoalEntity) -> Unit,
    onShareComplianceReport: (sealType: String, onShare: (Intent) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedSealIndex by remember { mutableIntStateOf(0) }
    var showAddGoalDialog by remember { mutableStateOf(false) }

    val currentSealInfo = QUALITY_SEALS_INFO[selectedSealIndex]
    val sealGoals = goals.filter { it.sealType.equals(currentSealInfo.type, ignoreCase = true) }
    val completedCount = sealGoals.count { it.isCompleted }
    val totalCount = sealGoals.size
    val progressPercent = if (totalCount > 0) (completedCount * 100) / totalCount else 0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .testTag("quality_seals_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(HoneyGoldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = null,
                                tint = HoneyGoldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Selos de Qualidade & Certificação",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Agregação de valor e metas de conformidade",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Abas dos Selos
                SecondaryTabRow(
                    selectedTabIndex = selectedSealIndex,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = currentSealInfo.primaryColor
                ) {
                    QUALITY_SEALS_INFO.forEachIndexed { index, seal ->
                        Tab(
                            selected = selectedSealIndex == index,
                            onClick = { selectedSealIndex = index },
                            text = {
                                Text(
                                    "${seal.badgeIcon} ${seal.acronym}",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedSealIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("seal_tab_${seal.type.lowercase()}")
                        )
                    }
                }

                // Conteúdo do Selo Selecionado
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Card Informativo do Selo
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(currentSealInfo.badgeIcon, fontSize = 28.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = currentSealInfo.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                            Text(
                                                text = currentSealInfo.officialOrg,
                                                fontSize = 11.sp,
                                                color = currentSealInfo.primaryColor,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (progressPercent == 100) MeadowGreenSecondary.copy(alpha = 0.2f) else HoneyGoldPrimary.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = if (progressPercent == 100) "Apto / 100%" else "$progressPercent% Concluído",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (progressPercent == 100) MeadowGreenSecondary else HoneyGoldPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = currentSealInfo.summary,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("💰", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = currentSealInfo.valueAddText,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Barra de Progresso e Métricas de Metas
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Progresso de Metas & Requisitos",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "$completedCount de $totalCount cumpridas",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = currentSealInfo.primaryColor
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = currentSealInfo.primaryColor,
                                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                )
                            }
                        }
                    }

                    // Ações: Compartilhar Dossiê e Adicionar Nova Meta
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    onShareComplianceReport(currentSealInfo.type) { shareIntent ->
                                        context.startActivity(shareIntent)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Exportar Dossiê", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            OutlinedButton(
                                onClick = { showAddGoalDialog = true },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Nova Meta", fontSize = 12.sp)
                            }
                        }
                    }

                    // Lista de Metas e Requisitos Oficiais
                    item {
                        Text(
                            text = "Checklist de Metas para o ${currentSealInfo.acronym}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    items(sealGoals, key = { it.id }) { goal ->
                        GoalItemCard(
                            goal = goal,
                            onToggle = { onToggleGoal(goal) },
                            onDelete = { onDeleteGoal(goal) }
                        )
                    }
                }
            }
        }
    }

    if (showAddGoalDialog) {
        AddQualityGoalDialog(
            sealType = currentSealInfo.type,
            onDismiss = { showAddGoalDialog = false },
            onSave = { title, desc, cat, evidence, mandatory ->
                onSaveGoal(currentSealInfo.type, title, desc, cat, evidence, mandatory)
                showAddGoalDialog = false
            }
        )
    }
}

@Composable
private fun GoalItemCard(
    goal: QualitySealGoalEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (goal.isCompleted)
                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f)
            else
                MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("goal_item_${goal.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = goal.isCompleted,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(checkedColor = MeadowGreenSecondary)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textDecoration = if (goal.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (goal.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Categoria: ${goal.requirementCategory}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expandir detalhes"
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 8.dp, end = 8.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = goal.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 17.sp
                    )

                    if (goal.verificationEvidence.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📄 Comprovação: ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(goal.verificationEvidence, fontSize = 11.sp, color = MeadowGreenSecondary)
                        }
                    }

                    if (goal.completedAt != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cumprida em: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(goal.completedAt))}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDelete) {
                            Text("Excluir", color = WarningAlertRed, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddQualityGoalDialog(
    sealType: String,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, cat: String, evidence: String, mandatory: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Boas Práticas & BPF") }
    var evidence by remember { mutableStateOf("") }
    var mandatory by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Nova Meta para o Selo $sealType",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título da Meta (ex: Análise de HMF)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Exigência Técnica / Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = evidence,
                    onValueChange = { evidence = it },
                    label = { Text("Documento de Comprovação (ex: Laudo nº 412)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(title, description, category, evidence, mandatory)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Adicionar Meta", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
