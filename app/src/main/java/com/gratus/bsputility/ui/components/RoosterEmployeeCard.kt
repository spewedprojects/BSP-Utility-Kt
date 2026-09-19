package com.gratus.bsputility.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gratus.bsputility.data.models.Employee
import com.gratus.bsputility.data.models.EmployeeTypes
import com.gratus.bsputility.ui.preview.PreviewData
import com.gratus.bsputility.ui.theme.MyApplicationTheme
import org.json.JSONObject

@Composable
fun RoosterEmployeeCard(
    employee: Employee,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .testTag("rooster_card_${employee.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (employee.empCode.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = employee.empCode,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = employee.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    StatusBadge(status = employee.status)
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (employee.type == EmployeeTypes.STAFF) {
                        if (employee.permanentDepartment.isNotBlank()) {
                            Text(
                                text = employee.permanentDepartment,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (employee.designation.isNotBlank()) {
                            Text(
                                text = "• ${employee.designation}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        if (employee.permanentDepartment.isNotBlank()) {
                            Text(
                                text = employee.permanentDepartment,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (employee.defaultWorkRole.isNotBlank()) {
                            val roleText = if (employee.permanentDepartment.isNotBlank()) "• ${employee.defaultWorkRole}" else employee.defaultWorkRole
                            Text(
                                text = roleText,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                if (employee.type != EmployeeTypes.STAFF && employee.contractorName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Contractor: ${employee.contractorName} • ${employee.defaultUnit}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Added: ${employee.dateAdded}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                // Custom fields badges
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
                if (customFieldsMap.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        customFieldsMap.entries.take(3).forEach { (k, v) ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = if (v.equals("true", ignoreCase = true)) k else "$k: $v",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }

            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Preview(name = "Rooster Employee Cards - Various Types", showBackground = true)
@Composable
fun RoosterEmployeeCards_Preview() {
    MyApplicationTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RoosterEmployeeCard(
                employee = PreviewData.sampleEmployees[0],
                onEdit = {},
                onDelete = {}
            )
            RoosterEmployeeCard(
                employee = PreviewData.sampleEmployees[3],
                onEdit = {},
                onDelete = {}
            )
            RoosterEmployeeCard(
                employee = PreviewData.sampleEmployees[6],
                onEdit = {},
                onDelete = {}
            )
        }
    }
}

@Preview(name = "Rooster Employee Cards - Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RoosterEmployeeCards_Preview_DarkTheme() {
    MyApplicationTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RoosterEmployeeCard(
                employee = PreviewData.sampleEmployees[0],
                onEdit = {},
                onDelete = {}
            )
            RoosterEmployeeCard(
                employee = PreviewData.sampleEmployees[3],
                onEdit = {},
                onDelete = {}
            )
        }
    }
}
