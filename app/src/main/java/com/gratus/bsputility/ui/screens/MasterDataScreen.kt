package com.gratus.bsputility.ui.screens

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.gratus.bsputility.ui.preview.PreviewData
import com.gratus.bsputility.ui.theme.IndustrialAmber600
import com.gratus.bsputility.ui.theme.MyApplicationTheme
import com.gratus.bsputility.ui.viewmodel.ManpowerViewModel

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
}

@Composable
fun AddContractorDialog(
    contractor: Contractor,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (Contractor) -> Unit
) {
    var name by remember { mutableStateOf(contractor.name) }
    var contact by remember { mutableStateOf(contractor.contactPerson) }
    var phone by remember { mutableStateOf(contractor.phone) }
    var notes by remember { mutableStateOf(contractor.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Add New Contractor" else "Edit Contractor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Contractor / Agency Name *") },
                    modifier = Modifier.fillMaxWidth().testTag("input_contractor_name")
                )
                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Contact Person") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (e.g. provides welders)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
                    val toSave = contractor.copy(
                        name = name.trim(),
                        contactPerson = contact.trim(),
                        phone = phone.trim(),
                        notes = notes.trim()
                    )
                    onSave(toSave)
                }
            }) {
                Text(if (isNew) "Add Contractor" else "Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddConfigDialog(
    category: String,
    tabTitle: String,
    configItem: ConfigItem? = null,
    availableDepartments: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (name: String, fieldType: String) -> Unit
) {
    val isEditing = configItem != null
    var itemName by remember(configItem) { mutableStateOf(configItem?.name ?: "") }

    val (initialFieldType, initialTargetAudience) = remember(configItem) {
        if (category == "CUSTOM_FIELD" && configItem != null) {
            val parts = configItem.extraType.split("|")
            val fType = parts.getOrNull(0)?.ifBlank { "TEXT" } ?: "TEXT"
            val target = parts.getOrNull(1)?.ifBlank { "ALL" } ?: "ALL"
            Pair(fType, target)
        } else {
            Pair("TEXT", "ALL")
        }
    }
    var fieldType by remember(initialFieldType) { mutableStateOf(initialFieldType) }
    var targetAudience by remember(initialTargetAudience) { mutableStateOf(initialTargetAudience) } // ALL, STAFF, LABOUR

    val initialDepts = remember(configItem) {
        if (category == "LABOUR_ROLE" && configItem != null) {
            val extra = configItem.extraType.trim()
            if (extra.isNotBlank() && !extra.equals("ALL", ignoreCase = true)) {
                extra.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
            } else {
                emptySet()
            }
        } else {
            emptySet()
        }
    }
    var selectedDepts by remember(initialDepts) { mutableStateOf<Set<String>>(initialDepts) }
    var deptDropdownOpen by remember { mutableStateOf(false) }

    val singularTitle = when (tabTitle) {
        "Departments" -> "Department"
        "Designations" -> "Designation"
        "Units" -> "Unit"
        "Shifts" -> "Shift"
        "Labour Roles" -> "Labour Role"
        "Custom Fields" -> "Custom Field"
        else -> tabTitle.removeSuffix("s")
    }
    val dialogTitle = if (isEditing) "Edit $singularTitle" else (if (tabTitle == "Shifts") "Add Shift" else "Add to $singularTitle")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(dialogTitle)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text(if (category == "SHIFT") "Shift Name (e.g. Shift A, General) *" else "Name / Title *") },
                    modifier = Modifier.fillMaxWidth().testTag("input_config_name")
                )

                if (category == "LABOUR_ROLE") {
                    Text("Associated Department(s):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Select which department(s) can use this role (or leave as All).", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = if (selectedDepts.isEmpty()) "All Departments" else selectedDepts.joinToString(", "),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Applies to Department") },
                            trailingIcon = {
                                IconButton(onClick = { deptDropdownOpen = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Departments")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { deptDropdownOpen = true }
                        )

                        DropdownMenu(
                            expanded = deptDropdownOpen,
                            onDismissRequest = { deptDropdownOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Departments (No restriction)") },
                                onClick = {
                                    selectedDepts = emptySet()
                                    deptDropdownOpen = false
                                }
                            )
                            availableDepartments.forEach { dept ->
                                val isSelected = dept in selectedDepts
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(checked = isSelected, onCheckedChange = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(dept)
                                        }
                                    },
                                    onClick = {
                                        selectedDepts = if (isSelected) selectedDepts - dept else selectedDepts + dept
                                    }
                                )
                            }
                        }
                    }

                    // Quick selection chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = selectedDepts.isEmpty(),
                            onClick = { selectedDepts = emptySet() },
                            label = { Text("All", fontSize = 10.sp) }
                        )
                        availableDepartments.forEach { dept ->
                            val isSelected = dept in selectedDepts
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedDepts = if (isSelected) selectedDepts - dept else selectedDepts + dept
                                },
                                label = { Text(dept, fontSize = 10.sp) }
                            )
                        }
                    }
                }

                if (category == "CUSTOM_FIELD") {
                    Text("Field Type:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    val types = listOf("TEXT", "NUMBER", "DROPDOWN", "DATE", "BOOLEAN")
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        types.forEach { t ->
                            FilterChip(
                                selected = fieldType == t,
                                onClick = { fieldType = t },
                                label = { Text(t, fontSize = 10.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Applies To:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("ALL" to "All Employees", "STAFF" to "Staff Only", "LABOUR" to "Labour Only").forEach { (targetKey, label) ->
                            FilterChip(
                                selected = targetAudience == targetKey,
                                onClick = { targetAudience = targetKey },
                                label = { Text(label, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (itemName.isNotBlank()) {
                    val finalExtraType = when (category) {
                        "CUSTOM_FIELD" -> "$fieldType|$targetAudience"
                        "LABOUR_ROLE" -> if (selectedDepts.isEmpty()) "ALL" else selectedDepts.joinToString(", ")
                        else -> fieldType
                    }
                    onSave(itemName.trim(), finalExtraType)
                }
            }) {
                Text(if (isEditing) "Save" else "Add")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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

@Preview(name = "Master Data Dialog - Add Contractor", showBackground = true)
@Composable
fun AddContractorDialog_Preview() {
    MyApplicationTheme {
        AddContractorDialog(
            contractor = Contractor(name = "Apex Facilities", contactPerson = "Mahesh Jadhav", phone = "9876543210", notes = "Housekeeping team"),
            isNew = true,
            onDismiss = {},
            onSave = {}
        )
    }
}

@Preview(name = "Master Data Dialog - Add Config Item", showBackground = true)
@Composable
fun AddConfigDialog_Preview() {
    MyApplicationTheme {
        AddConfigDialog(
            category = "CUSTOM_FIELD",
            tabTitle = "Custom Fields",
            onDismiss = {},
            onSave = { _, _ -> }
        )
    }
}
