package com.gratus.bsputility.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material3.Icon
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
fun LabourGroupedList(
    labourList: List<ManpowerViewModel.EmployeeAttendanceItem>
) {
    if (labourList.isEmpty()) {
        Text(
            text = "No contract labourers currently marked present for this department.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        )
        return
    }

    val isNight: (ManpowerViewModel.EmployeeAttendanceItem) -> Boolean = { item ->
        val s = item.effectiveShift.lowercase(Locale.getDefault())
        s.contains("night") || s.contains("shift c")
    }

    val dayWorkers = labourList.filter { !isNight(it) }
    val nightWorkers = labourList.filter { isNight(it) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (dayWorkers.isNotEmpty()) {
            if (nightWorkers.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(16.dp).padding(start = 4.dp))
                        Text(
                            text = "DAY SHIFT (${dayWorkers.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            LabourRoleSection(workers = dayWorkers)
        }

        if (nightWorkers.isNotEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Nightlight, contentDescription = null, modifier = Modifier.size(16.dp).padding(start = 4.dp))
                    Text(
                        text = "NIGHT SHIFT (${nightWorkers.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            LabourRoleSection(workers = nightWorkers)
        }
    }
}

@Composable
fun LabourRoleSection(
    workers: List<ManpowerViewModel.EmployeeAttendanceItem>
) {
    val roleGroups = workers.groupBy { it.effectiveWorkRole.ifBlank { "Helper" } }
    val showRoleHeadings = roleGroups.keys.size > 1

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(start = 6.dp)
    ) {
        roleGroups.forEach { (role, roleWorkers) ->
            if (showRoleHeadings) {
                Text(
                    text = "• $role (${roleWorkers.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                )
            }
            roleWorkers.forEachIndexed { idx, l ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${idx + 1}. ${l.employee.name}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (l.employee.empCode.isNotBlank()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(3.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = l.employee.empCode,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
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

@Preview(name = "Labour Grouped List - Preview", showBackground = true)
@Composable
fun LabourGroupedList_Preview() {
    val presentLabourers = PreviewData.sampleAttendanceItems.filter { it.isPresent && it.employee.type != EmployeeTypes.STAFF }
    MyApplicationTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            LabourGroupedList(labourList = presentLabourers)
        }
    }
}
