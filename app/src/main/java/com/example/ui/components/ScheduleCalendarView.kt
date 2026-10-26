package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.TaskScheduleEntity
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.MeadowGreenSecondary
import java.util.Calendar
import java.util.Locale

@Composable
fun ScheduleCalendarView(
    tasks: List<TaskScheduleEntity>,
    selectedDateMillis: Long?,
    onDateSelected: (Long?) -> Unit,
    onDateClickedEmpty: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val todayCalendar = remember { Calendar.getInstance() }
    val todayYear = todayCalendar.get(Calendar.YEAR)
    val todayMonth = todayCalendar.get(Calendar.MONTH)
    val todayDay = todayCalendar.get(Calendar.DAY_OF_MONTH)

    var currentYear by remember { mutableIntStateOf(todayYear) }
    var currentMonth by remember { mutableIntStateOf(todayMonth) }
    var isExpanded by remember { mutableStateOf(false) } // Compact week view by default
    var isMinimized by remember { mutableStateOf(false) } // Allow collapsing to single status bar

    val monthNames = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")
    val daysOfWeek = listOf("D", "S", "T", "Q", "Q", "S", "S")

    val tasksDaySet = remember(tasks) {
        tasks.map {
            val c = Calendar.getInstance().apply {
                timeInMillis = it.dueDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            c.timeInMillis
        }.toSet()
    }

    // Days Grid calculation for the month
    val daysInMonthList = remember(currentYear, currentMonth) {
        val cal = Calendar.getInstance().apply {
            set(currentYear, currentMonth, 1)
        }
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val startDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday
        val list = mutableListOf<Long?>()
        for (i in 0 until startDayOfWeek) {
            list.add(null)
        }
        for (d in 1..maxDays) {
            val c = Calendar.getInstance().apply {
                set(currentYear, currentMonth, d, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            list.add(c.timeInMillis)
        }
        list
    }

    val rows = daysInMonthList.chunked(7)

    // Calculate visible rows: either all rows (month) or just active week row (compact)
    val visibleRows = if (isExpanded) {
        rows
    } else {
        val targetMillis = selectedDateMillis ?: System.currentTimeMillis()
        val targetCal = Calendar.getInstance().apply { timeInMillis = targetMillis }
        val targetDay = targetCal.get(Calendar.DAY_OF_MONTH)
        val targetMonth = targetCal.get(Calendar.MONTH)
        val targetYear = targetCal.get(Calendar.YEAR)

        val activeRowIndex = rows.indexOfFirst { row ->
            row.any { dayMillis ->
                if (dayMillis == null) false
                else {
                    val dCal = Calendar.getInstance().apply { timeInMillis = dayMillis }
                    dCal.get(Calendar.YEAR) == targetYear &&
                    dCal.get(Calendar.MONTH) == targetMonth &&
                    dCal.get(Calendar.DAY_OF_MONTH) == targetDay
                }
            }
        }.takeIf { it >= 0 } ?: rows.indexOfFirst { row -> row.any { it != null } }.coerceAtLeast(0)

        if (rows.isNotEmpty()) listOf(rows[activeRowIndex]) else emptyList()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("compact_schedule_calendar_view"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            // Header Bar: Month, Year, Filter Pill & Inline Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Month/Year + Compact Filter Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = "${monthNames[currentMonth]} $currentYear",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = HoneyGoldPrimary
                    )

                    // Inline Filter Indicator Chip (saves vertical space)
                    if (selectedDateMillis != null) {
                        val selCal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                        val dateStr = String.format(Locale.getDefault(), "%02d/%02d", selCal.get(Calendar.DAY_OF_MONTH), selCal.get(Calendar.MONTH) + 1)
                        Surface(
                            onClick = { onDateSelected(null) },
                            shape = RoundedCornerShape(12.dp),
                            color = HoneyGoldPrimary.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, HoneyGoldPrimary.copy(alpha = 0.4f)),
                            modifier = Modifier.testTag("calendar_clear_filter_badge")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = dateStr,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HoneyGoldPrimary
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpar filtro de data",
                                    modifier = Modifier.size(10.dp),
                                    tint = HoneyGoldPrimary
                                )
                            }
                        }
                    }
                }

                // Right side: Compact Controls (Today Jump, View Mode Toggle, Prev/Next, Minimize)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Quick "Hoje" jump button if outside current month
                    if (currentMonth != todayMonth || currentYear != todayYear) {
                        Surface(
                            onClick = {
                                currentMonth = todayMonth
                                currentYear = todayYear
                            },
                            shape = RoundedCornerShape(4.dp),
                            color = HoneyGoldPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.padding(end = 2.dp)
                        ) {
                            Text(
                                text = "Hoje",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = HoneyGoldPrimary,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (!isMinimized) {
                        // Toggle Semana (Compacto) vs Mês (Completo)
                        Surface(
                            onClick = { isExpanded = !isExpanded },
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.testTag("calendar_toggle_compact_mode_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (isExpanded) "Modo Semana" else "Modo Mês",
                                    modifier = Modifier.size(11.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = if (isExpanded) "Mês" else "Sem",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Month Navigation Arrows (Compact)
                        IconButton(
                            onClick = {
                                if (currentMonth == 0) {
                                    currentMonth = 11
                                    currentYear -= 1
                                } else {
                                    currentMonth -= 1
                                }
                            },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Mês Anterior",
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (currentMonth == 11) {
                                    currentMonth = 0
                                    currentYear += 1
                                } else {
                                    currentMonth += 1
                                }
                            },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Próximo Mês",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // Minimize / Expand Full Calendar View
                    IconButton(
                        onClick = { isMinimized = !isMinimized },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = if (isMinimized) Icons.Default.CalendarMonth else Icons.Default.ExpandLess,
                            contentDescription = if (isMinimized) "Expandir Calendário" else "Recolher Calendário",
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Animated Collapsible Body
            AnimatedVisibility(
                visible = !isMinimized,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(2.dp))

                    // Days of Week Header (Single-letter, responsive weight)
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        daysOfWeek.forEach { day ->
                            Text(
                                text = day,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Days Grid (Ultra-compact 24dp cells with full tap accessibility)
                    visibleRows.forEach { rowDays ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            rowDays.forEach { dayMillis ->
                                if (dayMillis == null) {
                                    Spacer(modifier = Modifier.weight(1f).height(27.dp))
                                } else {
                                    val dayCal = Calendar.getInstance().apply { timeInMillis = dayMillis }
                                    val dayNum = dayCal.get(Calendar.DAY_OF_MONTH)
                                    val isToday = dayCal.get(Calendar.YEAR) == todayYear &&
                                            dayCal.get(Calendar.MONTH) == todayMonth &&
                                            dayNum == todayDay
                                    val hasTask = tasksDaySet.contains(dayMillis)
                                    val isSelected = selectedDateMillis?.let {
                                        val sCal = Calendar.getInstance().apply {
                                            timeInMillis = it
                                            set(Calendar.HOUR_OF_DAY, 0)
                                            set(Calendar.MINUTE, 0)
                                            set(Calendar.SECOND, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }
                                        sCal.timeInMillis == dayMillis
                                    } ?: false

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(27.dp)
                                            .clickable {
                                                if (hasTask) {
                                                    if (isSelected) {
                                                        onDateSelected(null)
                                                    } else {
                                                        onDateSelected(dayMillis)
                                                    }
                                                } else {
                                                    onDateClickedEmpty(dayMillis)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(23.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when {
                                                        isSelected -> HoneyGoldPrimary
                                                        hasTask -> MeadowGreenSecondary.copy(alpha = 0.20f)
                                                        else -> Color.Transparent
                                                    }
                                                )
                                                .then(
                                                    if (isToday && !isSelected) {
                                                        Modifier.border(1.dp, HoneyGoldPrimary, CircleShape)
                                                    } else {
                                                        Modifier
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = "$dayNum",
                                                    fontSize = 9.5.sp,
                                                    fontWeight = if (isSelected || hasTask || isToday) FontWeight.Bold else FontWeight.Medium,
                                                    color = when {
                                                        isSelected -> Color.White
                                                        isToday -> HoneyGoldPrimary
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    },
                                                    lineHeight = 10.sp
                                                )
                                                if (hasTask && !isSelected) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(2.5.dp)
                                                            .clip(CircleShape)
                                                            .background(MeadowGreenSecondary)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(1.dp))
                    }
                }
            }
        }
    }
}
