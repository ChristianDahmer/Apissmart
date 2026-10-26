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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.theme.AppThemeConfig
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.FontColorOption
import com.example.ui.theme.FontSizeScale
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.MeadowGreenSecondary
import com.example.ui.theme.PrimaryPalette
import com.example.ui.theme.SubtitleColorOption
import com.example.ui.viewmodel.ScreenTab

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemeCustomizationDialog(
    currentConfig: AppThemeConfig,
    visibleTabs: List<ScreenTab>,
    onDismiss: () -> Unit,
    onSaveConfig: (AppThemeConfig) -> Unit,
    onResetDefaults: () -> Unit,
    onUpdateVisibleTabs: (List<ScreenTab>) -> Unit
) {
    var tempThemeMode by remember { mutableStateOf(currentConfig.themeMode) }
    var tempPalette by remember { mutableStateOf(currentConfig.primaryPalette) }
    var tempFontColor by remember { mutableStateOf(currentConfig.fontColorOption) }
    var tempSubtitleColor by remember { mutableStateOf(currentConfig.subtitleColorOption) }
    var tempFontSizeScale by remember { mutableStateOf(currentConfig.fontSizeScale) }
    var tempVisibleTabs by remember { mutableStateOf(visibleTabs) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .testTag("theme_customization_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
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
                                .background(tempPalette.primaryLight.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = tempPalette.primaryLight
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Personalização & Visual",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                            Text(
                                text = "Temas, cores de fontes e legendas",
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
                            .testTag("close_theme_dialog_btn")
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

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            tempThemeMode = AppThemeMode.DARK
                            tempPalette = PrimaryPalette.HONEY_GOLD
                            tempFontColor = FontColorOption.DEFAULT
                            tempSubtitleColor = SubtitleColorOption.DEFAULT_SLATE
                            tempFontSizeScale = FontSizeScale.NORMAL
                            tempVisibleTabs = listOf(
                                ScreenTab.APIARIES,
                                ScreenTab.INSPECTIONS,
                                ScreenTab.DASHBOARD,
                                ScreenTab.HARVESTS,
                                ScreenTab.SCHEDULE,
                                ScreenTab.AI_ASSISTANT
                            )
                            onResetDefaults()
                            onUpdateVisibleTabs(tempVisibleTabs)
                        },
                        modifier = Modifier.testTag("reset_theme_btn")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Padrão", fontSize = 13.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val newConfig = AppThemeConfig(
                                    themeMode = tempThemeMode,
                                    primaryPalette = tempPalette,
                                    fontColorOption = tempFontColor,
                                    subtitleColorOption = tempSubtitleColor,
                                    fontSizeScale = tempFontSizeScale
                                )
                                onSaveConfig(newConfig)
                                onUpdateVisibleTabs(tempVisibleTabs)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = tempPalette.primaryLight),
                            modifier = Modifier.testTag("save_theme_btn")
                        ) {
                            Text("Salvar & Aplicar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // --- LIVE PREVIEW CARD ---
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when (tempThemeMode) {
                                AppThemeMode.LIGHT -> Color(0xFFFAFAF9)
                                AppThemeMode.DARK -> Color(0xFF0F172A)
                                AppThemeMode.SYSTEM -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 2.dp,
                                color = tempPalette.primaryLight.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "👁️ Pré-visualização ao Vivo",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (13 * tempFontSizeScale.scaleFactor).sp,
                                    color = tempPalette.primaryLight
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(tempPalette.primaryLight)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Status: Ativo",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = (11 * tempFontSizeScale.scaleFactor).sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Colmeia Langstroth #07 (Florada de Alecrim)",
                                fontWeight = FontWeight.Bold,
                                fontSize = (16 * tempFontSizeScale.scaleFactor).sp,
                                color = if (tempThemeMode == AppThemeMode.DARK) tempFontColor.darkColor else tempFontColor.lightColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Legenda & Detalhes: 8 quadros com melgueira operculada • Rainha marcada 2026",
                                fontSize = (13 * tempFontSizeScale.scaleFactor).sp,
                                color = if (tempThemeMode == AppThemeMode.DARK) tempSubtitleColor.darkColor else tempSubtitleColor.lightColor
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(tempPalette.containerLight)
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Estoque: 18.5 kg Mel",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = (12 * tempFontSizeScale.scaleFactor).sp,
                                        color = tempPalette.primaryLight
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MeadowGreenSecondary.copy(alpha = 0.15f))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Sanidade: Excelente",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = (12 * tempFontSizeScale.scaleFactor).sp,
                                        color = MeadowGreenSecondary
                                    )
                                }
                            }
                        }
                    }

                    // --- SEÇÃO 1: MODO DO TEMA (CLARO / ESCURO / SISTEMA) ---
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SettingsBrightness,
                                contentDescription = null,
                                tint = tempPalette.primaryLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "1. Modo do Tema",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppThemeMode.entries.forEach { mode ->
                                val isSelected = tempThemeMode == mode
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { tempThemeMode = mode }
                                        .testTag("theme_mode_${mode.name.lowercase()}"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) {
                                            tempPalette.primaryLight.copy(alpha = 0.15f)
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        }
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = if (isSelected) {
                                        androidx.compose.foundation.BorderStroke(1.5.dp, tempPalette.primaryLight)
                                    } else null
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = when (mode) {
                                                    AppThemeMode.SYSTEM -> Icons.Default.PhoneAndroid
                                                    AppThemeMode.LIGHT -> Icons.Default.LightMode
                                                    AppThemeMode.DARK -> Icons.Default.DarkMode
                                                },
                                                contentDescription = null,
                                                tint = if (isSelected) tempPalette.primaryLight else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = mode.label,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) tempPalette.primaryLight else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = mode.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selecionado",
                                                tint = tempPalette.primaryLight
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- SEÇÃO 2: PALETA DE CORES DE DESTAQUE ---
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = tempPalette.primaryLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "2. Cor de Destaque / Identidade Visual",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            PrimaryPalette.entries.forEach { palette ->
                                val isSelected = tempPalette == palette
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { tempPalette = palette },
                                    label = { Text(palette.label, fontSize = 12.sp) },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(palette.primaryLight)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = palette.primaryLight.copy(alpha = 0.2f),
                                        selectedLabelColor = palette.primaryLight
                                    ),
                                    modifier = Modifier.testTag("palette_${palette.name.lowercase()}")
                                )
                            }
                        }
                    }

                    // --- SEÇÃO 3: COR DE FONTES & TÍTULOS ---
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FormatColorText,
                                contentDescription = null,
                                tint = tempPalette.primaryLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "3. Cor dos Títulos e Textos Principais",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FontColorOption.entries.forEach { fontOption ->
                                val isSelected = tempFontColor == fontOption
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { tempFontColor = fontOption },
                                    label = { Text(fontOption.label, fontSize = 12.sp) },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (tempThemeMode == AppThemeMode.DARK) fontOption.darkColor else fontOption.lightColor
                                                )
                                                .border(1.dp, Color.Gray, CircleShape)
                                        )
                                    },
                                    modifier = Modifier.testTag("font_color_${fontOption.name.lowercase()}")
                                )
                            }
                        }
                    }

                    // --- SEÇÃO 4: COR DE LEGENDAS E SUBTÍTULOS ---
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Subtitles,
                                contentDescription = null,
                                tint = tempPalette.primaryLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "4. Cor das Legendas e Informações Secundárias",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SubtitleColorOption.entries.forEach { subOption ->
                                val isSelected = tempSubtitleColor == subOption
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { tempSubtitleColor = subOption },
                                    label = { Text(subOption.label, fontSize = 12.sp) },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (tempThemeMode == AppThemeMode.DARK) subOption.darkColor else subOption.lightColor
                                                )
                                                .border(1.dp, Color.Gray, CircleShape)
                                        )
                                    },
                                    modifier = Modifier.testTag("subtitle_color_${subOption.name.lowercase()}")
                                )
                            }
                        }
                    }

                    // --- SEÇÃO 5: TAMANHO DE FONTE (ESCALA) ---
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FormatSize,
                                contentDescription = null,
                                tint = tempPalette.primaryLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "5. Escala de Tamanho das Fontes",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FontSizeScale.entries.forEach { scale ->
                                val isSelected = tempFontSizeScale == scale
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { tempFontSizeScale = scale }
                                        .testTag("font_scale_${scale.name.lowercase()}"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) {
                                            tempPalette.primaryLight.copy(alpha = 0.2f)
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        }
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    border = if (isSelected) {
                                        androidx.compose.foundation.BorderStroke(1.5.dp, tempPalette.primaryLight)
                                    } else null
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Aa",
                                            fontSize = (16 * scale.scaleFactor).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) tempPalette.primaryLight else MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = scale.label.split(" ").first(),
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // --- SEÇÃO 6: PERSONALIZAÇÃO DO MENU INFERIOR ---
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = tempPalette.primaryLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "6. Itens do Menu Inferior (Personalizado)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Text(
                            text = "Selecione quais telas deseja manter visíveis na barra inferior para acesso rápido:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ScreenTab.entries.forEach { tab ->
                                val isSelected = tempVisibleTabs.contains(tab)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected) {
                                            // Prevent removing all tabs (must have at least 2)
                                            if (tempVisibleTabs.size > 2) {
                                                tempVisibleTabs = tempVisibleTabs.filter { it != tab }
                                            }
                                        } else {
                                            tempVisibleTabs = tempVisibleTabs + tab
                                        }
                                    },
                                    label = {
                                        Text("${tab.iconEmoji} ${tab.title}", fontSize = 12.sp)
                                    },
                                    modifier = Modifier.testTag("menu_custom_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            }

        }
    }
}
