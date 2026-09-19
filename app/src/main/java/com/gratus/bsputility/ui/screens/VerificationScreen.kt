package com.gratus.bsputility.ui.screens

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Badge
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gratus.bsputility.data.models.ConfigItem
import com.gratus.bsputility.data.models.DepartmentVerification
import com.gratus.bsputility.data.models.Employee
import com.gratus.bsputility.data.models.EmployeeTypes
import com.gratus.bsputility.ui.components.DepartmentVerificationCard
import com.gratus.bsputility.ui.preview.PreviewData
import com.gratus.bsputility.ui.theme.MyApplicationTheme
import com.gratus.bsputility.ui.theme.PresentGreen
import com.gratus.bsputility.ui.viewmodel.ManpowerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VerificationScreen(
    viewModel: ManpowerViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.dailyAttendanceItems.collectAsStateWithLifecycle()
    val allEmployees by viewModel.allEmployees.collectAsStateWithLifecycle()
    val verifications by viewModel.verificationRecords.collectAsStateWithLifecycle()
    val configItems by viewModel.allConfigItems.collectAsStateWithLifecycle()
    val yesterdayDeptCounts by viewModel.yesterdayDepartmentPresentCounts.collectAsStateWithLifecycle()
    val twoDaysAgoDeptCounts by viewModel.twoDaysAgoDepartmentPresentCounts.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()

    val isToday = remember(selectedDate) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        selectedDate == today
    }

    val day1Label = remember(selectedDate) {
        val d = viewModel.getDayOfWeekAbbreviation(selectedDate, 1)
        if (d.isNotBlank()) "-1 ($d)" else "-1"
    }
    val day2Label = remember(selectedDate) {
        val d = viewModel.getDayOfWeekAbbreviation(selectedDate, 2)
        if (d.isNotBlank()) "-2 ($d)" else "-2"
    }

    // Staff list for selecting verifier
    val staffList = remember(allEmployees) {
        allEmployees.filter { it.type == EmployeeTypes.STAFF }
    }

    // List of departments with present contract labourers today
    val presentLabourers = remember(items) {
        items.filter { it.isPresent && it.employee.type != EmployeeTypes.STAFF }
    }

    val deptMap = remember(presentLabourers) {
        presentLabourers.groupBy { it.effectiveDepartment }
    }

    // Departments that have active workers today OR active workers yesterday (-1d) OR day before yesterday (-2d)
    val allDepts = remember(deptMap, yesterdayDeptCounts, twoDaysAgoDeptCounts) {
        val todayDepts = deptMap.keys.filter { it.isNotBlank() && it != "Unassigned" && (deptMap[it]?.isNotEmpty() == true) }
        val yesterdayDepts = yesterdayDeptCounts.keys.filter { it.isNotBlank() && it != "Unassigned" && (yesterdayDeptCounts[it] ?: 0) > 0 }
        val twoDaysAgoDepts = twoDaysAgoDeptCounts.keys.filter { it.isNotBlank() && it != "Unassigned" && (twoDaysAgoDeptCounts[it] ?: 0) > 0 }
        (todayDepts + yesterdayDepts + twoDaysAgoDepts).distinct().sorted()
    }

    val verifMap = remember(verifications) {
        verifications.associateBy { it.departmentName }
    }

    // Verified count
    val verifiedCount = remember(allDepts, verifMap) {
        allDepts.count { verifMap[it]?.isVerified == true }
    }

    VerificationScreenContent(
        allDepts = allDepts,
        deptMap = deptMap,
        verifMap = verifMap,
        staffList = staffList,
        verifiedCount = verifiedCount,
        yesterdayDeptCounts = yesterdayDeptCounts,
        twoDaysAgoDeptCounts = twoDaysAgoDeptCounts,
        day1Label = day1Label,
        day2Label = day2Label,
        isToday = isToday,
        items = items,
        configItems = configItems,
        onVerify = { deptName, staffId, staffName, isVerified, remarks ->
            viewModel.verifyDepartment(deptName, staffId, staffName, isVerified, remarks)
        },
        onMarkSameAsDay = { deptName, dayOffset ->
            viewModel.markDepartmentSameAsDay(deptName, dayOffset)
        },
        modifier = modifier
    )
}

