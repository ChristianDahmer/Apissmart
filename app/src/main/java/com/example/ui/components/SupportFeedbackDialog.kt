package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

val HoneyPrimary = Color(0xFFD97706) // Amber 600
val HoneyDark = Color(0xFFB45309)
val HoneyLight = Color(0xFFFEF3C7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportFeedbackDialog(
    onDismiss: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Suporte, 1: Feedback, 2: FAQ
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(HoneyDark, HoneyPrimary, Color(0xFFF59E0B))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
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
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SupportAgent,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Central de Ajuda & Suporte",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Estamos aqui para ajudar sua apicultura",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fechar",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Custom TabRow
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        text = { Text("Suporte Técnico", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        text = { Text("Feedback & Sugestões", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab == 2 },
                        icon = { Icon(Icons.Default.HelpCenter, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        text = { Text("FAQ & Dúvidas", fontWeight = FontWeight.SemiBold) }
                    )
                }

                // Content Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    when (selectedTab) {
                        0 -> SupportFormTab(onDismiss)
                        1 -> FeedbackFormTab(onDismiss)
                        2 -> FaqTab(onOpenPrivacyPolicy = onOpenPrivacyPolicy)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportFormTab(onDismiss: () -> Unit) {
    val scrollState = rememberScrollState()
    var problemType by remember { mutableStateOf("Erro no Aplicativo / Falha") }
    var problemTypeExpanded by remember { mutableStateOf(false) }
    
    var priority by remember { mutableStateOf("Média") }
    var userEmail by remember { mutableStateOf("apicultor@email.com") }
    var description by remember { mutableStateOf("") }
    var includeLogs by remember { mutableStateOf(true) }
    var isSubmitted by remember { mutableStateOf(false) }
    var ticketId by remember { mutableStateOf("") }

    if (isSubmitted) {
        SuccessTicketView(
            ticketNumber = ticketId,
            message = "Seu chamado de suporte técnico foi registrado com sucesso! Nossa equipe técnica responderá em até 12 horas úteis.",
            onDone = onDismiss
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = HoneyLight.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = HoneyDark)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Encontrou dificuldades no manejo, sensores ou sincronização? Descreva abaixo para análise prioritária.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Problem Category Selection (Combobox / ExposedDropdownMenuBox)
        Text("Tipo de Problema", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        
        val categories = listOf("Erro no Aplicativo / Falha", "Conexão Bluetooth / Sensores", "Dúvida de Manejo / Colmeias", "Licenciamento e CNPJ", "Outro")
        
        ExposedDropdownMenuBox(
            expanded = problemTypeExpanded,
            onExpandedChange = { problemTypeExpanded = !problemTypeExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = problemType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Selecione o Tipo de Problema") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = problemTypeExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            ExposedDropdownMenu(
                expanded = problemTypeExpanded,
                onDismissRequest = { problemTypeExpanded = false }
            ) {
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = {
                            problemType = cat
                            problemTypeExpanded = false
                        }
                    )
                }
            }
        }

        // Priority Selection
        Text("Urgência do Chamado", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Baixa", "Média", "Alta", "Urgente").forEach { p ->
                FilterChip(
                    selected = priority == p,
                    onClick = { priority = p },
                    label = { Text(p) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = HoneyPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Contact Email
        OutlinedTextField(
            value = userEmail,
            onValueChange = { userEmail = it },
            label = { Text("E-mail para Contato") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Description
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descrição detalhada do problema *") },
            placeholder = { Text("Descreva o que aconteceu, passos para reproduzir ou mensagens de erro...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(12.dp)
        )

        // Include Logs Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = includeLogs,
                onCheckedChange = { includeLogs = it },
                colors = CheckboxDefaults.colors(checkedColor = HoneyPrimary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Incluir dados diagnósticos e versão do app", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("Ajuda a equipe a identificar e resolver falhas mais rapidamente.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (description.isNotBlank()) {
                    ticketId = "#APIS-${(1000..9999).random()}"
                    isSubmitted = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HoneyPrimary),
            enabled = description.isNotBlank()
        ) {
            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Enviar Chamado de Suporte", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackFormTab(onDismiss: () -> Unit) {
    val scrollState = rememberScrollState()
    var category by remember { mutableStateOf("Nova Funcionalidade") }
    var categoryExpanded by remember { mutableStateOf(false) }
    
    var suggestionTitle by remember { mutableStateOf("") }
    var suggestionDesc by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(5) }
    var isSubmitted by remember { mutableStateOf(false) }

    if (isSubmitted) {
        SuccessTicketView(
            ticketNumber = "#FB-${(1000..9999).random()}",
            message = "Agradecemos imensamente seu feedback! Suas sugestões nos ajudam a construir o melhor ecossistema para apicultores.",
            onDone = onDismiss
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = HoneyLight.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = HoneyDark)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Como você avalia sua experiência geral com o aplicativo Smart Apis? Sua opinião vale ouro!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Star Rating Bar
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Avaliação Geral", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 1..5) {
                    IconButton(
                        onClick = { rating = i },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "$i estrelas",
                            tint = if (i <= rating) Color(0xFFF59E0B) else Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            Text(
                text = when(rating) {
                    5 -> "Excelente! Superou expectativas"
                    4 -> "Muito bom, atende bem"
                    3 -> "Bom, pode melhorar"
                    2 -> "Regular"
                    else -> "Precisa de muitas melhorias"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = HoneyDark
            )
        }

        Divider(color = MaterialTheme.colorScheme.surfaceVariant)

        // Suggestion Category (Combobox / ExposedDropdownMenuBox)
        Text("Área da Sugestão", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        val categories = listOf("Nova Funcionalidade", "Melhoria no Dashboard", "Sensores IoT & BLE", "Relatórios e Exportação", "Outro")
        
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = !categoryExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                label = { Text("Selecione a Área da Sugestão") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = {
                            category = cat
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        // Title
        OutlinedTextField(
            value = suggestionTitle,
            onValueChange = { suggestionTitle = it },
            label = { Text("Título da Sugestão *") },
            placeholder = { Text("Ex: Alerta de enxameação via notificação push") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Description
        OutlinedTextField(
            value = suggestionDesc,
            onValueChange = { suggestionDesc = it },
            label = { Text("Descrição detalhada da melhoria *") },
            placeholder = { Text("Conte como essa funcionalidade ajudaria no seu dia a dia no apiário...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (suggestionTitle.isNotBlank() && suggestionDesc.isNotBlank()) {
                    isSubmitted = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HoneyPrimary),
            enabled = suggestionTitle.isNotBlank() && suggestionDesc.isNotBlank()
        ) {
            Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Enviar Sugestão de Melhoria", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FaqTab(onOpenPrivacyPolicy: () -> Unit = {}) {
    val scrollState = rememberScrollState()
    val faqs = listOf(
        Pair(
            "Como funciona o Assistente Rápido de Colmeias?",
            "Ao cadastrar um novo apiário, o assistente guia você em 4 passos intuitivos: Dados do Apiário, Número de Colmeias, Categoria e Espécie da Abelha (Ex: Apis mellifera, Jataí) e Modelo de Colmeia (Langstroth, Rational, etc.), gerando tudo automaticamente."
        ),
        Pair(
            "Como validar Associações de Apicultores via CNPJ?",
            "Na tela de Licenças e Solicitações, ao selecionar a categoria de Associação de Apicultores, digite o CNPJ da instituição. O sistema consulta automaticamente a API oficial do Governo (Brasil API) para verificar a autenticidade e atividade."
        ),
        Pair(
            "Como conectar balanças e sensores via Bluetooth?",
            "Ative o Bluetooth do seu dispositivo, vá na seção de Rastreio e Sensores BLE do apiário e toque em 'Buscar Sensores Próximos'. O app fará a varredura automática das balanças e termômetros de colmeia."
        ),
        Pair(
            "Como exportar relatórios de produção em PDF?",
            "No menu superior ou na tela de histórico de colmeias e produções de mel, utilize o botão de exportação para gerar relatórios completos formatados em PDF com gráficos e dados geolocalizados."
        ),
        Pair(
            "Meus dados ficam salvos na nuvem?",
            "Sim! Com a integração Firebase Firestore, todos os seus apiários, inspeções, colmeias e notas de manejo são sincronizados de forma segura e em tempo real, podendo ser acessados em qualquer dispositivo após login com Google."
        ),
        Pair(
            "Como gerenciar ou cancelar minha assinatura Google Play?",
            "Sua assinatura é processada com total segurança pela Google Play Store. Você pode gerenciar suas formas de pagamento ou cancelar a renovação automática a qualquer momento abrindo o app Google Play Store > Perfil > Pagamentos e assinaturas > Assinaturas."
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Dúvidas Frequentes & Tutoriais",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = HoneyDark
        )
        
        faqs.forEach { (question, answer) ->
            var expanded by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = question,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = HoneyPrimary
                            )
                        }
                    }
                    
                    AnimatedVisibility(visible = expanded) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = answer,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onOpenPrivacyPolicy,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Policy, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Política de Privacidade & Termos Google Play", fontSize = 12.sp)
        }
    }
}

@Composable
fun SuccessTicketView(
    ticketNumber: String,
    message: String,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(HoneyLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = HoneyPrimary,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Enviado com Sucesso!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Text(
                text = "Protocolo: $ticketNumber",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = HoneyDark,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HoneyPrimary)
        ) {
            Text("Concluir e Voltar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
