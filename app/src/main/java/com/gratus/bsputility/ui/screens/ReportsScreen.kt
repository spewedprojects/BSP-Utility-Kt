package com.gratus.bsputility.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gratus.bsputility.ui.theme.IndustrialAmber600
import com.gratus.bsputility.ui.theme.IndustrialNavy900
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
                    Column {
                        Text(
                            text = "BSP METATECH LLP, CHAKAN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndustrialAmber600,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Morning Manpower Summary",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = date,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Big Counter Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricCounter(label = "Total Manpower", value = summary.grandTotalPresent.toString(), highlight = true)
                    MetricCounter(label = "Staff Present", value = summary.totalStaffPresent.toString())
                    MetricCounter(label = "Contract Labour", value = summary.totalLabourersPresent.toString())
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
                    onClick = {
                        val reportText = viewModel.generateMorningReportWhatsApp()
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Morning Manpower Report", reportText))
                        Toast.makeText(context, "Copied Morning Report to Clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_copy_morning_report"),
                    colors = ButtonDefaults.buttonColors(containerColor = IndustrialAmber600)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy WhatsApp Morning Report", fontWeight = FontWeight.Bold)
                }

                // Share Intent
                OutlinedButton(
                    onClick = {
                        val reportText = viewModel.generateMorningReportWhatsApp()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, reportText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Manpower Report"))
                    },
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
                        onClick = {
                            val csv = viewModel.generateSummaryCsv()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Summary CSV", csv))
                            Toast.makeText(context, "Summary CSV copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Summary CSV", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val csv = viewModel.generateDetailedCsv()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Detailed Attendance CSV", csv))
                            Toast.makeText(context, "Detailed CSV copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
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
            emptyMessage = "No contractor labourers marked present today"
        )

        // --- 4. DEPARTMENT-WISE BREAKDOWN ---
        BreakdownCard(
            title = "Department-wise Allocation",
            icon = Icons.Default.Engineering,
            counts = summary.departmentCounts,
            emptyMessage = "No department allocation recorded today"
        )

        // --- 5. UNIT & SHIFT BREAKDOWN ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                BreakdownCard(
                    title = "Unit-wise",
                    icon = Icons.Default.LocationOn,
                    counts = summary.unitCounts,
                    emptyMessage = "No data"
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                BreakdownCard(
                    title = "Shift-wise",
                    icon = Icons.Default.Schedule,
                    counts = summary.shiftCounts,
                    emptyMessage = "No data"
                )
            }
        }

        // --- 6. ROLE BREAKDOWN ---
        BreakdownCard(
            title = "Role / Category-wise Breakdown",
            icon = Icons.Default.Group,
            counts = summary.roleCounts,
            emptyMessage = "No role breakdown available"
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
}

@Composable
fun MetricCounter(label: String, value: String, highlight: Boolean = false) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (highlight) IndustrialAmber600 else Color.White.copy(alpha = 0.7f),
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
    emptyMessage: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = key,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
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
