package com.gratus.bsputility.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gratus.bsputility.data.models.Employee
import com.gratus.bsputility.ui.theme.PresentGreen
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AttendanceStaffDialog(
    employee: Employee,
    currentPresence: Boolean,
    currentTime: String,
    currentRemarks: String,
    isFutureDate: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (isPresent: Boolean, attendanceTime: String, remarks: String) -> Unit
) {
    var isPresent by remember { mutableStateOf(currentPresence) }
    var timeText by remember {
        mutableStateOf(
            if (currentTime.isNotBlank()) currentTime
            else SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        )
    }
    var remarks by remember { mutableStateOf(currentRemarks) }

    val presetTimes = listOf("08:30 AM", "08:45 AM", "09:00 AM", "09:15 AM", "09:30 AM")

    val customFieldsMap = remember(employee.customFieldsJson) {
        val map = mutableMapOf<String, String>()
        try {
            if (employee.customFieldsJson.isNotBlank()) {
                val json = JSONObject(employee.customFieldsJson)
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Staff Attendance",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
                Text(
                    text = "${employee.name} (${employee.designation.ifBlank { employee.permanentDepartment }})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Presence Switch
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isPresent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Today's Attendance",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isFutureDate) "Future date: Marking disabled" else if (isPresent) "Marked PRESENT" else "Marked ABSENT",
                                color = if (isFutureDate) MaterialTheme.colorScheme.error else if (isPresent) PresentGreen else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = if (isFutureDate) false else isPresent,
                            onCheckedChange = { if (!isFutureDate) isPresent = it },
                            enabled = !isFutureDate,
                            modifier = Modifier.testTag("switch_presence_staff"),
                            colors = SwitchDefaults.colors(checkedThumbColor = PresentGreen)
                        )
                    }
                }

                if (isPresent) {
                    // Time input
                    OutlinedTextField(
                        value = timeText,
                        onValueChange = { timeText = it },
                        label = { Text("Attendance Time (Hours : Minutes AM/PM)") },
                        leadingIcon = {
                            Icon(Icons.Default.AccessTime, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_staff_time")
                    )

                    // Quick Time Chips
                    Text(
                        text = "Quick Presets:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        presetTimes.take(3).forEach { t ->
                            FilterChip(
                                selected = timeText == t,
                                onClick = { timeText = t },
                                label = { Text(t, fontSize = 11.sp) }
                            )
                        }
                        FilterChip(
                            selected = false,
                            onClick = {
                                timeText = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                            },
                            label = { Text("Now", fontSize = 11.sp) }
                        )
                    }
                }

                if (customFieldsMap.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Staff Custom Fields",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            customFieldsMap.forEach { (k, v) ->
                                Text(
                                    text = "• $k: $v",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Daily Remarks
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Daily Remarks (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_staff_remarks"),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalPresent = if (isFutureDate) false else isPresent
                    onSave(finalPresent, if (finalPresent) timeText else "", remarks)
                },
                modifier = Modifier.testTag("btn_save_staff_attendance")
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save Record")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
