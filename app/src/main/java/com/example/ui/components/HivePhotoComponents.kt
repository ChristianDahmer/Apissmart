package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.entities.HiveEntity
import com.example.data.local.entities.HivePhotoEntity
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.MeadowGreenSecondary
import com.example.ui.theme.WarningAlertRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Photo categories for hive monitoring
val HIVE_PHOTO_CATEGORIES = listOf(
    "Ninho & Postura",
    "Melgueira & Mel",
    "Rainha & Marcação",
    "Alvado & Entrada",
    "Sanidade & Diagnóstico",
    "Geral da Caixa"
)

data class SampleFieldPhoto(
    val title: String,
    val category: String,
    val uri: String,
    val defaultCaption: String,
    val iconEmoji: String
)

val SAMPLE_FIELD_PHOTOS = listOf(
    SampleFieldPhoto("Cria Fechada Uniforme", "Ninho & Postura", "sample://comb_uniform_brood", "Postura compacta e operculada sem falhas", "🥚"),
    SampleFieldPhoto("Melgueira Pronta", "Melgueira & Mel", "sample://honey_supers", "Quadros 100% operculados com cera clara", "🍯"),
    SampleFieldPhoto("Rainha Marcada 2026", "Rainha & Marcação", "sample://queen_marked", "Rainha vigorosa marcada com tinta branca oficial", "👑"),
    SampleFieldPhoto("Alvado em Movimento", "Alvado & Entrada", "sample://entrance_activity", "Intensa movimentação e entrada de pólen", "🚪"),
    SampleFieldPhoto("Diagnóstico Sanitário", "Sanidade & Diagnóstico", "sample://varroa_alert", "Inspeção sanitária de fundo e crias", "🔬"),
    SampleFieldPhoto("Vista Geral do Apiário", "Geral da Caixa", "sample://hive_box_general", "Posicionamento e suporte da colmeia no campo", "📦")
)

