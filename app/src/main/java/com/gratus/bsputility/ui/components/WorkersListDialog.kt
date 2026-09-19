package com.gratus.bsputility.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gratus.bsputility.data.models.EmployeeTypes
import com.gratus.bsputility.ui.preview.PreviewData
import com.gratus.bsputility.ui.theme.MyApplicationTheme
import com.gratus.bsputility.ui.viewmodel.ManpowerViewModel
import java.util.Locale

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

@Preview(name = "Reports Dialog - Workers List", showBackground = true)
@Composable
fun WorkersListDialog_Preview() {
    MyApplicationTheme {
        WorkersListDialog(
            title = "Welding Shop Workers",
            workers = PreviewData.sampleAttendanceItems.filter { it.isPresent },
            onDismiss = {}
        )
    }
}
