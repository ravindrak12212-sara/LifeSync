package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.LightBG
import com.example.viewmodel.LifeSyncViewModel
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: LifeSyncViewModel = viewModel()
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                var activeTab by remember { mutableStateOf("Home") }
                var showSettings by remember { mutableStateOf(false) }

                // Top level Scaffold for responsive screen layouts
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent, // Let the background gradient show through
                    bottomBar = {
                        val isLight = MaterialTheme.colorScheme.background == LightBG
                        val navBorderColor = if (isLight) Color(0xFFE2E8F0).copy(alpha = 0.6f) else Color(0xFF334155).copy(alpha = 0.6f)
                        NavigationBar(
                            modifier = Modifier
                                .testTag("bottom_nav_bar")
                                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                .border(width = 1.dp, color = navBorderColor, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                            containerColor = if (isLight) Color.White.copy(alpha = 0.85f) else Color(0xFF1E293B).copy(alpha = 0.85f),
                            tonalElevation = 0.dp
                        ) {
                            NavigationBarItem(
                                selected = activeTab == "Home",
                                onClick = { activeTab = "Home" },
                                label = { Text("Home") },
                                icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
                                modifier = Modifier.testTag("nav_home_tab")
                            )
                            NavigationBarItem(
                                selected = activeTab == "Planner",
                                onClick = { activeTab = "Planner" },
                                label = { Text("Planner") },
                                icon = { Icon(imageVector = Icons.Default.EventNote, contentDescription = "Planner") },
                                modifier = Modifier.testTag("nav_planner_tab")
                            )
                            NavigationBarItem(
                                selected = activeTab == "Logger",
                                onClick = { activeTab = "Logger" },
                                label = { Text("Logger") },
                                icon = { Icon(imageVector = Icons.Default.EditNote, contentDescription = "Logger") },
                                modifier = Modifier.testTag("nav_logger_tab")
                            )
                            NavigationBarItem(
                                selected = activeTab == "Analytics",
                                onClick = { activeTab = "Analytics" },
                                label = { Text("Analytics") },
                                icon = { Icon(imageVector = Icons.Default.Analytics, contentDescription = "Analytics") },
                                modifier = Modifier.testTag("nav_analytics_tab")
                            )
                            NavigationBarItem(
                                selected = activeTab == "Coach",
                                onClick = { activeTab = "Coach" },
                                label = { Text("AI Coach") },
                                icon = { Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Coach") },
                                modifier = Modifier.testTag("nav_coach_tab")
                            )
                        }
                    }
                ) { innerPadding ->
                    val isLight = MaterialTheme.colorScheme.background == LightBG
                    val backgroundBrush = if (isLight) {
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFF3F4F6), Color(0xFFEEF2FF), Color(0xFFE0F2FE))
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF0B132B))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundBrush)
                            .padding(innerPadding)
                    ) {
                        when (activeTab) {
                            "Home" -> DashboardScreen(
                                viewModel = viewModel,
                                onOpenSettings = { showSettings = true }
                            )
                            "Planner" -> PlannerScreen(viewModel = viewModel)
                            "Logger" -> LoggerScreen(viewModel = viewModel)
                            "Analytics" -> AnalyticsScreen(viewModel = viewModel)
                            "Coach" -> CoachScreen(viewModel = viewModel)
                        }

                        // Floating Settings Modal as responsive overlay
                        if (showSettings) {
                            SettingsModal(
                                viewModel = viewModel,
                                onDismiss = { showSettings = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
