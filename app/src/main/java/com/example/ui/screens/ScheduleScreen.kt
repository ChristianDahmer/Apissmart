package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entities.ApiaryEntity
import com.example.data.local.entities.TaskScheduleEntity
import com.example.ui.components.PriorityBadge
import com.example.ui.components.ScheduleCalendarView
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.MeadowGreenSecondary
import com.example.util.ApisNotificationHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ScheduleScreen(
    tasks: List<TaskScheduleEntity>,
    apiaries: List<ApiaryEntity>,
    onToggleTask: (TaskScheduleEntity) -> Unit,
    onSaveTask: (title: String, description: String, taskType: String, apiaryId: Long?, hiveId: Long?, apiaryName: String?, hiveNumber: String?, dueDate: Long, priority: String) -> Unit,
    onDeleteTask: (TaskScheduleEntity) -> Unit,
    onOpenMaintenanceTips: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("Pendentes") }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Trigger notifications for tasks due today
    LaunchedEffect(tasks) {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        val todayStart = calendar.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
        val todayEnd = todayStart + (24 * 60 * 60 * 1000L) - 1

        val prefs = context.getSharedPreferences("apis_notifications", Context.MODE_PRIVATE)
        
        tasks.filter { !it.isCompleted && it.dueDate in todayStart..todayEnd }.forEach { task ->
            val hasBeenNotified = prefs.getBoolean("notified_${task.id}_${task.dueDate}", false)
            if (!hasBeenNotified) {
                ApisNotificationHelper.showTaskDueNotification(context, task.id, task.title, dateFormat.format(Date(task.dueDate)))
                prefs.edit().putBoolean("notified_${task.id}_${task.dueDate}", true).apply()
            }
        }
    }

    val filterOptions = listOf("Pendentes", "Todas", "Concluídas", "Sanitárias")

    val filteredTasks = tasks.filter { task ->
        val matchesFilter = when (selectedFilter) {
            "Pendentes" -> !task.isCompleted
            "Concluídas" -> task.isCompleted
            "Sanitárias" -> task.priority == "URGENTE_SANITÁRIA" || task.taskType.contains("Sanitário", ignoreCase = true)
            else -> true
        }

        val matchesDate = if (selectedDateMillis != null) {
            val tCal = Calendar.getInstance().apply {
                timeInMillis = task.dueDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val sCal = Calendar.getInstance().apply {
                timeInMillis = selectedDateMillis!!
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            tCal.timeInMillis == sCal.timeInMillis
        } else {
            true
        }

        matchesFilter && matchesDate
    }

    Scaffold(
        modifier = modifier.testTag("schedule_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = HoneyGoldPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .testTag("add_task_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Agendar Manejo")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card Guia Técnico de Manejo & Dicas de Manutenção
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenMaintenanceTips() }
                        .testTag("schedule_maintenance_tips_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(HoneyGoldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TipsAndUpdates,
                                contentDescription = null,
                                tint = HoneyGoldPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Dicas de Manejo & Manutenção",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = HoneyGoldPrimary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "BOAS PRÁTICAS",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HoneyGoldPrimary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Consulte protocolos técnicos e agende manutenções com 1 clique.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedButton(
                            onClick = onOpenMaintenanceTips,
                            shape = RoundedCornerShape(8.dp),
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
                        text = "CRONOGRAMA & CALENDÁRIO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${filteredTasks.size} ${if (filteredTasks.size == 1) "tarefa" else "tarefas"}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Interactive Ultra-Compact Calendar Component
            item {
                ScheduleCalendarView(
                    tasks = tasks,
                    selectedDateMillis = selectedDateMillis,
                    onDateSelected = { selectedDateMillis = it },
                    onDateClickedEmpty = { date ->
                        selectedDateMillis = date
                        showAddTaskDialog = true
                    }
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filterOptions) { opt ->
                        FilterChip(
                            selected = selectedFilter == opt,
                            onClick = { selectedFilter = opt },
                            label = { Text(opt, fontSize = 11.sp) },
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }
            }

            if (filteredTasks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Nenhuma tarefa neste período ou filtro.", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Clique no botão '+' para agendar novos manejos personalizados.")
                        }
                    }
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskCardItem(
                        task = task,
                        dueDateFormatted = dateFormat.format(Date(task.dueDate)),
                        onToggle = { onToggleTask(task) },
                        onDelete = { onDeleteTask(task) }
                    )
                }
            }
        }
    }

    if (showAddTaskDialog) {
        TaskFormDialog(
            apiaries = apiaries,
            context = context,
            existingTasks = tasks,
            initialDateMillis = selectedDateMillis,
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, desc, type, apiaryId, apiaryName, dueDate, priority ->
                onSaveTask(title, desc, type, apiaryId, null, apiaryName, null, dueDate, priority)
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun TaskCardItem(
    task: TaskScheduleEntity,
    dueDateFormatted: String,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (task.priority == "URGENTE_SANITÁRIA" && !task.isCompleted) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surface
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
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (task.isCompleted) MeadowGreenSecondary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Concluída", tint = MeadowGreenSecondary, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.RadioButtonUnchecked, contentDescription = "Pendente", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🏷️ [${task.taskType}] ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HoneyGoldPrimary
                    )
                }
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📅 Prazo: $dueDateFormatted • ${task.apiaryName ?: "Geral"}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(horizontalAlignment = Alignment.End) {
                PriorityBadge(priority = task.priority)
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Excluir", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

data class TaskTitleSuggestion(
    val title: String,
    val taskType: String,
    val defaultDescription: String,
    val defaultPriority: String
)

val DEFAULT_TASK_TITLE_SUGGESTIONS = listOf(
    // Alimentação
    TaskTitleSuggestion(
        "Alimentação estimulante com xarope 1:1",
        "Alimentação Artificial",
        "Fornecer xarope energético 1:1 para estimular postura da rainha na pré-florada.",
        "MÉDIA"
    ),
    TaskTitleSuggestion(
        "Fornecimento de pasta proteica / pólen",
        "Alimentação Artificial",
        "Colocar bife proteico sobre os quadros do ninho para fortalecimento das crias.",
        "MÉDIA"
    ),
    TaskTitleSuggestion(
        "Alimentação concentrada 2:1 pré-inverno",
        "Alimentação Artificial",
        "Fornecer xarope denso para estocagem de reserva energética no período de entressafra.",
        "ALTA"
    ),
    TaskTitleSuggestion(
        "Limpeza e abastecimento de alimentadores",
        "Alimentação Artificial",
        "Higienizar alimentadores de cocho ou externos e repor xarope fresco.",
        "BAIXA"
    ),

    // Sanidade / Varroa
    TaskTitleSuggestion(
        "Aplicação de tiras de Ácido Oxálico",
        "Tratamento Sanitário",
        "Instalar tiras de papelão com ácido oxálico e glicerina entre quadros de cria para controle de Varroa.",
        "URGENTE_SANITÁRIA"
    ),
    TaskTitleSuggestion(
        "Monitoramento de infestação de Varroa",
        "Tratamento Sanitário",
        "Realizar teste do açúcar em amostra de abelhas para calcular percentual de infestação.",
        "ALTA"
    ),
    TaskTitleSuggestion(
        "Tratamento fitoterápico com Timol",
        "Tratamento Sanitário",
        "Aplicar sachê ou evaporador com timol no ninho conforme temperatura ambiente.",
        "ALTA"
    ),
    TaskTitleSuggestion(
        "Desinfecção de fundos com maçarico",
        "Tratamento Sanitário",
        "Flamejar peças de madeira para esterilização e profilaxia contra patógenos.",
        "MÉDIA"
    ),

    // Revisão / Inspeção
    TaskTitleSuggestion(
        "Revisão de postura e espaço da rainha",
        "Revisão / Inspeção",
        "Verificar padrão de postura (ovos, larvas e crias operculadas uniformes) e área útil.",
        "MÉDIA"
    ),
    TaskTitleSuggestion(
        "Avaliação de reservas de mel e pólen",
        "Revisão / Inspeção",
        "Conferir se a colmeia possui pelo menos 2 quadros laterais com alimento estocado.",
        "MÉDIA"
    ),
    TaskTitleSuggestion(
        "Inspeção sanitária de alvéolos e crias",
        "Revisão / Inspeção",
        "Observar sanidade das crias, ausência de opérculos perfurados ou crias calafriadas.",
        "ALTA"
    ),
    TaskTitleSuggestion(
        "Revisão pré-safra e troca de alvados",
        "Revisão / Inspeção",
        "Retirar redutores de entrada e preparar a colmeia para máxima entrada de néctar.",
        "MÉDIA"
    ),

    // Rainha & Multiplicação
    TaskTitleSuggestion(
        "Introdução de rainha fecundada",
        "Troca de Rainha",
        "Instalar gaiola com a nova rainha e docinho candi; verificar aceitação em 48h.",
        "ALTA"
    ),
    TaskTitleSuggestion(
        "Eliminação de realeiras de enxameação",
        "Troca de Rainha",
        "Destruir realeiras naturais nas bordas dos favos para evitar enxameação iminente.",
        "ALTA"
    ),
    TaskTitleSuggestion(
        "Marcação da rainha no tórax",
        "Troca de Rainha",
        "Localizar a rainha e aplicar marcador atóxico com a cor internacional do ano.",
        "BAIXA"
    ),
    TaskTitleSuggestion(
        "Divisão de colmeia / Formação de núcleo",
        "Divisão de Enxame",
        "Transferir 3 quadros de cria madura e 2 de alimento para nova caixa núcleo.",
        "ALTA"
    ),

    // Produção / Espaço / Cera
    TaskTitleSuggestion(
        "Instalação de melgueira com cera alveolada",
        "Instalação de Melgueiras",
        "Adicionar melgueira com quadros novos de cera sobre a tela excluidora.",
        "MÉDIA"
    ),
    TaskTitleSuggestion(
        "Troca e descarte de quadros velhos",
        "Troca de Cera",
        "Substituir quadros escuros/deformados por quadros novos aramados com cera.",
        "BAIXA"
    ),
    TaskTitleSuggestion(
        "Instalação de tela excluidora de rainha",
        "Instalação de Melgueiras",
        "Posicionar tela excluidora entre o ninho e a primeira melgueira para mel puro.",
        "MÉDIA"
    ),
    TaskTitleSuggestion(
        "Colheita de melgueiras operculadas (>80%)",
        "Colheita de Mel",
        "Retirar melgueiras maduras para centrifugação e beneficiamento do mel.",
        "ALTA"
    ),
    TaskTitleSuggestion(
        "Adição de sobrecaixa de melgueira",
        "Colheita de Mel",
        "Inserir melgueira extra abaixo da melgueira já quase cheia em plena florada.",
        "MÉDIA"
    )
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskFormDialog(
    apiaries: List<ApiaryEntity>,
    context: Context,
    existingTasks: List<TaskScheduleEntity> = emptyList(),
    initialDateMillis: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, taskType: String, apiaryId: Long?, apiaryName: String?, dueDate: Long, priority: String) -> Unit
) {
    val prefs = remember { context.getSharedPreferences("apis_task_types_prefs", Context.MODE_PRIVATE) }
    var customTypes by remember {
        mutableStateOf(
            prefs.getStringSet("custom_types", emptySet())?.toList() ?: emptyList()
        )
    }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var taskType by remember { mutableStateOf("Alimentação Artificial") }
    var expandedTaskTypeMenu by remember { mutableStateOf(false) }
    var showNewTypeDialog by remember { mutableStateOf(false) }
    var newCustomTypeInput by remember { mutableStateOf("") }

    var selectedApiaryId by remember { mutableStateOf(apiaries.firstOrNull()?.id) }
    var expandedApiaryMenu by remember { mutableStateOf(false) }
    
    // Use initial date or today
    var selectedDate by remember {
        mutableStateOf(initialDateMillis ?: System.currentTimeMillis())
    }
    
    var priority by remember { mutableStateOf("MÉDIA") }

    val defaultTypes = listOf(
        "Alimentação Artificial",
        "Tratamento Sanitário",
        "Troca de Rainha",
        "Divisão de Enxame",
        "Revisão / Inspeção",
        "Colheita de Mel",
        "Troca de Cera",
        "Controle de Varroa",
        "Instalação de Melgueiras",
        "Alimentação Energética / Proteica"
    )
    val allTaskTypes = (defaultTypes + customTypes).distinct()
    val priorities = listOf("BAIXA", "MÉDIA", "ALTA", "URGENTE_SANITÁRIA")

    // Sugestões dinâmicas para o título da tarefa baseadas no tipo de manejo ou texto digitado
    val availableTitleSuggestions = remember(title, taskType, existingTasks) {
        val itemsFromExisting = existingTasks.map {
            TaskTitleSuggestion(it.title, it.taskType, it.description, it.priority)
        }
        val combined = (DEFAULT_TASK_TITLE_SUGGESTIONS + itemsFromExisting).distinctBy { it.title.lowercase().trim() }
        if (title.isBlank()) {
            val forType = combined.filter {
                it.taskType.contains(taskType, ignoreCase = true) || taskType.contains(it.taskType, ignoreCase = true)
            }
            if (forType.isNotEmpty()) forType else combined.take(8)
        } else {
            combined.filter { it.title.contains(title, ignoreCase = true) }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
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
                        Text(
                            text = "Novo Agendamento de Manejo",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    val apiaryName = apiaries.find { it.id == selectedApiaryId }?.name
                                    onConfirm(title.trim(), description.trim(), taskType.trim(), selectedApiaryId, apiaryName, selectedDate, priority)
                                }
                            },
                            enabled = title.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary)
                        ) {
                            Text("Salvar")
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), CircleShape)
                                .testTag("schedule_dialog_close_btn")
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

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título da Tarefa") },
                        placeholder = { Text("Ex: Alimentar com xarope 1:1") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("schedule_task_title_input"),
                        trailingIcon = {
                            if (title.isNotBlank()) {
                                IconButton(onClick = { title = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Limpar título",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    )

                    // Barra de Sugestões de Manejo para o Título
                    if (availableTitleSuggestions.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "💡 Sugestões de Manejo:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HoneyGoldPrimary
                                )
                                Text(
                                    text = "Toque para aplicar",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(availableTitleSuggestions.take(10)) { suggestion ->
                                    SuggestionChip(
                                        onClick = {
                                            title = suggestion.title
                                            if (description.isBlank()) {
                                                description = suggestion.defaultDescription
                                            }
                                            if (suggestion.taskType.isNotBlank()) {
                                                taskType = suggestion.taskType
                                            }
                                            if (suggestion.defaultPriority.isNotBlank()) {
                                                priority = suggestion.defaultPriority
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = suggestion.title,
                                                fontSize = 11.sp,
                                                maxLines = 1
                                            )
                                        },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                        ),
                                        modifier = Modifier.testTag("task_title_suggestion_${suggestion.title.hashCode()}")
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Detalhes / Instruções de Manejo") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Task Type Selection Combo
                    Text("Tipo de Manejo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    ExposedDropdownMenuBox(
                        expanded = expandedTaskTypeMenu,
                        onExpandedChange = { expandedTaskTypeMenu = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("schedule_task_type_combo_box")
                    ) {
                        OutlinedTextField(
                            value = taskType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selecione o Tipo de Manejo") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTaskTypeMenu)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                                .testTag("schedule_task_type_combo")
                        )

                        ExposedDropdownMenu(
                            expanded = expandedTaskTypeMenu,
                            onDismissRequest = { expandedTaskTypeMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            // Opção especial para cadastrar novo tipo no topo do combo
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = HoneyGoldPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "➕ Cadastrar novo tipo de manejo...",
                                            fontWeight = FontWeight.Bold,
                                            color = HoneyGoldPrimary
                                        )
                                    }
                                },
                                onClick = {
                                    expandedTaskTypeMenu = false
                                    showNewTypeDialog = true
                                },
                                modifier = Modifier.testTag("combo_add_new_task_type_item")
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // Exibição de todos os tipos de manejo cadastrados
                            allTaskTypes.forEach { type ->
                                val isCustom = customTypes.contains(type)
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = type,
                                                fontWeight = if (type == taskType) FontWeight.Bold else FontWeight.Normal,
                                                color = if (type == taskType) HoneyGoldPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isCustom) {
                                                Text(
                                                    text = "Personalizado",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        taskType = type
                                        expandedTaskTypeMenu = false
                                    },
                                    leadingIcon = {
                                        Text(
                                            text = when {
                                                type.contains("Alimentação", ignoreCase = true) -> "🍯"
                                                type.contains("Sanitário", ignoreCase = true) || type.contains("Varroa", ignoreCase = true) -> "💊"
                                                type.contains("Rainha", ignoreCase = true) -> "👑"
                                                type.contains("Divisão", ignoreCase = true) -> "🔀"
                                                type.contains("Colheita", ignoreCase = true) -> "⚖️"
                                                type.contains("Cera", ignoreCase = true) -> "🕯️"
                                                type.contains("Melgueira", ignoreCase = true) -> "📦"
                                                type.contains("Revisão", ignoreCase = true) || type.contains("Inspeção", ignoreCase = true) -> "🔍"
                                                else -> "🐝"
                                            },
                                            fontSize = 16.sp
                                        )
                                    },
                                    modifier = Modifier.testTag("task_type_item_${type.replace(" ", "_")}")
                                )
                            }
                        }
                    }

                    // Modal Dialog para cadastrar novo tipo de manejo
                    if (showNewTypeDialog) {
                        AlertDialog(
                            onDismissRequest = {
                                showNewTypeDialog = false
                                newCustomTypeInput = ""
                            },
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("➕", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Novo Tipo de Manejo", fontWeight = FontWeight.Bold)
                                }
                            },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Digite o nome da nova operação ou manejo para incluir no catálogo de tarefas:",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    OutlinedTextField(
                                        value = newCustomTypeInput,
                                        onValueChange = { newCustomTypeInput = it },
                                        label = { Text("Nome do Manejo") },
                                        placeholder = { Text("Ex: Instalação de Tela Excluidora") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("new_custom_type_input")
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        val trimmed = newCustomTypeInput.trim()
                                        if (trimmed.isNotBlank()) {
                                            val updated = (customTypes + trimmed).distinct()
                                            customTypes = updated
                                            prefs.edit().putStringSet("custom_types", updated.toSet()).apply()
                                            taskType = trimmed
                                            newCustomTypeInput = ""
                                            showNewTypeDialog = false
                                        }
                                    },
                                    enabled = newCustomTypeInput.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary),
                                    modifier = Modifier.testTag("confirm_new_task_type_btn")
                                ) {
                                    Text("Cadastrar")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        showNewTypeDialog = false
                                        newCustomTypeInput = ""
                                    }
                                ) {
                                    Text("Cancelar")
                                }
                            }
                        )
                    }

                    // Apiary Selection Dropdown
                    if (apiaries.isNotEmpty()) {
                        Text("Apiário Relacionado", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        ExposedDropdownMenuBox(
                            expanded = expandedApiaryMenu,
                            onExpandedChange = { expandedApiaryMenu = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val selectedName = apiaries.find { it.id == selectedApiaryId }?.name ?: "Geral (Todos os Apiários)"
                            OutlinedTextField(
                                value = selectedName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Selecione o Apiário") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedApiaryMenu)
                                },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                                    .testTag("schedule_apiary_combo")
                            )

                            ExposedDropdownMenu(
                                expanded = expandedApiaryMenu,
                                onDismissRequest = { expandedApiaryMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Geral (Todos os Apiários)") },
                                    onClick = {
                                        selectedApiaryId = null
                                        expandedApiaryMenu = false
                                    }
                                )
                                apiaries.forEach { ap ->
                                    DropdownMenuItem(
                                        text = { Text(ap.name) },
                                        onClick = {
                                            selectedApiaryId = ap.id
                                            expandedApiaryMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Date Selection
                    Text("Data do Manejo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    val datePickerDialog = remember(selectedDate) {
                        val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
                        android.app.DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val newCal = Calendar.getInstance().apply {
                                    set(year, month, day)
                                }
                                selectedDate = newCal.timeInMillis
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        )
                    }

                    OutlinedButton(
                        onClick = { datePickerDialog.show() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedDate))
                        Text("Data Selecionada: $formattedDate")
                    }

                    Text("Prioridade", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        priorities.forEach { p ->
                            PriorityBadge(
                                priority = p,
                                modifier = Modifier
                                    .clickable { priority = p }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
