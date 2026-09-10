package com.gratus.bsputility

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gratus.bsputility.ui.components.DateNavigationBar
import com.gratus.bsputility.ui.screens.AttendanceScreen
import com.gratus.bsputility.ui.screens.MasterDataScreen
import com.gratus.bsputility.ui.screens.ReportsScreen
import com.gratus.bsputility.ui.screens.RoosterScreen
import com.gratus.bsputility.ui.screens.VerificationScreen
import com.gratus.bsputility.ui.theme.IndustrialAmber600
import com.gratus.bsputility.ui.theme.IndustrialNavy900
import com.gratus.bsputility.ui.theme.MyApplicationTheme
import com.gratus.bsputility.ui.viewmodel.ManpowerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDark = androidx.compose.foundation.isSystemInDarkTheme()

            LaunchedEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    },
                    navigationBarStyle = if (isDark) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    }
                )
            }
            MyApplicationTheme {
                val viewModel: ManpowerViewModel = viewModel()
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

enum class NavigationTab(val route: String, val title: String, val icon: ImageVector) {
    Attendance("attendance", "Attendance", Icons.Default.FactCheck),
    Rooster("rooster", "Rooster", Icons.Default.People),
    Verification("verification", "Verify", Icons.Default.VerifiedUser),
    Reports("reports", "Reports", Icons.Default.Assessment),
    Settings("settings", "Settings", Icons.Default.Settings);
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: ManpowerViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(NavigationTab.Attendance) }
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Brand Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = IndustrialNavy900,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
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

                // Date Navigation Bar (visible on Attendance, Verification, and Reports)
                if (selectedTab != NavigationTab.Settings && selectedTab != NavigationTab.Rooster) {
                    DateNavigationBar(
                        selectedDate = selectedDate,
                        onPreviousDay = { viewModel.selectPreviousDay() },
                        onNextDay = { viewModel.selectNextDay() },
                        onDateSelected = { viewModel.selectDate(it) }
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
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
                        onClick = { selectedTab = tab },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = IndustrialAmber600,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                NavigationTab.Attendance -> AttendanceScreen(viewModel = viewModel)
                NavigationTab.Rooster -> RoosterScreen(viewModel = viewModel)
                NavigationTab.Verification -> VerificationScreen(viewModel = viewModel)
                NavigationTab.Reports -> ReportsScreen(viewModel = viewModel)
                NavigationTab.Settings -> MasterDataScreen(viewModel = viewModel)
            }
        }
    }
}