@Composable
fun HivePhotoThumbnail(
    photoUri: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    if (photoUri.startsWith("sample://")) {
        val sampleType = photoUri.removePrefix("sample://")
        val bgGradient = when (sampleType) {
            "comb_uniform_brood" -> listOf(Color(0xFFD97706), Color(0xFF78350F))
            "honey_supers" -> listOf(Color(0xFFFBBF24), Color(0xFFB45309))
            "queen_marked" -> listOf(Color(0xFFF59E0B), Color(0xFF451A03))
            "entrance_activity" -> listOf(Color(0xFF059669), Color(0xFF064E3B))
            "varroa_alert" -> listOf(Color(0xFFDC2626), Color(0xFF7F1D1D))
            else -> listOf(Color(0xFF0284C7), Color(0xFF075985))
        }
        val icon = when (sampleType) {
            "comb_uniform_brood" -> "🥚"
            "honey_supers" -> "🍯"
            "queen_marked" -> "👑"
            "entrance_activity" -> "🚪"
            "varroa_alert" -> "🔬"
            else -> "📦"
        }
        val label = when (sampleType) {
            "comb_uniform_brood" -> "Ninho / Cria"
            "honey_supers" -> "Melgueira"
            "queen_marked" -> "Rainha"
            "entrance_activity" -> "Alvado"
            "varroa_alert" -> "Sanidade"
            else -> "Colmeia"
        }

        Box(
            modifier = modifier
                .background(Brush.linearGradient(bgGradient)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = icon, fontSize = 28.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    } else {
        AsyncImage(
            model = photoUri,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}

// -------------------------------------------------------------
// DIALOG: ADICIONAR FOTO DA COLMEIA
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddHivePhotoDialog(
    hive: HiveEntity,
    onDismiss: () -> Unit,
    onSavePhoto: (photoUri: String, category: String, caption: String, notes: String, inspectorName: String) -> Unit
) {
    var selectedUri by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf(HIVE_PHOTO_CATEGORIES[0]) }
    var caption by remember { mutableStateOf("") }
    var inspectorName by remember { mutableStateOf("Apicultor") }
    var notes by remember { mutableStateOf("") }
    var expCategory by remember { mutableStateOf(false) }

    // Zero-permission Android Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            selectedUri = it.toString()
            if (caption.isBlank()) {
                caption = "Foto de campo da Colmeia #${hive.hiveNumber}"
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("add_hive_photo_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .imePadding()
                    .navigationBarsPadding()
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(HoneyGoldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = HoneyGoldPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Adicionar Foto da Colmeia",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Colmeia #${hive.hiveNumber} (${hive.beeSpecies})",
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
                            .testTag("add_photo_dialog_close_btn")
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

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Image Preview / Selection Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedUri != null) {
                            HivePhotoThumbnail(
                                photoUri = selectedUri!!,
                                contentDescription = "Foto Selecionada",
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            ) {
                                IconButton(
                                    onClick = { selectedUri = null },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remover", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = HoneyGoldPrimary,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Nenhuma foto selecionada",
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Escolha da galeria ou use uma foto modelo abaixo",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Gallery Picker Button
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pick_photo_from_gallery_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = HoneyGoldPrimary)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Selecionar Foto da Galeria do Celular", fontWeight = FontWeight.Bold)
                    }

                    // Preset Field Photo Samples (Useful for fast field record & emulator)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Ou escolha uma Foto de Manejo Modelo:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SAMPLE_FIELD_PHOTOS.forEach { sample ->
                                val isSelected = selectedUri == sample.uri
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedUri = sample.uri
                                        selectedCategory = sample.category
                                        if (caption.isBlank() || SAMPLE_FIELD_PHOTOS.any { it.defaultCaption == caption }) {
                                            caption = sample.defaultCaption
                                        }
                                    },
                                    label = { Text("${sample.iconEmoji} ${sample.title}", fontSize = 12.sp) },
                                    modifier = Modifier.testTag("sample_photo_${sample.category.lowercase().take(5)}")
                                )
                            }
                        }
                    }

                    // Category Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expCategory,
                        onExpandedChange = { expCategory = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoria da Foto") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expCategory) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expCategory,
                            onDismissRequest = { expCategory = false }
                        ) {
                            HIVE_PHOTO_CATEGORIES.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        expCategory = false
                                    }
                                )
                            }
                        }
                    }

                    // Caption
                    OutlinedTextField(
                        value = caption,
                        onValueChange = { caption = it },
                        label = { Text("Legenda da Foto (Descrição breve)") },
                        placeholder = { Text("Ex: Postura uniforme de 7 quadros, rainha ativa...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("photo_caption_input")
                    )

                    // Inspector Name
                    OutlinedTextField(
                        value = inspectorName,
                        onValueChange = { inspectorName = it },
                        label = { Text("Responsável / Inspetor") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Observações Técnicas de Campo (Opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val uriToSave = selectedUri ?: "sample://comb_uniform_brood"
                            val finalCaption = if (caption.isNotBlank()) caption else "Registro visual da colmeia"
                            onSavePhoto(uriToSave, selectedCategory, finalCaption, notes, inspectorName)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MeadowGreenSecondary),
                        modifier = Modifier.testTag("confirm_save_photo_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salvar Foto", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// DIALOG: VISUALIZADOR DE FOTO EM TELA CHEIA (FULLSCREEN)
// -------------------------------------------------------------
@Composable
fun FullPhotoViewDialog(
    photo: HivePhotoEntity,
    hive: HiveEntity,
    onDismiss: () -> Unit,
    onDelete: (HivePhotoEntity) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .testTag("full_photo_view_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(HoneyGoldPrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = photo.category,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = HoneyGoldPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Colmeia #${hive.hiveNumber}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.testTag("delete_photo_btn")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Excluir Foto", tint = WarningAlertRed)
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f), CircleShape)
                                .testTag("full_photo_close_btn")
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

                // Photo Display Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    HivePhotoThumbnail(
                        photoUri = photo.photoUri,
                        contentDescription = photo.caption,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                // Metadata Details Footer
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (photo.caption.isNotBlank()) photo.caption else "Foto de Manejo da Colmeia",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📅 ${dateFormat.format(Date(photo.dateTaken))} • 👤 ${photo.inspectorName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (photo.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Notas: ${photo.notes}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Excluir Foto?") },
            text = { Text("Tem certeza de que deseja remover esta foto do prontuário da colmeia?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(photo)
                        showDeleteConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningAlertRed)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
