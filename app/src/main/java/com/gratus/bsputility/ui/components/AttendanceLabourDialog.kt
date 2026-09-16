package com.gratus.bsputility.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.gratus.bsputility.data.models.Contractor
import com.gratus.bsputility.data.models.Employee
import com.gratus.bsputility.ui.theme.PresentGreen
import org.json.JSONObject

@Composable
fun AttendanceLabourDialog(
    employee: Employee,
    currentPresence: Boolean,
    currentDepartment: String,
    currentWorkRole: String,
    currentContractor: String,
    currentUnit: String,
    currentShift: String,
    currentRemarks: String,
    availableDepartments: List<String>,
    availableRoles: List<String>,
    availableContractors: List<Contractor>,
    availableUnits: List<String>,
    availableShifts: List<String> = emptyList(),
    allConfigItems: List<com.gratus.bsputility.data.models.ConfigItem> = emptyList(),
    isFutureDate: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (
        isPresent: Boolean,
        department: String,
        workRole: String,
        contractor: String,
        unit: String,
        shift: String,
        remarks: String
    ) -> Unit
) {
    var isPresent by remember { mutableStateOf(currentPresence) }
    val defaultDeptInitial = remember(currentDepartment, employee.permanentDepartment, availableDepartments) {
        if (currentDepartment.isNotBlank() && currentDepartment != "Unassigned") {
            currentDepartment
        } else if (employee.permanentDepartment.isNotBlank() && employee.permanentDepartment != "Unassigned") {
            employee.permanentDepartment
        } else {
            availableDepartments.firstOrNull() ?: "Welding Shop"
        }
    }
    var selectedDept by remember { mutableStateOf(defaultDeptInitial) }
    var selectedRole by remember { mutableStateOf(currentWorkRole.ifBlank { employee.defaultWorkRole }) }
    var selectedContractor by remember { mutableStateOf(currentContractor.ifBlank { employee.contractorName }) }
    var selectedUnit by remember { mutableStateOf(currentUnit.ifBlank { employee.defaultUnit }) }
    var selectedShift by remember { mutableStateOf(currentShift.ifBlank { employee.defaultShift }) }
    var remarks by remember { mutableStateOf(currentRemarks) }

    // Dropdown expansion states
    var deptExpanded by remember { mutableStateOf(false) }
    var roleExpanded by remember { mutableStateOf(false) }
    var contractorExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }
    var shiftExpanded by remember { mutableStateOf(false) }

    val shifts = remember(availableShifts) {
        if (availableShifts.isEmpty()) listOf("Shift A", "Shift B", "Shift C", "General") else availableShifts
    }

    val filteredRoles = remember(selectedDept, availableRoles, allConfigItems) {
        val labourRoleConfigs = allConfigItems.filter { it.category == "LABOUR_ROLE" }
        if (labourRoleConfigs.isEmpty()) {
            availableRoles
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
            if (matching.isEmpty()) availableRoles else matching
        }
    }

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
                    text = "Daily Labour Assignment",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
                Text(
                    text = employee.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Changes apply to today only without affecting master profile.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            modifier = Modifier.testTag("switch_presence_labour"),
                            colors = SwitchDefaults.colors(checkedThumbColor = PresentGreen)
                        )
                    }
                }

                // Department Dropdown
                Column {
                    Text("Assigned Department (Today)", style = MaterialTheme.typography.labelMedium)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { deptExpanded = true }
                            .testTag("dropdown_labour_dept"),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedDept, fontWeight = FontWeight.Medium)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Dept")
                        }
                        DropdownMenu(
                            expanded = deptExpanded,
                            onDismissRequest = { deptExpanded = false }
                        ) {
                            availableDepartments.forEach { dept ->
                                DropdownMenuItem(
                                    text = { Text(dept) },
                                    onClick = {
                                        selectedDept = dept
                                        deptExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Work Role / Category Dropdown (Helper, Welder, Operator, Bender, Painter, etc.)
                Column {
                    Text("Today's Role / Activity", style = MaterialTheme.typography.labelMedium)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { roleExpanded = true }
                            .testTag("dropdown_labour_role"),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedRole, fontWeight = FontWeight.Medium)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Role")
                        }
                        DropdownMenu(
                            expanded = roleExpanded,
                            onDismissRequest = { roleExpanded = false }
                        ) {
                            filteredRoles.forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role) },
                                    onClick = {
                                        selectedRole = role
                                        roleExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Contractor Dropdown
                Column {
                    Text("Contractor", style = MaterialTheme.typography.labelMedium)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { contractorExpanded = true }
                            .testTag("dropdown_labour_contractor"),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedContractor.ifBlank { "Select Contractor" }, fontWeight = FontWeight.Medium)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Contractor")
                        }
                        DropdownMenu(
                            expanded = contractorExpanded,
                            onDismissRequest = { contractorExpanded = false }
                        ) {
                            availableContractors.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name) },
                                    onClick = {
                                        selectedContractor = c.name
                                        contractorExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Row with Unit and Shift
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Unit Dropdown
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Unit", style = MaterialTheme.typography.labelMedium)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { unitExpanded = true },
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
                                Text(selectedUnit, fontSize = 13.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Unit")
                            }
                            DropdownMenu(
                                expanded = unitExpanded,
                                onDismissRequest = { unitExpanded = false }
                            ) {
                                availableUnits.forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit) },
                                        onClick = {
                                            selectedUnit = unit
                                            unitExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Shift Dropdown
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Shift", style = MaterialTheme.typography.labelMedium)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { shiftExpanded = true },
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
                                Text(selectedShift, fontSize = 13.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Shift")
                            }
                            DropdownMenu(
                                expanded = shiftExpanded,
                                onDismissRequest = { shiftExpanded = false }
                            ) {
                                shifts.forEach { shift ->
                                    DropdownMenuItem(
                                        text = { Text(shift) },
                                        onClick = {
                                            selectedShift = shift
                                            shiftExpanded = false
                                        }
                                    )
                                }
                            }
                        }
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
                                "Employee Custom Fields",
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
                    label = { Text("Daily Remarks (e.g. Assigned to Laser today)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_labour_remarks"),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        if (isFutureDate) false else isPresent,
                        selectedDept,
                        selectedRole,
                        selectedContractor,
                        selectedUnit,
                        selectedShift,
                        remarks
                    )
                },
                modifier = Modifier.testTag("btn_save_labour_assignment")
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Confirm Assignment")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
