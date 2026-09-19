package com.gratus.bsputility.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gratus.bsputility.data.models.ConfigItem
import com.gratus.bsputility.data.models.Contractor
import com.gratus.bsputility.data.models.EmployeeTypes
import com.gratus.bsputility.ui.preview.PreviewData
import com.gratus.bsputility.ui.theme.MyApplicationTheme

@Composable
fun PasteImportDialog(
    contractors: List<Contractor>,
    departments: List<String>,
    roles: List<String>,
    allConfigItems: List<ConfigItem> = emptyList(),
    onDismiss: () -> Unit,
    onImport: (
        namesText: String,
        targetType: String,
        contractorId: Long?,
        contractorName: String,
        dept: String,
        role: String
    ) -> Unit
) {
    var namesText by remember { mutableStateOf("") }
    var targetType by remember { mutableStateOf(EmployeeTypes.LABOUR) }
    var selectedContractor by remember { mutableStateOf(contractors.firstOrNull()?.name ?: "") }
    var selectedDept by remember { mutableStateOf(departments.firstOrNull() ?: "Welding Shop") }

    val filteredRoles = remember(selectedDept, roles, allConfigItems) {
        val labourRoleConfigs = allConfigItems.filter { it.category == "LABOUR_ROLE" }
        if (labourRoleConfigs.isEmpty()) {
            roles
        } else {
            val matching = labourRoleConfigs.filter { roleItem ->
                val extra = roleItem.extraType.trim()
                if (extra.isBlank() || extra.equals("ALL", ignoreCase = true)) {
                    true
                } else {
                    val parts = extra.split(",").map { it.trim().lowercase() }
                    parts.contains("all") || parts.contains(selectedDept.trim().lowercase())
                }
            }.map { it.name }
            if (matching.isEmpty()) roles else matching
        }
    }

    var selectedRole by remember(selectedDept) { mutableStateOf(filteredRoles.firstOrNull() ?: "Helper") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Fast Batch Name Import", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Paste a list of names, one per line:",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = namesText,
                    onValueChange = { namesText = it },
                    placeholder = { Text("Ramesh Pawar\nVijay Thorat\nPravin Shinde\n...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("input_batch_names")
                )

                Text("Where should these names be added?", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    EmployeeTypes.all.forEach { t ->
                        FilterChip(
                            selected = targetType == t,
                            onClick = { targetType = t },
                            label = { Text(t, fontSize = 11.sp) }
                        )
                    }
                }

                // Permanent Department (Applies to both Staff and Labour)
                var deptMenuExp by remember { mutableStateOf(false) }
                Text("Select Permanent Department:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { deptMenuExp = true },
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedDept.ifBlank { "Select Department" }, fontSize = 13.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = deptMenuExp,
                        onDismissRequest = { deptMenuExp = false }
                    ) {
                        departments.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d) },
                                onClick = {
                                    selectedDept = d
                                    deptMenuExp = false
                                }
                            )
                        }
                    }
                }

                if (targetType != EmployeeTypes.STAFF) {
                    var contractorMenuExp by remember { mutableStateOf(false) }
                    Text("Select Contractor:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { contractorMenuExp = true },
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedContractor.ifBlank { "Select Contractor" }, fontSize = 13.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = contractorMenuExp,
                            onDismissRequest = { contractorMenuExp = false }
                        ) {
                            contractors.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name) },
                                    onClick = {
                                        selectedContractor = c.name
                                        contractorMenuExp = false
                                    }
                                )
                            }
                        }
                    }

                    var roleMenuExp by remember { mutableStateOf(false) }
                    Text("Select Default Role:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { roleMenuExp = true },
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedRole.ifBlank { "Select Role" }, fontSize = 13.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = roleMenuExp,
                            onDismissRequest = { roleMenuExp = false }
                        ) {
                            filteredRoles.forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(r) },
                                    onClick = {
                                        selectedRole = r
                                        roleMenuExp = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val contractorObj = if (targetType == EmployeeTypes.STAFF) null else contractors.find { it.name == selectedContractor }
                    val finalContractorName = if (targetType == EmployeeTypes.STAFF) "" else selectedContractor
                    onImport(
                        namesText,
                        targetType,
                        contractorObj?.id,
                        finalContractorName,
                        selectedDept,
                        selectedRole
                    )
                },
                modifier = Modifier.testTag("btn_confirm_import")
            ) {
                Text("Import Names")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Preview(name = "Rooster Dialog - Batch Import", showBackground = true)
@Composable
fun PasteImportDialog_Preview() {
    MyApplicationTheme {
        PasteImportDialog(
            contractors = PreviewData.sampleContractors,
            departments = listOf("Laser Cutting", "Fabrication", "Welding Shop"),
            roles = listOf("Helper", "Welder", "Operator"),
            onDismiss = {},
            onImport = { _, _, _, _, _, _ -> }
        )
    }
}
