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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.viewmodel.DashboardCardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardCustomizationDialog(
    currentCards: List<DashboardCardType>,
    onSaveCards: (List<DashboardCardType>) -> Unit,
    onResetDefaults: () -> Unit,
    onDismiss: () -> Unit
) {
    // Current ordered list of active cards
    var activeCards by remember(currentCards) { mutableStateOf(currentCards) }

    // All available cards (to allow enabling ones that are currently disabled)
    val allCards = remember { DashboardCardType.entries }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dashboard_customization_dialog")
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(HoneyGoldPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DashboardCustomize,
                                contentDescription = null,
                                tint = HoneyGoldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Personalizar Iniciar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Organize a ordem e ative os cards",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Disposição dos Cards:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                // List of all cards with reorder controls and visibility switches
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Display current active cards in order
                    itemsIndexed(activeCards) { index, card ->
                        CardItemRow(
                            card = card,
                            isEnabled = true,
                            canMoveUp = index > 0,
                            canMoveDown = index < activeCards.size - 1,
                            onMoveUp = {
                                if (index > 0) {
                                    val mutable = activeCards.toMutableList()
                                    val temp = mutable[index]
                                    mutable[index] = mutable[index - 1]
                                    mutable[index - 1] = temp
                                    activeCards = mutable
                                }
                            },
                            onMoveDown = {
                                if (index < activeCards.size - 1) {
                                    val mutable = activeCards.toMutableList()
                                    val temp = mutable[index]
                                    mutable[index] = mutable[index + 1]
                                    mutable[index + 1] = temp
                                    activeCards = mutable
                                }
                            },
                            onToggle = {
                                // Must keep at least 1 card
                                if (activeCards.size > 1) {
                                    activeCards = activeCards.filter { it != card }
                                }
                            }
                        )
                    }

                    // Display inactive cards
                    val inactiveCards = allCards.filter { !activeCards.contains(it) }
                    if (inactiveCards.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Cards Desativados (Toque para ativar):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        itemsIndexed(inactiveCards) { _, card ->
                            CardItemRow(
                                card = card,
                                isEnabled = false,
                                canMoveUp = false,
                                canMoveDown = false,
                                onMoveUp = {},
                                onMoveDown = {},
                                onToggle = {
                                    activeCards = activeCards + card
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            onResetDefaults()
                            activeCards = DashboardCardType.entries
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Padrão", fontSize = 12.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancelar", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                onSaveCards(activeCards)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Salvar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardItemRow(
    card: DashboardCardType,
    isEnabled: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = card.iconEmoji,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = card.description,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isEnabled) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = canMoveUp,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Mover para cima",
                            tint = if (canMoveUp) HoneyGoldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onMoveDown,
                        enabled = canMoveDown,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Mover para baixo",
                            tint = if (canMoveDown) HoneyGoldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = HoneyGoldPrimary,
                        checkedTrackColor = HoneyGoldPrimary.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.size(38.dp)
                )
            }
        }
    }
}
