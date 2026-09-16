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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gratus.bsputility.data.models.DepartmentVerification
import com.gratus.bsputility.data.models.ManpowerSummary
import com.gratus.bsputility.ui.preview.PreviewData
import com.gratus.bsputility.ui.theme.IndustrialAmber500
import com.gratus.bsputility.ui.theme.IndustrialAmber600
import com.gratus.bsputility.ui.theme.IndustrialNavy900
import com.gratus.bsputility.ui.theme.MyApplicationTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import com.gratus.bsputility.data.models.EmployeeTypes
import com.gratus.bsputility.utils.StorageHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.gratus.bsputility.ui.theme.PresentGreen
import com.gratus.bsputility.ui.viewmodel.ManpowerViewModel

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

    ReportsScreenContent(
        summary = summary,
        date = date,
        verifications = verifications,
        attendanceItems = attendanceItems,
        onCopyWhatsAppReport = {
            val reportText = viewModel.generateMorningReportWhatsApp()
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Morning Manpower Report", reportText))
            Toast.makeText(context, "Copied Morning Report to Clipboard!", Toast.LENGTH_SHORT).show()
        },
        onShareWhatsAppReport = {
            val reportText = viewModel.generateMorningReportWhatsApp()
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, reportText)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share Manpower Report"))
        },
        onCopySummaryCsv = {
            val csv = viewModel.generateSummaryCsv()
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val fileName = "BSP_Manpower_Summary_${date}_$timeStamp.csv"
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
    onCopyWhatsAppReport: () -> Unit,
    onShareWhatsAppReport: () -> Unit,
    onCopySummaryCsv: () -> Unit,
    onCopyDetailedCsv: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeDialogCriteria by remember {
        mutableStateOf<Pair<String, List<ManpowerViewModel.EmployeeAttendanceItem>>?>(null)
    }

    val presentWorkers = remember(attendanceItems) {
        attendanceItems.filter { it.isPresent }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. HERO MANPOWER STATS CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = IndustrialNavy900),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        Text(
                            text = "BSP METATECH LLP, CHAKAN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndustrialAmber600,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Surface(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = date,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.5.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "Morning Manpower Summary",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Big Counter Grid (Clickable to show individuals: Issue #18)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricCounter(
                        label = "Total Manpower",
                        value = summary.grandTotalPresent.toString(),
                        highlight = true,
                        onClick = {
                            activeDialogCriteria = "Total Present Manpower" to presentWorkers
                        }
                    )
                    MetricCounter(
                        label = "Staff Present",
                        value = summary.totalStaffPresent.toString(),
                        onClick = {
                            activeDialogCriteria = "Staff Present" to presentWorkers.filter { it.employee.type == EmployeeTypes.STAFF }
                        }
                    )
                    MetricCounter(
                        label = "Contract Labour",
                        value = summary.totalLabourersPresent.toString(),
                        onClick = {
                            activeDialogCriteria = "Contract Labour Present" to presentWorkers.filter { it.employee.type != EmployeeTypes.STAFF }
                        }
                    )
                }
            }
        }

        // --- 2. FAST EXPORT ACTIONS (Submission before 11:00 AM) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Report Submissions (Target < 11:00 AM)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                // Copy WhatsApp Report Button
                Button(
                    onClick = onCopyWhatsAppReport,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_copy_morning_report"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy WhatsApp Morning Report", fontWeight = FontWeight.Bold)
                }

                // Share Intent
                OutlinedButton(
                    onClick = onShareWhatsAppReport,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Report to Management")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCopySummaryCsv,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Summary CSV", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onCopyDetailedCsv,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Detailed CSV", fontSize = 12.sp)
                    }
                }
            }
        }

        // --- 3. CONTRACTOR-WISE BREAKDOWN ---
        BreakdownCard(
            title = "Contractor-wise Labour Count",
            icon = Icons.Default.Business,
            counts = summary.contractorCounts,
            emptyMessage = "No contractor labourers marked present today",
            onItemClick = { contractor ->
                activeDialogCriteria = "Contractor: $contractor" to presentWorkers.filter {
                    it.effectiveContractor.equals(contractor, ignoreCase = true)
                }
            }
        )

        // --- 4. DEPARTMENT-WISE BREAKDOWN (Two-Column Layout: Issue #19) ---
        DepartmentAllocationCard(
            summary = summary,
            onDeptStaffClick = { dept ->
                activeDialogCriteria = "$dept — Staff" to presentWorkers.filter {
                    it.effectiveDepartment.equals(dept, ignoreCase = true) && it.employee.type == EmployeeTypes.STAFF
                }
            },
            onDeptLabourClick = { dept ->
                activeDialogCriteria = "$dept — Contract Labour" to presentWorkers.filter {
                    it.effectiveDepartment.equals(dept, ignoreCase = true) && it.employee.type != EmployeeTypes.STAFF
                }
            },
            onDeptAllClick = { dept ->
                activeDialogCriteria = "$dept — All Workers" to presentWorkers.filter {
                    it.effectiveDepartment.equals(dept, ignoreCase = true)
                }
            }
        )

        // --- 5. UNIT & SHIFT BREAKDOWN ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                BreakdownCard(
                    title = "Unit-wise",
                    icon = Icons.Default.LocationOn,
                    counts = summary.unitCounts,
                    emptyMessage = "No data",
                    onItemClick = { unit ->
                        activeDialogCriteria = "Unit: $unit" to presentWorkers.filter {
                            it.effectiveUnit.equals(unit, ignoreCase = true)
                        }
                    }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                BreakdownCard(
                    title = "Shift-wise",
                    icon = Icons.Default.Schedule,
                    counts = summary.shiftCounts,
                    emptyMessage = "No data",
                    onItemClick = { shift ->
                        activeDialogCriteria = "Shift: $shift" to presentWorkers.filter {
                            it.effectiveShift.equals(shift, ignoreCase = true)
                        }
                    }
                )
            }
        }

        // --- 6. ROLE BREAKDOWN ---
        BreakdownCard(
            title = "Role / Category-wise Breakdown",
            icon = Icons.Default.Group,
            counts = summary.roleCounts,
            emptyMessage = "No role breakdown available",
            onItemClick = { role ->
                activeDialogCriteria = "Role: $role" to presentWorkers.filter {
                    it.effectiveWorkRole.equals(role, ignoreCase = true)
                }
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

@Composable
fun MetricCounter(
    label: String,
    value: String,
    highlight: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = if (onClick != null) {
            Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable { onClick() }
                .padding(4.dp)
        } else {
            Modifier.padding(4.dp)
        }
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (highlight) IndustrialAmber500 else Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = if (highlight) 24.sp else 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}

@Composable
fun BreakdownCard(
    title: String,
    icon: ImageVector,
    counts: Map<String, Int>,
    emptyMessage: String,
    modifier: Modifier = Modifier,
    onItemClick: ((String) -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (counts.isEmpty()) {
                Text(
                    text = emptyMessage,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                )
            } else {
                counts.forEach { (key, count) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (onItemClick != null) Modifier.clip(RoundedCornerShape(4.dp)).clickable { onItemClick(key) } else Modifier
                            )
                            .padding(vertical = 4.dp, horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = key,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = count.toString(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                }
            }
        }
    }
}

@Composable
fun DepartmentAllocationCard(
    summary: ManpowerSummary,
    onDeptStaffClick: (String) -> Unit,
    onDeptLabourClick: (String) -> Unit,
    onDeptAllClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allDepts = remember(summary) {
        (summary.departmentCounts.keys + summary.departmentStaffCounts.keys + summary.departmentLabourCounts.keys)
            .filter { it.isNotBlank() && it != "Unassigned" }
            .distinct()
            .sorted()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Engineering, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Department-wise Allocation", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (allDepts.isEmpty()) {
                Text(
                    text = "No department allocation recorded today",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                )
            } else {
                // Table Header (Issue #19)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Department",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1.6f)
                    )
                    Text(
                        text = "Staff",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(0.8f)
                    )
                    Text(
                        text = "Labour",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(0.8f)
                    )
                    Text(
                        text = "Total",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                allDepts.forEach { dept ->
                    val staffCount = summary.departmentStaffCounts[dept] ?: 0
                    val labourCount = summary.departmentLabourCounts[dept] ?: 0
                    val totalCount = summary.departmentCounts[dept] ?: (staffCount + labourCount)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onDeptAllClick(dept) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dept,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1.6f)
                        )

                        // Staff Column (clickable)
                        Box(
                            modifier = Modifier
                                .weight(0.8f)
                                .clickable { onDeptStaffClick(dept) },
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(3.dp),
                                color = if (staffCount > 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent
                            ) {
                                Text(
                                    text = staffCount.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = if (staffCount > 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (staffCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Labour Column (clickable)
                        Box(
                            modifier = Modifier
                                .weight(0.8f)
                                .clickable { onDeptLabourClick(dept) },
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(3.dp),
                                color = if (labourCount > 0) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f) else Color.Transparent
                            ) {
                                Text(
                                    text = labourCount.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = if (labourCount > 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (labourCount > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Total Column
                        Text(
                            text = totalCount.toString(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(0.8f)
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                }
            }
        }
    }
}

@Composable
fun WorkersListDialog(
    title: String,
    workers: List<ManpowerViewModel.EmployeeAttendanceItem>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${workers.size} Present",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            if (workers.isEmpty()) {
                Text(
                    text = "No workers found for this criteria.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                )
            } else {
                val staffList = workers.filter { it.employee.type == EmployeeTypes.STAFF }
                val labourList = workers.filter { it.employee.type != EmployeeTypes.STAFF }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 440.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (staffList.isNotEmpty()) {
                        if (labourList.isNotEmpty()) {
                            item {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "STAFF (${staffList.size})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        items(staffList) { s ->
                            WorkerDetailItem(item = s)
                        }
                    }

                    if (labourList.isNotEmpty()) {
                        if (staffList.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "CONTRACT LABOUR (${labourList.size})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        val isNight: (ManpowerViewModel.EmployeeAttendanceItem) -> Boolean = {
                            val sh = it.effectiveShift.lowercase(Locale.getDefault())
                            sh.contains("night") || sh.contains("shift c")
                        }
                        val dayLabour = labourList.filter { !isNight(it) }
                        val nightLabour = labourList.filter { isNight(it) }

                        if (dayLabour.isNotEmpty()) {
                            if (nightLabour.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Day Shift (${dayLabour.size})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                            val roleGroups = dayLabour.groupBy { it.effectiveWorkRole.ifBlank { "Helper" } }
                            val showRoles = roleGroups.keys.size > 1
                            roleGroups.forEach { (role, rWorkers) ->
                                if (showRoles) {
                                    item {
                                        Text(
                                            text = "• $role (${rWorkers.size})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                                        )
                                    }
                                }
                                items(rWorkers) { w ->
                                    WorkerDetailItem(item = w)
                                }
                            }
                        }

                        if (nightLabour.isNotEmpty()) {
                            item {
                                Text(
                                    text = "🌙 Night Shift (${nightLabour.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                            val roleGroups = nightLabour.groupBy { it.effectiveWorkRole.ifBlank { "Helper" } }
                            val showRoles = roleGroups.keys.size > 1
                            roleGroups.forEach { (role, rWorkers) ->
                                if (showRoles) {
                                    item {
                                        Text(
                                            text = "• $role (${rWorkers.size})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                                        )
                                    }
                                }
                                items(rWorkers) { w ->
                                    WorkerDetailItem(item = w)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun WorkerDetailItem(item: ManpowerViewModel.EmployeeAttendanceItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.employee.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = if (item.employee.type == EmployeeTypes.STAFF) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (item.employee.type == EmployeeTypes.STAFF) "Staff" else item.effectiveWorkRole.ifBlank { "Labour" },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.employee.type == EmployeeTypes.STAFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                val subText = if (item.employee.type == EmployeeTypes.STAFF) {
                    item.employee.designation.ifBlank { item.effectiveDepartment }
                } else {
                    "${item.effectiveDepartment} • ${item.effectiveContractor.ifBlank { "Direct" }}"
                }
                Text(
                    text = subText,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = item.effectiveUnit.ifBlank { "Unit I" },
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = item.effectiveShift.ifBlank { "Shift A" },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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

@Preview(name = "Reports Breakdown Cards - Various Types", showBackground = true)
@Composable
fun ReportsBreakdownCards_Preview() {
    MyApplicationTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Populated Breakdown
            BreakdownCard(
                title = "Contractor-wise Labour Count",
                icon = Icons.Default.Business,
                counts = mapOf("Om Sai Enterprises" to 12, "Shree Ganesh Manpower" to 8),
                emptyMessage = "No data"
            )

            // Empty Breakdown
            BreakdownCard(
                title = "Contractor-wise Labour Count (Empty)",
                icon = Icons.Default.Business,
                counts = emptyMap(),
                emptyMessage = "No contractor labourers marked present today"
            )
        }
    }
}
