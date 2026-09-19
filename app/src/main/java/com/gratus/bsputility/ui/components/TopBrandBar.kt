package com.gratus.bsputility.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gratus.bsputility.ui.theme.IndustrialAmber600
import com.gratus.bsputility.ui.theme.IndustrialNavy900
import com.gratus.bsputility.ui.theme.MyApplicationTheme

@Composable
fun TopBrandBar(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = IndustrialNavy900,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(IndustrialAmber600),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Factory,
                        contentDescription = null,
                        tint = IndustrialNavy900,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "BSP Metatech LLP",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Chakan • Contract Labour & Staff Attendance",
                        fontSize = 11.sp,
                        color = IndustrialAmber600,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(name = "Top Brand Bar - Preview", showBackground = true)
@Composable
fun TopBrandBar_Preview() {
    MyApplicationTheme {
        TopBrandBar()
    }
}