@Composable
fun VerificationScreenContent(
    allDepts: List<String>,
    deptMap: Map<String, List<ManpowerViewModel.EmployeeAttendanceItem>>,
    verifMap: Map<String, DepartmentVerification>,
    staffList: List<Employee>,
    verifiedCount: Int,
    yesterdayDeptCounts: Map<String, Int> = emptyMap(),
    twoDaysAgoDeptCounts: Map<String, Int> = emptyMap(),
    day1Label: String = "-1",
    day2Label: String = "-2",
    isToday: Boolean = true,
    items: List<ManpowerViewModel.EmployeeAttendanceItem> = emptyList(),
    configItems: List<ConfigItem> = emptyList(),
    onVerify: (deptName: String, staffId: Long?, staffName: String, isVerified: Boolean, remarks: String) -> Unit,
    onMarkSameAsDay: (deptName: String, dayOffset: Int) -> Unit = { _, _ -> },
    onMarkSameAsYesterday: (deptName: String) -> Unit = { onMarkSameAsDay(it, 1) },
    modifier: Modifier = Modifier
) {
    var selectedUnit by remember { mutableStateOf<String?>(null) }

    val configuredUnits = remember(configItems) {
        configItems.filter { it.category == "UNIT" }.map { it.name }
    }
    val unitsFromAttendance = remember(items) {
        items.map { it.effectiveUnit }.filter { it.isNotBlank() }.distinct()
    }
    val availableUnits = remember(configuredUnits, unitsFromAttendance) {
        val base = if (configuredUnits.isNotEmpty()) configuredUnits else listOf("Unit I", "Unit II", "Unit III")
        (base + unitsFromAttendance).distinct()
    }

    val scopedLabourers = remember(items, selectedUnit, deptMap) {
        if (selectedUnit == null) {
            items.filter { it.isPresent && it.employee.type != EmployeeTypes.STAFF }
        } else {
            items.filter { it.isPresent && it.employee.type != EmployeeTypes.STAFF && it.effectiveUnit.equals(selectedUnit, ignoreCase = true) }
        }
    }

    val effectiveDeptMap = remember(scopedLabourers, selectedUnit, deptMap) {
        if (selectedUnit == null) deptMap
        else scopedLabourers.groupBy { it.effectiveDepartment }
    }

    val effectiveDepts = remember(effectiveDeptMap, yesterdayDeptCounts, twoDaysAgoDeptCounts, selectedUnit, allDepts) {
        if (selectedUnit == null) allDepts
        else {
            effectiveDeptMap.keys.filter { it.isNotBlank() && it != "Unassigned" && (effectiveDeptMap[it]?.isNotEmpty() == true) }.sorted()
        }
    }

    val effectiveVerifiedCount = remember(effectiveDepts, verifMap) {
        effectiveDepts.count { verifMap[it]?.isVerified == true }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- 1. VERIFICATION HEADER & PROGRESS ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (selectedUnit == null) "Department Verification" else "Verification ($selectedUnit)",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Badge(
                                containerColor = if (effectiveVerifiedCount == effectiveDepts.size && effectiveDepts.isNotEmpty()) PresentGreen else MaterialTheme.colorScheme.secondary,
                                contentColor = if (effectiveVerifiedCount == effectiveDepts.size && effectiveDepts.isNotEmpty()) Color.White else MaterialTheme.colorScheme.onSecondary
                            ) {
                                Text(
                                    text = "$effectiveVerifiedCount / ${effectiveDepts.size}",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Walk to each department head and confirm daily labour list before 11:00 AM",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { if (effectiveDepts.isEmpty()) 0f else effectiveVerifiedCount.toFloat() / effectiveDepts.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = PresentGreen,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Unit Selector Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        FilterChip(
                            selected = selectedUnit == null,
                            onClick = { selectedUnit = null },
                            label = { Text("All Units", fontWeight = if (selectedUnit == null) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag("verif_unit_chip_all")
                        )
                    }
                    items(availableUnits) { unit ->
                        FilterChip(
                            selected = selectedUnit == unit,
                            onClick = { selectedUnit = if (selectedUnit == unit) null else unit },
                            label = { Text(unit, fontWeight = if (selectedUnit == unit) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag("verif_unit_chip_$unit")
                        )
                    }
                }
            }
        }

        // --- 2. DEPARTMENT VERIFICATION LIST ---
        if (effectiveDepts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "No Active Departments",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (selectedUnit == null) {
                            "No departments have active workers on this date. Allocate workers or mark attendance to verify manpower."
                        } else {
                            "No departments have active workers allocated to $selectedUnit on this date."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(effectiveDepts) { deptName ->
                    val labourInDept = effectiveDeptMap[deptName] ?: emptyList()
                    val verif = verifMap[deptName]
                    val yCount = yesterdayDeptCounts[deptName] ?: 0
                    val y2Count = twoDaysAgoDeptCounts[deptName] ?: 0

                    DepartmentVerificationCard(
                        departmentName = deptName,
                        labourList = labourInDept,
                        verification = verif,
                        staffList = staffList,
                        yesterdayCount = yCount,
                        twoDaysAgoCount = y2Count,
                        day1Label = day1Label,
                        day2Label = day2Label,
                        isToday = isToday,
                        onVerify = { staffId, staffName, isVerified, remarks ->
                            onVerify(deptName, staffId, staffName, isVerified, remarks)
                        },
                        onMarkSameAsDay = { offset ->
                            onMarkSameAsDay(deptName, offset)
                        },
                        onMarkSameAsYesterday = {
                            onMarkSameAsDay(deptName, 1)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

// ==========================================
// PREVIEWS - ALL STATES
// ==========================================

@Preview(name = "Verification Screen - Mixed Progress (50%)", showBackground = true)
@Composable
fun VerificationScreenPreview_MixedProgress() {
    val presentLabourers = PreviewData.sampleAttendanceItems.filter { it.isPresent && it.employee.type != EmployeeTypes.STAFF }
    val deptMap = presentLabourers.groupBy { it.effectiveDepartment }
    val verifMap = PreviewData.sampleVerifications.associateBy { it.departmentName }
    val depts = listOf("Fabrication", "Laser Cutting", "Press Shop", "Welding Shop")
    val staffList = PreviewData.sampleEmployees.filter { it.type == EmployeeTypes.STAFF }

    MyApplicationTheme(darkTheme = false) {
        VerificationScreenContent(
            allDepts = depts,
            deptMap = deptMap,
            verifMap = verifMap,
            staffList = staffList,
            verifiedCount = 2,
            yesterdayDeptCounts = mapOf("Welding Shop" to 3, "Fabrication" to 2, "Press Shop" to 4, "Laser Cutting" to 2),
            twoDaysAgoDeptCounts = mapOf("Welding Shop" to 4, "Fabrication" to 2, "Press Shop" to 3, "Laser Cutting" to 1),
            day1Label = "-1 (Wed)",
            day2Label = "-2 (Tue)",
            isToday = true,
            onVerify = { _, _, _, _, _ -> },
            onMarkSameAsDay = { _, _ -> }
        )
    }
}

@Preview(name = "Verification Screen - Fully Verified (100%)", showBackground = true)
@Composable
fun VerificationScreenPreview_FullyVerified() {
    val presentLabourers = PreviewData.sampleAttendanceItems.filter { it.isPresent && it.employee.type != EmployeeTypes.STAFF }
    val deptMap = presentLabourers.groupBy { it.effectiveDepartment }
    val depts = listOf("Fabrication", "Laser Cutting", "Press Shop", "Welding Shop")
    val verifMap = depts.associateWith { d ->
        DepartmentVerification(
            departmentName = d,
            isVerified = true,
            verifiedByStaffName = "Rahul Kulkarni",
            verifiedAtTime = "10:15 AM",
            date = "2026-09-17"
        )
    }
    val staffList = PreviewData.sampleEmployees.filter { it.type == EmployeeTypes.STAFF }

    MyApplicationTheme(darkTheme = false) {
        VerificationScreenContent(
            allDepts = depts,
            deptMap = deptMap,
            verifMap = verifMap,
            staffList = staffList,
            verifiedCount = 4,
            yesterdayDeptCounts = mapOf("Welding Shop" to 3, "Laser Cutting" to 2),
            twoDaysAgoDeptCounts = mapOf("Welding Shop" to 4, "Laser Cutting" to 1),
            day1Label = "-1 (Wed)",
            day2Label = "-2 (Tue)",
            isToday = false,
            onVerify = { _, _, _, _, _ -> },
            onMarkSameAsDay = { _, _ -> }
        )
    }
}

@Preview(name = "Verification Screen - All Pending (0%)", showBackground = true)
@Composable
fun VerificationScreenPreview_AllPending() {
    val presentLabourers = PreviewData.sampleAttendanceItems.filter { it.isPresent && it.employee.type != EmployeeTypes.STAFF }
    val deptMap = presentLabourers.groupBy { it.effectiveDepartment }
    val depts = listOf("Fabrication", "Laser Cutting", "Press Shop", "Welding Shop")
    val staffList = PreviewData.sampleEmployees.filter { it.type == EmployeeTypes.STAFF }

    MyApplicationTheme(darkTheme = false) {
        VerificationScreenContent(
            allDepts = depts,
            deptMap = deptMap,
            verifMap = emptyMap(),
            staffList = staffList,
            verifiedCount = 0,
            yesterdayDeptCounts = mapOf("Welding Shop" to 3, "Fabrication" to 2, "Press Shop" to 4, "Laser Cutting" to 2),
            twoDaysAgoDeptCounts = mapOf("Welding Shop" to 4, "Fabrication" to 2, "Press Shop" to 3, "Laser Cutting" to 1),
            day1Label = "-1 (Wed)",
            day2Label = "-2 (Tue)",
            isToday = true,
            onVerify = { _, _, _, _, _ -> },
            onMarkSameAsDay = { _, _ -> }
        )
    }
}

@Preview(name = "Verification Screen - Empty State (No Active Depts)", showBackground = true)
@Composable
fun VerificationScreenPreview_Empty() {
    val staffList = PreviewData.sampleEmployees.filter { it.type == EmployeeTypes.STAFF }

    MyApplicationTheme(darkTheme = false) {
        VerificationScreenContent(
            allDepts = emptyList(),
            deptMap = emptyMap(),
            verifMap = emptyMap(),
            staffList = staffList,
            verifiedCount = 0,
            yesterdayDeptCounts = emptyMap(),
            twoDaysAgoDeptCounts = emptyMap(),
            day1Label = "-1 (Wed)",
            day2Label = "-2 (Tue)",
            isToday = true,
            onVerify = { _, _, _, _, _ -> },
            onMarkSameAsDay = { _, _ -> }
        )
    }
}

@Preview(name = "Verification Screen - Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun VerificationScreenPreview_DarkTheme() {
    val presentLabourers = PreviewData.sampleAttendanceItems.filter { it.isPresent && it.employee.type != EmployeeTypes.STAFF }
    val deptMap = presentLabourers.groupBy { it.effectiveDepartment }
    val verifMap = PreviewData.sampleVerifications.associateBy { it.departmentName }
    val depts = listOf("Fabrication", "Laser Cutting", "Press Shop", "Welding Shop")
    val staffList = PreviewData.sampleEmployees.filter { it.type == EmployeeTypes.STAFF }

    MyApplicationTheme(darkTheme = true) {
        VerificationScreenContent(
            allDepts = depts,
            deptMap = deptMap,
            verifMap = verifMap,
            staffList = staffList,
            verifiedCount = 2,
            yesterdayDeptCounts = mapOf("Welding Shop" to 3, "Fabrication" to 2, "Press Shop" to 4, "Laser Cutting" to 2),
            twoDaysAgoDeptCounts = mapOf("Welding Shop" to 4, "Fabrication" to 2, "Press Shop" to 3, "Laser Cutting" to 1),
            day1Label = "-1 (Wed)",
            day2Label = "-2 (Tue)",
            isToday = true,
            onVerify = { _, _, _, _, _ -> },
            onMarkSameAsDay = { _, _ -> }
        )
    }
}
