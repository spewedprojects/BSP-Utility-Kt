package com.gratus.bsputility.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gratus.bsputility.ui.theme.MyApplicationTheme

enum class NavigationTab(val route: String, val title: String, val icon: ImageVector) {
    Attendance("attendance", "Attendance", Icons.Default.FactCheck),
    Rooster("rooster", "Rooster", Icons.Default.People),
    Verification("verification", "Verify", Icons.Default.VerifiedUser),
    Reports("reports", "Reports", Icons.Default.Assessment),
    Settings("settings", "Settings", Icons.Default.Settings);
}

@Composable
fun AppNavigationBar(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        windowInsets = NavigationBarDefaults.windowInsets,
        tonalElevation = 6.dp,
        modifier = modifier.testTag("bottom_nav_bar")
    ) {
        NavigationTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.secondary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_tab_${tab.route}")
            )
        }
    }
}

@Preview(name = "App Navigation Bar - Preview", showBackground = true)
@Composable
fun AppNavigationBar_Preview() {
    MyApplicationTheme {
        AppNavigationBar(
            selectedTab = NavigationTab.Attendance,
            onTabSelected = {}
        )
    }
}
