package com.gratus.bsputility.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.gratus.bsputility.ui.preview.PreviewData
import com.gratus.bsputility.ui.theme.MyApplicationTheme

@Composable
fun AddConfigDialog(
    category: String,
    tabTitle: String,
    configItem: ConfigItem? = null,
    availableDepartments: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (name: String, fieldType: String) -> Unit
) {
    val isEditing = configItem != null
    var itemName by remember(configItem) { mutableStateOf(configItem?.name ?: "") }

    val (initialFieldType, initialTargetAudience) = remember(configItem) {
        if (category == "CUSTOM_FIELD" && configItem != null) {
            val parts = configItem.extraType.split("|")
            val fType = parts.getOrNull(0)?.ifBlank { "TEXT" } ?: "TEXT"
            val target = parts.getOrNull(1)?.ifBlank { "ALL" } ?: "ALL"
            Pair(fType, target)
        } else {
            Pair("TEXT", "ALL")
        }
    }
    var fieldType by remember(initialFieldType) { mutableStateOf(initialFieldType) }
    var targetAudience by remember(initialTargetAudience) { mutableStateOf(initialTargetAudience) } // ALL, STAFF, LABOUR

    val initialDepts = remember(configItem) {
        if (category == "LABOUR_ROLE" && configItem != null) {
            val extra = configItem.extraType.trim()
            if (extra.isNotBlank() && !extra.equals("ALL", ignoreCase = true)) {
                extra.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
            } else {
                emptySet()
            }
        } else {
            emptySet()
        }
    }
    var selectedDepts by remember(initialDepts) { mutableStateOf<Set<String>>(initialDepts) }
    var deptDropdownOpen by remember { mutableStateOf(false) }

    val singularTitle = when (tabTitle) {
        "Departments" -> "Department"
        "Designations" -> "Designation"
        "Units" -> "Unit"
        "Shifts" -> "Shift"
        "Labour Roles" -> "Labour Role"
        "Custom Fields" -> "Custom Field"
        else -> tabTitle.removeSuffix("s")
    }
    val dialogTitle = if (isEditing) "Edit $singularTitle" else (if (tabTitle == "Shifts") "Add Shift" else "Add to $singularTitle")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(dialogTitle)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text(if (category == "SHIFT") "Shift Name (e.g. Shift A, General) *" else "Name / Title *") },
                    modifier = Modifier.fillMaxWidth().testTag("input_config_name")
                )

                if (category == "LABOUR_ROLE") {
                    Text("Associated Department(s):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Select which department(s) can use this role (or leave as All).", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = if (selectedDepts.isEmpty()) "All Departments" else selectedDepts.joinToString(", "),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Applies to Department") },
                            trailingIcon = {
                                IconButton(onClick = { deptDropdownOpen = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Departments")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { deptDropdownOpen = true }
                        )

                        DropdownMenu(
                            expanded = deptDropdownOpen,
                            onDismissRequest = { deptDropdownOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Departments (No restriction)") },
                                onClick = {
                                    selectedDepts = emptySet()
                                    deptDropdownOpen = false
                                }
                            )
                            availableDepartments.forEach { dept ->
                                val isSelected = dept in selectedDepts
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(checked = isSelected, onCheckedChange = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(dept)
                                        }
                                    },
                                    onClick = {
                                        selectedDepts = if (isSelected) selectedDepts - dept else selectedDepts + dept
                                    }
                                )
                            }
                        }
                    }

                    // Quick selection chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = selectedDepts.isEmpty(),
                            onClick = { selectedDepts = emptySet() },
                            label = { Text("All", fontSize = 10.sp) }
                        )
                        availableDepartments.forEach { dept ->
                            val isSelected = dept in selectedDepts
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedDepts = if (isSelected) selectedDepts - dept else selectedDepts + dept
                                },
                                label = { Text(dept, fontSize = 10.sp) }
                            )
                        }
                    }
                }

                if (category == "CUSTOM_FIELD") {
                    Text("Field Type:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    val types = listOf("TEXT", "NUMBER", "DROPDOWN", "DATE", "BOOLEAN")
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        types.forEach { t ->
                            FilterChip(
                                selected = fieldType == t,
                                onClick = { fieldType = t },
                                label = { Text(t, fontSize = 10.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Applies To:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("ALL" to "All Employees", "STAFF" to "Staff Only", "LABOUR" to "Labour Only").forEach { (targetKey, label) ->
                            FilterChip(
                                selected = targetAudience == targetKey,
                                onClick = { targetAudience = targetKey },
                                label = { Text(label, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (itemName.isNotBlank()) {
                    val finalExtraType = when (category) {
                        "CUSTOM_FIELD" -> "$fieldType|$targetAudience"
                        "LABOUR_ROLE" -> if (selectedDepts.isEmpty()) "ALL" else selectedDepts.joinToString(", ")
                        else -> fieldType
                    }
                    onSave(itemName.trim(), finalExtraType)
                }
            }) {
                Text(if (isEditing) "Save" else "Add")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(name = "Master Data Dialog - Add Config Item", showBackground = true)
@Composable
fun AddConfigDialog_Preview() {
    MyApplicationTheme {
        AddConfigDialog(
            category = "DEPARTMENT",
            tabTitle = "Departments",
            configItem = PreviewData.sampleConfigItems[0],
            availableDepartments = listOf("Laser Cutting", "Fabrication", "Welding Shop"),
            onDismiss = {},
            onSave = { _, _ -> }
        )
    }
}
