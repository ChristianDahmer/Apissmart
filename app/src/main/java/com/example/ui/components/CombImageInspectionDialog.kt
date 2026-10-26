package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hive
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.ApiaryEntity
import com.example.data.local.entities.HiveEntity

enum class CombSampleType(val title: String, val desc: String, val iconColor: Color) {
    UNIFORM_BROOD(
        "Favo com Postura Compacta",
        "Opérculos fechados concêntricos, rainha ativa e vigorosa.",
        Color(0xFF16A34A)
    ),
    VARROA_ALERT(
        "Favo com Falhas e Varroa",
        "Opérculos perfurados, falhas dispersas e ácaros no dorso.",
        Color(0xFFDC2626)
    ),
    HONEY_SUPERS(
        "Favo de Mel 100% Operculado",
        "Mel maduro pronto para colheita e centrifugação.",
        Color(0xFFD97706)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombImageInspectionDialog(
    apiaries: List<ApiaryEntity>,
    hives: List<HiveEntity>,
    isAnalyzing: Boolean,
    onDismiss: () -> Unit,
    onSubmitImageAnalysis: (apiaryId: Long, hiveId: Long, sampleType: CombSampleType) -> Unit
) {
    var selectedApiaryId by remember { mutableStateOf(apiaries.firstOrNull()?.id ?: 0L) }
    val filteredHives = hives.filter { it.apiaryId == selectedApiaryId }
    var selectedHiveId by remember { mutableStateOf(filteredHives.firstOrNull()?.id ?: 0L) }
    var selectedSample by remember { mutableStateOf(CombSampleType.UNIFORM_BROOD) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("comb_image_dialog")
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
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
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Diagnóstico Visual do Favo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "A IA analisa a foto do quadro para avaliar taxa de postura, opéculos furados, sanidade e reservas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Apiary Selection
                Text(text = "1. Selecione o Apiário:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    apiaries.take(3).forEach { apiary ->
                        FilterChip(
                            selected = selectedApiaryId == apiary.id,
                            onClick = {
                                selectedApiaryId = apiary.id
                                selectedHiveId = hives.firstOrNull { it.apiaryId == apiary.id }?.id ?: 0L
                            },
                            label = { Text(apiary.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Hive Selection
                Text(text = "2. Selecione a Colmeia:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    filteredHives.take(4).forEach { hive ->
                        FilterChip(
                            selected = selectedHiveId == hive.id,
                            onClick = { selectedHiveId = hive.id },
                            label = { Text(hive.hiveNumber) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sample Image Picker
                Text(
                    text = "3. Foto do Quadro / Favo para Análise:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                CombSampleType.values().forEach { sample ->
                    val isSelected = selectedSample == sample
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedSample = sample }
                            .then(
                                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                else Modifier
                            ),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(sample.iconColor.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = sample.iconColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sample.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = sample.desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Action
                Button(
                    onClick = {
                        onSubmitImageAnalysis(selectedApiaryId, selectedHiveId, selectedSample)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isAnalyzing && selectedHiveId != 0L
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analisando Favo com Visão IA...")
                    } else {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Processar Diagnóstico Óptico")
                    }
                }
            }
        }
    }
}
