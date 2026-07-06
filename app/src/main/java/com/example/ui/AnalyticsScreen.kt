package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HabitLog
import com.example.data.HealthLog
import com.example.ui.components.CustomLineChart
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.HeatmapChart
import com.example.viewmodel.LifeSyncViewModel
import java.time.LocalDate

@Composable
fun AnalyticsScreen(
    viewModel: LifeSyncViewModel,
    modifier: Modifier = Modifier
) {
    val healthLogs by viewModel.allHealthLogs.collectAsState()
    val habitLogs by viewModel.habitLogs.collectAsState()
    val habits by viewModel.habits.collectAsState()

    var activeSubTab by remember { mutableStateOf("Charts") } // Charts, Badges

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        TabRow(
            selectedTabIndex = if (activeSubTab == "Charts") 0 else 1,
            modifier = Modifier.padding(vertical = 8.dp),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(selected = activeSubTab == "Charts", onClick = { activeSubTab = "Charts" }) {
                Text("Charts & Heatmap", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Tab(selected = activeSubTab == "Badges", onClick = { activeSubTab = "Badges" }) {
                Text("Achievements", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (activeSubTab == "Charts") {
                ChartsSubScreen(healthLogs = healthLogs, habitLogs = habitLogs)
            } else {
                BadgesSubScreen(habitLogs = habitLogs, healthLogs = healthLogs)
            }
        }
    }
}

@Composable
fun ChartsSubScreen(
    healthLogs: List<HealthLog>,
    habitLogs: List<HabitLog>
) {
    // Generate water data points (take last 7 days sorted)
    val sortedHealth = healthLogs.sortedBy { it.date }.takeLast(7)
    val waterPoints = sortedHealth.map { it.waterIntakeMl.toFloat() }
    val sleepPoints = sortedHealth.map { it.sleepHours.toFloat() }
    val stepsPoints = sortedHealth.map { it.steps.toFloat() }

    val labels = sortedHealth.map {
        try {
            val date = LocalDate.parse(it.date)
            date.dayOfWeek.name.take(3)
        } catch (e: Exception) {
            "Day"
        }
    }

    // Completion status for last 28 days for the heatmap
    val last28DaysCompletions = remember(habitLogs) {
        val today = LocalDate.now()
        val list = mutableListOf<Boolean>()
        for (i in 27 downTo 0) {
            val queryDate = today.minusDays(i.toLong()).toString()
            val hasLogs = habitLogs.any { it.date == queryDate }
            list.add(hasLogs)
        }
        list
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
    ) {
        // 1. Heatmap
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.padding(16.dp)) {
                    HeatmapChart(completions = last28DaysCompletions, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        // 2. Hydration line graph
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("💧 Hydration Trend (Last 7 Logs)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    CustomLineChart(
                        dataPoints = if (waterPoints.isEmpty()) listOf(0f, 250f, 1000f, 1500f, 2000f) else waterPoints,
                        labels = if (labels.isEmpty()) listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun") else labels,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        lineColor = Color(0xFF06B6D4)
                    )
                }
            }
        }

        // 3. Sleep line graph
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🛌 Sleep Quality Cycle (Hours)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    CustomLineChart(
                        dataPoints = if (sleepPoints.isEmpty()) listOf(6f, 7.5f, 5f, 8f, 6.5f, 7f, 8f) else sleepPoints,
                        labels = if (labels.isEmpty()) listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun") else labels,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        lineColor = Color(0xFF8B5CF6)
                    )
                }
            }
        }

        // 4. Steps line graph
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("👟 Activity Metric (Steps)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    CustomLineChart(
                        dataPoints = if (stepsPoints.isEmpty()) listOf(3000f, 5500f, 4000f, 8000f, 10000f, 6000f, 7500f) else stepsPoints,
                        labels = if (labels.isEmpty()) listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun") else labels,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        lineColor = Color(0xFF10B981)
                    )
                }
            }
        }
    }
}

@Composable
fun BadgesSubScreen(
    habitLogs: List<HabitLog>,
    healthLogs: List<HealthLog>
) {
    // Determine dynamic badges based on logs!
    val totalCompletions = habitLogs.size
    val maxSteps = healthLogs.maxOfOrNull { it.steps } ?: 0
    val maxWater = healthLogs.maxOfOrNull { it.waterIntakeMl } ?: 0

    val badgesList = listOf(
        BadgeItem(
            title = "First Step",
            description = "Complete your first recorded habit checklist item.",
            unlocked = totalCompletions >= 1,
            icon = "🔥",
            color = Color(0xFFEF4444)
        ),
        BadgeItem(
            title = "Consistency Pro",
            description = "Complete over 15 habit sessions successfully.",
            unlocked = totalCompletions >= 15,
            icon = "⭐",
            color = Color(0xFFFFB300)
        ),
        BadgeItem(
            title = "Water Champion",
            description = "Reach 2000ml or more hydration in a single day.",
            unlocked = maxWater >= 2000,
            icon = "💧",
            color = Color(0xFF0EA5E9)
        ),
        BadgeItem(
            title = "Cardio Runner",
            description = "Achieve a massive 10,000 steps logged dynamically.",
            unlocked = maxSteps >= 10000,
            icon = "⚡",
            color = Color(0xFF10B981)
        ),
        BadgeItem(
            title = "Self Reflection Guru",
            description = "Begin recording details in your Daily Mindful Journal.",
            unlocked = true, // Free badge on discovery
            icon = "🧘",
            color = Color(0xFF8B5CF6)
        )
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Achievements",
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Gamified Milestones",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        items(badgesList) { badge ->
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (badge.unlocked) badge.color.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (badge.unlocked) badge.color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (badge.unlocked) badge.icon else "🔒",
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = badge.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (badge.unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = badge.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (badge.unlocked) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Unlocked",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

data class BadgeItem(
    val title: String,
    val description: String,
    val unlocked: Boolean,
    val icon: String,
    val color: Color
)
