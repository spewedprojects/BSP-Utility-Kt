package com.gratus.bsputility.data.models

import org.json.JSONObject

/**
 * Represents an external company-relevant contact outside the employee rooster
 * (e.g., Catering Service, Water Supplier, Snacks Supplier, Transport, Maintenance, Emergency).
 * Persisted via ConfigItem(category = "CONTACT") to avoid Room database schema migrations
 * while automatically participating in JSON backups and restores.
 */
data class ExternalContact(
    val id: Long = 0,
    val name: String,
    val serviceCategory: String = "Vendor / Supplier",
    val contactPerson: String = "",
    val phone: String = "",
    val notes: String = ""
) {
    fun toConfigItem(): ConfigItem {
        val json = JSONObject().apply {
            put("serviceCategory", serviceCategory.trim().ifBlank { "Vendor / Supplier" })
            put("contactPerson", contactPerson.trim())
            put("phone", phone.trim())
            put("notes", notes.trim())
        }
        return ConfigItem(
            id = id,
            category = CATEGORY_CONTACT,
            name = name.trim(),
            extraType = json.toString()
        )
    }

    companion object {
        const val CATEGORY_CONTACT = "CONTACT"

        val DEFAULT_CATEGORIES = listOf(
            "Catering Service",
            "Water Supplier",
            "Snacks / Tea Supplier",
            "Transport / Logistics",
            "Maintenance / Repair",
            "Medical / Emergency",
            "Stationery / Office",
            "Other Vendor"
        )

        fun fromConfigItem(item: ConfigItem): ExternalContact {
            val rawExtra = item.extraType.trim()
            if (rawExtra.startsWith("{")) {
                return try {
                    val json = JSONObject(rawExtra)
                    ExternalContact(
                        id = item.id,
                        name = item.name,
                        serviceCategory = json.optString("serviceCategory", "Vendor / Supplier").ifBlank { "Vendor / Supplier" },
                        contactPerson = json.optString("contactPerson", ""),
                        phone = json.optString("phone", ""),
                        notes = json.optString("notes", "")
                    )
                } catch (_: Exception) {
                    ExternalContact(id = item.id, name = item.name)
                }
            } else {
                val parts = rawExtra.split("|")
                return ExternalContact(
                    id = item.id,
                    name = item.name,
                    serviceCategory = parts.getOrNull(0)?.trim()?.ifBlank { "Vendor / Supplier" } ?: "Vendor / Supplier",
                    contactPerson = parts.getOrNull(1)?.trim() ?: "",
                    phone = parts.getOrNull(2)?.trim() ?: "",
                    notes = parts.getOrNull(3)?.trim() ?: ""
                )
            }
        }
    }
}
