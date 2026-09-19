package com.gratus.bsputility.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gratus.bsputility.ui.theme.MyApplicationTheme
import com.gratus.bsputility.utils.StorageHelper
import kotlinx.coroutines.launch

@Composable
fun RestoreFullDatabaseDialog(
    onRestore: suspend (String) -> Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var jsonInput by remember { mutableStateOf("") }
    var isRestoring by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = StorageHelper.getFileName(context, uri) ?: "backup file"
            val text = StorageHelper.readTextFromUri(context, uri)
            if (!text.isNullOrBlank()) {
                jsonInput = text
                Toast.makeText(context, "Loaded $fileName (${text.length} characters)", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Selected file is empty or could not be read", Toast.LENGTH_SHORT).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isRestoring) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Restore Full Database", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Select a full database backup JSON file or paste backup contents below to recover workers, contractors, settings, and historical daily attendance records across all dates.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = {
                        filePickerLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Select Backup File (.json)")
                }

                Text(
                    text = "Or paste JSON content directly:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                OutlinedTextField(
                    value = jsonInput,
                    onValueChange = { jsonInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    placeholder = { Text("{\n  \"backupType\": \"FULL_DATABASE_BACKUP\",\n  \"employees\": [...],\n  \"dailyAttendance\": [...]\n}") }
                )

                Text(
                    text = "Warning: Restoring will overwrite existing master records and replace historical attendance records with those from the backup.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (jsonInput.isNotBlank()) {
                        isRestoring = true
                        coroutineScope.launch {
                            val success = onRestore(jsonInput)
                            isRestoring = false
                            if (success) {
                                Toast.makeText(context, "Database and attendance history restored successfully!", Toast.LENGTH_LONG).show()
                                onDismiss()
                            } else {
                                Toast.makeText(context, "Failed to parse or restore database JSON!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Please choose a backup file or paste JSON first", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !isRestoring && jsonInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(if (isRestoring) "Restoring..." else "Restore Full Database")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isRestoring
            ) {
                Text("Cancel")
            }
        }
    )
}

@Preview(name = "Master Data Dialog - Restore Database", showBackground = true)
@Composable
fun RestoreFullDatabaseDialog_Preview() {
    MyApplicationTheme {
        RestoreFullDatabaseDialog(
            onRestore = { true },
            onDismiss = {}
        )
    }
}
