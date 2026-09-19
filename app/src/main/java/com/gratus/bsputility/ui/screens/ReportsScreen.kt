package com.gratus.bsputility.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gratus.bsputility.data.models.ConfigItem
import com.gratus.bsputility.data.models.DepartmentVerification
import com.gratus.bsputility.data.models.EmployeeTypes
import com.gratus.bsputility.data.models.ManpowerSummary
import com.gratus.bsputility.ui.components.BreakdownCard
import com.gratus.bsputility.ui.components.DepartmentAllocationCard
import com.gratus.bsputility.ui.components.MetricCounter
import com.gratus.bsputility.ui.components.WorkersListDialog
import com.gratus.bsputility.ui.preview.PreviewData
import com.gratus.bsputility.ui.theme.IndustrialAmber500
import com.gratus.bsputility.ui.theme.IndustrialAmber600
import com.gratus.bsputility.ui.theme.IndustrialNavy900
import com.gratus.bsputility.ui.theme.MyApplicationTheme
import com.gratus.bsputility.ui.theme.PresentGreen
import com.gratus.bsputility.ui.viewmodel.ManpowerViewModel
import com.gratus.bsputility.utils.StorageHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: ManpowerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val summary by viewModel.manpowerSummary.collectAsStateWithLifecycle()
    val date by viewModel.selectedDate.collectAsStateWithLifecycle()
    val verifications by viewModel.verificationRecords.collectAsStateWithLifecycle()
    val attendanceItems by viewModel.dailyAttendanceItems.collectAsStateWithLifecycle()
    val configItems by viewModel.allConfigItems.collectAsStateWithLifecycle()

    ReportsScreenContent(
        summary = summary,
        date = date,
        verifications = verifications,
        attendanceItems = attendanceItems,
        configItems = configItems,
        onCopyWhatsAppReport = { targetUnit ->
            val reportText = viewModel.generateMorningReportWhatsApp(targetUnit)
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Morning Manpower Report", reportText))
            val msg = if (targetUnit.isNullOrBlank() || targetUnit == "All") "Copied Morning Report to Clipboard!" else "Copied [$targetUnit] Report to Clipboard!"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        },
        onShareWhatsAppReport = { targetUnit ->
            val reportText = viewModel.generateMorningReportWhatsApp(targetUnit)
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, reportText)
                type = "text/plain"
            }
            val title = if (targetUnit.isNullOrBlank() || targetUnit == "All") "Share Manpower Report" else "Share [$targetUnit] Manpower Report"
            context.startActivity(Intent.createChooser(sendIntent, title))
        },
        onCopySummaryCsv = { targetUnit ->
            val csv = viewModel.generateSummaryCsv(targetUnit)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val unitSuffix = if (targetUnit.isNullOrBlank() || targetUnit == "All") "" else "_${targetUnit.replace(" ", "_")}"
            val fileName = "BSP_Manpower_Summary_${date}${unitSuffix}_$timeStamp.csv"
            val saved = StorageHelper.exportToDocuments(context, fileName, "text/csv", csv)
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Summary CSV", csv))
            if (saved.success) {
                Toast.makeText(context, "Saved to Documents/BSPManpower/$fileName & copied to clipboard", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Summary CSV copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        },
        onCopyDetailedCsv = {
            val csv = viewModel.generateDetailedCsv()
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val fileName = "BSP_Attendance_Detailed_${date}_$timeStamp.csv"
            val saved = StorageHelper.exportToDocuments(context, fileName, "text/csv", csv)
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Detailed Attendance CSV", csv))
            if (saved.success) {
                Toast.makeText(context, "Saved to Documents/BSPManpower/$fileName & copied to clipboard", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Detailed CSV copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        },
        modifier = modifier
    )
}

@Composable
fun ReportsScreenContent(
    summary: ManpowerSummary,
    date: String,
    verifications: List<DepartmentVerification>,
    attendanceItems: List<ManpowerViewModel.EmployeeAttendanceItem> = emptyList(),
    configItems: List<ConfigItem> = emptyList(),
    onCopyWhatsAppReport: (String?) -> Unit,
    onShareWhatsAppReport: (String?) -> Unit,
    onCopySummaryCsv: (String?) -> Unit,
    onCopyDetailedCsv: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedUnit by remember { mutableStateOf<String?>(null) }
    var activeDialogCriteria by remember {
        mutableStateOf<Pair<String, List<ManpowerViewModel.EmployeeAttendanceItem>>?>(null)
    }

    val configuredUnits = remember(configItems) {
        configItems.filter { it.category == "UNIT" }.map { it.name }
    }
    val unitsFromAttendance = remember(attendanceItems) {
        attendanceItems.map { it.effectiveUnit }.filter { it.isNotBlank() }.distinct()
    }
    val availableUnits = remember(configuredUnits, unitsFromAttendance) {
        val base = if (configuredUnits.isNotEmpty()) configuredUnits else listOf("Unit I", "Unit II", "Unit III")
        (base + unitsFromAttendance).distinct()
    }

    val scopedAttendanceItems = remember(attendanceItems, selectedUnit) {
        if (selectedUnit == null) attendanceItems
        else attendanceItems.filter { it.effectiveUnit.equals(selectedUnit, ignoreCase = true) }
    }

    val presentWorkers = remember(scopedAttendanceItems) {
        scopedAttendanceItems.filter { it.isPresent }
    }

    val effectiveSummary = remember(summary, selectedUnit, scopedAttendanceItems) {
        if (selectedUnit == null) summary
        else {
            val presentRecords = scopedAttendanceItems.filter { it.isPresent }
            var staffCount = 0
            var labourCount = 0
            var housekeepingCount = 0
            val contractorMap = mutableMapOf<String, Int>()
            val deptMap = mutableMapOf<String, Int>()
            val deptStaffMap = mutableMapOf<String, Int>()
            val deptLabourMap = mutableMapOf<String, Int>()
            val unitMap = mutableMapOf<String, Int>()
            val roleMap = mutableMapOf<String, Int>()
            val shiftMap = mutableMapOf<String, Int>()

            presentRecords.forEach { item ->
                when (item.employee.type) {
                    EmployeeTypes.STAFF -> {
                        staffCount++
                        val d = item.effectiveDepartment.ifBlank { "Unassigned" }
                        deptStaffMap[d] = (deptStaffMap[d] ?: 0) + 1
                    }
                    EmployeeTypes.HOUSEKEEPING -> {
                        housekeepingCount++
                        labourCount++
                        val c = item.effectiveContractor.ifBlank { "Direct" }
                        contractorMap[c] = (contractorMap[c] ?: 0) + 1
                        val d = item.effectiveDepartment.ifBlank { "Unassigned" }
                        deptLabourMap[d] = (deptLabourMap[d] ?: 0) + 1
                    }
                    else -> {
                        labourCount++
                        val c = item.effectiveContractor.ifBlank { "Direct" }
                        contractorMap[c] = (contractorMap[c] ?: 0) + 1
                        val d = item.effectiveDepartment.ifBlank { "Unassigned" }
                        deptLabourMap[d] = (deptLabourMap[d] ?: 0) + 1
                    }
                }
                val d = item.effectiveDepartment.ifBlank { "Unassigned" }
                deptMap[d] = (deptMap[d] ?: 0) + 1

                val u = item.effectiveUnit.ifBlank { "Unit I" }
                unitMap[u] = (unitMap[u] ?: 0) + 1

                val r = item.effectiveWorkRole.ifBlank { "General" }
                roleMap[r] = (roleMap[r] ?: 0) + 1

                val s = item.effectiveShift.ifBlank { "Shift A" }
                shiftMap[s] = (shiftMap[s] ?: 0) + 1
            }

            summary.copy(
                totalStaffPresent = staffCount,
                totalLabourersPresent = labourCount,
                totalHousekeepingPresent = housekeepingCount,
                grandTotalPresent = staffCount + labourCount,
                contractorCounts = contractorMap,
                departmentCounts = deptMap,
                departmentStaffCounts = deptStaffMap,
                departmentLabourCounts = deptLabourMap,
                unitCounts = unitMap,
                roleCounts = roleMap,
                shiftCounts = shiftMap
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. HERO SUMMARY CARD ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = IndustrialNavy900,
            shadowElevation = 3.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (selectedUnit == null) "PLANT-WIDE MANPOWER" else "MANPOWER: $selectedUnit",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndustrialAmber600,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Headcount Summary",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                    Text(
                        text = date,
                        fontSize = 12.sp,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricCounter(
                        label = "Grand Total",
                        value = "${effectiveSummary.grandTotalPresent}",
                        highlight = true,
                        onClick = {
                            activeDialogCriteria = Pair(
                                if (selectedUnit == null) "All Present Workers" else "Present Workers ($selectedUnit)",
                                presentWorkers
                            )
                        }
                    )
                    MetricCounter(
                        label = "Staff",
                        value = "${effectiveSummary.totalStaffPresent}",
                        onClick = {
                            activeDialogCriteria = Pair(
                                if (selectedUnit == null) "Present Staff Members" else "Present Staff ($selectedUnit)",
                                presentWorkers.filter { it.employee.type == EmployeeTypes.STAFF }
                            )
                        }
                    )
                    MetricCounter(
                        label = "Contract Labour",
                        value = "${effectiveSummary.totalLabourersPresent}",
                        onClick = {
                            activeDialogCriteria = Pair(
                                if (selectedUnit == null) "Present Contract Labourers" else "Present Labourers ($selectedUnit)",
                                presentWorkers.filter { it.employee.type != EmployeeTypes.STAFF }
                            )
                        }
                    )
                    MetricCounter(
                        label = "Verified",
                        value = "${effectiveSummary.verifiedDepartmentsCount}/${effectiveSummary.totalDepartmentsCount}",
                        onClick = null
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Unit Selector Chips inside Hero
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        FilterChip(
                            selected = selectedUnit == null,
                            onClick = { selectedUnit = null },
                            label = { Text("All Units", fontSize = 11.sp, fontWeight = if (selectedUnit == null) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                    items(availableUnits) { unit ->
                        FilterChip(
                            selected = selectedUnit == unit,
                            onClick = { selectedUnit = if (selectedUnit == unit) null else unit },
                            label = { Text(unit, fontSize = 11.sp, fontWeight = if (selectedUnit == unit) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        }

        // --- 2. FAST ACTION BUTTONS: EXPORTS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onCopyWhatsAppReport(selectedUnit) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_copy_whatsapp_report"),
                colors = ButtonDefaults.buttonColors(containerColor = PresentGreen)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = { onShareWhatsAppReport(selectedUnit) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_share_whatsapp_report"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share Report", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onCopySummaryCsv(selectedUnit) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_copy_summary_csv")
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Summary CSV", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onCopyDetailedCsv,
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_copy_detailed_csv")
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Detailed CSV", fontSize = 12.sp)
            }
        }

        // --- 3. DEPARTMENT ALLOCATION TABLE CARD ---
        DepartmentAllocationCard(
            summary = effectiveSummary,
            onDeptStaffClick = { dept ->
                activeDialogCriteria = Pair(
                    "$dept - Staff Members",
                    presentWorkers.filter { it.effectiveDepartment == dept && it.employee.type == EmployeeTypes.STAFF }
                )
            },
            onDeptLabourClick = { dept ->
                activeDialogCriteria = Pair(
                    "$dept - Contract Labourers",
                    presentWorkers.filter { it.effectiveDepartment == dept && it.employee.type != EmployeeTypes.STAFF }
                )
            },
            onDeptAllClick = { dept ->
                activeDialogCriteria = Pair(
                    "$dept - All Personnel",
                    presentWorkers.filter { it.effectiveDepartment == dept }
                )
            }
        )

        // --- 4. CONTRACTOR DISTRIBUTION CARD ---
        BreakdownCard(
            title = "Contractor-wise Labour Count",
            icon = Icons.Default.Business,
            counts = effectiveSummary.contractorCounts,
            emptyMessage = "No contractor labourers marked present today.",
            onItemClick = { contractor ->
                activeDialogCriteria = Pair(
                    "Contractor: $contractor",
                    presentWorkers.filter { it.effectiveContractor.equals(contractor, ignoreCase = true) }
                )
            }
        )

        // --- 5. PLANT UNIT DISTRIBUTION CARD ---
        BreakdownCard(
            title = "Plant Unit Distribution",
            icon = Icons.Default.LocationOn,
            counts = effectiveSummary.unitCounts,
            emptyMessage = "No unit distribution data recorded.",
            onItemClick = { unit ->
                activeDialogCriteria = Pair(
                    "Plant: $unit",
                    presentWorkers.filter { it.effectiveUnit.equals(unit, ignoreCase = true) }
                )
            }
        )

        // --- 6. SHIFT BREAKDOWN CARD ---
        BreakdownCard(
            title = "Shift Allocation",
            icon = Icons.Default.Schedule,
            counts = effectiveSummary.shiftCounts,
            emptyMessage = "No shift data recorded.",
            onItemClick = { shift ->
                activeDialogCriteria = Pair(
                    "Shift: $shift",
                    presentWorkers.filter { it.effectiveShift.equals(shift, ignoreCase = true) }
                )
            }
        )

        // --- 7. VERIFICATION AUDIT TRAIL ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PresentGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Department Head Verifications (${verifications.count { it.isVerified }})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (verifications.none { it.isVerified }) {
                    Text(
                        text = "Department heads have not yet signed off on today's labour list.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic
                    )
                } else {
                    verifications.filter { it.isVerified }.forEach { v ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(v.departmentName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Verified by: ${v.verifiedByStaffName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(v.verifiedAtTime, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PresentGreen)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    activeDialogCriteria?.let { (criteriaTitle, workersList) ->
        WorkersListDialog(
            title = criteriaTitle,
            workers = workersList,
            onDismiss = { activeDialogCriteria = null }
        )
    }
}

// ==========================================
// PREVIEWS - ALL STATES
// ==========================================

@Preview(name = "Reports Screen - Default Populated", showBackground = true)
@Composable
fun ReportsScreenPreview_Default() {
    MyApplicationTheme(darkTheme = false) {
        ReportsScreenContent(
            summary = PreviewData.sampleSummary,
            date = "2026-09-10",
            verifications = PreviewData.sampleVerifications,
            attendanceItems = PreviewData.sampleAttendanceItems,
            onCopyWhatsAppReport = {},
            onShareWhatsAppReport = {},
            onCopySummaryCsv = {},
            onCopyDetailedCsv = {}
        )
    }
}

@Preview(name = "Reports Screen - Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ReportsScreenPreview_DarkTheme() {
    MyApplicationTheme(darkTheme = true) {
        ReportsScreenContent(
            summary = PreviewData.sampleSummary,
            date = "2026-09-10",
            verifications = PreviewData.sampleVerifications,
            attendanceItems = PreviewData.sampleAttendanceItems,
            onCopyWhatsAppReport = {},
            onShareWhatsAppReport = {},
            onCopySummaryCsv = {},
            onCopyDetailedCsv = {}
        )
    }
}

@Preview(name = "Reports Screen - Initial / Empty State", showBackground = true)
@Composable
fun ReportsScreenPreview_Empty() {
    MyApplicationTheme {
        ReportsScreenContent(
            summary = ManpowerSummary(),
            date = "2026-09-10",
            verifications = emptyList(),
            onCopyWhatsAppReport = {},
            onShareWhatsAppReport = {},
            onCopySummaryCsv = {},
            onCopyDetailedCsv = {}
        )
    }
}
