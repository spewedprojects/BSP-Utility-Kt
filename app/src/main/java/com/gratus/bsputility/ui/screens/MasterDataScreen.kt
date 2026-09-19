package com.gratus.bsputility.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gratus.bsputility.data.models.ConfigItem
import com.gratus.bsputility.data.models.Contractor
import com.gratus.bsputility.data.models.Employee
import com.gratus.bsputility.ui.components.AddConfigDialog
import com.gratus.bsputility.ui.components.AddContractorDialog
import com.gratus.bsputility.ui.components.RestoreFullDatabaseDialog
import com.gratus.bsputility.ui.preview.PreviewData
import com.gratus.bsputility.ui.theme.IndustrialAmber600
import com.gratus.bsputility.ui.theme.MyApplicationTheme
import com.gratus.bsputility.ui.viewmodel.ManpowerViewModel
import com.gratus.bsputility.utils.StorageHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun MasterDataScreen(
    viewModel: ManpowerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val contractors by viewModel.allContractors.collectAsStateWithLifecycle()
    val allConfigs by viewModel.allConfigItems.collectAsStateWithLifecycle()
    val allEmployees by viewModel.allEmployees.collectAsStateWithLifecycle()
    val is24Hour by viewModel.is24HourFormat.collectAsStateWithLifecycle()

    MasterDataScreenContent(
        contractors = contractors,
        allConfigs = allConfigs,
        allEmployees = allEmployees,
        is24Hour = is24Hour,
        onToggle24Hour = { viewModel.set24HourFormat(it) },
        onClearStaffContractors = {
            viewModel.clearStaffContractorAssociations { count ->
                Toast.makeText(context, "Sanitized staff data: $count associations removed", Toast.LENGTH_LONG).show()
            }
        },
        onMigrateTimeData = {
            viewModel.migrateLegacyTimeRecords { count ->
                Toast.makeText(context, "Migrated $count time records to timestamps", Toast.LENGTH_LONG).show()
            }
        },
        onSyncLabourDefaults = {
            viewModel.syncLabourerDefaultsFromAttendance { count ->
                Toast.makeText(context, "Transferred defaults for $count labourers into rooster library", Toast.LENGTH_LONG).show()
            }
        },
        onRelinkAttendanceHistory = {
            viewModel.relinkAttendanceRecords { count ->
                Toast.makeText(context, "Re-linked and verified $count past attendance records by worker name", Toast.LENGTH_LONG).show()
            }
        },
        onExportFullBackup = { viewModel.exportCompleteDatabaseBackupJson() },
        onRestoreFullBackup = { viewModel.restoreCompleteDatabaseBackupJson(it) },
        onAddContractor = { viewModel.addContractor(it) },
        onUpdateContractor = { viewModel.updateContractor(it) },
        onDeleteContractor = {
            viewModel.deleteContractor(it)
            Toast.makeText(context, "Deleted ${it.name}", Toast.LENGTH_SHORT).show()
        },
        onAddConfigItem = { cat, name, type -> viewModel.addConfigItem(cat, name, type) },
        onUpdateConfigItem = { viewModel.updateConfigItem(it) },
        onDeleteConfigItem = {
            viewModel.deleteConfigItem(it)
            Toast.makeText(context, "Removed ${it.name}", Toast.LENGTH_SHORT).show()
        },
        modifier = modifier
    )
}

