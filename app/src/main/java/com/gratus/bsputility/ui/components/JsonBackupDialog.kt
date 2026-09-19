package com.gratus.bsputility.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gratus.bsputility.ui.theme.MyApplicationTheme
import com.gratus.bsputility.utils.StorageHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JsonBackupDialog(
    onExportJson: () -> String,
    onExportCsv: () -> String,
    onImportJson: (String) -> Boolean,
    onImportCsv: ((String) -> Boolean)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var jsonInput by remember { mutableStateOf("") }
    var exportTab by remember { mutableStateOf(true) }
    var generatedJson by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = StorageHelper.getFileName(context, uri) ?: "file"
            val text = StorageHelper.readTextFromUri(context, uri)
            if (!text.isNullOrBlank()) {
                val isCsv = fileName.endsWith(".csv", ignoreCase = true) || (!text.trimStart().startsWith("{") && !text.trimStart().startsWith("["))
                if (isCsv && onImportCsv != null) {
                    jsonInput = "[Loaded CSV from $fileName (${text.lines().size} lines)]"
                    val success = onImportCsv(text)
                    if (success) {
                        Toast.makeText(context, "Replaced employee rooster from $fileName successfully!", Toast.LENGTH_LONG).show()
                        onDismiss()
                    } else {
                        Toast.makeText(context, "Failed to parse CSV file", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    jsonInput = text
                    val success = onImportJson(text)
                    if (success) {
                        Toast.makeText(context, "Replaced master data from $fileName successfully!", Toast.LENGTH_LONG).show()
                        onDismiss()
                    } else {
                        Toast.makeText(context, "Invalid JSON in $fileName", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Selected file is empty or could not be read", Toast.LENGTH_SHORT).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (exportTab) "Export Full Master Data" else "Restore / Import Data", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = exportTab,
                        onClick = { exportTab = true },
                        label = { Text("Export (JSON/CSV)") }
                    )
                    FilterChip(
                        selected = !exportTab,
                        onClick = { exportTab = false },
                        label = { Text("Restore / Import") }
                    )
                }

                if (exportTab) {
                    Text("Export all employee profiles, contractors, and master categories to device storage and clipboard.", fontSize = 12.sp)

                    Button(
                        onClick = {
                            val json = onExportJson()
                            generatedJson = json
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("BSP Manpower Data", json))
                            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                            val fileName = "BSP_MasterData_$timestamp.json"
                            val res = StorageHelper.exportToDocuments(context, fileName, "application/json", json)
                            val msg = if (res.success) "Saved to ${res.filePathOrUri} & copied to clipboard!" else "Copied to clipboard"
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export & Copy JSON")
                    }

                    OutlinedButton(
                        onClick = {
                            val csv = onExportCsv()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("BSP Employee Roster CSV", csv))
                            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                            val fileName = "BSP_Rooster_$timestamp.csv"
                            val res = StorageHelper.exportToDocuments(context, fileName, "text/csv", csv)
                            val msg = if (res.success) "Saved to ${res.filePathOrUri} & copied to clipboard!" else "Copied to clipboard"
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export & Copy CSV")
                    }

                    if (generatedJson.isNotBlank()) {
                        Text("Preview:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(
                            text = generatedJson.take(300) + "...",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(8.dp)
                        )
                    }
                } else {
                    Text("Select a backup file from storage or paste raw JSON below:", fontSize = 12.sp)

                    OutlinedButton(
                        onClick = {
                            filePickerLauncher.launch(arrayOf("application/json", "text/csv", "text/comma-separated-values", "text/plain", "*/*"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Choose File (.json / .csv)")
                    }

                    Text(
                        text = "Note: Importing will replace existing data in the library.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )

                    OutlinedTextField(
                        value = jsonInput,
                        onValueChange = { jsonInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        placeholder = { Text("{\n  \"employees\": [...]\n}") }
                    )
                    Button(
                        onClick = {
                            if (jsonInput.isNotBlank()) {
                                val isCsv = !jsonInput.trimStart().startsWith("{") && !jsonInput.trimStart().startsWith("[")
                                if (isCsv && onImportCsv != null) {
                                    val success = onImportCsv(jsonInput)
                                    if (success) {
                                        Toast.makeText(context, "Employee rooster replaced from CSV successfully!", Toast.LENGTH_LONG).show()
                                        onDismiss()
                                    } else {
                                        Toast.makeText(context, "Failed to parse CSV format!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    val success = onImportJson(jsonInput)
                                    if (success) {
                                        Toast.makeText(context, "Master data replaced successfully!", Toast.LENGTH_LONG).show()
                                        onDismiss()
                                    } else {
                                        Toast.makeText(context, "Invalid JSON format!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore / Replace From Text")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Preview(name = "Rooster Dialog - JSON Backup / Restore", showBackground = true)
@Composable
fun JsonBackupDialog_Preview() {
    MyApplicationTheme {
        JsonBackupDialog(
            onExportJson = { "{ \"employees\": [] }" },
            onExportCsv = { "Name,Type\nRakesh,Staff" },
            onImportJson = { true },
            onDismiss = {}
        )
    }
}
