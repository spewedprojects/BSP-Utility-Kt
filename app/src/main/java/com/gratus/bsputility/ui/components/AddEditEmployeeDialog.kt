package com.gratus.bsputility.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gratus.bsputility.data.models.ConfigItem
import com.gratus.bsputility.data.models.Contractor
import com.gratus.bsputility.data.models.Employee
import com.gratus.bsputility.data.models.EmployeeStatuses
import com.gratus.bsputility.data.models.EmployeeTypes
import com.gratus.bsputility.ui.preview.PreviewData
import com.gratus.bsputility.ui.theme.MyApplicationTheme
import org.json.JSONObject

@Composable
fun AddEditEmployeeDialog(
    employee: Employee,
    isNew: Boolean,
    departments: List<String>,
    roles: List<String>,
    designations: List<String>,
    contractors: List<Contractor>,
    units: List<String>,
    shifts: List<String> = emptyList(),
    customFields: List<ConfigItem> = emptyList(),
    allConfigItems: List<ConfigItem> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (Employee) -> Unit
) {
    var empCode by remember { mutableStateOf(employee.empCode) }
    var name by remember { mutableStateOf(employee.name) }
    var type by remember { mutableStateOf(employee.type) }
    var status by remember { mutableStateOf(employee.status) }
    var department by remember { mutableStateOf(employee.permanentDepartment) }
    var designation by remember { mutableStateOf(employee.designation) }
    var contractorName by remember { mutableStateOf(employee.contractorName) }
    var workRole by remember { mutableStateOf(employee.defaultWorkRole) }
    var unit by remember { mutableStateOf(employee.defaultUnit) }
    var shift by remember { mutableStateOf(employee.defaultShift) }
    var remarks by remember { mutableStateOf(employee.permanentRemarks) }

    // Dropdown booleans
    var deptExp by remember { mutableStateOf(false) }
    var roleExp by remember { mutableStateOf(false) }
    var contractorExp by remember { mutableStateOf(false) }
    var unitExp by remember { mutableStateOf(false) }
    var shiftExp by remember { mutableStateOf(false) }

    val availableShifts = remember(shifts) {
        if (shifts.isEmpty()) listOf("Shift A", "Shift B", "Shift C", "General") else shifts
    }

    val filteredRoles = remember(department, roles, allConfigItems) {
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
                    parts.contains("all") || parts.contains(department.trim().lowercase())
                }
            }.map { it.name }
            if (matching.isEmpty()) roles else matching
        }
    }

    val initialCustomFields = remember(employee.customFieldsJson) {
        val map = mutableMapOf<String, String>()
        try {
            if (employee.customFieldsJson.isNotBlank()) {
                val json = JSONObject(employee.customFieldsJson)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    map[k] = json.optString(k)
                }
            }
        } catch (_: Exception) {}
        map
    }
    var customFieldValues by remember { mutableStateOf(initialCustomFields) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isNew) "Add to Employee Library" else "Edit Employee Record",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Employee Code / eSSL Machine ID
                OutlinedTextField(
                    value = empCode,
                    onValueChange = { empCode = it },
                    label = { Text("Employee Code / eSSL ID (Optional)") },
                    placeholder = { Text("e.g. 101, EMP-042") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_emp_code")
                )

                // Full Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Employee Full Name *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_emp_name")
                )

                // Employee Type Selector
                Column {
                    Text("Employee Type", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        EmployeeTypes.all.forEach { t ->
                            FilterChip(
                                selected = type == t,
                                onClick = { type = t },
                                label = { Text(t, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Status Selector (Active, Out, Debarred)
                Column {
                    Text("Employment Status", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        EmployeeStatuses.all.forEach { st ->
                            FilterChip(
                                selected = status == st,
                                onClick = { status = st },
                                label = { Text(st, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Permanent Department (Applies to both Staff and Labour)
                Column {
                    Text("Permanent Department *", style = MaterialTheme.typography.labelMedium)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { deptExp = true },
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
                            Text(department.ifBlank { "Select Department" }, fontSize = 13.sp)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(expanded = deptExp, onDismissRequest = { deptExp = false }) {
                            departments.forEach { d ->
                                DropdownMenuItem(
                                    text = { Text(d) },
                                    onClick = {
                                        department = d
                                        deptExp = false
                                    }
                                )
                            }
                        }
                    }
                }

                // If Staff: Designation
                if (type == EmployeeTypes.STAFF) {
                    OutlinedTextField(
                        value = designation,
                        onValueChange = { designation = it },
                        label = { Text("Designation (e.g. HR Trainee, Supervisor)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // If Labour: Contractor
                    Column {
                        Text("Contractor", style = MaterialTheme.typography.labelMedium)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { contractorExp = true },
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
                                Text(contractorName.ifBlank { "Select Contractor" }, fontSize = 13.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(expanded = contractorExp, onDismissRequest = { contractorExp = false }) {
                                contractors.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c.name) },
                                        onClick = {
                                            contractorName = c.name
                                            contractorExp = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Default Work Role Dropdown
                    Column {
                        Text("Default Work / Category", style = MaterialTheme.typography.labelMedium)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { roleExp = true },
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
                                Text(workRole.ifBlank { "Select Role" }, fontSize = 13.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(expanded = roleExp, onDismissRequest = { roleExp = false }) {
                                filteredRoles.forEach { r ->
                                    DropdownMenuItem(
                                        text = { Text(r) },
                                        onClick = {
                                            workRole = r
                                            roleExp = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Default Unit & Shift (Applies to both Staff and Labour)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Unit", style = MaterialTheme.typography.labelMedium)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { unitExp = true },
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
                                Text(unit, fontSize = 13.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(expanded = unitExp, onDismissRequest = { unitExp = false }) {
                                units.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(u) },
                                        onClick = {
                                            unit = u
                                            unitExp = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Shift", style = MaterialTheme.typography.labelMedium)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { shiftExp = true },
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
                                Text(shift.ifBlank { availableShifts.firstOrNull() ?: "Shift A" }, fontSize = 13.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(expanded = shiftExp, onDismissRequest = { shiftExp = false }) {
                                availableShifts.forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(s) },
                                        onClick = {
                                            shift = s
                                            shiftExp = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Permanent Remarks
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Permanent Remarks (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Custom Fields Section
                val applicableFields = remember(customFields, type) {
                    customFields.filter { cf ->
                        val parts = cf.extraType.split("|")
                        val target = parts.getOrNull(1)?.uppercase() ?: "ALL"
                        when (target) {
                            "STAFF" -> type == EmployeeTypes.STAFF
                            "LABOUR" -> type != EmployeeTypes.STAFF
                            else -> true
                        }
                    }
                }

                if (applicableFields.isNotEmpty()) {
                    Text("Custom Fields", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        applicableFields.forEach { cf ->
                            val parts = cf.extraType.split("|")
                            val fieldType = parts.getOrNull(0)?.uppercase() ?: "TEXT"
                            val currentValue = customFieldValues[cf.name] ?: ""

                            if (fieldType == "BOOLEAN") {
                                val isChecked = currentValue.equals("true", ignoreCase = true)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(cf.name, fontSize = 13.sp)
                                    Switch(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            customFieldValues = customFieldValues.toMutableMap().apply {
                                                put(cf.name, checked.toString())
                                            }
                                        }
                                    )
                                }
                            } else if (fieldType == "NUMBER") {
                                OutlinedTextField(
                                    value = currentValue,
                                    onValueChange = { num ->
                                        customFieldValues = customFieldValues.toMutableMap().apply {
                                            put(cf.name, num)
                                        }
                                    },
                                    label = { Text(cf.name) },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            } else {
                                OutlinedTextField(
                                    value = currentValue,
                                    onValueChange = { txt ->
                                        customFieldValues = customFieldValues.toMutableMap().apply {
                                            put(cf.name, txt)
                                        }
                                    },
                                    label = { Text(cf.name) },
                                    modifier = Modifier.fillMaxWidth()
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
                    if (name.isNotBlank()) {
                        val jsonObj = JSONObject()
                        customFieldValues.forEach { (k, v) ->
                            if (v.isNotBlank()) {
                                jsonObj.put(k, v)
                            }
                        }
                        val updated = employee.copy(
                            empCode = empCode.trim(),
                            name = name.trim(),
                            type = type,
                            status = status,
                            permanentDepartment = (department.ifBlank { departments.firstOrNull() ?: "" }).trim(),
                            designation = if (type == EmployeeTypes.STAFF) designation.trim() else "",
                            contractorId = if (type == EmployeeTypes.STAFF) null else contractors.find { it.name.equals(contractorName, ignoreCase = true) }?.id,
                            contractorName = if (type == EmployeeTypes.STAFF) "" else contractorName,
                            defaultWorkRole = if (type != EmployeeTypes.STAFF) workRole else "",
                            defaultUnit = unit,
                            defaultShift = shift.ifBlank { availableShifts.firstOrNull() ?: "Shift A" },
                            permanentRemarks = remarks.trim(),
                            customFieldsJson = jsonObj.toString()
                        )
                        onSave(updated)
                    }
                },
                modifier = Modifier.testTag("btn_save_employee")
            ) {
                Text(if (isNew) "Add to Library" else "Save Changes")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(name = "Rooster Dialog - Add/Edit Employee", showBackground = true)
@Composable
fun AddEditEmployeeDialog_Preview() {
    MyApplicationTheme {
        AddEditEmployeeDialog(
            employee = PreviewData.sampleEmployees[0],
            isNew = false,
            departments = listOf("Laser Cutting", "Fabrication", "Welding Shop", "Press Shop"),
            roles = listOf("Helper", "Welder", "Operator", "Fitter"),
            designations = listOf("Production Supervisor", "Shift In-charge"),
            contractors = PreviewData.sampleContractors,
            units = listOf("Unit I", "Unit II", "Unit III"),
            onDismiss = {},
            onSave = {}
        )
    }
}