@Composable
fun MasterDataScreenContent(
    contractors: List<Contractor>,
    allConfigs: List<ConfigItem>,
    allEmployees: List<Employee>,
    is24Hour: Boolean = false,
    onToggle24Hour: (Boolean) -> Unit = {},
    onClearStaffContractors: () -> Unit = {},
    onMigrateTimeData: () -> Unit = {},
    onSyncLabourDefaults: () -> Unit = {},
    onRelinkAttendanceHistory: () -> Unit = {},
    onExportFullBackup: (suspend () -> String)? = null,
    onRestoreFullBackup: (suspend (String) -> Boolean)? = null,
    onAddContractor: (Contractor) -> Unit,
    onUpdateContractor: (Contractor) -> Unit,
    onDeleteContractor: (Contractor) -> Unit,
    onAddConfigItem: (category: String, name: String, fieldType: String) -> Unit,
    onUpdateConfigItem: (ConfigItem) -> Unit = {},
    onDeleteConfigItem: (ConfigItem) -> Unit,
    initialTab: String = "Contractors",
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    val tabs = listOf("Contractors", "Departments", "Designations", "Units", "Shifts", "Labour Roles", "Custom Fields", "Preferences")

    // Modals
    var showAddContractorDialog by remember { mutableStateOf<Contractor?>(null) }
    var isNewContractor by remember { mutableStateOf(false) }
    var showAddConfigDialog by remember { mutableStateOf(false) }
    var editingConfigItem by remember { mutableStateOf<ConfigItem?>(null) }
    var newConfigCategory by remember { mutableStateOf("DEPARTMENT") }
    var showRestoreFullBackupDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Settings & Master Data",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        )
                    }
                    Text(
                        text = "Configure contractors, plant departments, labour roles, units and custom fields",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(tabs) { tab ->
                            FilterChip(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                label = { Text(tab, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            // Body content per tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectedTab == "Contractors") {
                    item {
                        Text(
                            text = "Configured Contractors (${contractors.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(contractors) { contractor ->
                        val assignedCount = allEmployees.count { it.contractorName == contractor.name }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = contractor.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (contractor.contactPerson.isNotBlank() || contractor.phone.isNotBlank()) {
                                        Text(
                                            text = "Contact: ${contractor.contactPerson} (${contractor.phone})",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "$assignedCount labourers linked",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Row {
                                    IconButton(onClick = {
                                        isNewContractor = false
                                        showAddContractorDialog = contractor
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = {
                                        onDeleteContractor(contractor)
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedTab == "Preferences") {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = "Time Format",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                Text(
                                    text = "Choose 12-hour (AM/PM) or 24-hour time format for marking staff attendance times.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        RadioButton(
                                            selected = !is24Hour,
                                            onClick = { onToggle24Hour(false) },
                                            modifier = Modifier.testTag("radio_12_hour")
                                        )
                                        Text(
                                            text = "12-Hour (08:30 AM)",
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        RadioButton(
                                            selected = is24Hour,
                                            onClick = { onToggle24Hour(true) },
                                            modifier = Modifier.testTag("radio_24_hour")
                                        )
                                        Text(
                                            text = "24-Hour (08:30)",
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(start = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Storage,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = "Full Database Backup & Disaster Recovery",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                Text(
                                    text = "Export an all-inclusive snapshot of all employee records, contractors, master categories, daily attendance across all dates, and department verifications into a single JSON file. Restore to recover complete system state in case of data loss.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                )

                                Button(
                                    onClick = {
                                        if (onExportFullBackup != null) {
                                            coroutineScope.launch {
                                                val json = onExportFullBackup()
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("BSP Manpower Full Database Backup", json))
                                                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                                val fileName = "BSP_Full_Database_Backup_$timestamp.json"
                                                val res = StorageHelper.exportToDocuments(context, fileName, "application/json", json)
                                                val msg = if (res.success) "Full backup saved to ${res.filePathOrUri} & copied to clipboard!" else "Backup JSON copied to clipboard!"
                                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_export_full_database_backup")
                                ) {
                                    Icon(Icons.Default.Backup, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Export Complete Database Backup (.JSON)")
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        showRestoreFullBackupDialog = true
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_restore_full_database_backup")
                                ) {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Restore Database / Disaster Recovery")
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = "Data Integrity & Maintenance",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                Text(
                                    text = "Maintain database consistency and clean up legacy data associations.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                )

                                OutlinedButton(
                                    onClick = onClearStaffContractors,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_clear_staff_contractors")
                                ) {
                                    Text("Clear Staff Contractor Links")
                                }
                                Text(
                                    text = "Ensures no staff members are erroneously tied to any contractor in the rooster or attendance records.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 12.dp)
                                )

                                OutlinedButton(
                                    onClick = onMigrateTimeData,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_migrate_time_data")
                                ) {
                                    Text("Migrate Legacy Time Records")
                                }
                                Text(
                                    text = "Converts legacy string time records (e.g., '08:30 AM') into standard epoch timestamps.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 12.dp)
                                )

                                OutlinedButton(
                                    onClick = onSyncLabourDefaults,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_sync_labour_defaults")
                                ) {
                                    Text("Sync Labour Defaults from Attendance")
                                }
                                Text(
                                    text = "Transfers allotted departments, roles, units, and shifts from daily attendance history back into labourer rooster records.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 12.dp)
                                )

                                OutlinedButton(
                                    onClick = onRelinkAttendanceHistory,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_relink_attendance_history")
                                ) {
                                    Text("Re-link Attendance History by Worker Name")
                                }
                                Text(
                                    text = "Scans all historical daily attendance records and re-links them to current roster employee IDs based on worker names.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Category Items (Departments, Designations, Units, Shifts, Labour Roles, Custom Fields)
                    val catKey = when (selectedTab) {
                        "Departments" -> "DEPARTMENT"
                        "Designations" -> "DESIGNATION"
                        "Units" -> "UNIT"
                        "Shifts" -> "SHIFT"
                        "Labour Roles" -> "LABOUR_ROLE"
                        else -> "CUSTOM_FIELD"
                    }
                    val itemsForCat = allConfigs.filter { it.category == catKey }

                    item {
                        Text(
                            text = "$selectedTab (${itemsForCat.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(itemsForCat) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    if (item.category == "CUSTOM_FIELD") {
                                        val parts = item.extraType.split("|")
                                        val typeDisplay = parts.getOrNull(0)?.ifBlank { "TEXT" } ?: "TEXT"
                                        val target = parts.getOrNull(1)?.uppercase() ?: "ALL"
                                        val targetDisplay = when (target) {
                                            "STAFF" -> "Staff Only"
                                            "LABOUR" -> "Labour Only"
                                            else -> "All Employees"
                                        }
                                        Text(
                                            text = "Type: $typeDisplay • Applies to: $targetDisplay",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else if (item.category == "LABOUR_ROLE") {
                                        val depts = item.extraType.ifBlank { "ALL" }
                                        Text(
                                            text = "Applies to: ${if (depts.equals("ALL", ignoreCase = true)) "All Departments" else depts}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else if (item.extraType.isNotBlank()) {
                                        Text(
                                            text = "Type: ${item.extraType}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row {
                                    IconButton(onClick = {
                                        editingConfigItem = item
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = {
                                        onDeleteConfigItem(item)
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        if (selectedTab != "Preferences") {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == "Contractors") {
                        isNewContractor = true
                        showAddContractorDialog =
                            Contractor(name = "", contactPerson = "", phone = "", notes = "")
                    } else {
                        newConfigCategory = when (selectedTab) {
                            "Departments" -> "DEPARTMENT"
                            "Designations" -> "DESIGNATION"
                            "Units" -> "UNIT"
                            "Shifts" -> "SHIFT"
                            "Labour Roles" -> "LABOUR_ROLE"
                            else -> "CUSTOM_FIELD"
                        }
                        editingConfigItem = null
                        showAddConfigDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("fab_add_master_item")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    }

    // Modal: Add / Edit Contractor
    showAddContractorDialog?.let { c ->
        AddContractorDialog(
            contractor = c,
            isNew = isNewContractor,
            onDismiss = { showAddContractorDialog = null },
            onSave = { toSave ->
                if (isNewContractor) onAddContractor(toSave)
                else onUpdateContractor(toSave)
                showAddContractorDialog = null
            }
        )
    }

    // Modal: Add / Edit Config Item (Department, Role, Unit, Custom field)
    if (showAddConfigDialog || editingConfigItem != null) {
        val targetCat = editingConfigItem?.category ?: newConfigCategory
        AddConfigDialog(
            category = targetCat,
            tabTitle = selectedTab,
            configItem = editingConfigItem,
            availableDepartments = allConfigs.filter { it.category == "DEPARTMENT" }.map { it.name },
            onDismiss = {
                showAddConfigDialog = false
                editingConfigItem = null
            },
            onSave = { name, fieldType ->
                val currentEditing = editingConfigItem
                if (currentEditing != null) {
                    onUpdateConfigItem(currentEditing.copy(name = name, extraType = fieldType))
                } else {
                    onAddConfigItem(targetCat, name, fieldType)
                }
                showAddConfigDialog = false
                editingConfigItem = null
            }
        )
    }

    // Modal: Full Database Restore (Disaster Recovery)
    if (showRestoreFullBackupDialog && onRestoreFullBackup != null) {
        RestoreFullDatabaseDialog(
            onRestore = onRestoreFullBackup,
            onDismiss = { showRestoreFullBackupDialog = false }
        )
    }
}

// ==========================================
// PREVIEWS - ALL STATES
// ==========================================

@Preview(name = "Master Data - Contractors Tab", showBackground = true)
@Composable
fun MasterDataScreenPreview_Contractors() {
    MyApplicationTheme(darkTheme = false) {
        MasterDataScreenContent(
            contractors = PreviewData.sampleContractors,
            allConfigs = PreviewData.sampleConfigItems,
            allEmployees = PreviewData.sampleEmployees,
            onAddContractor = {},
            onUpdateContractor = {},
            onDeleteContractor = {},
            onAddConfigItem = { _, _, _ -> },
            onDeleteConfigItem = {},
            initialTab = "Contractors"
        )
    }
}

@Preview(name = "Master Data - Departments Tab", showBackground = true)
@Composable
fun MasterDataScreenPreview_Departments() {
    MyApplicationTheme {
        MasterDataScreenContent(
            contractors = PreviewData.sampleContractors,
            allConfigs = PreviewData.sampleConfigItems,
            allEmployees = PreviewData.sampleEmployees,
            onAddContractor = {},
            onUpdateContractor = {},
            onDeleteContractor = {},
            onAddConfigItem = { _, _, _ -> },
            onDeleteConfigItem = {},
            initialTab = "Departments"
        )
    }
}

@Preview(name = "Master Data - Shifts Tab", showBackground = true)
@Composable
fun MasterDataScreenPreview_Shifts() {
    MyApplicationTheme {
        MasterDataScreenContent(
            contractors = PreviewData.sampleContractors,
            allConfigs = PreviewData.sampleConfigItems,
            allEmployees = PreviewData.sampleEmployees,
            onAddContractor = {},
            onUpdateContractor = {},
            onDeleteContractor = {},
            onAddConfigItem = { _, _, _ -> },
            onDeleteConfigItem = {},
            initialTab = "Shifts"
        )
    }
}

@Preview(name = "Master Data - Custom Fields Tab", showBackground = true)
@Composable
fun MasterDataScreenPreview_CustomFields() {
    MyApplicationTheme {
        MasterDataScreenContent(
            contractors = PreviewData.sampleContractors,
            allConfigs = PreviewData.sampleConfigItems,
            allEmployees = PreviewData.sampleEmployees,
            onAddContractor = {},
            onUpdateContractor = {},
            onDeleteContractor = {},
            onAddConfigItem = { _, _, _ -> },
            onDeleteConfigItem = {},
            initialTab = "Custom Fields"
        )
    }
}

@Preview(name = "Master Data - Preferences", showBackground = true)
@Composable
fun MasterDataScreenPreview_Preferences() {
    MyApplicationTheme {
        MasterDataScreenContent(
            contractors = PreviewData.sampleContractors,
            allConfigs = PreviewData.sampleConfigItems,
            allEmployees = PreviewData.sampleEmployees,
            is24Hour = false,
            onToggle24Hour = {},
            onClearStaffContractors = {},
            onMigrateTimeData = {},
            onAddContractor = {},
            onUpdateContractor = {},
            onDeleteContractor = {},
            onAddConfigItem = { _, _, _ -> },
            onDeleteConfigItem = {},
            initialTab = "Preferences"
        )
    }
}

@Preview(name = "Master Data - Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MasterDataScreenPreview_DarkTheme() {
    MyApplicationTheme(darkTheme = true) {
        MasterDataScreenContent(
            contractors = PreviewData.sampleContractors,
            allConfigs = PreviewData.sampleConfigItems,
            allEmployees = PreviewData.sampleEmployees,
            onAddContractor = {},
            onUpdateContractor = {},
            onDeleteContractor = {},
            onAddConfigItem = { _, _, _ -> },
            onDeleteConfigItem = {},
            initialTab = "Contractors"
        )
    }
}
