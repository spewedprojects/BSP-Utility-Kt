package com.gratus.bsputility.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
import com.gratus.bsputility.utils.StorageHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gratus.bsputility.data.models.ConfigItem
import com.gratus.bsputility.data.models.Contractor
import com.gratus.bsputility.data.models.Employee
import com.gratus.bsputility.data.models.EmployeeStatuses
import com.gratus.bsputility.data.models.EmployeeTypes
import com.gratus.bsputility.ui.components.CollapsibleSection
import com.gratus.bsputility.ui.components.StatusBadge
import com.gratus.bsputility.ui.preview.PreviewData
import com.gratus.bsputility.ui.theme.IndustrialAmber600
import com.gratus.bsputility.ui.theme.IndustrialNavy900
import com.gratus.bsputility.ui.theme.MyApplicationTheme
import com.gratus.bsputility.ui.viewmodel.ManpowerViewModel
import org.json.JSONObject

@Composable
fun RoosterScreen(
    viewModel: ManpowerViewModel,
    modifier: Modifier = Modifier
) {
    val allEmployees by viewModel.allEmployees.collectAsStateWithLifecycle()
    val contractors by viewModel.allContractors.collectAsStateWithLifecycle()
    val configItems by viewModel.allConfigItems.collectAsStateWithLifecycle()

    val searchQuery by viewModel.roosterSearchQuery.collectAsStateWithLifecycle()
    val filterStatus by viewModel.roosterFilterStatus.collectAsStateWithLifecycle()
    val filterContractor by viewModel.roosterFilterContractor.collectAsStateWithLifecycle()
    val filterDepartment by viewModel.roosterFilterDepartment.collectAsStateWithLifecycle()
    val collapsedGroups by viewModel.collapsedGroups.collectAsStateWithLifecycle()

    val departments = remember(configItems) {
        configItems.filter { it.category == "DEPARTMENT" }.map { it.name }
    }
    val roles = remember(configItems) {
        configItems.filter { it.category == "LABOUR_ROLE" }.map { it.name }
    }
    val designations = remember(configItems) {
        configItems.filter { it.category == "DESIGNATION" }.map { it.name }
    }
    val units = remember(configItems) {
        configItems.filter { it.category == "UNIT" }.map { it.name }
    }
    val shifts = remember(configItems) {
        val list = configItems.filter { it.category == "SHIFT" }.map { it.name }
        if (list.isEmpty()) listOf("Shift A", "Shift B", "Shift C", "General") else list
    }
    val customFields = remember(configItems) {
        configItems.filter { it.category == "CUSTOM_FIELD" }
    }

    RoosterScreenContent(
        allEmployees = allEmployees,
        contractors = contractors,
        departments = departments,
        roles = roles,
        designations = designations,
        units = units,
        shifts = shifts,
        customFields = customFields,
        searchQuery = searchQuery,
        onSearchQueryChange = { viewModel.roosterSearchQuery.value = it },
        filterStatus = filterStatus,
        onFilterStatusChange = { viewModel.setRoosterFilterStatus(it) },
        filterContractor = filterContractor,
        onFilterContractorChange = { viewModel.setRoosterFilterContractor(it) },
        filterDepartment = filterDepartment,
        onFilterDepartmentChange = { viewModel.roosterFilterDepartment.value = it },
        collapsedGroups = collapsedGroups,
        onToggleCollapse = { viewModel.toggleCategoryCollapse(it) },
        onAddEmployee = { viewModel.addEmployee(it) },
        onUpdateEmployee = { viewModel.updateEmployee(it) },
        onDeleteEmployee = { viewModel.deleteEmployee(it) },
        onImportPastedNames = { namesText, targetType, contractorId, contractorName, dept, role ->
            viewModel.importPastedNames(namesText, targetType, contractorId, contractorName, dept, role)
        },
        onExportJson = { viewModel.exportAllDataJson() },
        onExportCsv = { viewModel.generateRoosterCsv() },
        onImportJson = { viewModel.importAllDataJson(it) },
        onImportCsv = { viewModel.importCsv(it) > 0 },
        allConfigItems = configItems,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoosterScreenContent(
    allEmployees: List<Employee>,
    contractors: List<Contractor>,
    departments: List<String>,
    roles: List<String>,
    designations: List<String>,
    units: List<String>,
    shifts: List<String> = emptyList(),
    customFields: List<ConfigItem> = emptyList(),
    allConfigItems: List<ConfigItem> = emptyList(),
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filterStatus: String,
    onFilterStatusChange: (String) -> Unit,
    filterContractor: String? = null,
    onFilterContractorChange: (String?) -> Unit = {},
    filterDepartment: String? = null,
    onFilterDepartmentChange: (String?) -> Unit = {},
    collapsedGroups: Map<String, Boolean>,
    onToggleCollapse: (String) -> Unit,
    onAddEmployee: (Employee) -> Unit,
    onUpdateEmployee: (Employee) -> Unit,
    onDeleteEmployee: (Employee) -> Unit,
    onImportPastedNames: (
        namesText: String,
        targetType: String,
        contractorId: Long?,
        contractorName: String,
        dept: String,
        role: String
    ) -> Int,
    onExportJson: () -> String,
    onExportCsv: () -> String,
    onImportJson: (String) -> Boolean,
    onImportCsv: ((String) -> Boolean)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Modals
    var showAddEditDialog by remember { mutableStateOf<Employee?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var showPasteImportDialog by remember { mutableStateOf(false) }
    var showJsonExportImportDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf<Employee?>(null) }
    var showContractorMenu by remember { mutableStateOf(false) }
    var showDeptMenu by remember { mutableStateOf(false) }

    // Filter employees based on search & filter chips
    val filteredEmployees = remember(allEmployees, searchQuery, filterStatus, filterContractor, filterDepartment) {
        allEmployees.filter { emp ->
            val matchesQuery = searchQuery.isBlank() ||
                emp.name.contains(searchQuery, ignoreCase = true) ||
                emp.permanentDepartment.contains(searchQuery, ignoreCase = true) ||
                emp.contractorName.contains(searchQuery, ignoreCase = true) ||
                emp.defaultWorkRole.contains(searchQuery, ignoreCase = true) ||
                emp.designation.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (filterStatus) {
                "Staff" -> emp.type == EmployeeTypes.STAFF
                "Labour" -> emp.type == EmployeeTypes.LABOUR
                "Housekeeping" -> emp.type == EmployeeTypes.HOUSEKEEPING
                "Debarred" -> emp.status == EmployeeStatuses.DEBARRED
                "Out" -> emp.status == EmployeeStatuses.OUT
                else -> true
            }

            val matchesContractor = when (filterContractor) {
                null -> true
                "ALL_CONTRACTORS" -> emp.contractorName.isNotBlank() && emp.type != EmployeeTypes.STAFF
                else -> emp.contractorName.equals(filterContractor, ignoreCase = true) && emp.type != EmployeeTypes.STAFF
            }

            val matchesDepartment = filterDepartment == null ||
                emp.permanentDepartment.equals(filterDepartment, ignoreCase = true)

            matchesQuery && matchesFilter && matchesContractor && matchesDepartment
        }
    }

    // Grouping by type for collapsible display
    val staffList = remember(filteredEmployees) { filteredEmployees.filter { it.type == EmployeeTypes.STAFF } }
    val labourList = remember(filteredEmployees) { filteredEmployees.filter { it.type == EmployeeTypes.LABOUR } }
    val hkList = remember(filteredEmployees) { filteredEmployees.filter { it.type == EmployeeTypes.HOUSEKEEPING } }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Bar with Quick Import & Export actions
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Employee Roster",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                            Text(
                                text = "${allEmployees.size} total personnel in database",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Quick Paste Button
                            IconButton(
                                onClick = { showPasteImportDialog = true },
                                modifier = Modifier.testTag("btn_quick_paste_import")
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Quick Paste Import", tint = MaterialTheme.colorScheme.primary)
                            }
                            // JSON / CSV Import/Export Button
                            IconButton(
                                onClick = { showJsonExportImportDialog = true },
                                modifier = Modifier.testTag("btn_export_import_json")
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = "Export / Import Data", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search name, dept, contractor, role...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_rooster_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filter chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val filters = listOf("All", "Staff", "Labour", "Housekeeping", "Debarred", "Out")
                        items(filters) { f ->
                            FilterChip(
                                selected = filterStatus == f,
                                onClick = {
                                    onFilterStatusChange(f)
                                    if (f == "Staff") {
                                        onFilterContractorChange(null)
                                    }
                                },
                                label = { Text(f, fontSize = 12.sp) }
                            )
                        }

                        // Contractor filter dropdown chip
                        item {
                            val contractorChipLabel = when (filterContractor) {
                                null -> "Contractor"
                                "ALL_CONTRACTORS" -> "All Contractors"
                                else -> filterContractor
                            }
                            Box {
                                FilterChip(
                                    selected = filterContractor != null,
                                    onClick = { showContractorMenu = true },
                                    label = {
                                        Text(
                                            text = contractorChipLabel,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(14.dp))
                                    }
                                )
                                DropdownMenu(
                                    expanded = showContractorMenu,
                                    onDismissRequest = { showContractorMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Clear Filter") },
                                        onClick = {
                                            onFilterContractorChange(null)
                                            showContractorMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("All Contractors") },
                                        onClick = {
                                            onFilterContractorChange("ALL_CONTRACTORS")
                                            if (filterStatus == "Staff") onFilterStatusChange("All")
                                            showContractorMenu = false
                                        }
                                    )
                                    contractors.forEach { c ->
                                        DropdownMenuItem(
                                            text = { Text(c.name) },
                                            onClick = {
                                                onFilterContractorChange(c.name)
                                                if (filterStatus == "Staff") onFilterStatusChange("All")
                                                showContractorMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Department filter dropdown chip
                        item {
                            Box {
                                FilterChip(
                                    selected = filterDepartment != null,
                                    onClick = { showDeptMenu = true },
                                    label = {
                                        Text(
                                            text = filterDepartment ?: "Dept",
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(14.dp))
                                    }
                                )
                                DropdownMenu(
                                    expanded = showDeptMenu,
                                    onDismissRequest = { showDeptMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("All Departments") },
                                        onClick = {
                                            onFilterDepartmentChange(null)
                                            showDeptMenu = false
                                        }
                                    )
                                    departments.forEach { dept ->
                                        DropdownMenuItem(
                                            text = { Text(dept) },
                                            onClick = {
                                                onFilterDepartmentChange(dept)
                                                showDeptMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (filteredEmployees.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No employees found matching filter.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Employee List with Collapsible Groups
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Staff Section
                    if (staffList.isNotEmpty()) {
                        item {
                            val isExpanded = !(collapsedGroups["STAFF"] ?: false)
                            CollapsibleSection(
                                title = "Staff Members",
                                count = staffList.size,
                                isExpanded = isExpanded,
                                onToggle = { onToggleCollapse("STAFF") },
                                leadingIcon = Icons.Default.Badge,
                                iconTint = MaterialTheme.colorScheme.primary
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    staffList.forEach { emp ->
                                        RoosterEmployeeCard(
                                            employee = emp,
                                            onEdit = {
                                                isCreatingNew = false
                                                showAddEditDialog = emp
                                            },
                                            onDelete = { showDeleteConfirmDialog = emp }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Contract Labour Section
                    if (labourList.isNotEmpty()) {
                        item {
                            val isExpanded = !(collapsedGroups["LABOUR"] ?: false)
                            CollapsibleSection(
                                title = "Contract Labourers",
                                count = labourList.size,
                                isExpanded = isExpanded,
                                onToggle = { onToggleCollapse("LABOUR") },
                                leadingIcon = Icons.Default.Engineering,
                                iconTint = MaterialTheme.colorScheme.secondary
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    labourList.forEach { emp ->
                                        RoosterEmployeeCard(
                                            employee = emp,
                                            onEdit = {
                                                isCreatingNew = false
                                                showAddEditDialog = emp
                                            },
                                            onDelete = { showDeleteConfirmDialog = emp }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Housekeeping Section
                    if (hkList.isNotEmpty()) {
                        item {
                            val isExpanded = !(collapsedGroups["HOUSEKEEPING"] ?: false)
                            CollapsibleSection(
                                title = "Housekeeping Staff",
                                count = hkList.size,
                                isExpanded = isExpanded,
                                onToggle = { onToggleCollapse("HOUSEKEEPING") },
                                leadingIcon = Icons.Default.Group,
                                iconTint = MaterialTheme.colorScheme.tertiary
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    hkList.forEach { emp ->
                                        RoosterEmployeeCard(
                                            employee = emp,
                                            onEdit = {
                                                isCreatingNew = false
                                                showAddEditDialog = emp
                                            },
                                            onDelete = { showDeleteConfirmDialog = emp }
                                        )
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
        }

        FloatingActionButton(
            onClick = {
                isCreatingNew = true
                showAddEditDialog = Employee(
                    name = "",
                    type = EmployeeTypes.LABOUR,
                    status = EmployeeStatuses.ACTIVE,
                    dateAdded = "",
                    permanentDepartment = departments.firstOrNull() ?: "Welding Shop",
                    contractorId = contractors.firstOrNull()?.id,
                    contractorName = contractors.firstOrNull()?.name ?: "",
                    defaultWorkRole = roles.firstOrNull() ?: "Helper",
                    defaultUnit = units.firstOrNull() ?: "Unit I",
                    defaultShift = "Shift A"
                )
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_employee")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Employee")
        }
    }

    // --- DIALOG: Add / Edit Employee ---
    showAddEditDialog?.let { emp ->
        AddEditEmployeeDialog(
            employee = emp,
            isNew = isCreatingNew,
            departments = departments,
            roles = roles,
            designations = designations,
            contractors = contractors,
            units = units,
            shifts = shifts,
            customFields = customFields,
            allConfigItems = allConfigItems,
            onDismiss = { showAddEditDialog = null },
            onSave = { updated ->
                val sanitized = if (updated.type == EmployeeTypes.STAFF) {
                    updated.copy(contractorId = null, contractorName = "")
                } else updated
                if (isCreatingNew) {
                    onAddEmployee(sanitized)
                    Toast.makeText(context, "Added ${sanitized.name} to library", Toast.LENGTH_SHORT).show()
                } else {
                    onUpdateEmployee(sanitized)
                    Toast.makeText(context, "Updated ${sanitized.name}", Toast.LENGTH_SHORT).show()
                }
                showAddEditDialog = null
            }
        )
    }

    // --- DIALOG: Copy / Paste Names Import ---
    if (showPasteImportDialog) {
        PasteImportDialog(
            contractors = contractors,
            departments = departments,
            roles = roles,
            allConfigItems = allConfigItems,
            onDismiss = { showPasteImportDialog = false },
            onImport = { namesText, targetType, contractorId, contractorName, dept, role ->
                val count = onImportPastedNames(namesText, targetType, contractorId, contractorName, dept, role)
                Toast.makeText(context, "Imported $count employees successfully!", Toast.LENGTH_LONG).show()
                showPasteImportDialog = false
            }
        )
    }

    // --- DIALOG: JSON Backup / Export / Import ---
    if (showJsonExportImportDialog) {
        JsonBackupDialog(
            onExportJson = onExportJson,
            onExportCsv = onExportCsv,
            onImportJson = onImportJson,
            onImportCsv = onImportCsv,
            onDismiss = { showJsonExportImportDialog = false }
        )
    }

    // --- DIALOG: Delete Confirmation ---
    showDeleteConfirmDialog?.let { emp ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Delete Employee") },
            text = { Text("Are you sure you want to permanently delete \"${emp.name}\" from the employee library?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteEmployee(emp)
                        Toast.makeText(context, "Deleted ${emp.name}", Toast.LENGTH_SHORT).show()
                        showDeleteConfirmDialog = null
                    },
                    modifier = Modifier.testTag("btn_confirm_delete")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun RoosterEmployeeCard(
    employee: Employee,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .testTag("rooster_card_${employee.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = employee.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    StatusBadge(status = employee.status)
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (employee.type == EmployeeTypes.STAFF) {
                        if (employee.permanentDepartment.isNotBlank()) {
                            Text(
                                text = employee.permanentDepartment,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (employee.designation.isNotBlank()) {
                            Text(
                                text = "• ${employee.designation}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        if (employee.permanentDepartment.isNotBlank()) {
                            Text(
                                text = employee.permanentDepartment,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (employee.defaultWorkRole.isNotBlank()) {
                            val roleText = if (employee.permanentDepartment.isNotBlank()) "• ${employee.defaultWorkRole}" else employee.defaultWorkRole
                            Text(
                                text = roleText,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                if (employee.type != EmployeeTypes.STAFF && employee.contractorName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Contractor: ${employee.contractorName} • ${employee.defaultUnit}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Added: ${employee.dateAdded}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                // Custom fields badges
                val customFieldsMap = remember(employee.customFieldsJson) {
                    val map = mutableMapOf<String, String>()
                    try {
                        if (employee.customFieldsJson.isNotBlank()) {
                            val json = JSONObject(employee.customFieldsJson)
                            val keys = json.keys()
                            while (keys.hasNext()) {
                                val k = keys.next()
                                val v = json.optString(k)
                                if (v.isNotBlank()) {
                                    map[k] = v
                                }
                            }
                        }
                    } catch (_: Exception) {}
                    map
                }
                if (customFieldsMap.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        customFieldsMap.entries.take(3).forEach { (k, v) ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = if (v.equals("true", ignoreCase = true)) k else "$k: $v",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }

            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun AddEditEmployeeDialog(
    employee: Employee,
    isNew: Boolean,
    departments: List<String>,
    roles: List<String>,
    designations: List<String>,
    contractors: List<Contractor>,
    units: List<String>,
    shifts: List<String> = emptyList(),
    customFields: List<ConfigItem> = emptyList(),
    allConfigItems: List<ConfigItem> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (Employee) -> Unit
) {
    var name by remember { mutableStateOf(employee.name) }
    var type by remember { mutableStateOf(employee.type) }
    var status by remember { mutableStateOf(employee.status) }
    var department by remember { mutableStateOf(employee.permanentDepartment) }
    var designation by remember { mutableStateOf(employee.designation) }
    var contractorName by remember { mutableStateOf(employee.contractorName) }
    var workRole by remember { mutableStateOf(employee.defaultWorkRole) }
    var unit by remember { mutableStateOf(employee.defaultUnit) }
    var shift by remember { mutableStateOf(employee.defaultShift) }
    var remarks by remember { mutableStateOf(employee.permanentRemarks) }

    // Dropdown booleans
    var deptExp by remember { mutableStateOf(false) }
    var roleExp by remember { mutableStateOf(false) }
    var contractorExp by remember { mutableStateOf(false) }
    var unitExp by remember { mutableStateOf(false) }
    var shiftExp by remember { mutableStateOf(false) }

    val availableShifts = remember(shifts) {
        if (shifts.isEmpty()) listOf("Shift A", "Shift B", "Shift C", "General") else shifts
    }

    val filteredRoles = remember(department, roles, allConfigItems) {
        val labourRoleConfigs = allConfigItems.filter { it.category == "LABOUR_ROLE" }
        if (labourRoleConfigs.isEmpty()) {
            roles
        } else {
            val matching = labourRoleConfigs.filter { roleItem ->
                val extra = roleItem.extraType.trim()
                if (extra.isBlank() || extra.equals("ALL", ignoreCase = true)) {
                    true
                } else {
                    val parts = extra.split(",").map { it.trim().lowercase() }
                    parts.contains("all") || parts.contains(department.trim().lowercase())
                }
            }.map { it.name }
            if (matching.isEmpty()) roles else matching
        }
    }

    val initialCustomFields = remember(employee.customFieldsJson) {
        val map = mutableMapOf<String, String>()
        try {
            if (employee.customFieldsJson.isNotBlank()) {
                val json = JSONObject(employee.customFieldsJson)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    map[k] = json.optString(k)
                }
            }
        } catch (_: Exception) {}
        map
    }
    var customFieldValues by remember { mutableStateOf(initialCustomFields) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isNew) "Add to Employee Library" else "Edit Employee Record",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Full Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Employee Full Name *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_emp_name")
                )

                // Employee Type Selector
                Column {
                    Text("Employee Type", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        EmployeeTypes.all.forEach { t ->
                            FilterChip(
                                selected = type == t,
                                onClick = { type = t },
                                label = { Text(t, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Status Selector (Active, Out, Debarred)
                Column {
                    Text("Employment Status", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        EmployeeStatuses.all.forEach { st ->
                            FilterChip(
                                selected = status == st,
                                onClick = { status = st },
                                label = { Text(st, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Permanent Department (Applies to both Staff and Labour)
                Column {
                    Text("Permanent Department *", style = MaterialTheme.typography.labelMedium)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { deptExp = true },
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(department.ifBlank { "Select Department" }, fontSize = 13.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(expanded = deptExp, onDismissRequest = { deptExp = false }) {
                            departments.forEach { d ->
                                DropdownMenuItem(
                                    text = { Text(d) },
                                    onClick = {
                                        department = d
                                        deptExp = false
                                    }
                                )
                            }
                        }
                    }
                }

                // If Staff: Designation
                if (type == EmployeeTypes.STAFF) {
                    OutlinedTextField(
                        value = designation,
                        onValueChange = { designation = it },
                        label = { Text("Designation (e.g. HR Trainee, Supervisor)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // If Labour: Contractor
                    Column {
                        Text("Contractor", style = MaterialTheme.typography.labelMedium)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { contractorExp = true },
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(contractorName.ifBlank { "Select Contractor" }, fontSize = 13.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(expanded = contractorExp, onDismissRequest = { contractorExp = false }) {
                                contractors.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c.name) },
                                        onClick = {
                                            contractorName = c.name
                                            contractorExp = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Default Work Role Dropdown
                    Column {
                        Text("Default Work / Category", style = MaterialTheme.typography.labelMedium)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { roleExp = true },
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(workRole.ifBlank { "Select Role" }, fontSize = 13.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(expanded = roleExp, onDismissRequest = { roleExp = false }) {
                                filteredRoles.forEach { r ->
                                    DropdownMenuItem(
                                        text = { Text(r) },
                                        onClick = {
                                            workRole = r
                                            roleExp = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Default Unit & Shift (Applies to both Staff and Labour)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Unit", style = MaterialTheme.typography.labelMedium)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { unitExp = true },
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(unit, fontSize = 13.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(expanded = unitExp, onDismissRequest = { unitExp = false }) {
                                units.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(u) },
                                        onClick = {
                                            unit = u
                                            unitExp = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Shift", style = MaterialTheme.typography.labelMedium)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { shiftExp = true },
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(shift.ifBlank { availableShifts.firstOrNull() ?: "Shift A" }, fontSize = 13.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(expanded = shiftExp, onDismissRequest = { shiftExp = false }) {
                                availableShifts.forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(s) },
                                        onClick = {
                                            shift = s
                                            shiftExp = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Permanent Remarks
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Permanent Remarks (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Custom Fields Section
                val applicableFields = remember(customFields, type) {
                    customFields.filter { cf ->
                        val parts = cf.extraType.split("|")
                        val target = parts.getOrNull(1)?.uppercase() ?: "ALL"
                        when (target) {
                            "STAFF" -> type == EmployeeTypes.STAFF
                            "LABOUR" -> type != EmployeeTypes.STAFF
                            else -> true
                        }
                    }
                }

                if (applicableFields.isNotEmpty()) {
                    Text("Custom Fields", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        applicableFields.forEach { cf ->
                            val parts = cf.extraType.split("|")
                            val fieldType = parts.getOrNull(0)?.uppercase() ?: "TEXT"
                            val currentValue = customFieldValues[cf.name] ?: ""

                            if (fieldType == "BOOLEAN") {
                                val isChecked = currentValue.equals("true", ignoreCase = true)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(cf.name, fontSize = 13.sp)
                                    Switch(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            customFieldValues = customFieldValues.toMutableMap().apply {
                                                put(cf.name, checked.toString())
                                            }
                                        }
                                    )
                                }
                            } else if (fieldType == "NUMBER") {
                                OutlinedTextField(
                                    value = currentValue,
                                    onValueChange = { num ->
                                        customFieldValues = customFieldValues.toMutableMap().apply {
                                            put(cf.name, num)
                                        }
                                    },
                                    label = { Text(cf.name) },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            } else {
                                OutlinedTextField(
                                    value = currentValue,
                                    onValueChange = { txt ->
                                        customFieldValues = customFieldValues.toMutableMap().apply {
                                            put(cf.name, txt)
                                        }
                                    },
                                    label = { Text(cf.name) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val jsonObj = JSONObject()
                        customFieldValues.forEach { (k, v) ->
                            if (v.isNotBlank()) {
                                jsonObj.put(k, v)
                            }
                        }
                        val updated = employee.copy(
                            name = name.trim(),
                            type = type,
                            status = status,
                            permanentDepartment = (department.ifBlank { departments.firstOrNull() ?: "" }).trim(),
                            designation = if (type == EmployeeTypes.STAFF) designation.trim() else "",
                            contractorId = if (type == EmployeeTypes.STAFF) null else contractors.find { it.name.equals(contractorName, ignoreCase = true) }?.id,
                            contractorName = if (type == EmployeeTypes.STAFF) "" else contractorName,
                            defaultWorkRole = if (type != EmployeeTypes.STAFF) workRole else "",
                            defaultUnit = unit,
                            defaultShift = shift.ifBlank { availableShifts.firstOrNull() ?: "Shift A" },
                            permanentRemarks = remarks.trim(),
                            customFieldsJson = jsonObj.toString()
                        )
                        onSave(updated)
                    }
                },
                modifier = Modifier.testTag("btn_save_employee")
            ) {
                Text(if (isNew) "Add to Library" else "Save Changes")
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
fun PasteImportDialog(
    contractors: List<Contractor>,
    departments: List<String>,
    roles: List<String>,
    allConfigItems: List<ConfigItem> = emptyList(),
    onDismiss: () -> Unit,
    onImport: (
        namesText: String,
        targetType: String,
        contractorId: Long?,
        contractorName: String,
        dept: String,
        role: String
    ) -> Unit
) {
    var namesText by remember { mutableStateOf("") }
    var targetType by remember { mutableStateOf(EmployeeTypes.LABOUR) }
    var selectedContractor by remember { mutableStateOf(contractors.firstOrNull()?.name ?: "") }
    var selectedDept by remember { mutableStateOf(departments.firstOrNull() ?: "Welding Shop") }

    val filteredRoles = remember(selectedDept, roles, allConfigItems) {
        val labourRoleConfigs = allConfigItems.filter { it.category == "LABOUR_ROLE" }
        if (labourRoleConfigs.isEmpty()) {
            roles
        } else {
            val matching = labourRoleConfigs.filter { roleItem ->
                val extra = roleItem.extraType.trim()
                if (extra.isBlank() || extra.equals("ALL", ignoreCase = true)) {
                    true
                } else {
                    val parts = extra.split(",").map { it.trim().lowercase() }
                    parts.contains("all") || parts.contains(selectedDept.trim().lowercase())
                }
            }.map { it.name }
            if (matching.isEmpty()) roles else matching
        }
    }

    var selectedRole by remember(selectedDept) { mutableStateOf(filteredRoles.firstOrNull() ?: "Helper") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Fast Batch Name Import", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Paste a list of names, one per line:",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = namesText,
                    onValueChange = { namesText = it },
                    placeholder = { Text("Ramesh Pawar\nVijay Thorat\nPravin Shinde\n...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("input_batch_names")
                )

                Text("Where should these names be added?", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    EmployeeTypes.all.forEach { t ->
                        FilterChip(
                            selected = targetType == t,
                            onClick = { targetType = t },
                            label = { Text(t, fontSize = 11.sp) }
                        )
                    }
                }

                // Permanent Department (Applies to both Staff and Labour)
                var deptMenuExp by remember { mutableStateOf(false) }
                Text("Select Permanent Department:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { deptMenuExp = true },
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedDept.ifBlank { "Select Department" }, fontSize = 13.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = deptMenuExp,
                        onDismissRequest = { deptMenuExp = false }
                    ) {
                        departments.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d) },
                                onClick = {
                                    selectedDept = d
                                    deptMenuExp = false
                                }
                            )
                        }
                    }
                }

                if (targetType != EmployeeTypes.STAFF) {
                    var contractorMenuExp by remember { mutableStateOf(false) }
                    Text("Select Contractor:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { contractorMenuExp = true },
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedContractor.ifBlank { "Select Contractor" }, fontSize = 13.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = contractorMenuExp,
                            onDismissRequest = { contractorMenuExp = false }
                        ) {
                            contractors.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name) },
                                    onClick = {
                                        selectedContractor = c.name
                                        contractorMenuExp = false
                                    }
                                )
                            }
                        }
                    }

                    var roleMenuExp by remember { mutableStateOf(false) }
                    Text("Select Default Role:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { roleMenuExp = true },
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedRole.ifBlank { "Select Role" }, fontSize = 13.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = roleMenuExp,
                            onDismissRequest = { roleMenuExp = false }
                        ) {
                            filteredRoles.forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(r) },
                                    onClick = {
                                        selectedRole = r
                                        roleMenuExp = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val contractorObj = if (targetType == EmployeeTypes.STAFF) null else contractors.find { it.name == selectedContractor }
                    val finalContractorName = if (targetType == EmployeeTypes.STAFF) "" else selectedContractor
                    onImport(
                        namesText,
                        targetType,
                        contractorObj?.id,
                        finalContractorName,
                        selectedDept,
                        selectedRole
                    )
                },
                modifier = Modifier.testTag("btn_confirm_import")
            ) {
                Text("Import Names")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

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

// ==========================================
// PREVIEWS - ALL STATES
// ==========================================

@Preview(name = "Rooster Screen - Default Populated", showBackground = true)
@Composable
fun RoosterScreenPreview_Default() {
    MyApplicationTheme(darkTheme = false) {
        RoosterScreenContent(
            allEmployees = PreviewData.sampleEmployees,
            contractors = PreviewData.sampleContractors,
            departments = listOf("Laser Cutting", "Fabrication", "Welding Shop", "Press Shop"),
            roles = listOf("Helper", "Welder", "Operator", "Fitter"),
            designations = listOf("Production Supervisor", "Shift In-charge"),
            units = listOf("Unit I", "Unit II", "Unit III"),
            searchQuery = "",
            onSearchQueryChange = {},
            filterStatus = "All",
            onFilterStatusChange = {},
            collapsedGroups = emptyMap(),
            onToggleCollapse = {},
            onAddEmployee = {},
            onUpdateEmployee = {},
            onDeleteEmployee = {},
            onImportPastedNames = { _, _, _, _, _, _ -> 0 },
            onExportJson = { "{}" },
            onExportCsv = { "" },
            onImportJson = { true }
        )
    }
}

@Preview(name = "Rooster Screen - Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RoosterScreenPreview_DarkTheme() {
    MyApplicationTheme(darkTheme = true) {
        RoosterScreenContent(
            allEmployees = PreviewData.sampleEmployees,
            contractors = PreviewData.sampleContractors,
            departments = listOf("Laser Cutting", "Fabrication", "Welding Shop", "Press Shop"),
            roles = listOf("Helper", "Welder", "Operator", "Fitter"),
            designations = listOf("Production Supervisor", "Shift In-charge"),
            units = listOf("Unit I", "Unit II", "Unit III"),
            searchQuery = "",
            onSearchQueryChange = {},
            filterStatus = "All",
            onFilterStatusChange = {},
            collapsedGroups = emptyMap(),
            onToggleCollapse = {},
            onAddEmployee = {},
            onUpdateEmployee = {},
            onDeleteEmployee = {},
            onImportPastedNames = { _, _, _, _, _, _ -> 0 },
            onExportJson = { "{}" },
            onExportCsv = { "" },
            onImportJson = { true }
        )
    }
}

@Preview(name = "Rooster Screen - Filtered (Debarred & Out)", showBackground = true)
@Composable
fun RoosterScreenPreview_Filtered() {
    MyApplicationTheme {
        RoosterScreenContent(
            allEmployees = PreviewData.sampleEmployees,
            contractors = PreviewData.sampleContractors,
            departments = listOf("Laser Cutting", "Fabrication", "Welding Shop", "Press Shop"),
            roles = listOf("Helper", "Welder", "Operator", "Fitter"),
            designations = listOf("Production Supervisor", "Shift In-charge"),
            units = listOf("Unit I", "Unit II", "Unit III"),
            searchQuery = "",
            onSearchQueryChange = {},
            filterStatus = "Debarred",
            onFilterStatusChange = {},
            collapsedGroups = emptyMap(),
            onToggleCollapse = {},
            onAddEmployee = {},
            onUpdateEmployee = {},
            onDeleteEmployee = {},
            onImportPastedNames = { _, _, _, _, _, _ -> 0 },
            onExportJson = { "{}" },
            onExportCsv = { "" },
            onImportJson = { true }
        )
    }
}

@Preview(name = "Rooster Screen - Empty Search State", showBackground = true)
@Composable
fun RoosterScreenPreview_Empty() {
    MyApplicationTheme {
        RoosterScreenContent(
            allEmployees = emptyList(),
            contractors = PreviewData.sampleContractors,
            departments = listOf("Laser Cutting", "Fabrication"),
            roles = listOf("Helper", "Welder"),
            designations = listOf("Production Supervisor"),
            units = listOf("Unit I"),
            searchQuery = "UnknownPerson",
            onSearchQueryChange = {},
            filterStatus = "All",
            onFilterStatusChange = {},
            collapsedGroups = emptyMap(),
            onToggleCollapse = {},
            onAddEmployee = {},
            onUpdateEmployee = {},
            onDeleteEmployee = {},
            onImportPastedNames = { _, _, _, _, _, _ -> 0 },
            onExportJson = { "{}" },
            onExportCsv = { "" },
            onImportJson = { true }
        )
    }
}

@Preview(name = "Rooster Employee Cards - Various Types", showBackground = true)
@Composable
fun RoosterEmployeeCards_Preview() {
    MyApplicationTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Staff Member Card
            RoosterEmployeeCard(
                employee = PreviewData.sampleEmployees[0],
                onEdit = {},
                onDelete = {}
            )
            // 2. Active Contract Labour Card
            RoosterEmployeeCard(
                employee = PreviewData.sampleEmployees[3],
                onEdit = {},
                onDelete = {}
            )
            // 3. Debarred Employee Card
            RoosterEmployeeCard(
                employee = PreviewData.sampleEmployees[6],
                onEdit = {},
                onDelete = {}
            )
            // 4. Housekeeping Staff Card
            RoosterEmployeeCard(
                employee = PreviewData.sampleEmployees[8],
                onEdit = {},
                onDelete = {}
            )
        }
    }
}

@Preview(name = "Rooster Dialog - Add/Edit Employee", showBackground = true)
@Composable
fun AddEditEmployeeDialog_Preview() {
    MyApplicationTheme {
        AddEditEmployeeDialog(
            employee = PreviewData.sampleEmployees[3],
            isNew = false,
            departments = listOf("Laser Cutting", "Fabrication", "Welding Shop", "Press Shop"),
            roles = listOf("Helper", "Welder", "Operator", "Fitter"),
            designations = listOf("Production Supervisor", "Shift In-charge"),
            contractors = PreviewData.sampleContractors,
            units = listOf("Unit I", "Unit II", "Unit III"),
            onDismiss = {},
            onSave = {}
        )
    }
}

@Preview(name = "Rooster Dialog - Fast Batch Paste Import", showBackground = true)
@Composable
fun PasteImportDialog_Preview() {
    MyApplicationTheme {
        PasteImportDialog(
            contractors = PreviewData.sampleContractors,
            departments = listOf("Laser Cutting", "Fabrication", "Welding Shop"),
            roles = listOf("Helper", "Welder", "Operator"),
            onDismiss = {},
            onImport = { _, _, _, _, _, _ -> }
        )
    }
}

@Preview(name = "Rooster Dialog - Json Backup / Restore", showBackground = true)
@Composable
fun JsonBackupDialog_Preview() {
    MyApplicationTheme {
        JsonBackupDialog(
            onExportJson = { "{\n  \"employees\": [ ... ]\n}" },
            onExportCsv = { "Name,Type,Department\nVijay,Labour,Welding" },
            onImportJson = { true },
            onDismiss = {}
        )
    }
}
