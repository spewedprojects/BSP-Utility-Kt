package com.gratus.bsputility.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Upload
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gratus.bsputility.data.models.Contractor
import com.gratus.bsputility.data.models.Employee
import com.gratus.bsputility.data.models.EmployeeStatuses
import com.gratus.bsputility.data.models.EmployeeTypes
import com.gratus.bsputility.ui.components.CollapsibleSection
import com.gratus.bsputility.ui.components.StatusBadge
import com.gratus.bsputility.ui.theme.IndustrialAmber600
import com.gratus.bsputility.ui.theme.IndustrialNavy900
import com.gratus.bsputility.ui.viewmodel.ManpowerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoosterScreen(
    viewModel: ManpowerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allEmployees by viewModel.allEmployees.collectAsStateWithLifecycle()
    val contractors by viewModel.allContractors.collectAsStateWithLifecycle()
    val configItems by viewModel.allConfigItems.collectAsStateWithLifecycle()

    val searchQuery by viewModel.roosterSearchQuery.collectAsStateWithLifecycle()
    val filterStatus by viewModel.roosterFilterStatus.collectAsStateWithLifecycle()
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

    // Modals
    var showAddEditDialog by remember { mutableStateOf<Employee?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var showPasteImportDialog by remember { mutableStateOf(false) }
    var showJsonExportImportDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf<Employee?>(null) }

    // Filter employees based on search & filter chip
    val filteredEmployees = remember(allEmployees, searchQuery, filterStatus) {
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
            matchesQuery && matchesFilter
        }
    }

    // Grouping by type for collapsible display
    val staffList = remember(filteredEmployees) { filteredEmployees.filter { it.type == EmployeeTypes.STAFF } }
    val labourList = remember(filteredEmployees) { filteredEmployees.filter { it.type == EmployeeTypes.LABOUR } }
    val hkList = remember(filteredEmployees) { filteredEmployees.filter { it.type == EmployeeTypes.HOUSEKEEPING } }

    Scaffold(
        floatingActionButton = {
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
                    .padding(bottom = 64.dp)
                    .testTag("fab_add_employee")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Employee")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                                text = "Employee Library (Rooster)",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            )
                            Text(
                                text = "${allEmployees.size} total registered employees",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Quick Paste Names Import
                            OutlinedButton(
                                onClick = { showPasteImportDialog = true },
                                modifier = Modifier.testTag("btn_paste_import")
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Paste", fontSize = 12.sp)
                            }

                            // Full Backup / Restore
                            IconButton(
                                onClick = { showJsonExportImportDialog = true },
                                modifier = Modifier.testTag("btn_backup_restore")
                            ) {
                                Icon(Icons.Default.Download, contentDescription = "Backup/Export", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Search Input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.roosterSearchQuery.value = it },
                        placeholder = { Text("Search library by name, dept, contractor...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.roosterSearchQuery.value = "" }) {
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
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val filters = listOf("All", "Staff", "Labour", "Housekeeping", "Debarred", "Out")
                        items(filters) { f ->
                            FilterChip(
                                selected = filterStatus == f,
                                onClick = { viewModel.roosterFilterStatus.value = f },
                                label = { Text(f, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

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
                            onToggle = { viewModel.toggleCategoryCollapse("STAFF") },
                            leadingIcon = Icons.Default.Badge,
                            iconTint = IndustrialNavy900
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
                            onToggle = { viewModel.toggleCategoryCollapse("LABOUR") },
                            leadingIcon = Icons.Default.Engineering,
                            iconTint = IndustrialAmber600
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
                            onToggle = { viewModel.toggleCategoryCollapse("HOUSEKEEPING") },
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
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
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
            onDismiss = { showAddEditDialog = null },
            onSave = { updated ->
                if (isCreatingNew) {
                    viewModel.addEmployee(updated)
                    Toast.makeText(context, "Added ${updated.name} to library", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.updateEmployee(updated)
                    Toast.makeText(context, "Updated ${updated.name}", Toast.LENGTH_SHORT).show()
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
            onDismiss = { showPasteImportDialog = false },
            onImport = { namesText, targetType, contractorId, contractorName, dept, role ->
                val count = viewModel.importPastedNames(namesText, targetType, contractorId, contractorName, dept, role)
                Toast.makeText(context, "Imported $count employees successfully!", Toast.LENGTH_LONG).show()
                showPasteImportDialog = false
            }
        )
    }

    // --- DIALOG: JSON Backup / Export / Import ---
    if (showJsonExportImportDialog) {
        JsonBackupDialog(
            viewModel = viewModel,
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
                        viewModel.deleteEmployee(emp)
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
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
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
                    Text(
                        text = employee.permanentDepartment,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (employee.type == EmployeeTypes.STAFF && employee.designation.isNotBlank()) {
                        Text(
                            text = "• ${employee.designation}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else if (employee.defaultWorkRole.isNotBlank()) {
                        Text(
                            text = "• ${employee.defaultWorkRole}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (employee.type != EmployeeTypes.STAFF && employee.contractorName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Contractor: ${employee.contractorName} • ${employee.defaultUnit}",
                        fontSize = 11.sp,
                        color = IndustrialAmber600
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Added: ${employee.dateAdded}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
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

                // Department Dropdown
                Column {
                    Text("Permanent Department", style = MaterialTheme.typography.labelMedium)
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
                                roles.forEach { r ->
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

                    // Default Unit & Shift
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
                            OutlinedTextField(
                                value = shift,
                                onValueChange = { shift = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val updated = employee.copy(
                            name = name.trim(),
                            type = type,
                            status = status,
                            permanentDepartment = department,
                            designation = designation.trim(),
                            contractorName = contractorName,
                            defaultWorkRole = workRole,
                            defaultUnit = unit,
                            defaultShift = shift,
                            permanentRemarks = remarks.trim()
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
    var selectedRole by remember { mutableStateOf(roles.firstOrNull() ?: "Helper") }

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

                if (targetType == EmployeeTypes.LABOUR) {
                    Text("Select Contractor:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    contractors.forEach { c ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedContractor = c.name }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedContractor == c.name,
                                onClick = { selectedContractor = c.name }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(c.name, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val contractorObj = contractors.find { it.name == selectedContractor }
                    onImport(
                        namesText,
                        targetType,
                        contractorObj?.id,
                        selectedContractor,
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
    viewModel: ManpowerViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var jsonInput by remember { mutableStateOf("") }
    var exportTab by remember { mutableStateOf(true) }
    var generatedJson by remember { mutableStateOf("") }

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
                        label = { Text("Restore (JSON)") }
                    )
                }

                if (exportTab) {
                    Text("Export all employee profiles, contractors, and master categories for backup or data transfer.", fontSize = 12.sp)

                    Button(
                        onClick = {
                            val json = viewModel.exportAllDataJson()
                            generatedJson = json
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("BSP Manpower Data", json))
                            Toast.makeText(context, "Copied JSON to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export & Copy JSON")
                    }

                    OutlinedButton(
                        onClick = {
                            val csv = viewModel.generateRoosterCsv()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("BSP Employee Roster CSV", csv))
                            Toast.makeText(context, "Copied Employee CSV to clipboard!", Toast.LENGTH_SHORT).show()
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
                    Text("Paste full JSON backup string to restore data:", fontSize = 12.sp)
                    OutlinedTextField(
                        value = jsonInput,
                        onValueChange = { jsonInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        placeholder = { Text("{\n  \"employees\": [...]\n}") }
                    )
                    Button(
                        onClick = {
                            if (jsonInput.isNotBlank()) {
                                val success = viewModel.importAllDataJson(jsonInput)
                                if (success) {
                                    Toast.makeText(context, "Master data restored successfully!", Toast.LENGTH_LONG).show()
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, "Invalid JSON format!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore From JSON")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
