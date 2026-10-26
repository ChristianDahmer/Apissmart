package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.ScrollableTabRow
import com.example.billing.BillingConnectionState
import com.example.billing.PlayProductItem
import com.example.billing.PlayBillingManager
import java.util.Locale
import kotlinx.coroutines.launch
import com.example.license.BrasilApiCnpjService
import com.example.license.CnpjAssociationVerificationResult
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.license.LicenseData
import com.example.license.LicenseStatus
import com.example.license.LicenseTier
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.MeadowGreenSecondary
import com.example.ui.theme.WarningAlertRed

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LicenseDialog(
    licenseData: LicenseData,
    isVerifyingServer: Boolean,
    serverMessage: String?,
    onDismiss: () -> Unit,
    onActivateKey: (key: String) -> Unit,
    onVerifyServer: (customUrl: String?) -> Unit,
    onUpdateServerUrl: (String) -> Unit,
    onGenerateAdminKey: (tier: LicenseTier, days: Int, targetDevId: String?, licensesCount: Int, associationName: String) -> String,
    onResetTrial: () -> Unit,
    onDeactivate: () -> Unit,
    playProducts: List<PlayProductItem> = emptyList(),
    billingConnectionState: BillingConnectionState = BillingConnectionState.CONNECTED,
    isBillingProcessing: Boolean = false,
    billingStatusMessage: String? = null,
    onPurchaseProduct: (productId: String) -> Unit = {},
    onRestorePurchases: () -> Unit = {},
    onManageGooglePlaySubscriptions: () -> Unit = {},
    onClearBillingMessage: () -> Unit = {},
    onShowPrivacyPolicy: () -> Unit = {}
) {
    val canDismiss = licenseData.canUseApp

    Dialog(
        onDismissRequest = {
            if (canDismiss) onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = canDismiss,
            dismissOnClickOutside = canDismiss
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp
        ) {
            LicenseManagementCard(
                licenseData = licenseData,
                isVerifyingServer = isVerifyingServer,
                serverMessage = serverMessage,
                onActivateKey = onActivateKey,
                onVerifyServer = onVerifyServer,
                onUpdateServerUrl = onUpdateServerUrl,
                onGenerateAdminKey = onGenerateAdminKey,
                onResetTrial = onResetTrial,
                onDeactivate = onDeactivate,
                playProducts = playProducts,
                billingConnectionState = billingConnectionState,
                isBillingProcessing = isBillingProcessing,
                billingStatusMessage = billingStatusMessage,
                onPurchaseProduct = onPurchaseProduct,
                onRestorePurchases = onRestorePurchases,
                onManageGooglePlaySubscriptions = onManageGooglePlaySubscriptions,
                onClearBillingMessage = onClearBillingMessage,
                onShowPrivacyPolicy = onShowPrivacyPolicy,
                onClose = if (canDismiss) onDismiss else null,
                modifier = Modifier.fillMaxSize().padding(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LicenseManagementCard(
    licenseData: LicenseData,
    isVerifyingServer: Boolean,
    serverMessage: String?,
    onActivateKey: (key: String) -> Unit,
    onVerifyServer: (customUrl: String?) -> Unit,
    onUpdateServerUrl: (String) -> Unit,
    onGenerateAdminKey: (tier: LicenseTier, days: Int, targetDevId: String?, licensesCount: Int, associationName: String) -> String,
    onResetTrial: () -> Unit,
    onDeactivate: () -> Unit,
    playProducts: List<PlayProductItem> = emptyList(),
    billingConnectionState: BillingConnectionState = BillingConnectionState.CONNECTED,
    isBillingProcessing: Boolean = false,
    billingStatusMessage: String? = null,
    onPurchaseProduct: (productId: String) -> Unit = {},
    onRestorePurchases: () -> Unit = {},
    onManageGooglePlaySubscriptions: () -> Unit = {},
    onClearBillingMessage: () -> Unit = {},
    onShowPrivacyPolicy: () -> Unit = {},
    onClose: (() -> Unit)? = null,
    initialDevAuthenticated: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var inputKey by remember { mutableStateOf("") }
    var inputServerUrl by remember { mutableStateOf(licenseData.serverVerificationUrl) }
    var showServerConfig by remember { mutableStateOf(false) }

    // Association plan simulation states (Tab 0)
    var simLicensesCount by remember { mutableIntStateOf(if (licenseData.tier == LicenseTier.ASSOCIATION && licenseData.licensesCount > 1) licenseData.licensesCount else 10) }
    var simBillingCycle by remember { mutableStateOf(if (licenseData.tier == LicenseTier.ASSOCIATION) licenseData.billingCycle else "MENSAL") }
    var simAssociationName by remember { mutableStateOf(licenseData.associationName) }
    var simCnpjInput by remember { mutableStateOf("") }
    var isConsultingCnpj by remember { mutableStateOf(false) }
    var cnpjVerificationResult by remember { mutableStateOf<CnpjAssociationVerificationResult?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val onConsultCnpj: () -> Unit = {
        val clean = simCnpjInput.filter { it.isDigit() }
        if (clean.length != 14) {
            Toast.makeText(context, "Informe os 14 dígitos do CNPJ para consulta na Brasil API.", Toast.LENGTH_SHORT).show()
        } else {
            isConsultingCnpj = true
            coroutineScope.launch {
                val result = BrasilApiCnpjService.consultAndValidateAssociation(clean)
                cnpjVerificationResult = result
                isConsultingCnpj = false
                if (result.isSuccess) {
                    if (simAssociationName.isBlank() || simAssociationName == "Associação de Apicultores") {
                        simAssociationName = result.nomeFantasia.ifBlank { result.razaoSocial }
                    }
                    if (result.isConfirmedBeekeeperAssociation) {
                        Toast.makeText(context, "✅ Associação de apicultores confirmada na Brasil API!", Toast.LENGTH_LONG).show()
                    } else if (result.isAssociationOrCoop) {
                        Toast.makeText(context, "ℹ️ Entidade associativa confirmada na Brasil API.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "⚠️ CNPJ não identificado como associação de apicultores.", Toast.LENGTH_LONG).show()
                    }
                } else if (result.errorMessage != null) {
                    Toast.makeText(context, result.errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Developer authentication states (Exclusive for Dahmer)
    var isDevAuthenticated by remember { mutableStateOf(initialDevAuthenticated) }
    var devUsernameInput by remember { mutableStateOf("") }
    var devPasswordInput by remember { mutableStateOf("") }
    var devPasswordVisible by remember { mutableStateOf(false) }
    var devAuthError by remember { mutableStateOf<String?>(null) }

    // Admin key generator states
    var adminTier by remember { mutableStateOf(LicenseTier.PRO) }
    var adminDays by remember { mutableIntStateOf(365) }
    var adminTargetDevId by remember { mutableStateOf("") }
    var adminLicensesCount by remember { mutableIntStateOf(10) }
    var adminAssociationName by remember { mutableStateOf("") }
    var adminCnpjInput by remember { mutableStateOf("") }
    var isAdminConsultingCnpj by remember { mutableStateOf(false) }
    var adminCnpjResult by remember { mutableStateOf<CnpjAssociationVerificationResult?>(null) }
    var generatedKeyResult by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Maximized Top Header with small Close button on top right
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(HoneyGoldPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Licença",
                        tint = HoneyGoldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Licenciamento ApisSmart",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Gestão independente & Emissão de licenças",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            // Small Close Button on Top Right
            if (onClose != null) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .testTag("admin_close_btn")
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

        Spacer(modifier = Modifier.height(12.dp))

        // Status Card
        val statusBgColor = when (licenseData.status) {
            LicenseStatus.ACTIVE -> Color(0xFFD4EDDA)
            LicenseStatus.TRIAL -> Color(0xFFFFF3CD)
            LicenseStatus.GRACE_PERIOD -> Color(0xFFFFE0B2)
            LicenseStatus.EXPIRED -> Color(0xFFFFCDD2)
        }
        val statusTextColor = when (licenseData.status) {
            LicenseStatus.ACTIVE -> Color(0xFF155724)
            LicenseStatus.TRIAL -> Color(0xFF856404)
            LicenseStatus.GRACE_PERIOD -> Color(0xFFBF360C)
            LicenseStatus.EXPIRED -> Color(0xFF721C24)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = statusBgColor)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${licenseData.status.badgeEmoji} ${licenseData.status.label}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = statusTextColor
                        )
                    )
                    Text(
                        text = licenseData.tier.displayName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = statusTextColor
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (licenseData.status == LicenseStatus.ACTIVE) {
                        "Validade: ${licenseData.formattedExpiry} (${if (licenseData.isLifetime) "Vitalícia" else "${licenseData.daysRemaining} dias restantes"})"
                    } else if (licenseData.status == LicenseStatus.TRIAL) {
                        "Restam ${licenseData.daysRemaining} dias do período gratuito de testes."
                    } else {
                        "Acesso bloqueado ou expirado. Ative com sua chave ou servidor."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(color = statusTextColor)
                )

                if (licenseData.tier == LicenseTier.ASSOCIATION) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🏛️ Associação: ${licenseData.associationName.ifBlank { "Associação de Apicultores" }} • ${licenseData.licensesCount} Licenças Autorizadas",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = statusTextColor
                        )
                    )
                    Text(
                        text = "Modalidade: Plano ${licenseData.billingCycle.lowercase().replaceFirstChar { it.uppercase() }} (R$ ${if (licenseData.billingCycle == "ANUAL") "149,90/ano" else "14,90/mês"} por licença)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = statusTextColor.copy(alpha = 0.9f)
                        )
                    )
                }

                if (licenseData.licenseKey.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Chave Ativa: ${licenseData.licenseKey}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = statusTextColor.copy(alpha = 0.85f)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Device ID Card with 1-click Copy & Share
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ID DESTE DISPOSITIVO:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = licenseData.deviceId,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("ID ApisSmart", licenseData.deviceId))
                                Toast.makeText(context, "ID copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copiar ID",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = {
                                val shareText = "Olá! Gostaria de ativar a licença do ApisSmart para o meu aparelho. ID do dispositivo: ${licenseData.deviceId}"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(intent, "Enviar ID ao Administrador"))
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Compartilhar ID",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Text(
                    text = "Envie este ID ao suporte para emitir sua licença personalizada (WhatsApp: 51 99677-6214).",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("🛍️ Google Play", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("🏷️ Associações", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("🔑 Chave", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("🌐 Servidor", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 4,
                onClick = { selectedTab = 4 },
                text = { Text("⚙️ Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 5,
                onClick = { selectedTab = 5 },
                text = { Text("🔒 Detalhes & Privacidade", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // TAB 0: Google Play Billing (Faturamento Oficial Play Store)
        if (selectedTab == 0) {
            GooglePlayBillingTabContent(
                licenseData = licenseData,
                playProducts = playProducts,
                billingConnectionState = billingConnectionState,
                isBillingProcessing = isBillingProcessing,
                billingStatusMessage = billingStatusMessage,
                onPurchaseProduct = onPurchaseProduct,
                onRestorePurchases = onRestorePurchases,
                onManageGooglePlaySubscriptions = onManageGooglePlaySubscriptions,
                onClearBillingMessage = onClearBillingMessage,
                onShowPrivacyPolicy = onShowPrivacyPolicy,
                onSwitchToAssociationTab = { selectedTab = 1 }
            )
        }

        // TAB 1: Planos Corporativos & Simulador para Associação de Apicultores
        if (selectedTab == 1) {
            Column {
                Text(
                    text = "Planos para Associações e Cooperativas",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Opções individuais ou pacotes corporativos para associações e cooperativas de apicultores.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Card dos Planos Individuais (Mensal R$ 14,90 e Anual R$ 149,90)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👤", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Plano Individual de Entrada",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Card Mensal: R$ 14,90
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (simBillingCycle == "MENSAL") HoneyGoldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                                ),
                                border = if (simBillingCycle == "MENSAL") androidx.compose.foundation.BorderStroke(1.5.dp, HoneyGoldPrimary) else null
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Plano Mensal",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = "R$ 14,90",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = HoneyGoldPrimary
                                            )
                                        )
                                        Text(
                                            text = "/mês",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Acesso completo mês a mês sem fidelidade.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }

                            // Card Anual: R$ 149,90
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (simBillingCycle == "ANUAL") HoneyGoldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                                ),
                                border = if (simBillingCycle == "ANUAL") androidx.compose.foundation.BorderStroke(2.dp, HoneyGoldPrimary) else null
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Plano Anual",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = HoneyGoldPrimary
                                        ) {
                                            Text(
                                                text = "-17% OFF",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Color.Black,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp
                                                ),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = "R$ 149,90",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = HoneyGoldPrimary
                                            )
                                        )
                                        Text(
                                            text = "/ano",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Economia de 2 meses! R$ 12,49/mês equiv.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card Especial: Plano para Associações de Apicultores
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, HoneyGoldPrimary.copy(alpha = 0.85f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🏛️", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Plano para Associações de Apicultores",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Text(
                                    text = "Defina o número de licenças para os associados",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // CNPJ da Associação com Consulta Brasil API
                        Text(
                            text = "CNPJ da Associação / Cooperativa:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = simCnpjInput,
                                onValueChange = { input ->
                                    val digits = input.filter { it.isDigit() }.take(14)
                                    simCnpjInput = BrasilApiCnpjService.formatCnpj(digits)
                                    if (cnpjVerificationResult != null && cnpjVerificationResult?.cnpjRaw != digits) {
                                        cnpjVerificationResult = null
                                    }
                                },
                                label = { Text("CNPJ da Entidade") },
                                placeholder = { Text("00.000.000/0000-00") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { onConsultCnpj() }),
                                leadingIcon = {
                                    Icon(Icons.Default.Business, contentDescription = null, tint = HoneyGoldPrimary)
                                },
                                trailingIcon = {
                                    if (isConsultingCnpj) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = HoneyGoldPrimary)
                                    } else if (cnpjVerificationResult?.isConfirmedBeekeeperAssociation == true) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Validado", tint = Color(0xFF2E7D32))
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )

                            Button(
                                onClick = onConsultCnpj,
                                enabled = !isConsultingCnpj,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary)
                            ) {
                                if (isConsultingCnpj) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Search, contentDescription = "Consultar", modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Consultar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Card com Resultado da Validação na Brasil API
                        cnpjVerificationResult?.let { result ->
                            Spacer(modifier = Modifier.height(8.dp))
                            val cardBgColor = when {
                                !result.isSuccess -> Color(0xFFFFEBEE)
                                result.isConfirmedBeekeeperAssociation -> Color(0xFFE8F5E9)
                                result.isAssociationOrCoop -> Color(0xFFFFF8E1)
                                else -> Color(0xFFFFEBEE)
                            }
                            val cardBorderColor = when {
                                !result.isSuccess -> Color(0xFFE57373)
                                result.isConfirmedBeekeeperAssociation -> Color(0xFF4CAF50)
                                result.isAssociationOrCoop -> Color(0xFFFFB74D)
                                else -> Color(0xFFE57373)
                            }
                            val cardTextColor = when {
                                !result.isSuccess -> Color(0xFFC62828)
                                result.isConfirmedBeekeeperAssociation -> Color(0xFF1B5E20)
                                result.isAssociationOrCoop -> Color(0xFFB78103)
                                else -> Color(0xFFC62828)
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorderColor)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (result.isConfirmedBeekeeperAssociation) Icons.Default.CheckCircle else if (result.isAssociationOrCoop) Icons.Default.Info else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = cardTextColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = result.statusBadgeText.ifBlank { if (result.isSuccess) "CONSULTA BRASIL API CONCLUÍDA" else "ERRO NA CONSULTA" },
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = cardTextColor
                                            )
                                        )
                                    }

                                    if (result.errorMessage != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = result.errorMessage,
                                            style = MaterialTheme.typography.bodySmall.copy(color = cardTextColor, fontSize = 11.5.sp)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Razão Social: ${result.razaoSocial}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                        )
                                        if (result.nomeFantasia.isNotBlank()) {
                                            Text(
                                                text = "Nome Fantasia: ${result.nomeFantasia}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                                            )
                                        }
                                        Text(
                                            text = "Natureza Jurídica: ${result.naturezaJuridica}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                        Text(
                                            text = "CNAE Principal: ${result.cnaeFiscal} - ${result.cnaeFiscalDescricao}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                        Text(
                                            text = "Situação: ${result.situacaoCadastral} • ${result.municipio}/${result.uf}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = result.statusMessage,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = cardTextColor,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Nome da Associação
                        OutlinedTextField(
                            value = simAssociationName,
                            onValueChange = { simAssociationName = it },
                            label = { Text("Nome da Associação / Cooperativa") },
                            placeholder = { Text("Ex: Associação de Apicultores da Mantiqueira") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Seletor de Ciclo de Pagamento
                        Text(
                            text = "Ciclo de Pagamento das Licenças:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            SuggestionChip(
                                onClick = { simBillingCycle = "MENSAL" },
                                label = { Text("Mensal (R$ 14,90/licença)", fontSize = 11.sp) },
                                colors = if (simBillingCycle == "MENSAL") {
                                    SuggestionChipDefaults.suggestionChipColors(containerColor = HoneyGoldPrimary.copy(alpha = 0.35f))
                                } else {
                                    SuggestionChipDefaults.suggestionChipColors()
                                }
                            )

                            SuggestionChip(
                                onClick = { simBillingCycle = "ANUAL" },
                                label = { Text("Anual (R$ 149,90/licença)", fontSize = 11.sp) },
                                colors = if (simBillingCycle == "ANUAL") {
                                    SuggestionChipDefaults.suggestionChipColors(containerColor = HoneyGoldPrimary.copy(alpha = 0.35f))
                                } else {
                                    SuggestionChipDefaults.suggestionChipColors()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Definição do Número de Licenças
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Número de Licenças da Associação:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "$simLicensesCount associados",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = HoneyGoldPrimary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Seletor rápido de quantidade
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(5, 10, 20, 30, 50, 100).forEach { count ->
                                SuggestionChip(
                                    onClick = { simLicensesCount = count },
                                    label = { Text("$count licenças", fontSize = 11.sp) },
                                    colors = if (simLicensesCount == count) {
                                        SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    } else {
                                        SuggestionChipDefaults.suggestionChipColors()
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Botões de incremento e decremento fino
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = { if (simLicensesCount > 1) simLicensesCount-- },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Diminuir", modifier = Modifier.size(18.dp))
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = "$simLicensesCount Licenças",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            IconButton(
                                onClick = { simLicensesCount++ },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Aumentar", modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = simLicensesCount.toString(),
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }
                                if (clean.isNotBlank()) {
                                    simLicensesCount = clean.toIntOrNull()?.coerceIn(1, 9999) ?: 1
                                }
                            },
                            label = { Text("Digitar quantidade exata de licenças") },
                            placeholder = { Text("Ex: 15, 35, 75...") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Resumo de Cálculo Financeiro da Associação
                        val unitPrice = if (simBillingCycle == "ANUAL") 149.90 else 14.90
                        val totalPrice = simLicensesCount * unitPrice
                        val totalFormatted = String.format(Locale.GERMANY, "%.2f", totalPrice)
                        val unitFormatted = String.format(Locale.GERMANY, "%.2f", unitPrice)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Cota para Associação:", style = MaterialTheme.typography.bodySmall)
                                    Text("$simLicensesCount apicultores", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                }
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Valor por Licença:", style = MaterialTheme.typography.bodySmall)
                                    Text("R$ $unitFormatted (${if (simBillingCycle == "ANUAL") "ano" else "mês"})", style = MaterialTheme.typography.bodySmall)
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Investimento Total:",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "R$ $totalFormatted / ${if (simBillingCycle == "ANUAL") "ano" else "mês"}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = HoneyGoldPrimary
                                        )
                                    )
                                }
                                if (simBillingCycle == "ANUAL") {
                                    val savings = (simLicensesCount * 14.90 * 12) - totalPrice
                                    val savingsFormatted = String.format(Locale.GERMANY, "%.2f", savings)
                                    Text(
                                        text = "💡 Economia anual da associação: R$ $savingsFormatted (2 meses grátis)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Botão para Contratação / Contato WhatsApp
                        Button(
                            onClick = {
                                val assocTitle = simAssociationName.ifBlank {
                                    cnpjVerificationResult?.razaoSocial?.ifBlank { "Associação de Apicultores" } ?: "Associação de Apicultores"
                                }
                                val cnpjDetails = if (simCnpjInput.isNotBlank()) {
                                    val cnpjNum = cnpjVerificationResult?.cnpjFormatted ?: simCnpjInput
                                    val statusTxt = cnpjVerificationResult?.statusBadgeText ?: "Informado"
                                    val cnaeTxt = cnpjVerificationResult?.cnaeFiscalDescricao?.takeIf { it.isNotBlank() }?.let { "🐝 *Atividade Principal:* $it\n" } ?: ""
                                    "📑 *CNPJ:* $cnpjNum\n" +
                                    "🔍 *Validação Brasil API:* $statusTxt\n" +
                                    cnaeTxt
                                } else ""

                                val whatsappMsg = "🐝 *Solicitação de Plano de Licença para Associação - ApisSmart*\n\n" +
                                        "🏛️ *Associação:* $assocTitle\n" +
                                        cnpjDetails +
                                        "👥 *Número de Licenças:* $simLicensesCount associados\n" +
                                        "📅 *Ciclo Escolhido:* ${if (simBillingCycle == "ANUAL") "Anual (R$ 149,90/licença)" else "Mensal (R$ 14,90/licença)"}\n" +
                                        "💰 *Valor Total Calculado:* R$ $totalFormatted / ${if (simBillingCycle == "ANUAL") "ano" else "mês"}\n" +
                                        "📱 *ID do Aparelho do Solicitante:* ${licenseData.deviceId}\n\n" +
                                        "Gostaria de fechar o pacote de licenças para os apicultores da nossa associação!"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, whatsappMsg)
                                }
                                context.startActivity(Intent.createChooser(intent, "Contratar Plano para Associação via WhatsApp"))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Contratar para Associação via WhatsApp", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        TextButton(
                            onClick = { selectedTab = 2 },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Já possui a chave de ativação da associação? Clique aqui", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // TAB 2: Chave de Acesso
        if (selectedTab == 2) {
            Column {
                Text(
                    text = "Ativação com Chave de Acesso",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Insira o código de ativação fornecido pelo desenvolvedor/administrador.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = inputKey,
                    onValueChange = { inputKey = it.uppercase() },
                    label = { Text("Chave de Acesso (Ex: APIS-PRO-...)") },
                    placeholder = { Text("APIS-PRO-XXXX-XXXX") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("license_input_key_field")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (inputKey.isNotBlank()) {
                            onActivateKey(inputKey)
                        } else {
                            Toast.makeText(context, "Digite uma chave de acesso válida.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("license_activate_key_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ativar Chave de Acesso", fontWeight = FontWeight.Bold)
                }
            }
        }

        // TAB 3: Servidor Online
        if (selectedTab == 3) {
            Column {
                Text(
                    text = "Verificação Remota no Servidor",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Validação automática online através do seu servidor de licenciamento.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Endpoint Atual:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = licenseData.serverVerificationUrl,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Última verificação: ${licenseData.formattedLastCheck}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = { showServerConfig = !showServerConfig }) {
                        Icon(imageVector = Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showServerConfig) "Ocultar URL" else "Alterar URL do Servidor", fontSize = 12.sp)
                    }
                }

                if (showServerConfig) {
                    OutlinedTextField(
                        value = inputServerUrl,
                        onValueChange = { inputServerUrl = it },
                        label = { Text("URL do Servidor de Licença") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = {
                            onUpdateServerUrl(inputServerUrl)
                            Toast.makeText(context, "URL do servidor atualizada!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Salvar URL")
                    }
                }

                serverMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = msg,
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { onVerifyServer(inputServerUrl) },
                    enabled = !isVerifyingServer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("license_verify_server_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isVerifyingServer) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verificando com o Servidor...")
                    } else {
                        Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verificar Licença no Servidor", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // TAB 4: Painel do Administrador / Desenvolvedor (Uso Exclusivo do Desenvolvedor: Dahmer / @140115Leo)
        if (selectedTab == 4) {
            Column {
                if (!isDevAuthenticated) {
                    // Developer Login Form (Exclusive Access)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        ),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(HoneyGoldPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Acesso Restrito",
                                    tint = HoneyGoldPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Área Exclusiva do Desenvolvedor",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "A emissão e geração de chaves é de uso restrito ao desenvolvedor autorizado.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = devUsernameInput,
                                onValueChange = {
                                    devUsernameInput = it
                                    devAuthError = null
                                },
                                label = { Text("Usuário do Desenvolvedor") },
                                placeholder = { Text("Digite o usuário") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("dev_login_user_input")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = devPasswordInput,
                                onValueChange = {
                                    devPasswordInput = it
                                    devAuthError = null
                                },
                                label = { Text("Senha do Desenvolvedor") },
                                placeholder = { Text("Digite a senha de segurança") },
                                singleLine = true,
                                visualTransformation = if (devPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { devPasswordVisible = !devPasswordVisible }) {
                                        Icon(
                                            imageVector = if (devPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Alternar Visibilidade",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (devUsernameInput.trim().equals("Dahmer", ignoreCase = true) && devPasswordInput == "@140115Leo") {
                                            isDevAuthenticated = true
                                            devAuthError = null
                                            Toast.makeText(context, "Acesso concedido! Bem-vindo, Dahmer.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            devAuthError = "Usuário ou senha incorretos. Acesso restrito ao desenvolvedor."
                                        }
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("dev_login_password_input")
                            )

                            devAuthError?.let { errorMsg ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f))
                                        .padding(8.dp)
                                    ) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = errorMsg,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    if (devUsernameInput.trim().equals("Dahmer", ignoreCase = true) && devPasswordInput == "@140115Leo") {
                                        isDevAuthenticated = true
                                        devAuthError = null
                                        Toast.makeText(context, "Acesso concedido! Bem-vindo, Dahmer.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        devAuthError = "Usuário ou senha incorretos. Acesso restrito ao desenvolvedor."
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("dev_login_submit_btn"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary)
                            ) {
                                Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Acessar Gerador de Chaves", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // DEVELOPER AUTHENTICATED PANEL (Full Key Generator Suite)
                    Column {
                        // Dev Status Banner with Lock/Logout option
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Desenvolvedor Autenticado: Dahmer",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }

                            TextButton(
                                onClick = {
                                    isDevAuthenticated = false
                                    devUsernameInput = ""
                                    devPasswordInput = ""
                                    devAuthError = null
                                },
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Bloquear", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Gerador de Chaves Criptografadas para Clientes",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Gere licenças exclusivas offline ou para ativação remota vinculadas ao aparelho do cliente.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Tier Selection
                        Text("1. Categoria da Licença:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            LicenseTier.values().filter { it != LicenseTier.TRIAL }.forEach { tier ->
                                SuggestionChip(
                                    onClick = { adminTier = tier },
                                    label = { Text(tier.displayName, fontSize = 11.sp) },
                                    colors = if (adminTier == tier) {
                                        SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = HoneyGoldPrimary.copy(alpha = 0.25f)
                                        )
                                    } else {
                                        SuggestionChipDefaults.suggestionChipColors()
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Campos Especiais quando for Plano Associação
                        if (adminTier == LicenseTier.ASSOCIATION) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = HoneyGoldPrimary.copy(alpha = 0.12f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, HoneyGoldPrimary)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "🏛️ Configuração do Plano para Associação:",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = HoneyGoldPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = adminCnpjInput,
                                            onValueChange = { input ->
                                                val digits = input.filter { it.isDigit() }.take(14)
                                                adminCnpjInput = BrasilApiCnpjService.formatCnpj(digits)
                                            },
                                            label = { Text("Consultar CNPJ (Brasil API)") },
                                            placeholder = { Text("00.000.000/0000-00") },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f)
                                        )

                                        Button(
                                            onClick = {
                                                val digits = adminCnpjInput.filter { it.isDigit() }
                                                if (digits.length == 14) {
                                                    isAdminConsultingCnpj = true
                                                    coroutineScope.launch {
                                                        val res = BrasilApiCnpjService.consultAndValidateAssociation(digits)
                                                        adminCnpjResult = res
                                                        isAdminConsultingCnpj = false
                                                        if (res.isSuccess) {
                                                            adminAssociationName = res.nomeFantasia.ifBlank { res.razaoSocial }
                                                        }
                                                    }
                                                } else {
                                                    Toast.makeText(context, "Informe 14 dígitos do CNPJ.", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            enabled = !isAdminConsultingCnpj,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            if (isAdminConsultingCnpj) {
                                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                                            } else {
                                                Text("Validar", fontSize = 11.sp)
                                            }
                                        }
                                    }

                                    adminCnpjResult?.let { res ->
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = res.statusBadgeText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (res.isConfirmedBeekeeperAssociation) Color(0xFF2E7D32) else WarningAlertRed
                                        )
                                        if (res.isSuccess && res.cnaeFiscalDescricao.isNotBlank()) {
                                            Text(
                                                text = "CNAE: ${res.cnaeFiscalDescricao} • ${res.municipio}/${res.uf}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    OutlinedTextField(
                                        value = adminAssociationName,
                                        onValueChange = { adminAssociationName = it },
                                        label = { Text("Nome da Associação / Cooperativa") },
                                        placeholder = { Text("Ex: Associação Apícola Regional") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Número de Licenças da Cota ($adminLicensesCount associados):",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf(5, 10, 20, 25, 50, 100).forEach { cnt ->
                                            SuggestionChip(
                                                onClick = { adminLicensesCount = cnt },
                                                label = { Text("$cnt licenças", fontSize = 11.sp) },
                                                colors = if (adminLicensesCount == cnt) {
                                                    SuggestionChipDefaults.suggestionChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                                } else {
                                                    SuggestionChipDefaults.suggestionChipColors()
                                                }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = adminLicensesCount.toString(),
                                        onValueChange = { input ->
                                            val clean = input.filter { it.isDigit() }
                                            if (clean.isNotBlank()) {
                                                adminLicensesCount = clean.toIntOrNull()?.coerceIn(1, 9999) ?: 1
                                            }
                                        },
                                        label = { Text("Ou digite o número exato de licenças da cota") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Duration Selection
                        Text("2. Duração do Acesso:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(30 to "30 Dias (Mensal)", 90 to "90 Dias (Trimestral)", 180 to "180 Dias", 365 to "1 Ano (Anual)", 9999 to "Vitalícia (Permanente)").forEach { (days, label) ->
                                SuggestionChip(
                                    onClick = { adminDays = days },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = if (adminDays == days) {
                                        SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    } else {
                                        SuggestionChipDefaults.suggestionChipColors()
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Target Device ID with 1-click insert
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("3. ID do Aparelho do Cliente:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            TextButton(
                                onClick = { adminTargetDevId = licenseData.deviceId },
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text("Usar ID deste celular", fontSize = 11.sp)
                            }
                        }

                        OutlinedTextField(
                            value = adminTargetDevId,
                            onValueChange = { adminTargetDevId = it },
                            label = { Text("ID do Aparelho (ou deixe ANY para qualquer celular)") },
                            placeholder = { Text("Digite o ID do cliente ou ANY") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_target_device_field")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val generated = onGenerateAdminKey(
                                    adminTier,
                                    adminDays,
                                    adminTargetDevId,
                                    adminLicensesCount,
                                    adminAssociationName
                                )
                                generatedKeyResult = generated
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_generate_key_submit_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gerar Chave de Ativação Oficial", fontWeight = FontWeight.Bold)
                        }

                        generatedKeyResult?.let { genKey ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "CHAVE GERADA COM SUCESSO:",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                    Text(
                                        text = genKey,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("Chave ApisSmart", genKey))
                                                Toast.makeText(context, "Chave copiada para a área de transferência!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Copiar", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = {
                                                val clientMsg = if (adminTier == LicenseTier.ASSOCIATION) {
                                                    "🐝 *Licença ApisSmart para Associação Liberada!*\n\n" +
                                                            "🏛️ *Associação:* ${adminAssociationName.ifBlank { "Associação de Apicultores" }}\n" +
                                                            "👥 *Cota:* $adminLicensesCount Licenças para Associados\n" +
                                                            "🔑 *Chave de Ativação:* `${genKey}`\n" +
                                                            "⏱️ *Validade:* ${if (adminDays >= 9999) "Vitalícia" else "$adminDays dias"}\n\n" +
                                                            "📲 *Como Ativar no ApisSmart:*\n" +
                                                            "1. Abra o ApisSmart no celular\n" +
                                                            "2. Acesse Licenciamento > Chave\n" +
                                                            "3. Cole a chave acima e clique em Ativar!\n\n" +
                                                            "Qualquer dúvida, suporte: (51) 99677-6214"
                                                } else {
                                                    "🐝 *Sua Licença ApisSmart foi Liberada!*\n\n" +
                                                            "🔑 *Chave de Ativação:* `${genKey}`\n" +
                                                            "📋 *Plano:* ${adminTier.displayName}\n" +
                                                            "⏱️ *Validade:* ${if (adminDays >= 9999) "Vitalícia" else "$adminDays dias"}\n\n" +
                                                            "📲 *Como Ativar:*\n" +
                                                            "1. Abra o ApisSmart no seu celular\n" +
                                                            "2. Acesse Licenciamento > Chave\n" +
                                                            "3. Cole a chave acima e clique em Ativar!\n\n" +
                                                            "Qualquer dúvida, suporte: (51) 99677-6214"
                                                }
                                                val intent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_TEXT, clientMsg)
                                                }
                                                context.startActivity(Intent.createChooser(intent, "Enviar Chave ao Cliente via WhatsApp"))
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("WhatsApp", fontSize = 12.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                inputKey = genKey
                                                selectedTab = 1
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Usar Aqui", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Ferramentas de Teste do Desenvolvedor:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onResetTrial()
                                    Toast.makeText(context, "Modo de avaliação de 30 dias reiniciado!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reiniciar Teste 30D", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    onDeactivate()
                                    Toast.makeText(context, "Licença desativada.", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Desativar Licença", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // TAB 5: Detalhes do Aplicativo & Política de Privacidade e Transparência
        if (selectedTab == 5) {
            AppDetailsAndPrivacyTabContent(
                licenseData = licenseData,
                onShowFullPolicy = onShowPrivacyPolicy
            )
        }
    }
}

@Composable
private fun AppDetailsAndPrivacyTabContent(
    licenseData: LicenseData,
    onShowFullPolicy: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Identity & Details Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            ),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(HoneyGoldPrimary.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🐝", fontSize = 24.sp)
                    }
                    Column {
                        Text(
                            text = "ApisSmart",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Gestão Apícola & Meliponicultura Inteligente",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Versão do App:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("4.44 (Build 74 - Release Oficial)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Pacote Android:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("com.aistudio.apissmart.bzkv", fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Desenvolvedor:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Christian Dahmer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Contato / Suporte:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("(51) 99677-6214", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MeadowGreenSecondary)
                }
            }
        }

        // Privacy Policy & Data Transparency Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = HoneyGoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Política de Privacidade & Proteção de Dados",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Para total transparência com o usuário e conformidade rigorosa com as políticas do Google Play e a LGPD (Lei 13.709/2018), veja abaixo como o ApisSmart lida com dados sensíveis e permissões do dispositivo móvel:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Detail Items
                PrivacyDetailItem(
                    icon = Icons.Default.Storage,
                    title = "1. Armazenamento 100% Local e Criptografado",
                    description = "Todos os seus registros de apiários, colmeias, floradas, inspeções, pesagens de mel e dados financeiros são armazenados localmente no banco de dados SQLite criptografado (Room) do seu celular. Nenhum dado de produção confidencial é enviado para servidores externos ou compartilhado com terceiros."
                )

                Spacer(modifier = Modifier.height(10.dp))

                PrivacyDetailItem(
                    icon = Icons.Default.CameraAlt,
                    title = "2. Câmera do Dispositivo",
                    description = "A câmera é acionada unicamente quando o usuário fotografa quadros de favos para diagnóstico fitossanitário de pragas (Varroa, loque, postura) com inteligência artificial, ou para leitura de QR Code colado nas caixas. As fotos permanecem sob controle do usuário e não são compartilhadas externamente."
                )

                Spacer(modifier = Modifier.height(10.dp))

                PrivacyDetailItem(
                    icon = Icons.Default.Mic,
                    title = "3. Microfone & Comando de Voz (Hands-Free)",
                    description = "Utilizado exclusivamente no Modo Luvas & Voz para permitir ao apicultor ditar anotações de manejo no campo sem retirar as luvas apícolas com própolis. O áudio é transcrito pontualmente em tempo real e não é gravado nem transmitido para servidores de publicidade."
                )

                Spacer(modifier = Modifier.height(10.dp))

                PrivacyDetailItem(
                    icon = Icons.Default.LocationOn,
                    title = "4. Localização Geográfica (GPS / Satélite)",
                    description = "As coordenadas do dispositivo são obtidas apenas com seu consentimento pontual para registrar a localização do apiário no mapa, traçar o raio de voo das abelhas (1,5 km a 3 km) e carregar a previsão agroclimática local. Não realizamos rastreamento de localização contínuo em segundo plano quando o app está fechado."
                )

                Spacer(modifier = Modifier.height(10.dp))

                PrivacyDetailItem(
                    icon = Icons.Default.Bluetooth,
                    title = "5. Bluetooth Low Energy (BLE)",
                    description = "Utilizado para busca e radar de proximidade de beacons ou tags antifurto fixadas em caixas-isca ou colmeias do próprio usuário. O app não acessa outros dispositivos pessoais e não monitora terceiros."
                )

                Spacer(modifier = Modifier.height(10.dp))

                PrivacyDetailItem(
                    icon = Icons.Default.Payment,
                    title = "6. Faturamento Seguro via Google Play",
                    description = "Todas as assinaturas e compras dentro do aplicativo são intermediadas pela infraestrutura oficial do Google Play Billing. O ApisSmart nunca coleta, processa ou armazena números de cartão de crédito ou dados bancários."
                )

                Spacer(modifier = Modifier.height(10.dp))

                PrivacyDetailItem(
                    icon = Icons.Default.Gavel,
                    title = "7. Seus Direitos (LGPD - Lei 13.709/2018)",
                    description = "Você tem total soberania sobre seus dados: pode consultar, editar, exportar backups em formatos abertos (JSON/CSV/PDF) ou excluir definitivamente todos os registros a qualquer momento através do aplicativo."
                )
            }
        }

        // Action Button
        Button(
            onClick = onShowFullPolicy,
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Policy, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ver Termos de Uso e Política Completa", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PrivacyDetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(HoneyGoldPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = HoneyGoldPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun GooglePlayBillingTabContent(
    licenseData: LicenseData,
    playProducts: List<PlayProductItem>,
    billingConnectionState: BillingConnectionState,
    isBillingProcessing: Boolean,
    billingStatusMessage: String?,
    onPurchaseProduct: (productId: String) -> Unit,
    onRestorePurchases: () -> Unit,
    onManageGooglePlaySubscriptions: () -> Unit,
    onClearBillingMessage: () -> Unit,
    onShowPrivacyPolicy: () -> Unit,
    onSwitchToAssociationTab: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Status Message Banner if present
        if (!billingStatusMessage.isNullOrBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = billingStatusMessage,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                    IconButton(
                        onClick = onClearBillingMessage,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Dispensar",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Header Card: Google Play Security Badge
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = "Google Play",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Google Play Faturamento Oficial",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MeadowGreenSecondary)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "100% SEGURO",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Assinatura gerenciada diretamente pela Google Play Store com garantia, cancelamento facilitado e sem expor seus dados.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        // Active Google Play Plan banner if user is active
        if (licenseData.status == LicenseStatus.ACTIVE) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MeadowGreenSecondary.copy(alpha = 0.12f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MeadowGreenSecondary)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MeadowGreenSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sua Assinatura está Ativa!",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MeadowGreenSecondary
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MeadowGreenSecondary)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = licenseData.billingCycle,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Categoria: ${licenseData.tier.displayName} • Validade: ${licenseData.formattedExpiry}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )
                    if (licenseData.licenseKey.startsWith("GP-")) {
                        Text(
                            text = "Código do Pedido: ${licenseData.licenseKey}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onManageGooglePlaySubscriptions,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gerenciar / Cancelar no Google Play", fontSize = 12.sp)
                    }
                }
            }
        }

        // Product Catalog Cards
        Text(
            text = "Escolha seu plano no Google Play:",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )

        playProducts.forEach { product ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("play_product_${product.productId}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (product.isBestValue) {
                        HoneyGoldPrimary.copy(alpha = 0.12f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f)
                    }
                ),
                border = if (product.isBestValue) {
                    androidx.compose.foundation.BorderStroke(2.dp, HoneyGoldPrimary)
                } else {
                    CardDefaults.outlinedCardBorder()
                }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        if (product.savingsBadge != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(HoneyGoldPrimary)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = product.savingsBadge,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.5.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = product.formattedPrice,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (product.isBestValue) HoneyGoldPrimary else MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = if (product.isSubscription) "Cobrado pela Google Play Store" else "Taxa única vitalícia permanente",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Button(
                            onClick = { onPurchaseProduct(product.productId) },
                            enabled = !isBillingProcessing,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (product.isBestValue) HoneyGoldPrimary else MaterialTheme.colorScheme.primary,
                                contentColor = if (product.isBestValue) Color.Black else MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("buy_btn_${product.productId}")
                        ) {
                            if (isBillingProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (product.isSubscription) "Assinar" else "Comprar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Data Protection and Privacy Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            )
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = HoneyGoldPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Seus dados estão protegidos: Armazenamento local SQLite criptografado (100% offline) e total conformidade com a LGPD e Google Play.",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Restore Purchases and Legal Links
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onRestorePurchases,
                modifier = Modifier
                    .weight(1f)
                    .testTag("restore_purchases_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Restaurar Compras", fontSize = 11.5.sp)
            }

            OutlinedButton(
                onClick = onShowPrivacyPolicy,
                modifier = Modifier
                    .weight(1f)
                    .testTag("open_privacy_policy_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Policy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Termos & Privacidade", fontSize = 11.5.sp)
            }
        }

        // Association multi-license link
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Representa uma Associação ou Cooperativa?",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Consulte pacotes coletivos com CNPJ e descontos corporativos.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                TextButton(onClick = onSwitchToAssociationTab) {
                    Text("Ver Pacotes", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Official Google Play Subscriptions Disclosure Text
        Text(
            text = "Informações sobre assinaturas na Google Play:\n" +
                    "• O pagamento será cobrado em sua conta Google Play após a confirmação da compra.\n" +
                    "• A assinatura é renovada automaticamente a menos que seja cancelada pelo menos 24 horas antes do término do período vigente.\n" +
                    "• A renovação será cobrada nas 24 horas anteriores ao término do período atual, mantendo o valor do plano selecionado.\n" +
                    "• Você pode gerenciar ou cancelar sua assinatura a qualquer momento nas configurações da Google Play Store.",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        )
    }
}
