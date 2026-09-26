package com.gratus.bsputility.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gratus.bsputility.ui.theme.MyApplicationTheme

/**
 * An adaptive DropdownMenu that caps maximum height and splits options
 * into a clean 2-column layout when there are more than 7 items.
 */
@Composable
fun <T> AdaptiveDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<T>,
    itemLabel: (T) -> String = { it.toString() },
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    selectedItem: T? = null,
    headerItems: List<DropdownHeaderAction>? = null,
    maxHeight: Dp = 280.dp
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
            .heightIn(max = maxHeight)
            .widthIn(min = if (items.size > 7) 280.dp else 180.dp)
    ) {
        // Optional Header Items (e.g. "Clear Filter", "All Contractors")
        if (!headerItems.isNullOrEmpty()) {
            headerItems.forEach { action ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = action.title,
                            fontWeight = if (action.isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (action.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = if (action.isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    } else null,
                    onClick = {
                        action.onClick()
                        onDismissRequest()
                    }
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        }

        if (items.isEmpty()) {
            DropdownMenuItem(
                text = { Text("No items available", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                onClick = onDismissRequest,
                enabled = false
            )
        } else if (items.size <= 7) {
            // Standard single column
            items.forEach { item ->
                val label = itemLabel(item)
                val isSelected = selectedItem != null && (selectedItem == item || itemLabel(selectedItem) == label)
                DropdownMenuItem(
                    text = {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    } else null,
                    onClick = {
                        onItemSelected(item)
                        onDismissRequest()
                    }
                )
            }
        } else {
            // Double-column layout for > 7 items
            val half = (items.size + 1) / 2
            val col1 = items.take(half)
            val col2 = items.drop(half)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                // Column 1
                Column(modifier = Modifier.weight(1f)) {
                    col1.forEach { item ->
                        AdaptiveMenuItemRow(
                            label = itemLabel(item),
                            isSelected = selectedItem != null && (selectedItem == item || itemLabel(selectedItem) == itemLabel(item)),
                            onClick = {
                                onItemSelected(item)
                                onDismissRequest()
                            }
                        )
                    }
                }

                // Column 2
                Column(modifier = Modifier.weight(1f)) {
                    col2.forEach { item ->
                        AdaptiveMenuItemRow(
                            label = itemLabel(item),
                            isSelected = selectedItem != null && (selectedItem == item || itemLabel(selectedItem) == itemLabel(item)),
                            onClick = {
                                onItemSelected(item)
                                onDismissRequest()
                            }
                        )
                    }
                }
            }
        }
    }
}

data class DropdownHeaderAction(
    val title: String,
    val isSelected: Boolean = false,
    val onClick: () -> Unit
)

@Composable
private fun AdaptiveMenuItemRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdaptiveDropdownMenu_Preview() {
    MyApplicationTheme {
        AdaptiveDropdownMenu(
            expanded = true,
            onDismissRequest = {},
            items = listOf(
                "Welding Shop", "Laser Cutting", "Fabrication", "Paint Shop",
                "Assembly", "Quality Control", "Tool Room", "Maintenance", "Stores"
            ),
            selectedItem = "Laser Cutting",
            onItemSelected = {}
        )
    }
}
