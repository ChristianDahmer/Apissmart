package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entities.ApiaryEntity
import com.example.ui.theme.HoneyGoldDark
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.viewmodel.ApiaryWeatherInfo
import com.example.ui.viewmodel.ManejoWindowStatus
import com.example.util.DailyForecastItem
import com.example.util.ExtendedWeatherForecast
import com.example.util.HourlyForecastItem
import com.example.util.WeatherForecastEngine
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherForecastModalDialog(
    weatherInfo: ApiaryWeatherInfo,
    apiaries: List<ApiaryEntity> = emptyList(),
    selectedApiaryId: Long? = null,
    onSelectApiary: ((Long) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var currentApiaryId by remember {
        mutableStateOf(selectedApiaryId ?: weatherInfo.apiaryId.takeIf { it > 0 } ?: apiaries.firstOrNull()?.id)
    }
    val currentApiary = remember(currentApiaryId, apiaries) {
        apiaries.find { it.id == currentApiaryId } ?: apiaries.firstOrNull()
    }

    var isLoading by remember { mutableStateOf(false) }
    var forecastData by remember { mutableStateOf<ExtendedWeatherForecast?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Visão Geral e Horária, 1: Previsão 7 Dias, 2: Regras de Voo & Manejo

    fun loadForecast() {
        coroutineScope.launch {
            isLoading = true
            forecastData = WeatherForecastEngine.getExtendedForecast(currentApiary)
            isLoading = false
        }
    }

    LaunchedEffect(currentApiaryId, currentApiary) {
        loadForecast()
    }

    val status = forecastData?.status ?: weatherInfo.status
    val isIdeal = status == ManejoWindowStatus.IDEAL
    val isCaution = status == ManejoWindowStatus.CAUTION

    val headerGradient = when {
        isIdeal -> Brush.verticalGradient(listOf(Color(0xFF065F46), Color(0xFF047857), Color(0xFF059669)))
        isCaution -> Brush.verticalGradient(listOf(Color(0xFF854D0E), Color(0xFFB45309), Color(0xFFD97706)))
        else -> Brush.verticalGradient(listOf(Color(0xFF991B1B), Color(0xFFB91C1C), Color(0xFFDC2626)))
    }

    val statusTitle = when {
        isIdeal -> "🟢 VOO PLENO E MANEJO IDEAL"
        isCaution -> "🟡 VOO RESTRITO / COM CAUTELA"
        else -> "🔴 VOO SUSPENSO / MANEJO PROIBIDO"
    }

    val currentDisplayName = currentApiary?.name ?: weatherInfo.apiaryName.ifBlank { "Apiário Geral" }
    val currentCoords = if (currentApiary?.latitude != null && currentApiary.longitude != null) {
        String.format(Locale.US, "%.4f, %.4f", currentApiary.latitude, currentApiary.longitude)
    } else {
        String.format(Locale.US, "%.4f, %.4f", weatherInfo.latitude ?: -22.7394, weatherInfo.longitude ?: -45.5913)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .testTag("weather_forecast_modal_fullscreen"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Hero Bar with Weather Colors
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerGradient)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🌤️", fontSize = 24.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Clima & Condições de Voo",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.9f),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "$currentDisplayName ($currentCoords)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { loadForecast() },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .testTag("weather_modal_refresh_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Atualizar previsão",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .testTag("weather_modal_close_btn")
                                    ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Fechar modal",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Current Temp and Quick Summary Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                val displayTemp = forecastData?.currentTemp ?: weatherInfo.temperatureC
                                val displayCondition = forecastData?.conditionDescription ?: weatherInfo.conditionDescription
                                Text(
                                    text = "$displayTemp°C",
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = displayCondition,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.95f)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.25f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = statusTitle,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Atualizado: ${forecastData?.lastUpdatedText ?: "Agora"}",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }
                }

                // Apiary selector chips if multiple apiaries exist
                if (apiaries.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        apiaries.forEach { apiary ->
                            val isSelected = apiary.id == currentApiaryId
                            val chipBg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            val chipText = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(chipBg)
                                    .clickable {
                                        currentApiaryId = apiary.id
                                        onSelectApiary?.invoke(apiary.id)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "📍 ${apiary.name}",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = chipText
                                )
                            }
                        }
                    }
                }

                // Tabs for Navigation: Visão Geral & Horária | Previsão 7 Dias | Regras de Voo
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Previsão & Horas", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("7 Dias (Semana)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Janela de Manejo", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Consultando telemetria meteorológica...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when (selectedTab) {
                            0 -> {
                                // Tab 0: Current Metrics + Hourly Forecast Cards
                                item {
                                    TelemetryMetricsGrid(forecast = forecastData, fallback = weatherInfo)
                                }

                                item {
                                    AgronomicAdviceCard(forecast = forecastData, fallback = weatherInfo)
                                }

                                item {
                                    Text(
                                        text = "⏱️ Previsão Horária (Próximas 24 Horas)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Acompanhe as oscilações de temperatura, vento e chuva para planejar a visita ao apiário:",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                item {
                                    val hourlyList = forecastData?.hourlyList ?: emptyList()
                                    if (hourlyList.isNotEmpty()) {
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            items(hourlyList) { hourItem ->
                                                HourlyForecastCard(hourItem)
                                            }
                                        }
                                    } else {
                                        Text("Previsão horária indisponível no momento.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                item {
                                    SunAndAtmosphereCard(forecast = forecastData)
                                }
                            }

                            1 -> {
                                // Tab 1: 7-Day Extended Forecast
                                item {
                                    Text(
                                        text = "📅 Previsão Estendida para os Próximos 7 Dias",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Planeje alimentações, revisões sanitárias, colheitas e inserção de melgueiras com antecedência:",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                val dailyList = forecastData?.dailyList ?: emptyList()
                                items(dailyList) { dailyItem ->
                                    DailyForecastCard(dailyItem)
                                }
                            }

                            2 -> {
                                // Tab 2: Biological & Flight Rules
                                item {
                                    FlightRulesGuideCard()
                                }

                                item {
                                    ManejoBestPracticesCard(status = status)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryMetricsGrid(
    forecast: ExtendedWeatherForecast?,
    fallback: ApiaryWeatherInfo
) {
    val temp = forecast?.currentTemp ?: fallback.temperatureC
    val humidity = forecast?.currentHumidity ?: fallback.humidityPercent
    val wind = forecast?.currentWind ?: fallback.windSpeedKmh
    val rain = forecast?.currentRainProb ?: fallback.rainProbabilityPercent

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "📊 Telemetria do Apiário em Tempo Real",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricDetailCard(
                    icon = Icons.Default.Thermostat,
                    label = "Temperatura",
                    value = "$temp°C",
                    subtext = if (temp in 18..33) "Faixa de Voo Ideal" else if (temp < 18) "Ameno / Voo Lento" else "Risco de Superaquecimento",
                    color = Color(0xFFD97706),
                    modifier = Modifier.weight(1f)
                )
                MetricDetailCard(
                    icon = Icons.Default.WaterDrop,
                    label = "Umidade Relativa",
                    value = "$humidity%",
                    subtext = if (humidity in 45..75) "Equilíbrio dos Favos" else if (humidity > 75) "Alta Umidade no Mel" else "Ar Seco",
                    color = Color(0xFF0284C7),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricDetailCard(
                    icon = Icons.Default.Air,
                    label = "Velocidade do Vento",
                    value = "$wind km/h",
                    subtext = if (wind <= 18) "Voo Seguro" else if (wind <= 25) "Vento Moderado" else "Voo Prejudicado",
                    color = Color(0xFF16A34A),
                    modifier = Modifier.weight(1f)
                )
                MetricDetailCard(
                    icon = Icons.Default.Thunderstorm,
                    label = "Probab. de Chuva",
                    value = "$rain%",
                    subtext = if (rain < 20) "Tempo Firme" else if (rain < 50) "Instabilidade Local" else "Chuva Provável",
                    color = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricDetailCard(
    icon: ImageVector,
    label: String,
    value: String,
    subtext: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtext, fontSize = 9.sp, color = color, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun AgronomicAdviceCard(
    forecast: ExtendedWeatherForecast?,
    fallback: ApiaryWeatherInfo
) {
    val status = forecast?.status ?: fallback.status
    val advice = forecast?.advice ?: fallback.advice
    val flightSummary = forecast?.flightConditionText ?: fallback.flightConditionText

    val isIdeal = status == ManejoWindowStatus.IDEAL
    val isCaution = status == ManejoWindowStatus.CAUTION

    val bgCol = if (isIdeal) Color(0xFFF0FDF4) else if (isCaution) Color(0xFFFEFCE8) else Color(0xFFFEF2F2)
    val borderCol = if (isIdeal) Color(0xFFBBF7D0) else if (isCaution) Color(0xFFFEF08A) else Color(0xFFFECACA)
    val textCol = if (isIdeal) Color(0xFF166534) else if (isCaution) Color(0xFF854D0E) else Color(0xFF991B1B)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgCol),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderCol)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = if (isIdeal) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = textCol,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = flightSummary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = textCol,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = advice,
                    style = MaterialTheme.typography.bodySmall,
                    color = textCol,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.HorizontalDivider(color = textCol.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = fallback.floraInfo,
                    style = MaterialTheme.typography.labelSmall,
                    color = textCol.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun HourlyForecastCard(item: HourlyForecastItem) {
    val statusBg = when (item.status) {
        ManejoWindowStatus.IDEAL -> Color(0xFFF0FDF4)
        ManejoWindowStatus.CAUTION -> Color(0xFFFEFCE8)
        ManejoWindowStatus.PROHIBITED -> Color(0xFFFEF2F2)
    }
    val statusBorder = when (item.status) {
        ManejoWindowStatus.IDEAL -> Color(0xFFBBF7D0)
        ManejoWindowStatus.CAUTION -> Color(0xFFFEF08A)
        ManejoWindowStatus.PROHIBITED -> Color(0xFFFECACA)
    }
    val statusText = when (item.status) {
        ManejoWindowStatus.IDEAL -> Color(0xFF166534)
        ManejoWindowStatus.CAUTION -> Color(0xFF854D0E)
        ManejoWindowStatus.PROHIBITED -> Color(0xFF991B1B)
    }

    Surface(
        modifier = Modifier.width(92.dp),
        shape = RoundedCornerShape(12.dp),
        color = statusBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, statusBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = item.timeLabel, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = item.conditionIcon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "${item.temperatureC}°C", fontWeight = FontWeight.Black, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(10.dp))
                Text(text = "${item.rainProbabilityPercent}%", fontSize = 9.sp, color = Color(0xFF0284C7))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Air, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(10.dp))
                Text(text = "${item.windSpeedKmh}k", fontSize = 9.sp, color = Color(0xFF16A34A))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(statusText.copy(alpha = 0.15f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(text = item.flightSummary, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = statusText, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun SunAndAtmosphereCard(forecast: ExtendedWeatherForecast?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFEA580C), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text("Nascer do Sol", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(forecast?.sunrise ?: "06:12", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.WbTwilight, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text("Pôr do Sol", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(forecast?.sunset ?: "18:24", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Compress, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text("Pressão", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${forecast?.pressureHpa ?: 1014} hPa", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DailyForecastCard(item: DailyForecastItem) {
    val isIdeal = item.status == ManejoWindowStatus.IDEAL
    val isCaution = item.status == ManejoWindowStatus.CAUTION

    val statusBadgeBg = if (isIdeal) Color(0xFFDCFCE7) else if (isCaution) Color(0xFFFEF9C3) else Color(0xFFFEE2E2)
    val statusBadgeText = if (isIdeal) Color(0xFF166534) else if (isCaution) Color(0xFF854D0E) else Color(0xFF991B1B)
    val statusLabel = if (isIdeal) "🟢 Ideal" else if (isCaution) "🟡 Cautela" else "🔴 Proibido"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(
                            text = item.dayOfWeek,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = item.dateLabel,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = item.conditionDescription,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${item.minTempC}°C / ${item.maxTempC}°C",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌧️ ${item.rainProbabilityPercent}%", fontSize = 10.sp, color = Color(0xFF0284C7))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("💨 ${item.maxWindKmh} km/h", fontSize = 10.sp, color = Color(0xFF16A34A))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusBadgeBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = statusLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusBadgeText)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = statusBadgeText,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = item.recommendation,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 13.sp
                )
            }
        }
    }
}

@Composable
private fun FlightRulesGuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "🐝 Regras Biológicas de Voo & Forrageamento",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            RuleRow(
                emoji = "🌡️",
                title = "Temperatura Térmica Crítica (18°C a 33°C)",
                desc = "Abaixo de 14°C ocorre paralisia muscular nas asas das campeiras (torpor térmico). Acima de 37°C as operárias interrompem a coleta para buscar água e resfriar a colmeia."
            )
            Spacer(modifier = Modifier.height(6.dp))
            RuleRow(
                emoji = "💨",
                title = "Velocidade Limite do Vento (Até 18 km/h)",
                desc = "Ventos de até 18 km/h permitem voo pleno de coleta de néctar e pólen. Acima de 25 km/h o consumo energético do voo supera o ganho calórico e as abelhas cessam as saídas."
            )
            Spacer(modifier = Modifier.height(6.dp))
            RuleRow(
                emoji = "🌧️",
                title = "Chuva e Pressão Barométrica",
                desc = "Quedas bruscas na pressão atmosférica e chuvas forçam o retorno imediato ao ninho, aumentando a defensividade da colônia."
            )
            Spacer(modifier = Modifier.height(6.dp))
            RuleRow(
                emoji = "🍯",
                title = "Impacto na Umidade do Mel",
                desc = "Dias com umidade acima de 75% dificultam a desidratação e operculação do mel nos favos pelas operárias, aumentando o risco de fermentação."
            )
        }
    }
}

@Composable
private fun RuleRow(emoji: String, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(emoji, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(text = desc, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 13.sp)
        }
    }
}

@Composable
private fun ManejoBestPracticesCard(status: ManejoWindowStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "📋 Recomendações Práticas para o Apicultor",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            when (status) {
                ManejoWindowStatus.IDEAL -> {
                    Text("✅ Janela Aberta: Aproveite para revisões profundas de ninhos, troca de quadros velhos por cera alveolada, introdução de rainhas fecundadas e colheita de melgueiras operculadas.", fontSize = 11.sp, lineHeight = 15.sp, color = Color(0xFF166534))
                }
                ManejoWindowStatus.CAUTION -> {
                    Text("⚠️ Janela Parcial: Realize apenas vistorias essenciais e rápidas. Evite manter a tampa ou ninhos abertos por mais de 5 minutos para não esfriar a cria nem atrair abelhas pilhadoras.", fontSize = 11.sp, lineHeight = 15.sp, color = Color(0xFF854D0E))
                }
                ManejoWindowStatus.PROHIBITED -> {
                    Text("🛑 Janela Fechada: Não abra nenhuma colmeia hoje. O estresse térmico ou chuva pode matar larvas abertas e enfurecer os enxames. Utilize o tempo para preparar caixas, pintar materiais ou formular alimentação artificial no galpão.", fontSize = 11.sp, lineHeight = 15.sp, color = Color(0xFF991B1B))
                }
            }
        }
    }
}
