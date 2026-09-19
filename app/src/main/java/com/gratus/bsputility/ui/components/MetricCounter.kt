package com.gratus.bsputility.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gratus.bsputility.ui.theme.IndustrialAmber500

@Composable
fun MetricCounter(
    label: String,
    value: String,
    highlight: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = if (onClick != null) {
            Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable { onClick() }
                .padding(4.dp)
        } else {
            Modifier.padding(4.dp)
        }
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (highlight) IndustrialAmber500 else Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = if (highlight) 24.sp else 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}
