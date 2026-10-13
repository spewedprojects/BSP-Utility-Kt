package com.gratus.bsputility.data.models

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Utility for standardizing, recognizing, and sorting dates and modification timestamps
 * across Rooster, Daily Attendance, CSV imports, and JSON backups.
 */
object DateUtils {

    const val KEY_LAST_MODIFIED = "_lastModified"

    private val isoRegex = Regex("""^(\d{4})-(\d{2})-(\d{2})$""")
    private val digitsOnlyRegex = Regex("""^\d{10,13}$""")

    private val namedMonthFormats = listOf(
        "dd-MMM-yyyy",
        "d-MMM-yyyy",
        "dd MMM yyyy",
        "d MMM yyyy",
        "dd/MMM/yyyy",
        "d/MMM/yyyy",
        "MMM dd, yyyy",
        "MMM d, yyyy",
        "dd MMMM yyyy",
        "d MMMM yyyy",
        "dd-MMM-yy",
        "d-MMM-yy",
        "dd/MMM/yy"
    )

    /**
     * Normalizes any recognized date string (e.g., "15/09/2026", "2026/09/15", "15-09-2026",
     * "5/9/2026", "09/15/2026", "15-Sep-2026", "2026-09-15T10:30:00") into canonical ISO 8601
     * "yyyy-MM-dd" format.
     */
    fun normalizeDate(raw: String?, fallback: String = ""): String {
        if (raw.isNullOrBlank()) return fallback
        val trimmed = raw.trim().removeSurrounding("\"").removeSurrounding("'").trim()
        if (trimmed.isBlank()) return fallback

        // 1. Fast-path if already valid yyyy-MM-dd
        val isoMatch = isoRegex.matchEntire(trimmed)
        if (isoMatch != null) {
            val y = isoMatch.groupValues[1].toIntOrNull() ?: 0
            val m = isoMatch.groupValues[2].toIntOrNull() ?: 0
            val d = isoMatch.groupValues[3].toIntOrNull() ?: 0
            if (y in 1970..2100 && m in 1..12 && d in 1..31) {
                return trimmed
            }
            // If someone wrote yyyy-dd-MM (e.g., 2026-15-09)
            if (y in 1970..2100 && d in 1..12 && m in 1..31) {
                return String.format(Locale.US, "%04d-%02d-%02d", y, d, m)
            }
        }

        // 2. Epoch timestamp (seconds or milliseconds)
        if (digitsOnlyRegex.matches(trimmed)) {
            val epoch = trimmed.toLongOrNull()
            if (epoch != null && epoch > 0L) {
                val millis = if (trimmed.length == 10) epoch * 1000L else epoch
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                return sdf.format(Date(millis))
            }
        }

        // 3. Strip time suffix if present for numeric date parsing (e.g. "15/09/2026 09:30" or "2026-09-15T10:00")
        val dateToken = trimmed
            .substringBefore('T')
            .substringBefore(' ')
            .trim()

        // 4. Numeric separators: '/', '-', '.'
        val parts = dateToken.split('/', '-', '.')
        if (parts.size == 3 && parts.all { it.isNotEmpty() && it.all { ch -> ch.isDigit() } }) {
            val p0 = parts[0].toIntOrNull()
            val p1 = parts[1].toIntOrNull()
            val p2 = parts[2].toIntOrNull()
            if (p0 != null && p1 != null && p2 != null) {
                var year = -1
                var month = -1
                var day = -1

                if (parts[0].length == 4 || p0 > 31) {
                    // Year-first: yyyy/MM/dd, yyyy-M-d, or yyyy/dd/MM
                    year = p0
                    if (p1 in 1..12 && p2 in 1..31) {
                        month = p1
                        day = p2
                    } else if (p2 in 1..12 && p1 in 1..31) {
                        month = p2
                        day = p1
                    }
                } else if (parts[2].length == 4 || p2 > 31 || parts[2].length == 2) {
                    // Day-first (dd/MM/yyyy, dd-MM-yyyy, d/M/yy) or Month-first (MM/dd/yyyy when p1 > 12)
                    year = if (p2 < 100) {
                        if (p2 >= 70) 1900 + p2 else 2000 + p2
                    } else {
                        p2
                    }

                    if (p0 > 12 && p1 in 1..12) {
                        // Unambiguous dd/MM/yyyy (e.g. 15/09/2026)
                        day = p0
                        month = p1
                    } else if (p1 > 12 && p0 in 1..12) {
                        // Unambiguous MM/dd/yyyy (e.g. 09/15/2026)
                        month = p0
                        day = p1
                    } else if (p0 in 1..12 && p1 in 1..12) {
                        // Standard Indian / International dd/MM/yyyy or dd-MM-yyyy
                        day = p0
                        month = p1
                    }
                }

                if (year in 1970..2100 && month in 1..12 && day in 1..31) {
                    return String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
                }
            }
        }

        // 5. Named month formats (e.g., "15-Sep-2026", "15 Sep 2026", "Sep 15, 2026")
        for (pattern in namedMonthFormats) {
            try {
                val parser = SimpleDateFormat(pattern, Locale.ENGLISH).apply {
                    isLenient = false
                }
                val parsed = parser.parse(trimmed)
                if (parsed != null) {
                    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(parsed)
                }
            } catch (_: Exception) {}
        }

        return fallback.ifBlank { trimmed }
    }

