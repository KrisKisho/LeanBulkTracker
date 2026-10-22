package com.leanbulk.tracker.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leanbulk.tracker.data.backup.BackupSummary
import com.leanbulk.tracker.data.backup.DataBackupManager
import com.leanbulk.tracker.data.local.AppDatabase
import com.leanbulk.tracker.ui.theme.AmberGold
import com.leanbulk.tracker.ui.theme.DarkBackground
import com.leanbulk.tracker.ui.theme.DarkSurfaceBorder
import com.leanbulk.tracker.ui.theme.DarkSurfaceElevated
import com.leanbulk.tracker.ui.theme.ElectricCyan
import com.leanbulk.tracker.ui.theme.NeonLime
import com.leanbulk.tracker.ui.theme.TextMuted
import com.leanbulk.tracker.ui.theme.TextPrimary
import com.leanbulk.tracker.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun DataBackupCard(
    modifier: Modifier = Modifier,
    onDataRestored: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getInstance(context) }
    val backupManager = remember { DataBackupManager(database) }

    var isProcessing by remember { mutableStateOf(false) }
    var restoreSummary by remember { mutableStateOf<BackupSummary?>(null) }
    var showExportOptions by remember { mutableStateOf(false) }

    // Launcher for saving export file via SAF (CreateDocument)
    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isProcessing = true
                try {
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        backupManager.writeBackupToOutputStream(os)
                    }
                    Toast.makeText(context, "Backup exported successfully!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Export failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    // Launcher for importing backup file via SAF (OpenDocument)
    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isProcessing = true
                try {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val summary = backupManager.importBackupFromInputStream(context, inputStream)
                        restoreSummary = summary
                        if (summary.success) {
                            onDataRestored()
                        }
                    }
                } catch (e: Exception) {
                    restoreSummary = BackupSummary(
                        success = false,
                        message = "Failed to read file: ${e.localizedMessage}"
                    )
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = "Data Backup",
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "DATA BACKUP & TRANSFER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan,
                                letterSpacing = 0.8.sp
                            )
                        )
                        Text(
                            text = "Export all data to transfer to a new device or restore backup",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                        )
                    }
                }

                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = ElectricCyan,
                        strokeWidth = 2.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Export Button
                OutlinedButton(
                    onClick = { showExportOptions = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isProcessing
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export Data", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Import Button
                Button(
                    onClick = {
                        openFileLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = DarkBackground
                    ),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isProcessing
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import Data", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }

    // Export Options Dialog
    if (showExportOptions) {
        AlertDialog(
            onDismissRequest = { showExportOptions = false },
            title = {
                Text("Export Backup Data", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Text(
                    "Save your complete backup JSON file to your device or share it directly to Google Drive, WhatsApp, or Email to import on another device.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportOptions = false
                        val timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
                        saveFileLauncher.launch("LeanBulk_Backup_$timeStamp.json")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = DarkBackground)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save to Files", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showExportOptions = false
                        scope.launch {
                            isProcessing = true
                            backupManager.createShareableBackupFile(context).onSuccess { shareUri ->
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_STREAM, shareUri)
                                    putExtra(Intent.EXTRA_SUBJECT, "Lean Bulk Tracker Backup")
                                    putExtra(Intent.EXTRA_TEXT, "Here is my Lean Bulk Tracker backup file.")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Lean Bulk Backup"))
                            }.onFailure { err ->
                                Toast.makeText(context, "Share failed: ${err.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                            isProcessing = false
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share Backup", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurfaceElevated,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Restore Result Dialog
    restoreSummary?.let { summary ->
        AlertDialog(
            onDismissRequest = { restoreSummary = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (summary.success) NeonLime else AmberGold,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = if (summary.success) "Restore Complete!" else "Restore Failed",
                    fontWeight = FontWeight.Bold,
                    color = if (summary.success) NeonLime else AmberGold
                )
            },
            text = {
                Column {
                    Text(summary.message, color = TextPrimary, fontSize = 13.sp)
                    if (summary.success) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("• User Profile: ${if (summary.userProfileImported) "Restored" else "None"}", color = TextSecondary, fontSize = 12.sp)
                        Text("• Weight Logs: ${summary.weightLogsCount} records", color = TextSecondary, fontSize = 12.sp)
                        Text("• Daily Meal Logs: ${summary.mealLogsCount} meals", color = TextSecondary, fontSize = 12.sp)
                        Text("• Workout Logs: ${summary.workoutLogsCount} segments", color = TextSecondary, fontSize = 12.sp)
                        Text("• Reminders: ${summary.remindersCount} alarms configured", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        restoreSummary = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (summary.success) NeonLime else AmberGold,
                        contentColor = DarkBackground
                    )
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurfaceElevated,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
