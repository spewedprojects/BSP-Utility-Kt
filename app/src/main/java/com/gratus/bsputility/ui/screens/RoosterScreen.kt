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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import com.gratus.bsputility.ui.components.AddEditEmployeeDialog
import com.gratus.bsputility.ui.components.CollapsibleSection
import com.gratus.bsputility.ui.components.JsonBackupDialog
import com.gratus.bsputility.ui.components.PasteImportDialog
import com.gratus.bsputility.ui.components.RoosterEmployeeCard
import com.gratus.bsputility.ui.preview.PreviewData
import com.gratus.bsputility.ui.theme.MyApplicationTheme
import com.gratus.bsputility.ui.viewmodel.ManpowerViewModel

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
                emp.empCode.contains(searchQuery, ignoreCase = true) ||
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
