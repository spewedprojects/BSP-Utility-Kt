package com.gratus.bsputility.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gratus.bsputility.data.models.DepartmentVerification
import com.gratus.bsputility.data.models.Employee
import com.gratus.bsputility.data.models.EmployeeTypes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.darkColorScheme
import com.gratus.bsputility.ui.preview.PreviewData
import com.gratus.bsputility.ui.theme.IndustrialAmber600
import com.gratus.bsputility.ui.theme.MyApplicationTheme
import com.gratus.bsputility.ui.theme.PresentGreen
import com.gratus.bsputility.ui.theme.PresentGreenDark
import com.gratus.bsputility.ui.theme.PresentGreenDarkBg
import com.gratus.bsputility.ui.theme.PresentGreenDarkText
import com.gratus.bsputility.ui.theme.PresentGreenLight
import com.gratus.bsputility.ui.viewmodel.ManpowerViewModel

@Composable
fun VerificationScreen(
    viewModel: ManpowerViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.dailyAttendanceItems.collectAsStateWithLifecycle()
    val allEmployees by viewModel.allEmployees.collectAsStateWithLifecycle()
    val verifications by viewModel.verificationRecords.collectAsStateWithLifecycle()
    val configItems by viewModel.allConfigItems.collectAsStateWithLifecycle()

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

    // Only departments that have active/present workers on this date (Issue #4)
    val allDepts = remember(deptMap) {
        deptMap.keys.filter { it.isNotBlank() && it != "Unassigned" && (deptMap[it]?.isNotEmpty() == true) }.sorted()
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
        onVerify = { deptName, staffId, staffName, isVerified, remarks ->
            viewModel.verifyDepartment(deptName, staffId, staffName, isVerified, remarks)
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
    onVerify: (deptName: String, staffId: Long?, staffName: String, isVerified: Boolean, remarks: String) -> Unit,
    modifier: Modifier = Modifier
) {
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
                                text = "Department Verification",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Badge(
                                containerColor = if (verifiedCount == allDepts.size && allDepts.isNotEmpty()) PresentGreen else MaterialTheme.colorScheme.secondary,
                                contentColor = if (verifiedCount == allDepts.size && allDepts.isNotEmpty()) Color.White else MaterialTheme.colorScheme.onSecondary
                            ) {
                                Text(
                                    text = "$verifiedCount / ${allDepts.size}",
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
                    progress = { if (allDepts.isEmpty()) 0f else verifiedCount.toFloat() / allDepts.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = PresentGreen,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // --- 2. DEPARTMENT VERIFICATION LIST ---
        if (allDepts.isEmpty()) {
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
                        text = "No departments have active workers on this date. Allocate workers or mark attendance to verify manpower.",
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
                items(allDepts) { deptName ->
                    val labourInDept = deptMap[deptName] ?: emptyList()
                    val verif = verifMap[deptName]

                    DepartmentVerificationCard(
                        departmentName = deptName,
                        labourList = labourInDept,
                        verification = verif,
                        staffList = staffList,
                        onVerify = { staffId, staffName, isVerified, remarks ->
                            onVerify(deptName, staffId, staffName, isVerified, remarks)
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

@Composable
fun DepartmentVerificationCard(
    departmentName: String,
    labourList: List<ManpowerViewModel.EmployeeAttendanceItem>,
    verification: DepartmentVerification?,
    staffList: List<Employee>,
    onVerify: (staffId: Long?, staffName: String, isVerified: Boolean, remarks: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isVerified = verification?.isVerified == true
    var isExpanded by remember { mutableStateOf(false) }

    var selectedStaffName by remember(verification) {
        mutableStateOf(verification?.verifiedByStaffName ?: "")
    }
    var staffDropdownOpen by remember { mutableStateOf(false) }
    var verifRemarks by remember(verification) {
        mutableStateOf(verification?.remarks ?: "")
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("verification_card_$departmentName"),
        colors = CardDefaults.cardColors(
            containerColor = if (isVerified) PresentGreenLight.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            1.dp,
            if (isVerified) PresentGreen.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Engineering,
                        contentDescription = null,
                        tint = if (isVerified) PresentGreen else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = departmentName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        Text(
                            text = "${labourList.size} Labourers Assigned Today",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val isDark = isSystemInDarkTheme()
                val verifiedBg = if (isDark) PresentGreenDarkBg.copy(alpha = 0.5f) else PresentGreenLight
                val verifiedContent = if (isDark) PresentGreenDarkText else PresentGreenDark

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isVerified) verifiedBg else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (isVerified) verifiedContent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isVerified) "VERIFIED" else "PENDING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isVerified) verifiedContent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expandable Labour List Preview
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { isExpanded = !isExpanded },
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) "Hide Labour List" else "View ${labourList.size} Assigned Labourers",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (labourList.isEmpty()) {
                        Text(
                            text = "No contract labourers currently marked present for this department.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(4.dp)
                        )
                    } else {
                        labourList.forEachIndexed { idx, l ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${idx + 1}. ${l.employee.name}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${l.effectiveWorkRole} • ${l.effectiveContractor}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${l.effectiveUnit} • ${l.effectiveShift}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Verified By Dropdown & Confirmation
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Verified By (Department Head / Staff):",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondary
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { staffDropdownOpen = true }
                        .testTag("dropdown_verifier_$departmentName"),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedStaffName.ifBlank { "Tap to select verifying staff member..." },
                            fontSize = 13.sp,
                            fontWeight = if (selectedStaffName.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedStaffName.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = staffDropdownOpen,
                        onDismissRequest = { staffDropdownOpen = false },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        staffList.forEach { s ->
                            DropdownMenuItem(
                                text = {
                                    Text("${s.name} (${s.designation.ifBlank { s.permanentDepartment }})")
                                },
                                onClick = {
                                    selectedStaffName = s.name
                                    staffDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                // Verification remarks or time stamped info
                if (isVerified && verification != null) {
                    Text(
                        text = "Verified on ${verification.verifiedAtTime} by ${verification.verifiedByStaffName}",
                        fontSize = 11.sp,
                        color = PresentGreen,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isVerified) {
                        OutlinedButton(
                            onClick = {
                                onVerify(null, "", false, "")
                            },
                            modifier = Modifier.testTag("btn_revoke_verif_$departmentName")
                        ) {
                            Text("Revoke", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        Button(
                            onClick = {
                                val verifierObj = staffList.find { it.name == selectedStaffName }
                                onVerify(
                                    verifierObj?.id,
                                    selectedStaffName.ifBlank { "Department In-charge" },
                                    true,
                                    verifRemarks
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PresentGreen),
                            modifier = Modifier.testTag("btn_confirm_verif_$departmentName")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Confirm & Sign Off", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
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
    val depts = listOf("Welding Shop", "Laser Cutting", "Fabrication", "Press Shop")
    val staffList = PreviewData.sampleEmployees.filter { it.type == EmployeeTypes.STAFF }

    MyApplicationTheme(darkTheme = false) {
        VerificationScreenContent(
            allDepts = depts,
            deptMap = deptMap,
            verifMap = verifMap,
            staffList = staffList,
            verifiedCount = 2,
            onVerify = { _, _, _, _, _ -> }
        )
    }
}

@Preview(name = "Verification Screen - Fully Verified (100%)", showBackground = true)
@Composable
fun VerificationScreenPreview_FullyVerified() {
    val presentLabourers = PreviewData.sampleAttendanceItems.filter { it.isPresent && it.employee.type != EmployeeTypes.STAFF }
    val deptMap = presentLabourers.groupBy { it.effectiveDepartment }
    val depts = listOf("Welding Shop", "Laser Cutting", "Fabrication", "Press Shop")
    val verifMap = depts.associateWith { d ->
        DepartmentVerification(
            departmentName = d,
            isVerified = true,
            verifiedByStaffName = "Rahul Kulkarni",
            verifiedAtTime = "10:15 AM",
            date = "2026-09-10"
        )
    }
    val staffList = PreviewData.sampleEmployees.filter { it.type == EmployeeTypes.STAFF }

    MyApplicationTheme {
        VerificationScreenContent(
            allDepts = depts,
            deptMap = deptMap,
            verifMap = verifMap,
            staffList = staffList,
            verifiedCount = 4,
            onVerify = { _, _, _, _, _ -> }
        )
    }
}

@Preview(name = "Verification Screen - All Pending (0%)", showBackground = true)
@Composable
fun VerificationScreenPreview_AllPending() {
    val presentLabourers = PreviewData.sampleAttendanceItems.filter { it.isPresent && it.employee.type != EmployeeTypes.STAFF }
    val deptMap = presentLabourers.groupBy { it.effectiveDepartment }
    val depts = listOf("Welding Shop", "Laser Cutting", "Fabrication", "Press Shop")
    val staffList = PreviewData.sampleEmployees.filter { it.type == EmployeeTypes.STAFF }

    MyApplicationTheme {
        VerificationScreenContent(
            allDepts = depts,
            deptMap = deptMap,
            verifMap = emptyMap(),
            staffList = staffList,
            verifiedCount = 0,
            onVerify = { _, _, _, _, _ -> }
        )
    }
}

@Preview(name = "Verification Screen - Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun VerificationScreenPreview_DarkTheme() {
    val presentLabourers = PreviewData.sampleAttendanceItems.filter { it.isPresent && it.employee.type != EmployeeTypes.STAFF }
    val deptMap = presentLabourers.groupBy { it.effectiveDepartment }
    val verifMap = PreviewData.sampleVerifications.associateBy { it.departmentName }
    val depts = listOf("Welding Shop", "Laser Cutting", "Fabrication", "Press Shop")
    val staffList = PreviewData.sampleEmployees.filter { it.type == EmployeeTypes.STAFF }

    MyApplicationTheme(darkTheme = true) {
        VerificationScreenContent(
            allDepts = depts,
            deptMap = deptMap,
            verifMap = verifMap,
            staffList = staffList,
            verifiedCount = 2,
            onVerify = { _, _, _, _, _ -> }
        )
    }
}

@Preview(name = "Department Verification Cards - Pending vs Verified", showBackground = true)
@Composable
fun DepartmentVerificationCards_Preview() {
    val presentLabourers = PreviewData.sampleAttendanceItems.filter { it.isPresent && it.employee.type != EmployeeTypes.STAFF }
    val deptMap = presentLabourers.groupBy { it.effectiveDepartment }
    val staffList = PreviewData.sampleEmployees.filter { it.type == EmployeeTypes.STAFF }

    MyApplicationTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Verified Card
            DepartmentVerificationCard(
                departmentName = "Welding Shop",
                labourList = deptMap["Welding Shop"] ?: emptyList(),
                verification = PreviewData.sampleVerifications[0],
                staffList = staffList,
                onVerify = { _, _, _, _ -> }
            )

            // 2. Pending Card
            DepartmentVerificationCard(
                departmentName = "Fabrication",
                labourList = deptMap["Fabrication"] ?: emptyList(),
                verification = PreviewData.sampleVerifications[2],
                staffList = staffList,
                onVerify = { _, _, _, _ -> }
            )
        }
    }
}
