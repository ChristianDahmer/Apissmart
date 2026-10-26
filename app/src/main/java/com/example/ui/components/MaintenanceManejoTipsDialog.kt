package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.MeadowGreenSecondary
import com.example.ui.theme.WarningAlertRed

data class ManejoTip(
    val id: String,
    val title: String,
    val category: String, // "Equipamentos & EPI", "Caixas & Quadros", "Ninho & Rainha", "Colheita & Mel", "Entre-safra & Nutrição", "Sanidade & Profilaxia"
    val urgencyLevel: String, // "Essencial", "Recomendado", "Sazonal"
    val summary: String,
    val detailedSteps: List<String>,
    val frequencyText: String,
    val defaultScheduleDays: Int = 7
)

val MANEJO_MAINTENANCE_TIPS = listOf(
    ManejoTip(
        id = "fumegador_epi",
        title = "Limpeza & Descarbonização do Fumegador",
        category = "Equipamentos & EPI",
        urgencyLevel = "Essencial",
        summary = "Evita faíscas que queimam as asas das abelhas e fumaça quente irritante que desencadeia picadas e defensividade.",
        detailedSteps = listOf(
            "Raspar as paredes internas e o bico com espátula para remover resíduos betuminosos e fuligem acumulada.",
            "Utilizar apenas combustíveis secos e atóxicos: maravalha de pinus virgem, sabugo de milho moído ou folhas secas de eucalipto.",
            "Nunca usar papelão com cola química, folhas verdes (produzem vapor d'água irritante) ou tecidos sintéticos.",
            "Testar a temperatura da fumaça na palma da mão (a 15 cm do bico) antes de aplicar na colmeia: a fumaça deve ser densa, branca e fria."
        ),
        frequencyText = "A cada 15 a 30 dias de uso em campo",
        defaultScheduleDays = 15
    ),
    ManejoTip(
        id = "esterilizacao_ferramentas",
        title = "Desinfecção de Formões & Luvas entre Apiários",
        category = "Equipamentos & EPI",
        urgencyLevel = "Essencial",
        summary = "Previne a disseminação horizontal de patógenos como Loque Americana, Loque Europeia e Nosemose entre apiários.",
        detailedSteps = listOf(
            "Flambar o formão com a chama do maçarico portátil ou lavá-lo com solução de hipoclorito a 2% entre a revisão de colmeias suspeitas.",
            "Lavar os macacões e luvas regularmente para remover vestígios de ferrões e o feromônio de alarme (isopentil acetato), que atrai novas ferroadas.",
            "Substituir luvas de couro com acúmulo excessivo de própolis endurecida por luvas de nitrila de alta espessura."
        ),
        frequencyText = "Ao final de cada dia de manejo ou entre apiários",
        defaultScheduleDays = 7
    ),
    ManejoTip(
        id = "rotacao_cera",
        title = "Substituição e Rotação de Quadros de Ninho",
        category = "Caixas & Quadros",
        urgencyLevel = "Essencial",
        summary = "Quadros velhos e escuros acumulam esporos de fungos, fezes de crias e toxinas, além de diminuírem o tamanho corporal das novas operárias.",
        detailedSteps = listOf(
            "Renovar entre 20% e 30% dos quadros do ninho anualmente (2 a 3 quadros por caixa Langstroth a cada primavera).",
            "Deslocar quadros velhos e pretos para as extremidades laterais (posições 1 e 10) até que a cria nasça e então descartá-los para fundição.",
            "Inserir quadros novos com cera alveolada pura entre a cria madura e a cria jovem (posições 3 e 8) em períodos de fluxo de néctar.",
            "Aramação correta: fios de inox bem esticados com ilhoses para evitar que o favo despenque com o calor e peso do mel."
        ),
        frequencyText = "Anualmente no início da primavera / florada",
        defaultScheduleDays = 30
    ),
    ManejoTip(
        id = "nivelamento_inclinacao",
        title = "Nivelamento dos Cavaletes com Inclinação Frontal",
        category = "Caixas & Quadros",
        urgencyLevel = "Recomendado",
        summary = "Facilita o trabalho de limpeza das abelhas faxineiras e impede que água de chuva empoce no fundo da colmeia.",
        detailedSteps = listOf(
            "Ajustar o cavalete com inclinação de 2 a 3 graus para a frente (direção do alvado).",
            "Isolar os pés do cavalete com graxa ecológica ou barreiras contra formigas lava-pés e formigas-carregadeiras.",
            "Manter as colmeias elevadas a pelo menos 40 a 50 cm do solo para protegê-las da umidade da terra e ataques de sapos e tatus."
        ),
        frequencyText = "A cada 3 meses ou após chuvas intensas",
        defaultScheduleDays = 60
    ),
    ManejoTip(
        id = "tela_excluidora",
        title = "Manutenção e Limpeza da Tela Excluidora",
        category = "Ninho & Rainha",
        urgencyLevel = "Essencial",
        summary = "Garante que a rainha permaneça no ninho enquanto as operárias sobem para as melgueiras, produzindo mel limpo sem crias.",
        detailedSteps = listOf(
            "Verificar se não há varetas tortas que permitam a passagem da rainha ou firam as operárias.",
            "Remover própolis e pontes de cera acumuladas nas fendas da grade utilizando o formão aquecido.",
            "Nunca instalar a tela excluidora antes que o ninho esteja 80% ocupado por crias e operárias para não frear o desenvolvimento."
        ),
        frequencyText = "Antes da colocação das primeiras melgueiras na safra",
        defaultScheduleDays = 14
    ),
    ManejoTip(
        id = "troca_rainha",
        title = "Avaliação do Padrão de Postura e Vigor da Rainha",
        category = "Ninho & Rainha",
        urgencyLevel = "Essencial",
        summary = "Rainhas com mais de 18 meses apresentam queda de até 50% na postura diária e aumentam a tendência de enxameação.",
        detailedSteps = listOf(
            "Observar a uniformidade da cria operculada: um ninho saudável deve apresentar um 'tapete' contínuo com menos de 10% de alvéolos vazios.",
            "Se houver ovos duplos nas paredes dos alvéolos e cria de zangão saliente (corcunda), o enxame está zanganeiro (órfão crônico).",
            "Trocar rainhas velhas ou improdutivas por rainhas fecundadas selecionadas marcadas com a cor oficial do ano.",
            "Introduzir a nova rainha em gaiola com pasta cândi, mantendo-a fechada por 48 horas antes da liberação pelas operárias."
        ),
        frequencyText = "Inspeções mensais ou a cada início de safra",
        defaultScheduleDays = 30
    ),
    ManejoTip(
        id = "operculacao_colheita",
        title = "Ponto Correto de Colheita & Medição de Umidade",
        category = "Colheita & Mel",
        urgencyLevel = "Essencial",
        summary = "Mel colhido verde (desoperculado) possui umidade superior a 19.5% e fermentará rapidamente na embalagem.",
        detailedSteps = listOf(
            "Colher apenas quadros com no mínimo 80% a 90% dos alvéolos totalmente operculados pelas abelhas.",
            "Efetuar o teste de sacudida no campo: vire o quadro na horizontal e sacuda com firmeza; se respingos de néctar caírem, o mel ainda está verde.",
            "Medir com refratômetro óptico ou digital limpo e calibrado: a umidade padrão de excelência deve estar entre 16.5% e 18.0%."
        ),
        frequencyText = "Em todas as rodadas de colheita no apiário",
        defaultScheduleDays = 7
    ),
    ManejoTip(
        id = "reserva_estrategica",
        title = "Manutenção de Reserva Estratégica na Entre-safra",
        category = "Entre-safra & Nutrição",
        urgencyLevel = "Essencial",
        summary = "Colheitas excessivas que rapam todas as melgueiras condenam o enxame à inanição, abandono da caixa ou pilhagem.",
        detailedSteps = listOf(
            "Deixar sempre no mínimo 4 a 6 kg de mel e 2 quadros com pólen no ninho ao finalizar a safra.",
            "Reduzir o alvado com redutor de entrada de madeira para conservar o calor interno e evitar pilhagem por enxames vizinhos.",
            "Se a florada cessar completamente e o peso da colmeia cair, iniciar alimentação de manutenção com xarope 2:1 no alimentador interno Boardman ou cobertura."
        ),
        frequencyText = "Ao término de cada safra e quinzenalmente no inverno",
        defaultScheduleDays = 14
    ),
    ManejoTip(
        id = "profilaxia_varroa",
        title = "Monitoramento do Índice de Infestação de Varroa",
        category = "Sanidade & Profilaxia",
        urgencyLevel = "Essencial",
        summary = "Infestações acima de 3% a 5% transmitem o vírus das asas deformadas (DWV) e causam o colapso do enxame.",
        detailedSteps = listOf(
            "Realizar o teste do álcool ou do açúcar de confeiteiro em amostra de ~300 abelhas colhidas de quadros de cria aberta.",
            "Contar os ácaros caídos: (Nº de ácaros / Nº de abelhas) * 100 = % de infestação.",
            "Se acima de 3%, aplicar tratamento profilático orgânico com ácido oxálico por sublimação ou gotejamento, ou tiras de timol.",
            "Nunca aplicar tratamentos químicos sintéticos durante a presença de melgueiras destinadas ao consumo humano."
        ),
        frequencyText = "A cada 60 dias e impreterivelmente na pré e pós-safra",
        defaultScheduleDays = 60
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceManejoTipsDialog(
    onScheduleTaskFromTip: (title: String, desc: String, taskType: String, days: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todas") }

    val categories = listOf("Todas", "Equipamentos & EPI", "Caixas & Quadros", "Ninho & Rainha", "Colheita & Mel", "Entre-safra & Nutrição", "Sanidade & Profilaxia")

    val filteredTips = MANEJO_MAINTENANCE_TIPS.filter { tip ->
        val matchesCategory = selectedCategory == "Todas" || tip.category.equals(selectedCategory, ignoreCase = true)
        val matchesQuery = searchQuery.isBlank() || tip.title.contains(searchQuery, ignoreCase = true) || tip.summary.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .testTag("maintenance_tips_dialog"),
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
                                .background(MeadowGreenSecondary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TipsAndUpdates,
                                contentDescription = null,
                                tint = MeadowGreenSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Dicas de Manutenção & Manejo",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Boas práticas técnicas para saúde e colheita récorde",
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

                // Filtros de Categoria (Horizontal Scroll)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HoneyGoldPrimary,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                // Lista de Dicas
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredTips, key = { it.id }) { tip ->
                        ManejoTipCard(
                            tip = tip,
                            onSchedule = {
                                onScheduleTaskFromTip(
                                    tip.title,
                                    tip.summary + "\n\nPassos:\n" + tip.detailedSteps.joinToString("\n• "),
                                    tip.category,
                                    tip.defaultScheduleDays
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ManejoTipCard(
    tip: ManejoTip,
    onSchedule: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .testTag("tip_card_${tip.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (tip.category) {
                            "Equipamentos & EPI" -> "🛠️"
                            "Caixas & Quadros" -> "📦"
                            "Ninho & Rainha" -> "👑"
                            "Colheita & Mel" -> "🍯"
                            "Entre-safra & Nutrição" -> "❄️"
                            else -> "🛡️"
                        },
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = tip.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "${tip.category} • ${tip.frequencyText}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (tip.urgencyLevel == "Essencial") WarningAlertRed.copy(alpha = 0.2f) else HoneyGoldPrimary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = tip.urgencyLevel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (tip.urgencyLevel == "Essencial") WarningAlertRed else HoneyGoldPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tip.summary,
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 17.sp
            )

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Procedimento Técnico Recomendado:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MeadowGreenSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    tip.detailedSteps.forEachIndexed { idx, step ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("• ", fontWeight = FontWeight.Bold, color = HoneyGoldPrimary)
                            Text(step, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Toque para recolher ▲" else "Ver passo a passo detalhado ▼",
                    fontSize = 11.sp,
                    color = HoneyGoldPrimary,
                    fontWeight = FontWeight.Medium
                )

                Button(
                    onClick = onSchedule,
                    colors = ButtonDefaults.buttonColors(containerColor = MeadowGreenSecondary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agendar Manejo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
