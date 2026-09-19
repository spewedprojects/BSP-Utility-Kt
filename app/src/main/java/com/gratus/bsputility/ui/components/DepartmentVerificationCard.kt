package com.gratus.bsputility.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gratus.bsputility.data.models.DepartmentVerification
import com.gratus.bsputility.data.models.Employee
import com.gratus.bsputility.data.models.EmployeeTypes
import com.gratus.bsputility.ui.preview.PreviewData
import com.gratus.bsputility.ui.theme.MyApplicationTheme
import com.gratus.bsputility.ui.theme.PresentGreen
import com.gratus.bsputility.ui.theme.PresentGreenDark
import com.gratus.bsputility.ui.theme.PresentGreenDarkBg
import com.gratus.bsputility.ui.theme.PresentGreenDarkText
import com.gratus.bsputility.ui.theme.PresentGreenLight
import com.gratus.bsputility.ui.viewmodel.ManpowerViewModel

@Composable
fun DepartmentVerificationCard(
    departmentName: String,
    labourList: List<ManpowerViewModel.EmployeeAttendanceItem>,
    verification: DepartmentVerification?,
    staffList: List<Employee>,
    yesterdayCount: Int = 0,
    twoDaysAgoCount: Int = 0,
    day1Label: String = "-1",
    day2Label: String = "-2",
    isToday: Boolean = true,
    onVerify: (staffId: Long?, staffName: String, isVerified: Boolean, remarks: String) -> Unit,
    onMarkSameAsDay: (dayOffset: Int) -> Unit = {},
    onMarkSameAsYesterday: () -> Unit = { onMarkSameAsDay(1) },
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

            // Action: Mark Present Same as -1d or -2d
            if (isToday && (yesterdayCount > 0 || twoDaysAgoCount > 0)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Column {
                            Text(
                                text = "Copy attendance from previous days:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            val historySummary = buildString {
                                if (yesterdayCount > 0) append("$day1Label: $yesterdayCount present")
                                if (yesterdayCount > 0 && twoDaysAgoCount > 0) append(" • ")
                                if (twoDaysAgoCount > 0) append("$day2Label: $twoDaysAgoCount present")
                            }
                            Text(
                                text = historySummary.ifBlank { "No records in previous 2 days" },
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Button -1 (Yesterday)
                            OutlinedButton(
                                onClick = { onMarkSameAsDay(1) },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .testTag("btn_same_as_day1_$departmentName")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Same as $day1Label" + if (yesterdayCount > 0) " ($yesterdayCount)" else "",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Button -2 (Day Before Yesterday)
                            OutlinedButton(
                                onClick = { onMarkSameAsDay(2) },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp)
                                    .testTag("btn_same_as_day2_$departmentName")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Same as $day2Label" + if (twoDaysAgoCount > 0) " ($twoDaysAgoCount)" else "",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
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
                LabourGroupedList(labourList = labourList)
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

@Preview(name = "Department Verification Cards - States", showBackground = true)
@Composable
fun DepartmentVerificationCards_Preview() {
    val presentLabourers = PreviewData.sampleAttendanceItems.filter { it.isPresent && it.employee.type != EmployeeTypes.STAFF }
    val weldingLabour = presentLabourers.filter { it.effectiveDepartment == "Welding Shop" }
    val laserLabour = presentLabourers.filter { it.effectiveDepartment == "Laser Cutting" }
    val staffList = PreviewData.sampleEmployees.filter { it.type == EmployeeTypes.STAFF }

    MyApplicationTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DepartmentVerificationCard(
                departmentName = "Welding Shop",
                labourList = weldingLabour,
                verification = PreviewData.sampleVerifications[0],
                staffList = staffList,
                yesterdayCount = 3,
                twoDaysAgoCount = 4,
                day1Label = "-1 (Wed)",
                day2Label = "-2 (Tue)",
                isToday = true,
                onVerify = { _, _, _, _ -> },
                onMarkSameAsDay = {}
            )
            DepartmentVerificationCard(
                departmentName = "Laser Cutting",
                labourList = laserLabour,
                verification = null,
                staffList = staffList,
                yesterdayCount = 2,
                twoDaysAgoCount = 1,
                day1Label = "-1 (Wed)",
                day2Label = "-2 (Tue)",
                isToday = true,
                onVerify = { _, _, _, _ -> },
                onMarkSameAsDay = {}
            )
        }
    }
}

@Preview(name = "Department Verification Cards - Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DepartmentVerificationCards_Preview_DarkTheme() {
    val presentLabourers = PreviewData.sampleAttendanceItems.filter { it.isPresent && it.employee.type != EmployeeTypes.STAFF }
    val weldingLabour = presentLabourers.filter { it.effectiveDepartment == "Welding Shop" }
    val staffList = PreviewData.sampleEmployees.filter { it.type == EmployeeTypes.STAFF }

    MyApplicationTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DepartmentVerificationCard(
                departmentName = "Welding Shop",
                labourList = weldingLabour,
                verification = PreviewData.sampleVerifications[0],
                staffList = staffList,
                yesterdayCount = 3,
                twoDaysAgoCount = 4,
                day1Label = "-1 (Wed)",
                day2Label = "-2 (Tue)",
                isToday = true,
                onVerify = { _, _, _, _ -> },
                onMarkSameAsDay = {}
            )
        }
    }
}
