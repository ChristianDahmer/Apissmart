package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.license.LicenseStatus
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.MeadowGreenSecondary
import com.example.ui.theme.WarningAlertRed
import com.example.ui.viewmodel.ScreenTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApisTopBar(
    currentTab: ScreenTab,
    visibleTabs: List<ScreenTab>,
    onNavigateToTab: (ScreenTab) -> Unit,
    onOpenThemeCustomization: () -> Unit,
    onOpenMenuCustomization: () -> Unit = {},
    onOpenLicense: () -> Unit = {},
    onOpenExportReports: () -> Unit = {},
    onOpenBackupRestore: () -> Unit = {},
    onOpenGeneralMap: () -> Unit = {},
    onOpenQrScanner: () -> Unit = {},
    onOpenNutrition: () -> Unit = {},
    onOpenSearch: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenHandsFreeMode: () -> Unit = {},
    onOpenMaintenanceTips: () -> Unit = {},
    onOpenQualitySeals: () -> Unit = {},
    onOpenMarketingMaterial: () -> Unit = {},
    onOpenSupport: () -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {},
    notificationCount: Int = 0,
    licenseStatus: LicenseStatus? = null,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    // Módulos que não estão visíveis no menu inferior são listados dinamicamente no menu superior
    val hiddenTabs = ScreenTab.entries.filter { it !in visibleTabs }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        CenterAlignedTopAppBar(
            modifier = Modifier.statusBarsPadding(),
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            // Canto Esquerdo: Menu do sistema contendo ferramentas e módulos fora do menu inferior
            navigationIcon = {
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.testTag("topbar_menu_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu do Sistema",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .testTag("topbar_dropdown_menu")
                    ) {
                        // --- FERRAMENTAS DO SISTEMA ---
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "FERRAMENTAS DO SISTEMA",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = HoneyGoldPrimary,
                                    letterSpacing = 0.5.sp
                                )
                            },
                            onClick = {},
                            enabled = false
                        )
                        HorizontalDivider()

                        DropdownMenuItem(
                            text = { Text("Suporte & Feedback", fontSize = 14.sp) },
                            onClick = {
                                menuExpanded = false
                                onOpenSupport()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Campaign, contentDescription = null, tint = HoneyGoldPrimary)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Licença & Admin", fontSize = 14.sp) },
                            onClick = {
                                menuExpanded = false
                                onOpenLicense()
                            },
                            leadingIcon = {
                                val iconTint = when (licenseStatus) {
                                    LicenseStatus.EXPIRED -> WarningAlertRed
                                    LicenseStatus.GRACE_PERIOD -> Color(0xFFE65100)
                                    LicenseStatus.TRIAL -> HoneyGoldPrimary
                                    LicenseStatus.ACTIVE -> MeadowGreenSecondary
                                    null -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                                Icon(Icons.Default.Security, contentDescription = null, tint = iconTint)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Política de Privacidade", fontSize = 14.sp) },
                            onClick = {
                                menuExpanded = false
                                onOpenPrivacyPolicy()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Policy, contentDescription = null, tint = HoneyGoldPrimary)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Personalizar Tema & Cores", fontSize = 14.sp) },
                            onClick = {
                                menuExpanded = false
                                onOpenThemeCustomization()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Personalizar Menu Inferior", fontSize = 14.sp) },
                            onClick = {
                                menuExpanded = false
                                onOpenMenuCustomization()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = HoneyGoldPrimary)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Relatórios & Rastreabilidade", fontSize = 14.sp) },
                            onClick = {
                                menuExpanded = false
                                onOpenExportReports()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Description, contentDescription = null, tint = HoneyGoldPrimary)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Backup Google Drive & Nuvem", fontSize = 14.sp) },
                            onClick = {
                                menuExpanded = false
                                onOpenBackupRestore()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = HoneyGoldPrimary)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Rastrear Tags BLE & Radar", fontSize = 14.sp) },
                            onClick = {
                                menuExpanded = false
                                onNavigateToTab(ScreenTab.BLUETOOTH_TAGS)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.BluetoothSearching, contentDescription = null, tint = HoneyGoldPrimary)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Mapa Geral de Apiários GPS", fontSize = 14.sp) },
                            onClick = {
                                menuExpanded = false
                                onOpenGeneralMap()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Map, contentDescription = null, tint = MeadowGreenSecondary)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Leitor QR Code / Scanner", fontSize = 14.sp) },
                            onClick = {
                                menuExpanded = false
                                onOpenQrScanner()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Calculadora Nutricional", fontSize = 14.sp) },
                            onClick = {
                                menuExpanded = false
                                onOpenNutrition()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Science, contentDescription = null, tint = HoneyGoldPrimary)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Modo Luvas & Voz", fontSize = 14.sp) },
                            onClick = {
                                menuExpanded = false
                                onOpenHandsFreeMode()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = HoneyGoldPrimary)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Dicas de Manejo & Manutenção", fontSize = 14.sp) },
                            onClick = {
                                menuExpanded = false
                                onOpenMaintenanceTips()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.TipsAndUpdates, contentDescription = null, tint = HoneyGoldPrimary)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Selos de Qualidade (ARTE, Orgânico, IG)", fontSize = 14.sp) },
                            onClick = {
                                menuExpanded = false
                                onOpenQualitySeals()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = MeadowGreenSecondary)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Material de Marketing & Divulgação", fontSize = 14.sp) },
                            onClick = {
                                menuExpanded = false
                                onOpenMarketingMaterial()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Campaign, contentDescription = null, tint = HoneyGoldPrimary)
                            }
                        )

                        // --- MÓDULOS FORA DO MENU INFERIOR ---
                        if (hiddenTabs.isNotEmpty()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "MÓDULOS DO SISTEMA (OUTROS)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = HoneyGoldPrimary,
                                        letterSpacing = 0.5.sp
                                    )
                                },
                                onClick = {},
                                enabled = false
                            )

                            hiddenTabs.forEach { tab ->
                                DropdownMenuItem(
                                    text = { Text(tab.title, fontSize = 14.sp) },
                                    onClick = {
                                        menuExpanded = false
                                        onNavigateToTab(tab)
                                    },
                                    leadingIcon = {
                                        val iconVector = when (tab) {
                                            ScreenTab.INVENTORY -> Icons.Default.Inventory2
                                            ScreenTab.FINANCE -> Icons.Default.PointOfSale
                                            ScreenTab.AI_ASSISTANT -> Icons.Default.AutoAwesome
                                            ScreenTab.BLUETOOTH_TAGS -> Icons.Default.BluetoothSearching
                                            else -> Icons.Default.Menu
                                        }
                                        Icon(iconVector, contentDescription = null, tint = HoneyGoldPrimary)
                                    }
                                )
                            }
                        }
                    }
                }
            },
            // Parte Central: Nome Apis (Branco) e Smart (Dourado)
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 21.sp)) {
                                append("Apis")
                            }
                            withStyle(SpanStyle(color = HoneyGoldPrimary, fontWeight = FontWeight.Bold, fontSize = 21.sp)) {
                                append("Smart")
                            }
                        }
                    )
                    if (currentTab != ScreenTab.DASHBOARD) {
                        Text(
                            text = currentTab.title,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            // Canto Direito: Modo Luvas & Voz, Lupa de Busca e Sino de Notificações
            actions = {
                // Modo Luvas & Voz (Mãos Livres)
                IconButton(
                    onClick = onOpenHandsFreeMode,
                    modifier = Modifier.testTag("topbar_handsfree_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Modo Luvas & Voz (Mãos Livres)",
                        tint = HoneyGoldPrimary
                    )
                }

                // Lupa de busca
                IconButton(
                    onClick = onOpenSearch,
                    modifier = Modifier.testTag("topbar_search_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Busca no Sistema",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Sino de notificações
                IconButton(
                    onClick = onOpenNotifications,
                    modifier = Modifier.testTag("topbar_notifications_btn")
                ) {
                    if (notificationCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = WarningAlertRed) {
                                    Text("$notificationCount", fontSize = 9.sp, color = Color.White)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificações do Sistema",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notificações do Sistema",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        )
    }
}
