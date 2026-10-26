package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.HoneyGoldContainer
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.viewmodel.ScreenTab

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BottomMenuCustomizationDialog(
    visibleTabs: List<ScreenTab>,
    onDismiss: () -> Unit,
    onSaveTabs: (List<ScreenTab>) -> Unit,
    onResetDefaults: () -> Unit
) {
    // Current editable list of tabs (including DASHBOARD in center)
    var tempTabs by remember {
        mutableStateOf(
            if (visibleTabs.isEmpty()) {
                listOf(
                    ScreenTab.APIARIES,
                    ScreenTab.INSPECTIONS,
                    ScreenTab.DASHBOARD,
                    ScreenTab.HARVESTS,
                    ScreenTab.SCHEDULE,
                    ScreenTab.AI_ASSISTANT
                )
            } else {
                visibleTabs
            }
        )
    }

    val sideTabs = tempTabs.filter { it != ScreenTab.DASHBOARD }
    val allSelectableTabs = ScreenTab.entries.filter { it != ScreenTab.DASHBOARD }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("bottom_menu_customization_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // --- CABEÇALHO ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = HoneyGoldPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Personalizar Menu Inferior",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                            Text(
                                text = "Escolha os atalhos e a ordem dos botões",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), CircleShape)
                            .testTag("close_menu_custom_dialog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // --- PRÉ-VIZUALIZAÇÃO EM TEMPO REAL ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HoneyGoldPrimary.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "👀 PRÉ-VISUALIZAÇÃO DA BARRA INFERIOR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HoneyGoldPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val half = (sideTabs.size + 1) / 2
                        val leftTabs = sideTabs.take(half)
                        val rightTabs = sideTabs.drop(half)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                .padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Lado Esquerdo
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                leftTabs.forEach { tab ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(tab.iconEmoji, fontSize = 16.sp)
                                        Text(
                                            tab.title,
                                            fontSize = 9.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            // Botão Central Fixo "Iniciar"
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(HoneyGoldPrimary)
                                    .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🐝", fontSize = 18.sp)
                            }

                            // Lado Direito
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                rightTabs.forEach { tab ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(tab.iconEmoji, fontSize = 16.sp)
                                        Text(
                                            tab.title,
                                            fontSize = 9.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // --- BOTOES DE ATALHOS RÁPIDOS / PRESETS ---
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            tempTabs = listOf(
                                ScreenTab.APIARIES,
                                ScreenTab.INSPECTIONS,
                                ScreenTab.DASHBOARD,
                                ScreenTab.HARVESTS,
                                ScreenTab.SCHEDULE,
                                ScreenTab.AI_ASSISTANT
                            )
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("⭐ Padrão", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            tempTabs = listOf(ScreenTab.DASHBOARD) + allSelectableTabs
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("📱 Exibir Todos", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            tempTabs = listOf(
                                ScreenTab.APIARIES,
                                ScreenTab.DASHBOARD,
                                ScreenTab.HARVESTS
                            )
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("⚡ Essencial", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- LISTA COM ROLAGEM DOS ITENS PARA ATIVAR / SELECIONAR / REORDENAR ---
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Selecione quais botões e ordem das abas laterais:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    allSelectableTabs.forEach { tab ->
                        val isVisible = sideTabs.contains(tab)
                        val currentIndex = sideTabs.indexOf(tab)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("menu_tab_item_${tab.name.lowercase()}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isVisible) {
                                    HoneyGoldPrimary.copy(alpha = 0.12f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainer
                                }
                            ),
                            border = if (isVisible) {
                                androidx.compose.foundation.BorderStroke(1.dp, HoneyGoldPrimary.copy(alpha = 0.5f))
                            } else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Switch(
                                        checked = isVisible,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                if (!sideTabs.contains(tab)) {
                                                    tempTabs = tempTabs + tab
                                                }
                                            } else {
                                                // Mínimo de 2 abas ativas
                                                if (sideTabs.size > 2) {
                                                    tempTabs = tempTabs.filter { it != tab }
                                                }
                                            }
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = HoneyGoldPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "${tab.iconEmoji} ${tab.title}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                // Botões para reordenar a posição
                                if (isVisible) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (currentIndex > 0) {
                                                    val mutableSide = sideTabs.toMutableList()
                                                    val item = mutableSide.removeAt(currentIndex)
                                                    mutableSide.add(currentIndex - 1, item)
                                                    tempTabs = listOf(ScreenTab.DASHBOARD) + mutableSide
                                                }
                                            },
                                            enabled = currentIndex > 0,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowUpward,
                                                contentDescription = "Mover para cima",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                if (currentIndex < sideTabs.size - 1) {
                                                    val mutableSide = sideTabs.toMutableList()
                                                    val item = mutableSide.removeAt(currentIndex)
                                                    mutableSide.add(currentIndex + 1, item)
                                                    tempTabs = listOf(ScreenTab.DASHBOARD) + mutableSide
                                                }
                                            },
                                            enabled = currentIndex < sideTabs.size - 1,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDownward,
                                                contentDescription = "Mover para baixo",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // --- BOTÕES DE AÇÃO RODAPÉ ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            tempTabs = listOf(
                                ScreenTab.APIARIES,
                                ScreenTab.INSPECTIONS,
                                ScreenTab.DASHBOARD,
                                ScreenTab.HARVESTS,
                                ScreenTab.SCHEDULE,
                                ScreenTab.AI_ASSISTANT
                            )
                            onResetDefaults()
                        },
                        modifier = Modifier.testTag("reset_menu_custom_btn")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restaurar", fontSize = 13.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                onSaveTabs(tempTabs)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HoneyGoldPrimary,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier.testTag("save_menu_custom_btn")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salvar Menu", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
