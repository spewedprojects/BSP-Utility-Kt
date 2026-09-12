package com.gratus.bsputility.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TimeInputComponent(
    initialTime: String,
    initialTimestamp: Long = 0L,
    is24Hour: Boolean = false,
    modifier: Modifier = Modifier,
    onTimeChange: (formattedTime: String, timestamp: Long) -> Unit
) {
    // Determine default hours, minutes, and amPm from initialTime or current time
    val parsed = remember(initialTime, is24Hour) {
        parseTime(initialTime, is24Hour)
    }

    var selectedHour by remember { mutableIntStateOf(parsed.first) }
    var selectedMinute by remember { mutableIntStateOf(parsed.second) }
    var selectedAmPm by remember { mutableStateOf(parsed.third) }
    var isDirectInputMode by remember { mutableStateOf(false) }
    var manualText by remember { mutableStateOf(initialTime.ifBlank { formatTime(parsed.first, parsed.second, parsed.third, is24Hour) }) }

    val focusManager = LocalFocusManager.current

    // Hours range: 1..12 or 0..23
    val hoursList = remember(is24Hour) {
        if (is24Hour) (0..23).toList() else (1..12).toList()
    }
    // 60 minutes: 00..59
    val minutesList = remember { (0..59).toList() }

    val hourScrollState = rememberLazyListState()
    val minuteScrollState = rememberLazyListState()

    // Scroll to current selected hour and minute initially
    LaunchedEffect(is24Hour) {
        val hIndex = hoursList.indexOf(selectedHour).coerceAtLeast(0)
        hourScrollState.scrollToItem((hIndex - 2).coerceAtLeast(0))
        minuteScrollState.scrollToItem((selectedMinute - 2).coerceAtLeast(0))
    }

    // Helper to notify changes
    fun updateTime(hour: Int, minute: Int, amPm: String) {
        val formatted = formatTime(hour, minute, amPm, is24Hour)
        val timestamp = calculateTimestamp(hour, minute, amPm, is24Hour)
        manualText = formatted
        onTimeChange(formatted, timestamp)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top display card with toggle between Picker and Direct Keypad
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatTime(selectedHour, selectedMinute, selectedAmPm, is24Hour),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("text_selected_time_display")
                    )
                }

                IconButton(
                    onClick = { isDirectInputMode = !isDirectInputMode },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isDirectInputMode) Icons.Default.AccessTime else Icons.Default.Keyboard,
                        contentDescription = if (isDirectInputMode) "Switch to Picker" else "Switch to Direct Input",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Direct Text Input Mode
        if (isDirectInputMode) {
            OutlinedTextField(
                value = manualText,
                onValueChange = { input ->
                    manualText = input
                    val p = parseTime(input, is24Hour)
                    selectedHour = p.first
                    selectedMinute = p.second
                    selectedAmPm = p.third
                    val timestamp = calculateTimestamp(p.first, p.second, p.third, is24Hour)
                    onTimeChange(input, timestamp)
                },
                label = { Text(if (is24Hour) "Time (HH:mm - 24hr)" else "Time (hh:mm AM/PM)") },
                placeholder = { Text(if (is24Hour) "14:30" else "02:30 PM") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_time_direct_text")
            )
        } else {
            // Interactive Scrollable Picker Mode
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Hours label & scroll row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (is24Hour) "Hours (00 - 23)" else "Hours (1 - 12)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!is24Hour) {
                        // AM / PM toggle
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("AM", "PM").forEach { period ->
                                val isSelected = selectedAmPm.equals(period, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .clickable {
                                            selectedAmPm = period
                                            updateTime(selectedHour, selectedMinute, period)
                                        }
                                        .testTag("btn_period_$period")
                                ) {
                                    Text(
                                        text = period,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                LazyRow(
                    state = hourScrollState,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(hoursList) { h ->
                        val isSelected = h == selectedHour
                        val displayH = if (is24Hour) String.format(Locale.getDefault(), "%02d", h) else h.toString()
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .clickable {
                                    selectedHour = h
                                    updateTime(h, selectedMinute, selectedAmPm)
                                }
                                .testTag("chip_hour_$displayH")
                        ) {
                            Text(
                                text = displayH,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Minutes label & scroll row (all 60 minutes)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Minutes (00 - 59)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    // Quick minute step adjustments
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(-5, +5).forEach { step ->
                            val label = if (step > 0) "+5m" else "-5m"
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.clickable {
                                    val newMin = (selectedMinute + step + 60) % 60
                                    selectedMinute = newMin
                                    updateTime(selectedHour, newMin, selectedAmPm)
                                }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                LazyRow(
                    state = minuteScrollState,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(minutesList) { m ->
                        val isSelected = m == selectedMinute
                        val displayM = String.format(Locale.getDefault(), "%02d", m)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .clickable {
                                    selectedMinute = m
                                    updateTime(selectedHour, m, selectedAmPm)
                                }
                                .testTag("chip_minute_$displayM")
                        ) {
                            Text(
                                text = displayM,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Preset Chips (Always visible)
        val presetTimes = remember(is24Hour) {
            if (is24Hour) listOf("08:30", "08:45", "09:00", "09:15", "09:30")
            else listOf("08:30 AM", "08:45 AM", "09:00 AM", "09:15 AM", "09:30 AM")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = false,
                onClick = {
                    val cal = Calendar.getInstance()
                    val h = if (is24Hour) cal.get(Calendar.HOUR_OF_DAY) else {
                        val hr = cal.get(Calendar.HOUR)
                        if (hr == 0) 12 else hr
                    }
                    val m = cal.get(Calendar.MINUTE)
                    val period = if (cal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
                    selectedHour = h
                    selectedMinute = m
                    selectedAmPm = period
                    updateTime(h, m, period)
                },
                label = { Text("Now", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("chip_preset_now")
            )
            presetTimes.take(3).forEach { preset ->
                val isSelected = manualText == preset
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val p = parseTime(preset, is24Hour)
                        selectedHour = p.first
                        selectedMinute = p.second
                        selectedAmPm = p.third
                        updateTime(p.first, p.second, p.third)
                    },
                    label = { Text(preset, fontSize = 11.sp) }
                )
            }
        }
    }
}

// Helpers
private fun parseTime(input: String, is24Hour: Boolean): Triple<Int, Int, String> {
    if (input.isBlank()) {
        val cal = Calendar.getInstance()
        val h = if (is24Hour) cal.get(Calendar.HOUR_OF_DAY) else {
            val hr = cal.get(Calendar.HOUR)
            if (hr == 0) 12 else hr
        }
        val m = cal.get(Calendar.MINUTE)
        val period = if (cal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
        return Triple(h, m, period)
    }

    try {
        if (is24Hour) {
            val parts = input.trim().split(":")
            if (parts.size >= 2) {
                val hour = parts[0].trim().toInt().coerceIn(0, 23)
                val minute = parts[1].trim().take(2).toInt().coerceIn(0, 59)
                return Triple(hour, minute, "")
            }
        } else {
            // 12-hour: hh:mm AM/PM
            val clean = input.trim().uppercase()
            val isPm = clean.contains("PM")
            val isAm = clean.contains("AM")
            val period = if (isPm) "PM" else "AM"
            val timeDigits = clean.replace("AM", "").replace("PM", "").trim()
            val parts = timeDigits.split(":")
            if (parts.size >= 2) {
                val hour = parts[0].trim().toInt().coerceIn(1, 12)
                val minute = parts[1].trim().take(2).toInt().coerceIn(0, 59)
                return Triple(hour, minute, period)
            }
        }
    } catch (_: Exception) {}

    // Fallback
    return Triple(if (is24Hour) 9 else 9, 0, "AM")
}

private fun formatTime(hour: Int, minute: Int, amPm: String, is24Hour: Boolean): String {
    return if (is24Hour) {
        String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
    } else {
        val period = if (amPm.isBlank()) "AM" else amPm.uppercase()
        String.format(Locale.getDefault(), "%02d:%02d %s", hour, minute, period)
    }
}

private fun calculateTimestamp(hour: Int, minute: Int, amPm: String, is24Hour: Boolean): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    if (is24Hour) {
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
    } else {
        cal.set(Calendar.HOUR, if (hour == 12) 0 else hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.AM_PM, if (amPm.equals("PM", ignoreCase = true)) Calendar.PM else Calendar.AM)
    }
    return cal.timeInMillis
}
