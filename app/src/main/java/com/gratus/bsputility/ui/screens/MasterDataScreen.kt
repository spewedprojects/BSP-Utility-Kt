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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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

    MasterDataScreenContent(
        contractors = contractors,
        allConfigs = allConfigs,
        allEmployees = allEmployees,
        onAddContractor = { viewModel.addContractor(it) },
        onUpdateContractor = { viewModel.updateContractor(it) },
        onDeleteContractor = {
            viewModel.deleteContractor(it)
            Toast.makeText(context, "Deleted ${it.name}", Toast.LENGTH_SHORT).show()
        },
        onAddConfigItem = { cat, name, type -> viewModel.addConfigItem(cat, name, type) },
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
    onAddContractor: (Contractor) -> Unit,
    onUpdateContractor: (Contractor) -> Unit,
    onDeleteContractor: (Contractor) -> Unit,
    onAddConfigItem: (category: String, name: String, fieldType: String) -> Unit,
    onDeleteConfigItem: (ConfigItem) -> Unit,
    initialTab: String = "Contractors",
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    val tabs = listOf("Contractors", "Departments", "Designations", "Units", "Shifts", "Labour Roles", "Custom Fields")

    // Modals
    var showAddContractorDialog by remember { mutableStateOf<Contractor?>(null) }
    var isNewContractor by remember { mutableStateOf(false) }
    var showAddConfigDialog by remember { mutableStateOf(false) }
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
                                Column {
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
                                    } else if (item.extraType.isNotBlank()) {
                                        Text(
                                            text = "Type: ${item.extraType}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
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

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

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

    // Modal: Add Config Item (Department, Role, Unit, Custom field)
    if (showAddConfigDialog) {
        AddConfigDialog(
            category = newConfigCategory,
            tabTitle = selectedTab,
            onDismiss = { showAddConfigDialog = false },
            onSave = { name, fieldType ->
                onAddConfigItem(newConfigCategory, name, fieldType)
                showAddConfigDialog = false
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
    onDismiss: () -> Unit,
    onSave: (name: String, fieldType: String) -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var fieldType by remember { mutableStateOf("TEXT") }
    var targetAudience by remember { mutableStateOf("ALL") } // ALL, STAFF, LABOUR

    val dialogTitle = if (tabTitle == "Shifts") "Add Shift" else "Add to ${tabTitle.removeSuffix("s")}"

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
                    val finalExtraType = if (category == "CUSTOM_FIELD") "$fieldType|$targetAudience" else fieldType
                    onSave(itemName.trim(), finalExtraType)
                }
            }) {
                Text("Add")
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
