package com.gratus.bsputility.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gratus.bsputility.ui.theme.IndustrialAmber600
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateNavigationBar(
    selectedDate: String, // YYYY-MM-DD
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    val isToday = selectedDate == todayStr

    val displayDate = remember(selectedDate) {
        try {
            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)
            if (parsed != null) {
                SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(parsed)
            } else selectedDate
        } catch (_: Exception) {
            selectedDate
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Previous day button
            IconButton(
                onClick = onPreviousDay,
                modifier = Modifier
                    .testTag("btn_prev_date")
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Day",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Clickable Date Display
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showDatePicker = true }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("btn_select_date"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Calendar",
                        tint = if (isToday) IndustrialAmber600 else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isToday) "Today, $displayDate" else displayDate,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Next day button
            IconButton(
                onClick = onNextDay,
                modifier = Modifier
                    .testTag("btn_next_date")
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Day",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Quick Today Shortcut button if not today
            if (!isToday) {
                IconButton(
                    onClick = { onDateSelected(todayStr) },
                    modifier = Modifier
                        .testTag("btn_jump_today")
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = "Jump to Today",
                        tint = IndustrialAmber600
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val initialEpochMillis = remember(selectedDate) {
            try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)?.time
                    ?: System.currentTimeMillis()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialEpochMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = selectedMillis
                            val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                            onDateSelected(formatted)
                        }
                        showDatePicker = false
                    },
                    modifier = Modifier.testTag("btn_confirm_date")
                ) {
                    Text("Select", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
