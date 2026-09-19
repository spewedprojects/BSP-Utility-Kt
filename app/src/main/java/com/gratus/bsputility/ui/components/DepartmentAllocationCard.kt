package com.gratus.bsputility.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gratus.bsputility.data.models.ManpowerSummary
import com.gratus.bsputility.ui.preview.PreviewData
import com.gratus.bsputility.ui.theme.MyApplicationTheme

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
                // Table Header
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

@Preview(name = "Department Allocation Card - Preview", showBackground = true)
@Composable
fun DepartmentAllocationCard_Preview() {
    MyApplicationTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            DepartmentAllocationCard(
                summary = PreviewData.sampleSummary,
                onDeptStaffClick = {},
                onDeptLabourClick = {},
                onDeptAllClick = {}
            )
        }
    }
}