    /**
     * Parses any recognized date string into deterministic UTC epoch milliseconds for accurate sorting.
     * Returns 0L if blank or unparseable.
     */
    fun parseDateToMillis(raw: String?): Long {
        val normalized = normalizeDate(raw, "")
        if (normalized.isBlank() || !isoRegex.matches(normalized)) return 0L
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
                isLenient = false
            }
            sdf.parse(normalized)?.time ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * Formats an epoch millisecond timestamp as "yyyy-MM-dd".
     */
    fun formatDateMillis(millis: Long): String {
        if (millis <= 0L) return ""
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))
    }

    /**
     * Injects or updates the internal `_lastModified` epoch timestamp inside an Employee's `customFieldsJson`.
     */
    fun withLastModified(customFieldsJson: String?, timestampMillis: Long = System.currentTimeMillis()): String {
        val json = try {
            if (!customFieldsJson.isNullOrBlank() && customFieldsJson.trim().startsWith("{")) {
                JSONObject(customFieldsJson)
            } else {
                JSONObject()
            }
        } catch (_: Exception) {
            JSONObject()
        }
        json.put(KEY_LAST_MODIFIED, timestampMillis.toString())
        return json.toString()
    }

    /**
     * Preserves an existing `_lastModified` timestamp from `sourceCustomFieldsJson` into `targetCustomFieldsJson`
     * if `targetCustomFieldsJson` does not already have one.
     */
    fun preserveLastModified(targetCustomFieldsJson: String?, sourceCustomFieldsJson: String?): String {
        val sourceMillis = extractLastModifiedMillis(sourceCustomFieldsJson)
        if (sourceMillis <= 0L) return targetCustomFieldsJson ?: "{}"
        val existingTargetMillis = extractLastModifiedMillis(targetCustomFieldsJson)
        if (existingTargetMillis > 0L) return targetCustomFieldsJson ?: "{}"
        return withLastModified(targetCustomFieldsJson, sourceMillis)
    }

    /**
     * Extracts the explicit `_lastModified` epoch millisecond timestamp from `customFieldsJson`, or 0L if absent.
     */
    fun extractLastModifiedMillis(customFieldsJson: String?): Long {
        if (customFieldsJson.isNullOrBlank() || !customFieldsJson.contains(KEY_LAST_MODIFIED)) return 0L
        return try {
            val json = JSONObject(customFieldsJson)
            json.optString(KEY_LAST_MODIFIED, "").toLongOrNull() ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * Returns the explicit `_lastModified` timestamp for an employee, or 0L if never explicitly modified.
     */
    fun getEmployeeExplicitLastModifiedMillis(employee: Employee): Long {
        return extractLastModifiedMillis(employee.customFieldsJson)
    }

    /**
     * Returns the effective modification timestamp for an employee:
     * 1. Explicit `_lastModified` epoch millis if present (> 0L)
     * 2. Otherwise parsed `dateAdded` epoch millis (> 0L)
     * 3. Otherwise 0L
     */
    fun getEmployeeLastModifiedMillis(employee: Employee): Long {
        val explicit = getEmployeeExplicitLastModifiedMillis(employee)
        if (explicit > 0L) return explicit
        return parseDateToMillis(employee.dateAdded)
    }

    /**
     * Returns true if a custom field key is an internal metadata key (starts with '_') and should be hidden from UI/CSV.
     */
    fun isInternalCustomFieldKey(key: String): Boolean {
        return key.startsWith("_")
    }
}
