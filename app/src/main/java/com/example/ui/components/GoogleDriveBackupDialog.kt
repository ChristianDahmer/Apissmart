package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.backup.BackupRestoreResult
import com.example.data.backup.BackupSummary
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.MeadowGreenSecondary
import com.example.ui.viewmodel.ApisSmartViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GoogleDriveBackupDialog(
    viewModel: ApisSmartViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val databaseSummary by viewModel.databaseSummary.collectAsState()
    val isOperating by viewModel.isBackupOperationInProgress.collectAsState()
    val lastResult by viewModel.lastBackupResult.collectAsState()

    var wipeExistingBeforeRestore by remember { mutableStateOf(true) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadDatabaseSummary()
    }

    // Storage Access Framework Launcher: Export to file (User can choose Google Drive folder or local storage)
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            viewModel.exportBackupToUri(it) {
                // finished
            }
        }
    }

    // Storage Access Framework Launcher: Restore from file (User can select file directly from Google Drive)
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            pendingRestoreUri = it
            showRestoreConfirmDialog = true
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("google_drive_backup_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .imePadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
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
                                .size(44.dp)
                                .background(HoneyGoldPrimary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = HoneyGoldPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Backup Google Drive",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Nuvem & Restauração Completa",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                CircleShape
                            )
                            .testTag("backup_dialog_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Database Statistics Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Base de Dados Atual",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(
                                onClick = { viewModel.loadDatabaseSummary() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Atualizar contagem",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val summary = databaseSummary
                        if (summary != null) {
                            Text(
                                text = "Total de registros cadastrados: ${summary.totalCount}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                StatChip("🌿 Apiários", summary.apiariesCount)
                                StatChip("🐝 Colmeias", summary.hivesCount)
                                StatChip("📋 Inspeções", summary.inspectionsCount)
                                StatChip("⚖️ Colheitas", summary.harvestsCount)
                                StatChip("💰 Finanças", summary.financialCount)
                                StatChip("📦 Estoque", summary.inventoryCount)
                                StatChip("📅 Tarefas", summary.tasksCount)
                                StatChip("👑 Rainhas", summary.queenHistoryCount)
                                StatChip("⚖️ Pesagens", summary.weightsCount)
                                StatChip("📸 Fotos", summary.photosCount)
                                StatChip("🪤 Iscas", summary.trapsCount)
                                StatChip("📡 Tags BLE", summary.tagsCount)
                                StatChip("🌸 Floradas", summary.observationsCount)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Carregando estatísticas...", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                // Progress Indicator
                if (isOperating) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 3.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Processando banco de dados com segurança...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Result Notification Banner
                lastResult?.let { res ->
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (res.success) MeadowGreenSecondary.copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (res.success) MeadowGreenSecondary.copy(alpha = 0.6f) else Color(0xFFEF4444).copy(alpha = 0.6f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (res.success) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (res.success) MeadowGreenSecondary else Color(0xFFEF4444),
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (res.success) "Operação Concluída" else "Aviso de Operação",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (res.success) MeadowGreenSecondary else Color(0xFFEF4444)
                                )
                                Text(
                                    text = res.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
                                onClick = { viewModel.clearBackupResult() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dispensar",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // SECTION 1: Export / Google Drive Backup
                Text(
                    text = "1. CRIAR BACKUP COMPLETO",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Option 1A: Salvar Direto no Google Drive via Share Chooser
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isOperating) {
                            viewModel.shareBackupToGoogleDrive(
                                onShareIntentReady = { chooserIntent, _ ->
                                    context.startActivity(chooserIntent)
                                },
                                onError = { /* handled */ }
                            )
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFF2563EB).copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Salvar no Google Drive / Compartilhar",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Gera o arquivo de backup e abre a opção 'Salvar no Drive' ou apps de compartilhamento.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Option 1B: Exportar com SAF para pasta específica do Drive ou dispositivo
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isOperating) {
                            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                            createDocumentLauncher.launch("apissmart_backup_$timeStamp.json")
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(HoneyGoldPrimary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = HoneyGoldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Exportar Arquivo (.JSON)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Escolha manualmente a pasta do Google Drive ou armazenamento interno para salvar.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // SECTION 2: Restore from Google Drive / File
                Text(
                    text = "2. RESTAURAR BACKUP DO DRIVE",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(MeadowGreenSecondary.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    tint = MeadowGreenSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Restauração de Dados",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Recupere apiários, colmeias, históricos e pesagens a partir de backup anterior.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Wipe option toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Substituir base de dados atual",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (wipeExistingBeforeRestore)
                                        "Limpa os dados atuais antes de restaurar (recomendado)"
                                    else
                                        "Mantém os dados atuais e adiciona/atualiza registros do backup",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = wipeExistingBeforeRestore,
                                onCheckedChange = { wipeExistingBeforeRestore = it },
                                modifier = Modifier.testTag("backup_wipe_toggle")
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                openDocumentLauncher.launch(
                                    arrayOf("application/json", "application/octet-stream", "*/*")
                                )
                            },
                            enabled = !isOperating,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_select_backup_file"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Selecionar Arquivo no Google Drive / Celular")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Helpful advisory box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = HoneyGoldPrimary,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "💡 Dica: Ao selecionar o arquivo no seletor do sistema, você pode navegar diretamente pela sua pasta do Google Drive ou pelo armazenamento local. Faça backups periódicos para proteger o histórico do seu apiário.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Confirmation Alert before restoring
    if (showRestoreConfirmDialog && pendingRestoreUri != null) {
        AlertDialog(
            onDismissRequest = {
                showRestoreConfirmDialog = false
                pendingRestoreUri = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFD97706)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirmar Restauração")
                }
            },
            text = {
                Text(
                    text = if (wipeExistingBeforeRestore) {
                        "Atenção: A opção 'Substituir base de dados atual' está ativada. Todos os dados atuais do aplicativo serão substituídos pelos registros contidos no arquivo de backup selecionado do Google Drive.\n\nDeseja prosseguir?"
                    } else {
                        "Os registros do arquivo de backup serão incorporados à sua base de dados atual.\n\nDeseja prosseguir com a restauração?"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = pendingRestoreUri
                        showRestoreConfirmDialog = false
                        pendingRestoreUri = null
                        if (uri != null) {
                            viewModel.restoreBackupFromUri(uri, wipeExisting = wipeExistingBeforeRestore) {
                                // done
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MeadowGreenSecondary
                    )
                ) {
                    Text("Confirmar e Restaurar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showRestoreConfirmDialog = false
                        pendingRestoreUri = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun StatChip(label: String, count: Int) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
