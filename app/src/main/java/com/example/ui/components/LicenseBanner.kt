package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.license.LicenseData
import com.example.license.LicenseStatus
import com.example.license.LicenseTier

@Composable
fun LicenseBanner(
    licenseData: LicenseData,
    onOpenLicenseDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Only show prominent banner if in Trial, Grace Period, or Expired
    // For active/lifetime, show an elegant compact badge
    val isWarning = licenseData.status == LicenseStatus.TRIAL ||
            licenseData.status == LicenseStatus.GRACE_PERIOD ||
            licenseData.status == LicenseStatus.EXPIRED

    val containerColor = when (licenseData.status) {
        LicenseStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        LicenseStatus.TRIAL -> Color(0xFFFFF3CD)
        LicenseStatus.GRACE_PERIOD -> Color(0xFFFFE0B2)
        LicenseStatus.EXPIRED -> Color(0xFFFFCDD2)
    }

    val contentColor = when (licenseData.status) {
        LicenseStatus.ACTIVE -> MaterialTheme.colorScheme.onPrimaryContainer
        LicenseStatus.TRIAL -> Color(0xFF856404)
        LicenseStatus.GRACE_PERIOD -> Color(0xFFBF360C)
        LicenseStatus.EXPIRED -> Color(0xFFB71C1C)
    }

    val iconVector = when (licenseData.status) {
        LicenseStatus.ACTIVE -> Icons.Default.VerifiedUser
        LicenseStatus.TRIAL -> Icons.Default.Key
        LicenseStatus.GRACE_PERIOD -> Icons.Default.Warning
        LicenseStatus.EXPIRED -> Icons.Default.Lock
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenLicenseDialog() }
            .testTag("license_banner_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(contentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = "Licença",
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = when (licenseData.status) {
                            LicenseStatus.ACTIVE -> if (licenseData.tier == LicenseTier.ASSOCIATION) {
                                "🏛️ Associação (${licenseData.licensesCount} licenças)"
                            } else {
                                "Licença ${licenseData.tier.displayName}"
                            }
                            LicenseStatus.TRIAL -> "Período de Demonstração (${licenseData.daysRemaining} dias)"
                            LicenseStatus.GRACE_PERIOD -> "Tolerância Offline Ativa (${licenseData.daysRemaining} dias)"
                            LicenseStatus.EXPIRED -> "Licença Expirada • Ativação Obrigatória"
                        },
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    )

                    Text(
                        text = when (licenseData.status) {
                            LicenseStatus.ACTIVE -> if (licenseData.tier == LicenseTier.ASSOCIATION && licenseData.associationName.isNotBlank()) {
                                "${licenseData.associationName} • Até ${licenseData.formattedExpiry}"
                            } else {
                                "Validade até ${licenseData.formattedExpiry} • Toque para gerenciar"
                            }
                            LicenseStatus.TRIAL -> "Acesso completo liberado • Toque para ativar chave definitiva"
                            LicenseStatus.GRACE_PERIOD -> "Reconecte à internet ou atualize sua chave de acesso"
                            LicenseStatus.EXPIRED -> "Toque aqui para inserir chave ou verificar no servidor"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.5.sp,
                            color = contentColor.copy(alpha = 0.85f)
                        ),
                        maxLines = 1
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Abrir Detalhes da Licença",
                tint = contentColor.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
