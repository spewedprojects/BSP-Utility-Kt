package com.gratus.bsputility.ui.screens

import android.content.res.Configuration
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gratus.bsputility.data.models.Contractor
import com.gratus.bsputility.data.models.Employee
import com.gratus.bsputility.data.models.EmployeeStatuses
import com.gratus.bsputility.data.models.EmployeeTypes
import com.gratus.bsputility.data.models.ManpowerSummary
import com.gratus.bsputility.ui.components.AttendanceLabourDialog
import com.gratus.bsputility.ui.components.AttendanceStaffDialog
import com.gratus.bsputility.ui.components.StatusBadge
import com.gratus.bsputility.ui.preview.PreviewData
import com.gratus.bsputility.ui.theme.IndustrialAmber600
import com.gratus.bsputility.ui.theme.MyApplicationTheme
import com.gratus.bsputility.ui.theme.PresentGreen
import com.gratus.bsputility.ui.theme.PresentGreenLight
import com.gratus.bsputility.ui.theme.StatusDebarred
import com.gratus.bsputility.ui.viewmodel.ManpowerViewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.forEach

@Composable
fun AttendanceScreen(
    viewModel: ManpowerViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.effectiveAttendanceItems.collectAsStateWithLifecycle()
    val summary by viewModel.manpowerSummary.collectAsStateWithLifecycle()
    val contractors by viewModel.allContractors.collectAsStateWithLifecycle()
    val configItems by viewModel.allConfigItems.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val areAllPresent by viewModel.areAllActivePresent.collectAsStateWithLifecycle()
    val is24Hour by viewModel.is24HourFormat.collectAsStateWithLifecycle()

    val searchQuery by viewModel.attendanceSearchQuery.collectAsStateWithLifecycle()
    val filterType by viewModel.attendanceFilterType.collectAsStateWithLifecycle()
    val filterContractor by viewModel.attendanceFilterContractor.collectAsStateWithLifecycle()
    val filterDepartment by viewModel.attendanceFilterDepartment.collectAsStateWithLifecycle()

    val departments = remember(configItems) {
        configItems.filter { it.category == "DEPARTMENT" }.map { it.name }
    }
    val roles = remember(configItems) {
        configItems.filter { it.category == "LABOUR_ROLE" }.map { it.name }
    }
    val units = remember(configItems) {
        configItems.filter { it.category == "UNIT" }.map { it.name }
    }
    val shifts = remember(configItems) {
        val list = configItems.filter { it.category == "SHIFT" }.map { it.name }
        if (list.isEmpty()) listOf("Shift A", "Shift B", "Shift C", "General") else list
    }

    AttendanceScreenContent(
        items = items,
        summary = summary,
        contractors = contractors,
        departments = departments,
        roles = roles,
        units = units,
        shifts = shifts,
        selectedDate = selectedDate,
        areAllPresent = areAllPresent,
        is24HourFormat = is24Hour,
        searchQuery = searchQuery,
        onSearchQueryChange = { viewModel.attendanceSearchQuery.value = it },
        filterType = filterType,
        onFilterTypeChange = { viewModel.setAttendanceFilterType(it) },
        filterContractor = filterContractor,
        onFilterContractorChange = { viewModel.setAttendanceFilterContractor(it) },
        filterDepartment = filterDepartment,
        onFilterDepartmentChange = { viewModel.attendanceFilterDepartment.value = it },
        onMarkAllPresent = { viewModel.markAllActivePresent() },
        onTogglePresence = { viewModel.toggleAttendance(it) },
        onSaveStaffAttendance = { emp, isPresent, time, remarks, timestamp ->
            viewModel.updateStaffDailyAttendance(emp, isPresent, time, remarks, timestamp)
        },
        onSaveLabourAssignment = { emp, isPresent, dept, role, contractor, unit, shift, remarks ->
            viewModel.updateLabourDailyAssignment(
                employee = emp,
                isPresent = isPresent,
                department = dept,
                workRole = role,
                contractor = contractor,
                unit = unit,
                shift = shift,
                remarks = remarks
            )
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreenContent(
    items: List<ManpowerViewModel.EmployeeAttendanceItem>,
    summary: ManpowerSummary,
    contractors: List<Contractor>,
    departments: List<String>,
    roles: List<String>,
    units: List<String>,
    shifts: List<String> = listOf("Shift A", "Shift B", "Shift C", "General"),
    selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    areAllPresent: Boolean = false,
    is24HourFormat: Boolean = false,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filterType: String,
    onFilterTypeChange: (String) -> Unit,
    filterContractor: String?,
    onFilterContractorChange: (String?) -> Unit,
    filterDepartment: String?,
    onFilterDepartmentChange: (String?) -> Unit,
    onMarkAllPresent: () -> Unit,
    onTogglePresence: (ManpowerViewModel.EmployeeAttendanceItem) -> Unit,
    onSaveStaffAttendance: (employee: Employee, isPresent: Boolean, time: String, remarks: String, timestamp: Long) -> Unit,
    onSaveLabourAssignment: (
        employee: Employee,
        isPresent: Boolean,
        department: String,
        workRole: String,
        contractor: String,
        unit: String,
        shift: String,
        remarks: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    val isFutureDate = selectedDate > todayStr

    // Dialog state
    var selectedItemForDialog by remember { mutableStateOf<ManpowerViewModel.EmployeeAttendanceItem?>(null) }
    var debarredItemToConfirm by remember { mutableStateOf<ManpowerViewModel.EmployeeAttendanceItem?>(null) }
    var showContractorMenu by remember { mutableStateOf(false) }
    var showDeptMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- 1. QUICK MANPOWER SUMMARY BAR ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isFutureDate) "FUTURE RECORD ($selectedDate)" else "MANPOWER TODAY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${summary.grandTotalPresent}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${summary.totalStaffPresent} Staff + ${summary.totalLabourersPresent} Labour)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }

                Button(
                    onClick = onMarkAllPresent,
                    enabled = !isFutureDate,
                    colors = if (areAllPresent) {
                        ButtonDefaults.buttonColors(
                            containerColor = PresentGreen,
                            contentColor = Color.White
                        )
                    } else {
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    },
                    border = if (areAllPresent) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.testTag("btn_mark_all_present")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (areAllPresent) Color.White else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (areAllPresent) "All Present (Undo)" else "Mark All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (areAllPresent) Color.White else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // --- FUTURE DATE WARNING BANNER ---
        if (isFutureDate) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Future Date ($selectedDate): Attendance marking is disabled.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // --- 2. SEARCH BAR: "Who's where?" ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search name, dept, role, contractor...") },
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
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_attendance_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // --- 3. FAST FILTER CHIPS ---
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val filterChips = listOf("All", "Labour", "Staff", "Present", "Absent")
                    items(filterChips) { filter ->
                        FilterChip(
                            selected = filterType == filter,
                            onClick = {
                                onFilterTypeChange(filter)
                                if (filter == "Staff") {
                                    onFilterContractorChange(null)
                                }
                            },
                            label = { Text(filter, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                            modifier = Modifier.testTag("filter_chip_$filter")
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
                                        if (filterType == "Staff") onFilterTypeChange("All")
                                        showContractorMenu = false
                                    }
                                )
                                contractors.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c.name) },
                                        onClick = {
                                            onFilterContractorChange(c.name)
                                            if (filterType == "Staff") onFilterTypeChange("All")
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

        // --- 4. ATTENDANCE LIST ---
        if (items.isEmpty()) {
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
                        text = "No employees match your search/filter.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.employee.id }) { item ->
                    AttendanceCard(
                        item = item,
                        isFutureDate = isFutureDate,
                        onTogglePresence = {
                            if (isFutureDate) {
                                Toast.makeText(context, "Cannot mark attendance for future dates", Toast.LENGTH_SHORT).show()
                            } else if (item.employee.status == EmployeeStatuses.DEBARRED && !item.isPresent) {
                                debarredItemToConfirm = item
                            } else {
                                onTogglePresence(item)
                            }
                        },
                        onEditDetails = { selectedItemForDialog = item }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Modal dialogs
    selectedItemForDialog?.let { item ->
        if (item.employee.type == EmployeeTypes.STAFF) {
            AttendanceStaffDialog(
                employee = item.employee,
                currentPresence = item.isPresent,
                currentTime = item.attendanceTime,
                currentRemarks = item.dayRemarks,
                currentTimestamp = item.attendanceTimestamp,
                is24HourFormat = is24HourFormat,
                isFutureDate = isFutureDate,
                onDismiss = { selectedItemForDialog = null },
                onSave = { isPresent, time, remarks, timestamp ->
                    onSaveStaffAttendance(item.employee, isPresent, time, remarks, timestamp)
                    selectedItemForDialog = null
                }
            )
        } else {
            AttendanceLabourDialog(
                employee = item.employee,
                currentPresence = item.isPresent,
                currentDepartment = item.effectiveDepartment,
                currentWorkRole = item.effectiveWorkRole,
                currentContractor = item.effectiveContractor,
                currentUnit = item.effectiveUnit,
                currentShift = item.effectiveShift,
                currentRemarks = item.dayRemarks,
                availableDepartments = departments,
                availableRoles = roles,
                availableContractors = contractors,
                availableUnits = units,
                availableShifts = shifts,
                isFutureDate = isFutureDate,
                onDismiss = { selectedItemForDialog = null },
                onSave = { isPresent, dept, role, contractor, unit, shift, remarks ->
                    onSaveLabourAssignment(
                        item.employee,
                        isPresent,
                        dept,
                        role,
                        contractor,
                        unit,
                        shift,
                        remarks
                    )
                    selectedItemForDialog = null
                }
            )
        }
    }

    // Confirmation dialog for Debarred Employee Presence Toggle
    debarredItemToConfirm?.let { debarredItem ->
        AlertDialog(
            onDismissRequest = { debarredItemToConfirm = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text("Debarred Worker Alert", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${debarredItem.employee.name} is currently marked as DEBARRED.",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                    if (debarredItem.employee.permanentRemarks.isNotBlank()) {
                        Text(
                            text = "Debarment Reason: ${debarredItem.employee.permanentRemarks}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Are you sure you want to mark this worker present for today's shift?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onTogglePresence(debarredItem)
                        debarredItemToConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Mark Present Anyway")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { debarredItemToConfirm = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreenContent(
    items: List<ManpowerViewModel.EmployeeAttendanceItem>,
    summary: ManpowerSummary,
    contractors: List<Contractor>,
    departments: List<String>,
    roles: List<String>,
    units: List<String>,
    shifts: List<String> = listOf("Shift A", "Shift B", "Shift C", "General"),
    selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    areAllPresent: Boolean = false,
    is24HourFormat: Boolean = false,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filterType: String,
    onFilterTypeChange: (String) -> Unit,
    filterContractor: String?,
    onFilterContractorChange: (String?) -> Unit,
    filterDepartment: String?,
    onFilterDepartmentChange: (String?) -> Unit,
    onMarkAllPresent: () -> Unit,
    onTogglePresence: (ManpowerViewModel.EmployeeAttendanceItem) -> Unit,
    onSaveStaffAttendance: (employee: Employee, isPresent: Boolean, time: String, remarks: String) -> Unit,
    onSaveLabourAssignment: (
        employee: Employee,
        isPresent: Boolean,
        department: String,
        workRole: String,
        contractor: String,
        unit: String,
        shift: String,
        remarks: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    AttendanceScreenContent(
        items = items,
        summary = summary,
        contractors = contractors,
        departments = departments,
        roles = roles,
        units = units,
        shifts = shifts,
        selectedDate = selectedDate,
        areAllPresent = areAllPresent,
        is24HourFormat = is24HourFormat,
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange,
        filterType = filterType,
        onFilterTypeChange = onFilterTypeChange,
        filterContractor = filterContractor,
        onFilterContractorChange = onFilterContractorChange,
        filterDepartment = filterDepartment,
        onFilterDepartmentChange = onFilterDepartmentChange,
        onMarkAllPresent = onMarkAllPresent,
        onTogglePresence = onTogglePresence,
        onSaveStaffAttendance = { emp, isPresent, time, remarks, _ ->
            onSaveStaffAttendance(emp, isPresent, time, remarks)
        },
        onSaveLabourAssignment = onSaveLabourAssignment,
        modifier = modifier
    )
}

@Composable
fun AttendanceCard(
    item: ManpowerViewModel.EmployeeAttendanceItem,
    isFutureDate: Boolean = false,
    onTogglePresence: () -> Unit,
    onEditDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val emp = item.employee
    val isDebarred = emp.status == EmployeeStatuses.DEBARRED

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onEditDetails() }
            .testTag("attendance_card_${emp.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isPresent) PresentGreenLight.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isPresent) 0.dp else 0.dp),
        border = BorderStroke(
            1.dp,
            if (isDebarred) StatusDebarred
            else if (item.isPresent) PresentGreen.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = emp.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (isDebarred) {
                        StatusBadge(status = EmployeeStatuses.DEBARRED)
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Subtitle: Role & Department & Contractor
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Type chip or Role badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (emp.type == EmployeeTypes.STAFF) emp.designation.ifBlank { "Staff" } else item.effectiveWorkRole,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Department
                    Text(
                        text = item.effectiveDepartment,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Unit / Shift
                    Text(
                        text = "• ${item.effectiveUnit} (${item.effectiveShift})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Contractor info for labour
                if (emp.type != EmployeeTypes.STAFF && item.effectiveContractor.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Contractor: ${item.effectiveContractor}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Attendance time for staff - only when present
                if (emp.type == EmployeeTypes.STAFF && item.isPresent && item.attendanceTime.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.attendanceTime,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Remarks if present
                if (item.dayRemarks.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Comment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.dayRemarks,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = FontStyle.Italic,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Custom fields summary tags
                val customFieldsMap = remember(emp.customFieldsJson) {
                    val map = mutableMapOf<String, String>()
                    try {
                        if (emp.customFieldsJson.isNotBlank()) {
                            val json = JSONObject(emp.customFieldsJson)
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
                        customFieldsMap.entries.take(2).forEach { (k, v) ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = if (v.equals("true", ignoreCase = true)) k else "$k: $v",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right side: Quick Presence Toggle Button (48dp target)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFutureDate) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        else if (item.isPresent) PresentGreen
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable(enabled = !isFutureDate) { onTogglePresence() }
                    .testTag("btn_toggle_presence_${emp.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (isFutureDate) {
                    Text(
                        text = "—",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                } else if (item.isPresent) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Marked Present",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "TAP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ==========================================
// PREVIEWS - ALL STATES
// ==========================================

@Preview(name = "Attendance Screen - Populated / Default", showBackground = true)
@Composable
fun AttendanceScreenPreview_Default() {
    MyApplicationTheme(darkTheme = false) {
        AttendanceScreenContent(
            items = PreviewData.sampleAttendanceItems,
            summary = PreviewData.sampleSummary,
            contractors = PreviewData.sampleContractors,
            departments = listOf("Laser Cutting", "Fabrication", "Welding Shop", "Press Shop"),
            roles = listOf("Helper", "Welder", "Operator", "Laser Operator", "Fitter"),
            units = listOf("Unit I", "Unit II", "Unit III"),
            searchQuery = "",
            onSearchQueryChange = {},
            filterType = "All",
            onFilterTypeChange = {},
            filterContractor = null,
            onFilterContractorChange = {},
            filterDepartment = null,
            onFilterDepartmentChange = {},
            onMarkAllPresent = {},
            onTogglePresence = {},
            onSaveStaffAttendance = { _, _, _, _ -> },
            onSaveLabourAssignment = { _, _, _, _, _, _, _, _ -> }
        )
    }
}

@Preview(name = "Attendance Screen - Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AttendanceScreenPreview_DarkTheme() {
    MyApplicationTheme(darkTheme = true) {
        AttendanceScreenContent(
            items = PreviewData.sampleAttendanceItems,
            summary = PreviewData.sampleSummary,
            contractors = PreviewData.sampleContractors,
            departments = listOf("Laser Cutting", "Fabrication", "Welding Shop", "Press Shop"),
            roles = listOf("Helper", "Welder", "Operator", "Laser Operator", "Fitter"),
            units = listOf("Unit I", "Unit II", "Unit III"),
            searchQuery = "",
            onSearchQueryChange = {},
            filterType = "All",
            onFilterTypeChange = {},
            filterContractor = null,
            onFilterContractorChange = {},
            filterDepartment = null,
            onFilterDepartmentChange = {},
            onMarkAllPresent = {},
            onTogglePresence = {},
            onSaveStaffAttendance = { _, _, _, _ -> },
            onSaveLabourAssignment = { _, _, _, _, _, _, _, _ -> }
        )
    }
}

@Preview(name = "Attendance Screen - Filtered (Labour Only)", showBackground = true)
@Composable
fun AttendanceScreenPreview_Filtered() {
    val filtered = PreviewData.sampleAttendanceItems.filter { it.employee.type == EmployeeTypes.LABOUR }
    MyApplicationTheme {
        AttendanceScreenContent(
            items = filtered,
            summary = PreviewData.sampleSummary,
            contractors = PreviewData.sampleContractors,
            departments = listOf("Laser Cutting", "Fabrication", "Welding Shop", "Press Shop"),
            roles = listOf("Helper", "Welder", "Operator", "Laser Operator", "Fitter"),
            units = listOf("Unit I", "Unit II", "Unit III"),
            searchQuery = "Welding",
            onSearchQueryChange = {},
            filterType = "Labour",
            onFilterTypeChange = {},
            filterContractor = "Om Sai Enterprises",
            onFilterContractorChange = {},
            filterDepartment = null,
            onFilterDepartmentChange = {},
            onMarkAllPresent = {},
            onTogglePresence = {},
            onSaveStaffAttendance = { _, _, _, _ -> },
            onSaveLabourAssignment = { _, _, _, _, _, _, _, _ -> }
        )
    }
}

@Preview(name = "Attendance Screen - Empty Search Results", showBackground = true)
@Composable
fun AttendanceScreenPreview_Empty() {
    MyApplicationTheme {
        AttendanceScreenContent(
            items = emptyList(),
            summary = PreviewData.sampleSummary.copy(grandTotalPresent = 0, totalStaffPresent = 0, totalLabourersPresent = 0),
            contractors = PreviewData.sampleContractors,
            departments = listOf("Laser Cutting", "Fabrication"),
            roles = listOf("Helper", "Welder"),
            units = listOf("Unit I", "Unit II"),
            searchQuery = "NonExistentEmployee",
            onSearchQueryChange = {},
            filterType = "All",
            onFilterTypeChange = {},
            filterContractor = null,
            onFilterContractorChange = {},
            filterDepartment = null,
            onFilterDepartmentChange = {},
            onMarkAllPresent = {},
            onTogglePresence = {},
            onSaveStaffAttendance = { _, _, _, _ -> },
            onSaveLabourAssignment = { _, _, _, _, _, _, _, _ -> }
        )
    }
}

@Preview(name = "Attendance Cards - Various States", showBackground = true)
@Composable
fun AttendanceCards_Preview() {
    MyApplicationTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Present Labour Card
            AttendanceCard(
                item = PreviewData.sampleAttendanceItems[3],
                onTogglePresence = {},
                onEditDetails = {}
            )
            // 2. Absent Labour Card
            AttendanceCard(
                item = PreviewData.sampleAttendanceItems[5],
                onTogglePresence = {},
                onEditDetails = {}
            )
            // 3. Present Staff Card (with time & designation)
            AttendanceCard(
                item = PreviewData.sampleAttendanceItems[1],
                onTogglePresence = {},
                onEditDetails = {}
            )
            // 4. Debarred Employee Card
            AttendanceCard(
                item = PreviewData.sampleAttendanceItems[6],
                onTogglePresence = {},
                onEditDetails = {}
            )
        }
    }
}

@Preview(name = "Attendance Dialog - Labour Daily Assignment", showBackground = true)
@Composable
fun AttendanceLabourDialog_Preview() {
    MyApplicationTheme {
        AttendanceLabourDialog(
            employee = PreviewData.sampleEmployees[3],
            currentPresence = true,
            currentDepartment = "Welding Shop",
            currentWorkRole = "Welder",
            currentContractor = "Om Sai Enterprises",
            currentUnit = "Unit I",
            currentShift = "Shift A",
            currentRemarks = "Line 1 welding job",
            availableDepartments = listOf("Laser Cutting", "Fabrication", "Welding Shop", "Press Shop"),
            availableRoles = listOf("Helper", "Welder", "Operator", "Fitter"),
            availableContractors = PreviewData.sampleContractors,
            availableUnits = listOf("Unit I", "Unit II", "Unit III"),
            onDismiss = {},
            onSave = { _, _, _, _, _, _, _ -> }
        )
    }
}

@Preview(name = "Attendance Dialog - Staff Attendance", showBackground = true)
@Composable
fun AttendanceStaffDialog_Preview() {
    MyApplicationTheme {
        AttendanceStaffDialog(
            employee = PreviewData.sampleEmployees[0],
            currentPresence = true,
            currentTime = "08:45 AM",
            currentRemarks = "Morning production review",
            onDismiss = {},
            onSave = { _, _, _ -> }
        )
    }
}
