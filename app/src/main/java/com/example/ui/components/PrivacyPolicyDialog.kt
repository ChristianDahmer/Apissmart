package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Dialog displaying Privacy Policy and Terms of Service / Google Play Subscriptions,
 * required for full Google Play Store compliance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("privacy_policy_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
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
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Default.Policy else Icons.Default.Gavel,
                                contentDescription = "Legal",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (selectedTab == 0) "Política de Privacidade" else "Termos de Uso e Assinaturas",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "ApisSmart • Conformidade Google Play & LGPD",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("privacy_dialog_close")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs
                SecondaryTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("🔒 Privacidade", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("📜 Termos & Google Play", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    if (selectedTab == 0) {
                        PrivacyPolicyContent(context = context)
                    } else {
                        TermsAndSubscriptionsContent(context = context)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("privacy_policy_confirm_button")
                    ) {
                        Text("Entendido e Concordo")
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacyPolicyContent(context: Context) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "No ApisSmart, respeitamos rigorosamente a sua privacidade e a soberania dos dados do seu apiário. Seus dados operacionais pertencem exclusivamente a você.",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                )
            }
        }

        LegalSection(
            title = "1. Coleta e Armazenamento de Dados",
            content = "Todos os cadastros de apiários, colmeias, floradas, registros de inspeção, manejos sanitários, colheitas de mel e transações financeiras são armazenados LOCALMENTE no dispositivo utilizando banco de dados SQLite criptografado (Room Database). O ApisSmart não envia seus registros confidenciais de produção para servidores de terceiros ou agências de publicidade."
        )

        LegalSection(
            title = "2. Uso de Permissões do Dispositivo",
            content = "• Câmera: Utilizada unicamente quando o usuário fotografa quadros de colmeias para detecção e diagnóstico de saúde e postura via inteligência artificial.\n" +
                    "• Microfone: Utilizado exclusivamente para transcrição de comandos e anotações por voz em campo (Hands-Free), permitindo ao apicultor inspecionar sem retirar luvas.\n" +
                    "• Localização (GPS): Utilizada para registrar a coordenada geográfica precisa do apiário e obter a previsão meteorológica agroclimática oficial em tempo real.\n" +
                    "• Bluetooth Low Energy (BLE): Utilizado para escanear e rastrear tags de monitoramento anexadas em colmeias e caixas-isca contra extravio ou furto."
        )

        LegalSection(
            title = "3. Pagamentos e Google Play Billing",
            content = "Todas as assinaturas e compras dentro do aplicativo são intermediadas com total segurança pelo sistema oficial Google Play Billing do Google. O ApisSmart nunca tem acesso a números de cartão de crédito, contas bancárias ou credenciais de pagamento do usuário."
        )

        LegalSection(
            title = "4. Backup e Exportação",
            content = "O usuário possui total liberdade para exportar cópias de segurança (Backup JSON/DB) para o Google Drive ou pasta do dispositivo e restaurar quando desejar, garantindo a portabilidade total de seus dados."
        )

        LegalSection(
            title = "5. Seus Direitos (LGPD)",
            content = "Em conformidade com a Lei Geral de Proteção de Dados (LGPD - Lei 13.709/2018), você pode a qualquer momento consultar, corrigir, exportar ou excluir definitivamente qualquer registro do aplicativo através das telas de gerenciamento ou redefinir todos os dados."
        )
    }
}

@Composable
private fun TermsAndSubscriptionsContent(context: Context) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Regras oficiais de faturamento, renovação automática e cancelamento de assinaturas via Google Play Store.",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                )
            }
        }

        LegalSection(
            title = "1. Planos de Assinatura e Faturamento",
            content = "• Plano Mensal Pro: R$ 14,90 cobrados mensalmente na sua conta Google Play.\n" +
                    "• Plano Anual Pro: R$ 149,90 cobrados anualmente na sua conta Google Play (desconto de ~17%).\n" +
                    "• Licença Vitalícia Pro: R$ 349,90 cobrados em pagamento único permanente via Google Play."
        )

        LegalSection(
            title = "2. Renovação Automática",
            content = "As assinaturas mensais e anuais são renovadas automaticamente a menos que a renovação automática seja desativada na Google Play Store pelo menos 24 horas antes do final do ciclo de faturamento atual. A cobrança da renovação ocorrerá dentro das 24 horas anteriores ao término do período vigente."
        )

        LegalSection(
            title = "3. Cancelamento a Qualquer Momento",
            content = "Você pode cancelar sua assinatura a qualquer momento através do aplicativo Google Play Store (Menu de Perfil -> Pagamentos e assinaturas -> Assinaturas). Após o cancelamento, seu acesso Pro permanecerá ativo até o final do período já pago."
        )

        OutlinedButton(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                } catch (_: Exception) {}
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Gerenciar Assinaturas no Google Play")
        }

        LegalSection(
            title = "4. Reembolsos",
            content = "Os reembolsos de compras e assinaturas seguem estritamente as políticas de reembolso da Google Play Store. Solicitações de estorno devem ser abertas diretamente pela Central de Ajuda do Google Play."
        )

        LegalSection(
            title = "5. Suporte Técnico e Contato",
            content = "Para dúvidas técnicas, suporte com assinaturas ou relatórios de feedback, utilize o canal de Suporte ApisSmart no menu lateral ou envie mensagem para o suporte oficial."
        )
    }
}

@Composable
private fun LegalSection(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp)
            )
        }
    }
}
