package com.gratus.bsputility.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gratus.bsputility.data.models.EmployeeStatuses
import com.gratus.bsputility.data.models.EmployeeTypes
import com.gratus.bsputility.ui.components.AttendanceLabourDialog
import com.gratus.bsputility.ui.components.AttendanceStaffDialog
import com.gratus.bsputility.ui.components.StatusBadge
import com.gratus.bsputility.ui.theme.IndustrialAmber600
import com.gratus.bsputility.ui.theme.PresentGreen
import com.gratus.bsputility.ui.theme.PresentGreenLight
import com.gratus.bsputility.ui.theme.StatusDebarred
import com.gratus.bsputility.ui.viewmodel.ManpowerViewModel
import kotlin.collections.forEach

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: ManpowerViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.effectiveAttendanceItems.collectAsStateWithLifecycle()
    val summary by viewModel.manpowerSummary.collectAsStateWithLifecycle()
    val contractors by viewModel.allContractors.collectAsStateWithLifecycle()
    val configItems by viewModel.allConfigItems.collectAsStateWithLifecycle()

    val searchQuery by viewModel.attendanceSearchQuery.collectAsStateWithLifecycle()
    val filterType by viewModel.attendanceFilterType.collectAsStateWithLifecycle()
    val filterContractor by viewModel.attendanceFilterContractor.collectAsStateWithLifecycle()
    val filterDepartment by viewModel.attendanceFilterDepartment.collectAsStateWithLifecycle()

    // Dialog state
    var selectedItemForDialog by remember { mutableStateOf<ManpowerViewModel.EmployeeAttendanceItem?>(null) }

    val departments = remember(configItems) {
        configItems.filter { it.category == "DEPARTMENT" }.map { it.name }
    }
    val roles = remember(configItems) {
        configItems.filter { it.category == "LABOUR_ROLE" }.map { it.name }
    }
    val units = remember(configItems) {
        configItems.filter { it.category == "UNIT" }.map { it.name }
    }

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
                        text = "MANPOWER TODAY",
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

                OutlinedButton(
                    onClick = { viewModel.markAllActivePresent() },
                    modifier = Modifier.testTag("btn_mark_all_present")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mark All", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
                    onValueChange = { viewModel.attendanceSearchQuery.value = it },
                    placeholder = { Text("Who's where? Search name, dept, role, contractor...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.attendanceSearchQuery.value = "" }) {
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
                            onClick = { viewModel.attendanceFilterType.value = filter },
                            label = { Text(filter, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                            modifier = Modifier.testTag("filter_chip_$filter")
                        )
                    }

                    // Contractor filter dropdown chip
                    item {
                        Box {
                            FilterChip(
                                selected = filterContractor != null,
                                onClick = { showContractorMenu = true },
                                label = {
                                    Text(
                                        text = filterContractor ?: "Contractor",
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
                                    text = { Text("All Contractors") },
                                    onClick = {
                                        viewModel.attendanceFilterContractor.value = null
                                        showContractorMenu = false
                                    }
                                )
                                contractors.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c.name) },
                                        onClick = {
                                            viewModel.attendanceFilterContractor.value = c.name
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
                                }
                            )
                            DropdownMenu(
                                expanded = showDeptMenu,
                                onDismissRequest = { showDeptMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Departments") },
                                    onClick = {
                                        viewModel.attendanceFilterDepartment.value = null
                                        showDeptMenu = false
                                    }
                                )
                                departments.forEach { dept ->
                                    DropdownMenuItem(
                                        text = { Text(dept) },
                                        onClick = {
                                            viewModel.attendanceFilterDepartment.value = dept
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
                        onTogglePresence = { viewModel.toggleAttendance(item) },
                        onEditDetails = { selectedItemForDialog = item }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp)) // Extra scroll padding for navigation bar
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
                onDismiss = { selectedItemForDialog = null },
                onSave = { isPresent, time, remarks ->
                    viewModel.updateStaffDailyAttendance(item.employee, isPresent, time, remarks)
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
                onDismiss = { selectedItemForDialog = null },
                onSave = { isPresent, dept, role, contractor, unit, shift, remarks ->
                    viewModel.updateLabourDailyAssignment(
                        employee = item.employee,
                        isPresent = isPresent,
                        department = dept,
                        workRole = role,
                        contractor = contractor,
                        unit = unit,
                        shift = shift,
                        remarks = remarks
                    )
                    selectedItemForDialog = null
                }
            )
        }
    }
}

@Composable
fun AttendanceCard(
    item: ManpowerViewModel.EmployeeAttendanceItem,
    onTogglePresence: () -> Unit,
    onEditDetails: () -> Unit
) {
    val emp = item.employee
    val isDebarred = emp.status == EmployeeStatuses.DEBARRED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onEditDetails() }
            .testTag("attendance_card_${emp.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isPresent) PresentGreenLight.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isPresent) 2.dp else 1.dp),
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
                        color = IndustrialAmber600,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Attendance time for staff
                if (emp.type == EmployeeTypes.STAFF && item.attendanceTime.isNotBlank()) {
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
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right side: Quick Presence Toggle Button (48dp target)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (item.isPresent) PresentGreen else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onTogglePresence() }
                    .testTag("btn_toggle_presence_${emp.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (item.isPresent) {
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
