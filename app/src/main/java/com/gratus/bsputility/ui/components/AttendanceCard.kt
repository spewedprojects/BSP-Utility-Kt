package com.gratus.bsputility.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.gratus.bsputility.data.models.EmployeeStatuses
import com.gratus.bsputility.data.models.EmployeeTypes
import com.gratus.bsputility.ui.preview.PreviewData
import com.gratus.bsputility.ui.theme.MyApplicationTheme
import com.gratus.bsputility.ui.theme.PresentGreen
import com.gratus.bsputility.ui.theme.PresentGreenLight
import com.gratus.bsputility.ui.theme.StatusDebarred
import com.gratus.bsputility.ui.viewmodel.ManpowerViewModel
import org.json.JSONObject

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
                    if (emp.empCode.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = emp.empCode,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
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

@Preview(name = "Attendance Cards - Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AttendanceCards_Preview_DarkTheme() {
    MyApplicationTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AttendanceCard(
                item = PreviewData.sampleAttendanceItems[3],
                onTogglePresence = {},
                onEditDetails = {}
            )
            AttendanceCard(
                item = PreviewData.sampleAttendanceItems[6],
                onTogglePresence = {},
                onEditDetails = {}
            )
        }
    }
}
