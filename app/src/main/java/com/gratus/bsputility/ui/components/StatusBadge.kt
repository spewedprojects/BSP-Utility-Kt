package com.gratus.bsputility.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gratus.bsputility.data.models.EmployeeStatuses
import com.gratus.bsputility.ui.theme.AbsentRed
import com.gratus.bsputility.ui.theme.AbsentRedDarkBg
import com.gratus.bsputility.ui.theme.AbsentRedDarkText
import com.gratus.bsputility.ui.theme.AbsentRedLight
import com.gratus.bsputility.ui.theme.IndustrialRed600
import com.gratus.bsputility.ui.theme.MyApplicationTheme
import com.gratus.bsputility.ui.theme.PresentGreen
import com.gratus.bsputility.ui.theme.PresentGreenDark
import com.gratus.bsputility.ui.theme.PresentGreenDarkBg
import com.gratus.bsputility.ui.theme.PresentGreenDarkText
import com.gratus.bsputility.ui.theme.PresentGreenLight
import com.gratus.bsputility.ui.theme.StatusActive
import com.gratus.bsputility.ui.theme.StatusActiveBg
import com.gratus.bsputility.ui.theme.StatusActiveBgDark
import com.gratus.bsputility.ui.theme.StatusActiveDark
import com.gratus.bsputility.ui.theme.StatusDebarred
import com.gratus.bsputility.ui.theme.StatusDebarredBg
import com.gratus.bsputility.ui.theme.StatusDebarredBgDark
import com.gratus.bsputility.ui.theme.StatusDebarredDark
import com.gratus.bsputility.ui.theme.StatusOut
import com.gratus.bsputility.ui.theme.StatusOutBg
import com.gratus.bsputility.ui.theme.StatusOutBgDark
import com.gratus.bsputility.ui.theme.StatusOutDark

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val (bgColor, textColor, borderColor, icon) = when (status) {
        EmployeeStatuses.ACTIVE -> if (isDark) {
            Quad(StatusActiveBgDark.copy(alpha = 0.5f), StatusActiveDark, StatusActiveDark.copy(alpha = 0.4f), Icons.Default.CheckCircle)
        } else {
            Quad(StatusActiveBg, StatusActive, StatusActive.copy(alpha = 0.3f), Icons.Default.CheckCircle)
        }
        EmployeeStatuses.DEBARRED -> if (isDark) {
            Quad(StatusDebarredBgDark.copy(alpha = 0.5f), StatusDebarredDark, StatusDebarredDark.copy(alpha = 0.5f), Icons.Default.Warning)
        } else {
            Quad(StatusDebarredBg, StatusDebarred, StatusDebarred.copy(alpha = 0.4f), Icons.Default.Warning)
        }
        EmployeeStatuses.OUT -> if (isDark) {
            Quad(StatusOutBgDark.copy(alpha = 0.5f), StatusOutDark, StatusOutDark.copy(alpha = 0.3f), Icons.Default.Block)
        } else {
            Quad(StatusOutBg, StatusOut, StatusOut.copy(alpha = 0.3f), Icons.Default.Block)
        }
        else -> if (isDark) {
            Quad(StatusActiveBgDark.copy(alpha = 0.5f), StatusActiveDark, StatusActiveDark.copy(alpha = 0.4f), Icons.Default.CheckCircle)
        } else {
            Quad(StatusActiveBg, StatusActive, StatusActive.copy(alpha = 0.3f), Icons.Default.CheckCircle)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(0.75.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = status,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = status.uppercase(),
                color = textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun AttendanceBadge(
    isPresent: Boolean,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val (bgColor, textColor, text) = if (isPresent) {
        if (isDark) {
            Triple(PresentGreenDarkBg.copy(alpha = 0.45f), PresentGreenDarkText, "PRESENT")
        } else {
            Triple(PresentGreenLight, PresentGreenDark, "PRESENT")
        }
    } else {
        if (isDark) {
            Triple(AbsentRedDarkBg.copy(alpha = 0.45f), AbsentRedDarkText, "ABSENT")
        } else {
            Triple(AbsentRedLight, IndustrialRed600, "ABSENT")
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

// ==========================================
// PREVIEWS
// ==========================================

@Preview(name = "Status Badges - All States", showBackground = true)
@Composable
fun StatusBadgePreview_All() {
    MyApplicationTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(status = EmployeeStatuses.ACTIVE)
                StatusBadge(status = EmployeeStatuses.DEBARRED)
                StatusBadge(status = EmployeeStatuses.OUT)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AttendanceBadge(isPresent = true)
                AttendanceBadge(isPresent = false)
            }
        }
    }
}

@Preview(name = "Status Badges - Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StatusBadgePreview_DarkTheme() {
    MyApplicationTheme(darkTheme = true) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusBadge(status = EmployeeStatuses.ACTIVE)
            StatusBadge(status = EmployeeStatuses.DEBARRED)
            StatusBadge(status = EmployeeStatuses.OUT)
        }
    }
}
