package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ApiaryEntity
import com.example.ui.viewmodel.ApiaryWeatherInfo
import com.example.ui.viewmodel.ManejoWindowStatus
import java.util.Locale

@Composable
fun WeatherManejoCard(
    weatherInfo: ApiaryWeatherInfo,
    apiaryName: String = weatherInfo.apiaryName,
    apiaries: List<ApiaryEntity> = emptyList(),
    selectedApiaryId: Long? = null,
    onSelectApiary: ((Long) -> Unit)? = null,
    onOpenFlightMap: (() -> Unit)? = null,
    onOpenForecastModal: (() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val hasNoApiaries = apiaries.isEmpty() && weatherInfo.apiaryName.isBlank() && apiaryName.isBlank()

    val statusBgColor = when {
        hasNoApiaries -> MaterialTheme.colorScheme.surfaceVariant
        weatherInfo.status == ManejoWindowStatus.IDEAL -> Color(0xFFDCFCE7)
        weatherInfo.status == ManejoWindowStatus.CAUTION -> Color(0xFFFEF9C3)
        else -> Color(0xFFFEE2E2)
    }

    val statusTextColor = when {
        hasNoApiaries -> MaterialTheme.colorScheme.onSurfaceVariant
        weatherInfo.status == ManejoWindowStatus.IDEAL -> Color(0xFF166534)
        weatherInfo.status == ManejoWindowStatus.CAUTION -> Color(0xFF854D0E)
        else -> Color(0xFF991B1B)
    }

    val statusTitle = when {
        hasNoApiaries -> "Aguardando Apiário"
        weatherInfo.status == ManejoWindowStatus.IDEAL -> "🟢 Voo Pleno / Ideal"
        weatherInfo.status == ManejoWindowStatus.CAUTION -> "🟡 Voo com Cautela"
        else -> "🔴 Voo Suspenso"
    }

    val currentDisplayName = when {
        weatherInfo.apiaryName.isNotBlank() -> weatherInfo.apiaryName
        apiaryName.isNotBlank() -> apiaryName
        else -> "Nenhum apiário cadastrado"
    }
    val lat = weatherInfo.latitude ?: -22.7394
    val lon = weatherInfo.longitude ?: -45.5913
    val coordsFormatted = String.format(Locale.US, "%.4f, %.4f", lat, lon)

    val cardModifier = if (onOpenForecastModal != null) {
        modifier
            .fillMaxWidth()
            .clickable { onOpenForecastModal() }
            .testTag("weather_manejo_card")
    } else {
        modifier
            .fillMaxWidth()
            .testTag("weather_manejo_card")
    }

    Card(
        modifier = cardModifier,
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            // Header: Title, Apiary Geolocation & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Clima & Condições de Voo",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = if (weatherInfo.location.isNotBlank()) "$currentDisplayName • ${weatherInfo.location}" else currentDisplayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (onOpenFlightMap != null && !hasNoApiaries) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { onOpenFlightMap() }
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🗺️ Mapa",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(statusBgColor, RoundedCornerShape(10.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusTitle,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusTextColor,
                            fontSize = 10.sp
                        )
                    }

                    if (onRefresh != null) {
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Atualizar clima",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Apiary Selector Chips when multiple apiaries exist
            if (apiaries.size > 1 && onSelectApiary != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    apiaries.forEach { apiary ->
                        val isSelected = (selectedApiaryId != null && apiary.id == selectedApiaryId) ||
                                (selectedApiaryId == null && apiary.name == weatherInfo.apiaryName)
                        val chipBg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        val chipText = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(chipBg)
                                .clickable { onSelectApiary(apiary.id) }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "📍 ${apiary.name}",
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = chipText
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Weather telemetry metrics in compact row: Temp, Umidade, Vento, Chuva
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                WeatherMetricPill(
                    icon = Icons.Default.Thermostat,
                    label = "Temp",
                    value = "${weatherInfo.temperatureC}°C",
                    tint = Color(0xFFD97706),
                    modifier = Modifier.weight(1f)
                )
                WeatherMetricPill(
                    icon = Icons.Default.WaterDrop,
                    label = "Umidade",
                    value = "${weatherInfo.humidityPercent}%",
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.weight(1f)
                )
                WeatherMetricPill(
                    icon = Icons.Default.Air,
                    label = "Vento",
                    value = "${weatherInfo.windSpeedKmh} km/h",
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.weight(1f)
                )
                WeatherMetricPill(
                    icon = Icons.Default.Thunderstorm,
                    label = "Chuva",
                    value = "${weatherInfo.rainProbabilityPercent}%",
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Flight condition & agronomic advice based on apiary geolocation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(statusBgColor.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (hasNoApiaries) Icons.Default.LocationOn else if (weatherInfo.status == ManejoWindowStatus.IDEAL) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = statusTextColor,
                            modifier = Modifier
                                .padding(top = 1.dp)
                                .size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Column {
                            Text(
                                text = if (hasNoApiaries) "📍 Cadastro de Apiário Necessário" else "🐝 ${weatherInfo.flightConditionText}",
                                style = MaterialTheme.typography.labelSmall,
                                color = statusTextColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                            Text(
                                text = if (hasNoApiaries) "Cadastre seu primeiro apiário na aba Apiários para obter a telemetria climática e o cálculo agronômico de voo com base na geolocalização." else weatherInfo.advice,
                                style = MaterialTheme.typography.bodySmall,
                                color = statusTextColor,
                                fontWeight = FontWeight.Normal,
                                fontSize = 10.sp,
                                lineHeight = 13.sp
                            )
                        }
                    }

                    if (onOpenForecastModal != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Toque p/ Ver Previsão ↗",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherMetricPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(13.dp))
        Spacer(modifier = Modifier.width(3.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 8.sp)
            Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }
    }
}
