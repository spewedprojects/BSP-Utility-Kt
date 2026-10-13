package com.gratus.bsputility.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gratus.bsputility.data.models.ExternalContact
import com.gratus.bsputility.ui.theme.MyApplicationTheme

@Composable
fun AddContactDialog(
    contact: ExternalContact,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (ExternalContact) -> Unit
) {
    var name by remember { mutableStateOf(contact.name) }
    var serviceCategory by remember { mutableStateOf(contact.serviceCategory.ifBlank { "Catering Service" }) }
    var contactPerson by remember { mutableStateOf(contact.contactPerson) }
    var phone by remember { mutableStateOf(contact.phone) }
    var notes by remember { mutableStateOf(contact.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Add Company Contact" else "Edit Company Contact") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Service / Business / Name *") },
                    placeholder = { Text("e.g. Sai Catering, Pure Water Supplier") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_contact_name")
                )

                Text(
                    text = "Quick Select Category",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExternalContact.DEFAULT_CATEGORIES.forEach { cat ->
                        FilterChip(
                            selected = serviceCategory.equals(cat, ignoreCase = true),
                            onClick = { serviceCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = serviceCategory,
                    onValueChange = { serviceCategory = it },
                    label = { Text("Category / Service Type") },
                    placeholder = { Text("e.g. Catering Service, Water Supplier, Snacks") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_contact_category")
                )

                OutlinedTextField(
                    value = contactPerson,
                    onValueChange = { contactPerson = it },
                    label = { Text("Contact Person") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_contact_person")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("input_contact_phone")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (e.g. delivery timings, rates)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_contact_notes")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val toSave = contact.copy(
                            name = name.trim(),
                            serviceCategory = serviceCategory.trim().ifBlank { "Vendor / Supplier" },
                            contactPerson = contactPerson.trim(),
                            phone = phone.trim(),
                            notes = notes.trim()
                        )
                        onSave(toSave)
                    }
                },
                modifier = Modifier.testTag("btn_save_contact")
            ) {
                Text(if (isNew) "Add Contact" else "Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(name = "Master Data Dialog - Add Contact", showBackground = true)
@Composable
fun AddContactDialog_Preview() {
    MyApplicationTheme {
        AddContactDialog(
            contact = ExternalContact(
                id = 1L,
                name = "Annapurna Catering Service",
                serviceCategory = "Catering Service",
                contactPerson = "Suresh Kadam",
                phone = "9850112233",
                notes = "Lunch delivery at 12:30 PM daily"
            ),
            isNew = true,
            onDismiss = {},
            onSave = {}
        )
    }
}
