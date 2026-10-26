package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entities.InventoryEntity
import com.example.data.local.entities.TaskScheduleEntity
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.WarningAlertRed
import com.example.ui.viewmodel.ScreenTab
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SystemNotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val isUrgent: Boolean,
    val categoryLabel: String,
    val targetTab: ScreenTab
)

@Composable
fun NotificationCenterDialog(
    tasks: List<TaskScheduleEntity>,
    lowStockItems: List<InventoryEntity>,
    sanitaryAlertCount: Int,
    onDismiss: () -> Unit,
    onNavigateToTab: (ScreenTab) -> Unit
) {
    val notificationList = mutableListOf<SystemNotificationItem>()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    if (sanitaryAlertCount > 0) {
        notificationList.add(
            SystemNotificationItem(
                id = "sanitary_alert",
                title = "🚨 Alerta Sanitário de Campo",
                message = "Existe $sanitaryAlertCount alerta(s) de varroose, postura fraca ou peste detectado nas inspeções.",
                isUrgent = true,
                categoryLabel = "Sanidade",
                targetTab = ScreenTab.APIARIES
            )
        )
    }

    lowStockItems.forEach { item ->
        notificationList.add(
            SystemNotificationItem(
                id = "stock_${item.id}",
                title = "📦 Estoque Baixo: ${item.name}",
                message = "Restam apenas ${item.quantity} ${item.unit}. Quantidade mínima ideal é ${item.minThreshold} ${item.unit}.",
                isUrgent = false,
                categoryLabel = "Estoque",
                targetTab = ScreenTab.INVENTORY
            )
        )
    }

    tasks.filter { !it.isCompleted }.forEach { task ->
        val formattedDate = dateFormat.format(Date(task.dueDate))
        notificationList.add(
            SystemNotificationItem(
                id = "task_${task.id}",
                title = "📅 Manejo Pendente: ${task.title}",
                message = "Data agendada: $formattedDate • Prioridade: ${task.priority}.",
                isUrgent = task.priority.equals("ALTA", ignoreCase = true) || task.priority.contains("URGENTE", ignoreCase = true),
                categoryLabel = "Manejos",
                targetTab = ScreenTab.SCHEDULE
            )
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().testTag("notification_center_dialog")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = HoneyGoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Central de Notificações",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("close_notifications_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (notificationList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🎉 Nenhuma notificação urgente ou alerta pendente no momento!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 340.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(notificationList, key = { it.id }) { item ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (item.isUrgent) WarningAlertRed.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = item.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = if (item.isUrgent) WarningAlertRed else MaterialTheme.colorScheme.onSurface
                                        )

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (item.isUrgent) WarningAlertRed.copy(alpha = 0.2f)
                                                    else HoneyGoldPrimary.copy(alpha = 0.2f)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = item.categoryLabel,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (item.isUrgent) WarningAlertRed else HoneyGoldPrimary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = item.message,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            onDismiss()
                                            onNavigateToTab(item.targetTab)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (item.isUrgent) WarningAlertRed else HoneyGoldPrimary
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.align(Alignment.End).height(32.dp)
                                    ) {
                                        Text("Ver Detalhes", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
