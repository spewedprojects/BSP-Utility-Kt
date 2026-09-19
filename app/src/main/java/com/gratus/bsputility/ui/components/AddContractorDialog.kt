package com.gratus.bsputility.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gratus.bsputility.data.models.Contractor
import com.gratus.bsputility.ui.preview.PreviewData
import com.gratus.bsputility.ui.theme.MyApplicationTheme

@Composable
fun AddContractorDialog(
    contractor: Contractor,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (Contractor) -> Unit
) {
    var name by remember { mutableStateOf(contractor.name) }
    var contact by remember { mutableStateOf(contractor.contactPerson) }
    var phone by remember { mutableStateOf(contractor.phone) }
    var notes by remember { mutableStateOf(contractor.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Add New Contractor" else "Edit Contractor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Contractor / Agency Name *") },
                    modifier = Modifier.fillMaxWidth().testTag("input_contractor_name")
                )
                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Contact Person") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (e.g. provides welders)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
                    val toSave = contractor.copy(
                        name = name.trim(),
                        contactPerson = contact.trim(),
                        phone = phone.trim(),
                        notes = notes.trim()
                    )
                    onSave(toSave)
                }
            }) {
                Text(if (isNew) "Add Contractor" else "Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(name = "Master Data Dialog - Add Contractor", showBackground = true)
@Composable
fun AddContractorDialog_Preview() {
    MyApplicationTheme {
        AddContractorDialog(
            contractor = PreviewData.sampleContractors[0],
            isNew = true,
            onDismiss = {},
            onSave = {}
        )
    }
}
