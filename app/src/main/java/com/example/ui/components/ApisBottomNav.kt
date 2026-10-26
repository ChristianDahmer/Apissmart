package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.outlined.Agriculture
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BluetoothSearching
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.viewmodel.ScreenTab

/**
 * Shape customizado com um recorte semicircular no topo central para acoplamento (Docked FAB).
 */
class SemicircularNotchedShape(
    private val cutoutRadiusDp: Float = 32f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cutoutRadius = cutoutRadiusDp * density.density
        val width = size.width
        val height = size.height
        val centerX = width / 2f

        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(centerX - cutoutRadius - 6f, 0f)
            cubicTo(
                centerX - cutoutRadius, 0f,
                centerX - cutoutRadius * 0.75f, cutoutRadius * 0.82f,
                centerX, cutoutRadius * 0.82f
            )
            cubicTo(
                centerX + cutoutRadius * 0.75f, cutoutRadius * 0.82f,
                centerX + cutoutRadius, 0f,
                centerX + cutoutRadius + 6f, 0f
            )
            lineTo(width, 0f)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        return Outline.Generic(path)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ApisBottomNav(
    currentTab: ScreenTab,
    visibleTabs: List<ScreenTab>,
    onTabSelected: (ScreenTab) -> Unit,
    onOpenMenuCustomization: () -> Unit = {},
    pendingTasksCount: Int = 0,
    sanitaryAlertCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val sideTabs = visibleTabs.filter { it != ScreenTab.DASHBOARD }
    val half = (sideTabs.size + 1) / 2
    val leftTabs = sideTabs.take(half)
    val rightTabs = sideTabs.drop(half)

    val isDashboardSelected = currentTab == ScreenTab.DASHBOARD
    val darkGrayNavBg = Color(0xFF1E293B) // Barra de navegação cinzenta-escura
    val amberYellow = Color(0xFFF59E0B) // Amarelo-Âmbar

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Barra de Navegação Inferior Cinzenta-Escura com Recorte Semicircular Superior
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = SemicircularNotchedShape(cutoutRadiusDp = 34f),
                    ambientColor = Color.Black.copy(alpha = 0.5f),
                    spotColor = Color.Black.copy(alpha = 0.7f)
                ),
            shape = SemicircularNotchedShape(cutoutRadiusDp = 34f),
            color = darkGrayNavBg,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- LADO ESQUERDO ---
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    leftTabs.forEach { tab ->
                        BottomNavItem(
                            tab = tab,
                            isSelected = currentTab == tab,
                            onClick = { onTabSelected(tab) },
                            onLongClick = onOpenMenuCustomization,
                            pendingTasksCount = pendingTasksCount,
                            sanitaryAlertCount = sanitaryAlertCount,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Espaço central do recorte semicircular para o FAB
                Spacer(modifier = Modifier.width(72.dp))

                // --- LADO DIREITO ---
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rightTabs.forEach { tab ->
                        BottomNavItem(
                            tab = tab,
                            isSelected = currentTab == tab,
                            onClick = { onTabSelected(tab) },
                            onLongClick = onOpenMenuCustomization,
                            pendingTasksCount = pendingTasksCount,
                            sanitaryAlertCount = sanitaryAlertCount,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // --- BOTÃO CENTRAL FLUTUANTE ENCAIXADO NO RECORTE (DOCKED FAB) ---
        val centerScale by animateFloatAsState(
            targetValue = if (isDashboardSelected) 1.08f else 1.0f,
            animationSpec = tween(durationMillis = 200),
            label = "center_scale"
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-16).dp)
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false, radius = 32.dp),
                    onClick = { onTabSelected(ScreenTab.DASHBOARD) },
                    onLongClick = onOpenMenuCustomization
                )
                .testTag("nav_dashboard"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botão circular amarelo-âmbar com borda preta parcialmente encaixado no recorte
            Box(
                modifier = Modifier
                    .scale(centerScale)
                    .size(54.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = CircleShape,
                        ambientColor = Color.Black,
                        spotColor = amberYellow
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFFFBBF24), // Amarelo-Âmbar brilhante
                                amberYellow,
                                Color(0xFFD97706)
                            )
                        )
                    )
                    .border(
                        width = 2.5.dp,
                        color = Color.Black, // Borda preta destacando o docked FAB
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.apis_smart_bee_icon_1790690258058),
                    contentDescription = "Menu Iniciar - Abelha ApisSmart",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Iniciar",
                fontSize = 10.sp,
                fontWeight = if (isDashboardSelected) FontWeight.Black else FontWeight.Bold,
                color = if (isDashboardSelected) amberYellow else Color(0xFFCBD5E1),
                maxLines = 1,
                letterSpacing = (-0.2).sp
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BottomNavItem(
    tab: ScreenTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    pendingTasksCount: Int,
    sanitaryAlertCount: Int,
    modifier: Modifier = Modifier
) {
    val amberYellow = Color(0xFFF59E0B)
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) amberYellow else Color(0xFF94A3B8),
        label = "icon_color"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 24.dp),
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 4.dp)
            .testTag("nav_${tab.name.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Indicador de aba selecionada
        Box(
            modifier = Modifier
                .height(32.dp)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(amberYellow.copy(alpha = 0.18f))
                )
            }

            when (tab) {
                ScreenTab.APIARIES -> {
                    if (sanitaryAlertCount > 0) {
                        BadgedBox(badge = { Badge { Text("$sanitaryAlertCount", fontSize = 9.sp) } }) {
                            Icon(
                                imageVector = if (isSelected) Icons.Filled.Agriculture else Icons.Outlined.Agriculture,
                                contentDescription = tab.title,
                                tint = iconColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (isSelected) Icons.Filled.Agriculture else Icons.Outlined.Agriculture,
                            contentDescription = tab.title,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                ScreenTab.INSPECTIONS -> {
                    Icon(
                        imageVector = if (isSelected) Icons.AutoMirrored.Filled.Assignment else Icons.AutoMirrored.Outlined.Assignment,
                        contentDescription = tab.title,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                ScreenTab.HARVESTS -> {
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.Scale else Icons.Outlined.Scale,
                        contentDescription = tab.title,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                ScreenTab.INVENTORY -> {
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2,
                        contentDescription = tab.title,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                ScreenTab.SCHEDULE -> {
                    if (pendingTasksCount > 0) {
                        BadgedBox(badge = { Badge { Text("$pendingTasksCount", fontSize = 9.sp) } }) {
                            Icon(
                                imageVector = if (isSelected) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                                contentDescription = tab.title,
                                tint = iconColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (isSelected) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                            contentDescription = tab.title,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                ScreenTab.FINANCE -> {
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.Payments else Icons.Outlined.Payments,
                        contentDescription = tab.title,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                ScreenTab.AI_ASSISTANT -> {
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                        contentDescription = tab.title,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                ScreenTab.BLUETOOTH_TAGS -> {
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.BluetoothSearching else Icons.Outlined.BluetoothSearching,
                        contentDescription = tab.title,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                ScreenTab.FLORAL_CALENDAR -> {
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.Eco else Icons.Outlined.Eco,
                        contentDescription = tab.title,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                ScreenTab.DASHBOARD -> Unit
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = tab.title,
            fontSize = 8.5.sp,
            letterSpacing = (-0.3).sp,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            textAlign = TextAlign.Center,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) amberYellow else Color(0xFF94A3B8)
        )
    }
}
