package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import com.example.data.PlannerTask
import com.example.data.UserGoal
import com.example.ui.components.GlassmorphicCard
import com.example.viewmodel.LifeSyncViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    viewModel: LifeSyncViewModel,
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableStateOf("Schedule") } // Schedule, Tasks, Goals
    val tasks by viewModel.selectedDateTasks.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }

    var prefilledTimeBlock by remember { mutableStateOf<String?>(null) }
    var prefilledStart by remember { mutableStateOf<String?>(null) }
    var prefilledEnd by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Tab Selector Row
        TabRow(
            selectedTabIndex = when (activeSubTab) {
                "Schedule" -> 0
                "Tasks" -> 1
                else -> 2
            },
            modifier = Modifier.padding(vertical = 8.dp),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(selected = activeSubTab == "Schedule", onClick = { activeSubTab = "Schedule" }) {
                Text("Timetable", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Tab(selected = activeSubTab == "Tasks", onClick = { activeSubTab = "Tasks" }) {
                Text("Planner ToDo", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Tab(selected = activeSubTab == "Goals", onClick = { activeSubTab = "Goals" }) {
                Text("Goals", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        if (activeSubTab == "Schedule") {
            BlackboardHeader(
                selectedDate = selectedDate,
                onDaySelected = { viewModel.selectDate(it.toString()) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Box(modifier = Modifier.weight(1f)) {
            when (activeSubTab) {
                "Schedule" -> TimetableSubScreen(
                    tasks = tasks.filter { it.isTimetable },
                    allTasks = allTasks,
                    selectedDate = selectedDate,
                    onToggle = { viewModel.updateTask(it) },
                    onDelete = { viewModel.deleteTask(it) },
                    onAddPresetTasks = { presetTasks ->
                        presetTasks.forEach { viewModel.addTask(it) }
                    },
                    onSelectDate = { viewModel.selectDate(it) },
                    onAssignSlot = { periodName, startTime, endTime ->
                        prefilledTimeBlock = periodName
                        prefilledStart = startTime
                        prefilledEnd = endTime
                        showAddTaskDialog = true
                    }
                )
                "Tasks" -> DailyPlannerSubScreen(tasks = tasks, onToggle = { viewModel.updateTask(it) }, onDelete = { viewModel.deleteTask(it) })
                "Goals" -> GoalsSubScreen(goals = goals, onToggle = { viewModel.updateGoal(it) }, onDelete = { viewModel.deleteGoal(it) })
            }

            // FLOATING ACTION BUTTON (Add item)
            FloatingActionButton(
                onClick = {
                    if (activeSubTab == "Goals") {
                        showAddGoalDialog = true
                    } else {
                        prefilledTimeBlock = null
                        prefilledStart = null
                        prefilledEnd = null
                        showAddTaskDialog = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("planner_add_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    }

    // --- DIALOG MODALS ---
    if (showAddTaskDialog) {
        var title by remember { mutableStateOf("") }
        var isTimetable by remember { mutableStateOf(activeSubTab == "Schedule" || prefilledTimeBlock != null) }
        var timeBlock by remember { mutableStateOf(prefilledTimeBlock ?: (if (activeSubTab == "Schedule" || prefilledTimeBlock != null) "Early Morning Walk" else "Morning")) }
        var category by remember { mutableStateOf("Personal") }
        var startTime by remember { mutableStateOf(prefilledStart ?: "06:00") }
        var endTime by remember { mutableStateOf(prefilledEnd ?: "07:30") }
        var priority by remember { mutableStateOf("Medium") }

        val professionalPeriods = listOf(
            "Early Morning Walk", "Healthy Breakfast", "Morning Focus Work", "Afternoon Lunch",
            "Afternoon Focus/Meetings", "Evening Tea & Snacks", "Evening Exercise/Unwind", "Night Light Dinner", "Sleep Wind Down"
        )
        val generalBlocks = listOf("Morning", "Afternoon", "Evening", "Night")

        AlertDialog(
            onDismissRequest = {
                showAddTaskDialog = false
                prefilledTimeBlock = null
                prefilledStart = null
                prefilledEnd = null
            },
            title = { Text("Create Routine Slot/Task", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task / Activity Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_title_input"),
                        shape = RoundedCornerShape(16.dp)
                    )

                    // Timetable Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(checked = isTimetable, onCheckedChange = { 
                            isTimetable = it 
                            if (it) {
                                timeBlock = "Early Morning Walk"
                                startTime = "06:00"
                                endTime = "07:30"
                            } else {
                                timeBlock = "Morning"
                                startTime = "09:00"
                                endTime = "09:30"
                            }
                        })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add to Daily Routine Schedule", fontSize = 14.sp)
                    }

                    // Time Block Selector
                    Text(
                        text = if (isTimetable) "Daily Routine Slot" else "General Time Frame",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    val currentBlocks = if (isTimetable) professionalPeriods else generalBlocks
                    val scrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        currentBlocks.forEach { block ->
                            FilterChip(
                                selected = timeBlock == block,
                                onClick = { 
                                    timeBlock = block 
                                    if (isTimetable) {
                                        val times = getTimesForPeriod(block)
                                        startTime = times.first
                                        endTime = times.second
                                    }
                                },
                                label = { Text(block, fontSize = 11.sp) }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Start Time") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("End Time") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }

                    // Category Selector
                    Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Personal", "Office", "Reading", "Health").forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Priority Selector
                    Text("Priority", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("High", "Medium", "Low").forEach { p ->
                            FilterChip(
                                selected = priority == p,
                                onClick = { priority = p },
                                label = { Text(p, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotEmpty()) {
                            viewModel.addTask(
                                PlannerTask(
                                    title = title,
                                    category = category,
                                    date = selectedDate,
                                    timeBlock = timeBlock,
                                    startTime = startTime,
                                    endTime = endTime,
                                    priority = priority,
                                    isTimetable = isTimetable
                                )
                            )
                            showAddTaskDialog = false
                            prefilledTimeBlock = null
                            prefilledStart = null
                            prefilledEnd = null
                        }
                    },
                    modifier = Modifier.testTag("submit_task_button")
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddTaskDialog = false 
                    prefilledTimeBlock = null
                    prefilledStart = null
                    prefilledEnd = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddGoalDialog) {
        var goalTitle by remember { mutableStateOf("") }
        var goalCategory by remember { mutableStateOf("Learning") }
        var targetValue by remember { mutableStateOf("100") }
        var priority by remember { mutableStateOf("Medium") }

        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = { Text("Create Life Goal", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = goalTitle,
                        onValueChange = { goalTitle = it },
                        label = { Text("Goal Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("goal_title_input"),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Text("Goal Type", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Fitness", "Career", "Financial", "Learning", "Travel").forEach { cat ->
                            FilterChip(
                                selected = goalCategory == cat,
                                onClick = { goalCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = targetValue,
                        onValueChange = { targetValue = it },
                        label = { Text("Target progress numeric goal (e.g. 100)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Text("Priority", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("High", "Medium", "Low").forEach { p ->
                            FilterChip(
                                selected = priority == p,
                                onClick = { priority = p },
                                label = { Text(p, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (goalTitle.isNotEmpty()) {
                            viewModel.addGoal(
                                UserGoal(
                                    title = goalTitle,
                                    category = goalCategory,
                                    deadline = LocalDate.now().plusDays(30).toString(),
                                    priority = priority,
                                    targetValue = targetValue.toIntOrNull() ?: 100,
                                    currentValue = 0
                                )
                            )
                            showAddGoalDialog = false
                        }
                    },
                    modifier = Modifier.testTag("submit_goal_button")
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BlackboardHeader(
    selectedDate: String,
    onDaySelected: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val currentSelectedLocalDate = try {
        LocalDate.parse(selectedDate)
    } catch (e: Exception) {
        today
    }
    
    val dayOfWeekVal = currentSelectedLocalDate.dayOfWeek.value // 1 (Mon) to 7 (Sun)
    val monday = currentSelectedLocalDate.minusDays((dayOfWeekVal - 1).toLong())
    val weekDays = (0..6).map { monday.plusDays(it.toLong()) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E293B)) // Sleek slate background for professionals
            .border(4.dp, Color(0xFF64748B), RoundedCornerShape(16.dp)) // Carbon steel trim
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "💼 PROFESSIONAL ROUTINE BOARD",
                    color = Color(0xFFF8FAFC),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.testTag("blackboard_title")
                )
                Text(
                    text = "TAP WEEKDAYS TO ACCESS YOUR ASSIGNED SLOTS & HEALTH TASKS",
                    color = Color(0xFF94A3B8),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Icon(
                imageVector = Icons.Default.WatchLater,
                contentDescription = "Routine Board Indicator",
                tint = Color(0xFFFCD34D),
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            weekDays.forEach { day ->
                val isSelected = day.toString() == selectedDate
                val isToday = day == today
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                isSelected -> Color(0xFF334155) // Slate highlight
                                isToday -> Color(0xFF475569).copy(alpha = 0.4f)
                                else -> Color.Transparent
                            }
                        )
                        .clickable { onDaySelected(day) }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = day.dayOfWeek.name.take(3),
                        color = if (isSelected) Color(0xFFFCD34D) else Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = day.dayOfMonth.toString(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

fun getTimesForPeriod(period: String): Pair<String, String> {
    return when (period) {
        "Early Morning Walk" -> "06:00" to "07:30"
        "Healthy Breakfast" -> "08:00" to "08:30"
        "Morning Focus Work" -> "09:00" to "12:30"
        "Afternoon Lunch" -> "13:00" to "14:00"
        "Afternoon Focus/Meetings" -> "14:00" to "16:30"
        "Evening Tea & Snacks" -> "17:00" to "17:30"
        "Evening Exercise/Unwind" -> "18:00" to "19:30"
        "Night Light Dinner" -> "20:30" to "21:15"
        "Sleep Wind Down" -> "22:30" to "23:00"
        else -> "09:00" to "09:30"
    }
}

@Composable
fun EmptyPeriodRow(
    periodName: String,
    timeRange: String,
    onAssign: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAssign() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AddCircleOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = periodName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = timeRange,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
            
            Text(
                text = "Assign Activity",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun WeeklyGridDialog(
    allTasks: List<PlannerTask>,
    selectedDate: String,
    onDismiss: () -> Unit,
    onSelectDate: (String) -> Unit
) {
    val today = LocalDate.now()
    val currentSelectedLocalDate = try {
        LocalDate.parse(selectedDate)
    } catch (e: Exception) {
        today
    }
    
    val dayOfWeekVal = currentSelectedLocalDate.dayOfWeek.value
    val monday = currentSelectedLocalDate.minusDays((dayOfWeekVal - 1).toLong())
    val weekDays = (0..6).map { monday.plusDays(it.toLong()) }
    
    val periods = listOf(
        "Early Morning Walk" to "06:00",
        "Healthy Breakfast" to "08:00",
        "Morning Focus Work" to "09:00",
        "Afternoon Lunch" to "13:00",
        "Afternoon Focus/Meetings" to "14:00",
        "Evening Tea & Snacks" to "17:00",
        "Evening Exercise/Unwind" to "18:00",
        "Night Light Dinner" to "20:30",
        "Sleep Wind Down" to "22:30"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.GridOn,
                    contentDescription = "Weekly Grid",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Weekly Routine Board",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Scroll horizontally to view your full weekly routine layout. Tapping a day header navigates directly to that page.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                val horizontalScrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(horizontalScrollState)
                        .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Header Row: Day / Period
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp, 36.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Day \\ Slot", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            
                            periods.forEach { (periodName, startTime) ->
                                Box(
                                    modifier = Modifier
                                        .size(100.dp, 36.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(periodName, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                                        Text(startTime, fontSize = 8.sp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                                    }
                                }
                            }
                        }
                        
                        // Weekday Rows
                        weekDays.forEach { day ->
                            val isCurrentDay = day.toString() == selectedDate
                            val dayTasks = allTasks.filter { it.date == day.toString() && it.isTimetable }
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp, 48.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (isCurrentDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        )
                                        .clickable {
                                            onSelectDate(day.toString())
                                            onDismiss()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = day.dayOfWeek.name.take(3),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrentDay) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${day.dayOfMonth}",
                                            fontSize = 9.sp,
                                            color = if (isCurrentDay) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                periods.forEach { (periodName, _) ->
                                    val taskForPeriod = dayTasks.firstOrNull { it.timeBlock == periodName }
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp, 48.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                when {
                                                    taskForPeriod != null -> {
                                                        if (taskForPeriod.isCompleted) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                                        else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                                    }
                                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                                                }
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = when {
                                                    taskForPeriod != null -> {
                                                        if (taskForPeriod.isCompleted) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                                                        else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                                                    }
                                                    else -> Color.Transparent
                                                },
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .clickable {
                                                onSelectDate(day.toString())
                                                onDismiss()
                                            }
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (taskForPeriod != null) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center,
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                Text(
                                                    text = taskForPeriod.title.take(16) + (if (taskForPeriod.title.length > 16) ".." else ""),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center,
                                                    color = if (taskForPeriod.isCompleted) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (taskForPeriod.isCompleted) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Completed",
                                                        tint = MaterialTheme.colorScheme.tertiary,
                                                        modifier = Modifier.size(9.dp)
                                                    )
                                                }
                                            }
                                        } else {
                                            Text(
                                                text = "-",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close Board")
            }
        }
    )
}

@Composable
fun TimetableSubScreen(
    tasks: List<PlannerTask>,
    allTasks: List<PlannerTask>,
    selectedDate: String,
    onToggle: (PlannerTask) -> Unit,
    onDelete: (PlannerTask) -> Unit,
    onAddPresetTasks: (List<PlannerTask>) -> Unit,
    onSelectDate: (String) -> Unit,
    onAssignSlot: (String, String, String) -> Unit
) {
    var showQuickSetupDialog by remember { mutableStateOf(false) }
    var showWeeklyGridDialog by remember { mutableStateOf(false) }
    
    val periods = listOf(
        "Early Morning Walk" to "06:00 - 07:30",
        "Healthy Breakfast" to "08:00 - 08:30",
        "Morning Focus Work" to "09:00 - 12:30",
        "Afternoon Lunch" to "13:00 - 14:00",
        "Afternoon Focus/Meetings" to "14:00 - 16:30",
        "Evening Tea & Snacks" to "17:00 - 17:30",
        "Evening Exercise/Unwind" to "18:00 - 19:30",
        "Night Light Dinner" to "20:30 - 21:15",
        "Sleep Wind Down" to "22:30 - 23:00"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Daily Routine",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = { showWeeklyGridDialog = true },
                    modifier = Modifier.testTag("weekly_grid_text_button")
                ) {
                    Icon(Icons.Default.GridOn, contentDescription = "Full Grid", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Weekly Grid", fontSize = 13.sp)
                }
                
                TextButton(
                    onClick = { showQuickSetupDialog = true },
                    modifier = Modifier.testTag("quick_setup_text_button")
                ) {
                    Icon(Icons.Default.PlaylistAddCheck, contentDescription = "Setup", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Quick Setup", fontSize = 13.sp)
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
        ) {
            // Render Professional Periods
            periods.forEach { (periodName, timeRange) ->
                val periodTasks = tasks.filter { it.timeBlock == periodName }
                if (periodTasks.isNotEmpty()) {
                    item(key = periodName) {
                        Column {
                            Text(
                                text = periodName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            periodTasks.forEach { task ->
                                TimetableRow(task = task, onToggle = onToggle, onDelete = onDelete)
                            }
                        }
                    }
                } else {
                    item(key = "empty_$periodName") {
                        val times = getTimesForPeriod(periodName)
                        EmptyPeriodRow(
                            periodName = periodName,
                            timeRange = timeRange,
                            onAssign = { onAssignSlot(periodName, times.first, times.second) }
                        )
                    }
                }
            }

            // Render other random items (not matched to any routine period)
            val routinePeriodNames = periods.map { it.first }
            val customTasks = tasks.filter { it.timeBlock !in routinePeriodNames }
            if (customTasks.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Other Scheduled Activities",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(customTasks) { task ->
                    TimetableRow(task = task, onToggle = onToggle, onDelete = onDelete)
                }
            }
        }
    }

    if (showWeeklyGridDialog) {
        WeeklyGridDialog(
            allTasks = allTasks,
            selectedDate = selectedDate,
            onDismiss = { showWeeklyGridDialog = false },
            onSelectDate = onSelectDate
        )
    }

    if (showQuickSetupDialog) {
        var activePresetType by remember { mutableStateOf("Workday") } // Workday, Weekend
        
        val workdayPresets = listOf(
            PresetTask("🏃 Early Morning Walk & Hydration", "Health", "Early Morning Walk", "06:00", "07:30", "High"),
            PresetTask("🍳 Nutritious Balanced Breakfast", "Health", "Healthy Breakfast", "08:00", "08:30", "Medium"),
            PresetTask("💻 Deep Focus Work (No Distractions)", "Office", "Morning Focus Work", "09:00", "12:30", "High"),
            PresetTask("🍱 Light Lunch & 10 Min Stroll", "Health", "Afternoon Lunch", "13:00", "14:00", "Medium"),
            PresetTask("📈 PM Meetings & Action Items", "Office", "Afternoon Focus/Meetings", "14:00", "16:30", "Low"),
            PresetTask("☕ Green Tea & Avoid Junk Food! 🚫", "Health", "Evening Tea & Snacks", "17:00", "17:30", "High"),
            PresetTask("🏋️ Cardio Workout / Jogging", "Health", "Evening Exercise/Unwind", "18:00", "19:30", "High"),
            PresetTask("🥗 Fresh Light Home Dinner", "Health", "Night Light Dinner", "20:30", "21:15", "Medium"),
            PresetTask("📖 No Screens & Deep Sleep Prep", "Personal", "Sleep Wind Down", "22:30", "23:00", "Low")
        )

        val weekendPresets = listOf(
            PresetTask("🌅 Sunrise Walk & Light Yoga", "Health", "Early Morning Walk", "06:30", "08:00", "Medium"),
            PresetTask("🥞 Leisure Breakfast & Planning", "Personal", "Healthy Breakfast", "08:30", "09:15", "Low"),
            PresetTask("🌱 Personal Hobby & Upskilling", "Learning", "Morning Focus Work", "09:30", "12:30", "High"),
            PresetTask("🍱 Enjoy Cheat Meal Mindfully! 🍕", "Personal", "Afternoon Lunch", "13:00", "14:00", "Low"),
            PresetTask("🎬 Family Recreation & Outing", "Personal", "Afternoon Focus/Meetings", "14:30", "16:30", "Medium"),
            PresetTask("🍿 Fruit Bowl & Warm Camomile Tea", "Health", "Evening Tea & Snacks", "17:00", "17:30", "Low"),
            PresetTask("🚴 Sunset Cycling / Park Walk", "Health", "Evening Exercise/Unwind", "18:00", "19:30", "High"),
            PresetTask("🥘 Healthy Homemade Dinner", "Health", "Night Light Dinner", "20:30", "21:15", "Medium"),
            PresetTask("🧘 Sunset Gratitude & Sleep", "Health", "Sleep Wind Down", "22:30", "23:00", "Low")
        )

        var presets by remember(activePresetType) {
            mutableStateOf(
                if (activePresetType == "Workday") workdayPresets else weekendPresets
            )
        }

        AlertDialog(
            onDismissRequest = { showQuickSetupDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlaylistAddCheck, contentDescription = "Setup Roster", tint = MaterialTheme.colorScheme.primary)
                    Text("Routine Setup", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Customize today's schedule using Workday or Weekend presets. Track your walk, breakfast, healthy meals, and sleep times!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { activePresetType = "Workday" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activePresetType == "Workday") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("💼 Workday", color = if (activePresetType == "Workday") Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                        Button(
                            onClick = { activePresetType = "Weekend" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activePresetType == "Weekend") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🎉 Weekend", color = if (activePresetType == "Weekend") Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val allSelected = presets.all { it.isSelected }
                        TextButton(
                            onClick = {
                                presets = presets.map { it.copy(isSelected = !allSelected) }
                            }
                        ) {
                            Text(if (allSelected) "Deselect All" else "Select All", fontSize = 12.sp)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                    val dialogScrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .verticalScroll(dialogScrollState),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEachIndexed { index, preset ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        presets = presets.mapIndexed { i, p ->
                                            if (i == index) p.copy(isSelected = !p.isSelected) else p
                                        }
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = preset.isSelected,
                                    onCheckedChange = { isChecked ->
                                        presets = presets.mapIndexed { i, p ->
                                            if (i == index) p.copy(isSelected = isChecked) else p
                                        }
                                    },
                                    modifier = Modifier.testTag("preset_checkbox_${index}")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(preset.title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("${preset.startTime} - ${preset.endTime}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(preset.category, fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toAdd = presets.filter { it.isSelected }.map {
                            PlannerTask(
                                title = it.title,
                                category = it.category,
                                date = selectedDate,
                                timeBlock = it.timeBlock,
                                startTime = it.startTime,
                                endTime = it.endTime,
                                priority = it.priority,
                                isTimetable = true
                            )
                        }
                        if (toAdd.isNotEmpty()) {
                            onAddPresetTasks(toAdd)
                        }
                        showQuickSetupDialog = false
                    },
                    modifier = Modifier.testTag("apply_routine_setup_button")
                ) {
                    Text("Apply Time Table")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickSetupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

data class PresetTask(
    val title: String,
    val category: String,
    val timeBlock: String,
    val startTime: String,
    val endTime: String,
    val priority: String,
    val isSelected: Boolean = true
)

@Composable
fun TimetableRow(task: PlannerTask, onToggle: (PlannerTask) -> Unit, onDelete: (PlannerTask) -> Unit) {
    val priorityColor = when (task.priority) {
        "High" -> Color(0xFFEF4444)
        "Medium" -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    var isExpanded by remember { mutableStateOf(false) }
    val checklist = remember(task.notes, task.title) { parseChecklist(task.title, task.notes) }
    val completedCount = checklist.count { it.isChecked }
    val totalCount = checklist.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(6.dp, 36.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(priorityColor)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(task.title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${task.startTime ?: "09:00"} - ${task.endTime ?: "09:30"} | ${task.category}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (totalCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Habits: $completedCount/$totalCount",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { onToggle(task.copy(isCompleted = it)) },
                        modifier = Modifier.testTag("timetable_checkbox_${task.id}")
                    )
                    IconButton(onClick = { onDelete(task) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Expanded checklist
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, start = 18.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "📋 Activity Habit Logger Checklist",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // Sub-item progress
                    if (totalCount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Text(
                                text = "${(progress * 100).toInt()}% Done",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Render checklist items
                    checklist.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val updatedChecklist = checklist.mapIndexed { i, cItem ->
                                        if (i == index) cItem.copy(isChecked = !cItem.isChecked) else cItem
                                    }
                                    val allChecked = updatedChecklist.all { it.isChecked }
                                    onToggle(
                                        task.copy(
                                            notes = formatChecklist(updatedChecklist),
                                            isCompleted = allChecked // Auto-check parent task if all habits are complete
                                        )
                                    )
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (item.isChecked) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .border(1.5.dp, if (item.isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (item.isChecked) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Checked",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = item.text,
                                fontSize = 13.sp,
                                color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Delete button to remove custom items
                            IconButton(
                                onClick = {
                                    val updatedChecklist = checklist.toMutableList().apply { removeAt(index) }
                                    onToggle(task.copy(notes = formatChecklist(updatedChecklist)))
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Remove habit item",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Input to add custom habit
                    var newHabitText by remember { mutableStateOf("") }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newHabitText,
                            onValueChange = { newHabitText = it },
                            placeholder = { Text("Add custom habit...", fontSize = 12.sp) },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                        IconButton(
                            onClick = {
                                if (newHabitText.isNotBlank()) {
                                    val updatedChecklist = checklist + RoutineChecklistItem(newHabitText.trim(), false)
                                    onToggle(task.copy(notes = formatChecklist(updatedChecklist)))
                                    newHabitText = ""
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add custom habit",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class RoutineChecklistItem(
    val text: String,
    val isChecked: Boolean
)

fun parseChecklist(title: String, notes: String): List<RoutineChecklistItem> {
    val lines = notes.lines().filter { it.isNotBlank() }
    val hasChecklist = lines.any { it.startsWith("[ ]") || it.startsWith("[x]") }
    
    if (hasChecklist) {
        return lines.map { line ->
            val isChecked = line.startsWith("[x]")
            val text = line.removePrefix("[x]").removePrefix("[ ]").trim()
            RoutineChecklistItem(text, isChecked)
        }
    }
    
    // Fallback: Generate default checklist items based on activity name
    val lowerTitle = title.lowercase()
    val defaults = when {
        lowerTitle.contains("walk") -> listOf("Walk 5,000 steps", "Stay hydrated (Drink water)")
        lowerTitle.contains("breakfast") -> listOf("Eat healthy proteins & fiber", "Avoid sugars & processed foods")
        lowerTitle.contains("focus work") || lowerTitle.contains("deep work") || lowerTitle.contains("morning focus") || lowerTitle.contains("meetings") -> listOf("Keep phone away / Silent mode", "Identify & complete 1 main task")
        lowerTitle.contains("lunch") -> listOf("Eat a balanced, portion-controlled meal", "10-minute light stroll after eating")
        lowerTitle.contains("meeting") || lowerTitle.contains("afternoon focus") -> listOf("Stay organized & take clear notes", "Do a 2-minute posture stretch")
        lowerTitle.contains("snacks") || lowerTitle.contains("tea") -> listOf("Drink green tea or warm water", "Say NO to bakery junk food 🚫")
        lowerTitle.contains("exercise") || lowerTitle.contains("workout") || lowerTitle.contains("jogging") || lowerTitle.contains("cycling") -> listOf("30 mins of physical activity", "Perform post-workout stretching")
        lowerTitle.contains("dinner") -> listOf("Eat a light homemade dinner", "Finish eating 2 hours before sleep")
        lowerTitle.contains("sleep") || lowerTitle.contains("wind down") -> listOf("No electronic screens 30 mins before sleep", "Log daily wins or express gratitude")
        else -> listOf("Complete main goal of the slot", "Stay mindful and focused")
    }
    
    return defaults.map { RoutineChecklistItem(it, false) }
}

fun formatChecklist(items: List<RoutineChecklistItem>): String {
    return items.joinToString("\n") { item ->
        val prefix = if (item.isChecked) "[x]" else "[ ]"
        "$prefix ${item.text}"
    }
}

@Composable
fun DailyPlannerSubScreen(
    tasks: List<PlannerTask>,
    onToggle: (PlannerTask) -> Unit,
    onDelete: (PlannerTask) -> Unit
) {
    if (tasks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No To-Dos logged for today. Tap + to add!", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
        ) {
            items(tasks) { task ->
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = task.category, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurfaceVariant))
                                Text(text = "Priority: ${task.priority}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = task.isCompleted, onCheckedChange = { onToggle(task.copy(isCompleted = it)) })
                            IconButton(onClick = { onDelete(task) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoalsSubScreen(
    goals: List<UserGoal>,
    onToggle: (UserGoal) -> Unit,
    onDelete: (UserGoal) -> Unit
) {
    if (goals.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No Life Goals created yet. Tap + to set a target!", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
        ) {
            items(goals) { goal ->
                val progressPercent = if (goal.targetValue > 0) goal.currentValue.toFloat() / goal.targetValue else 0f

                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(goal.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Category: ${goal.category} | Deadline: ${goal.deadline}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { onDelete(goal) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { progressPercent },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Text(text = "${goal.currentValue} / ${goal.targetValue}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    val newVal = (goal.currentValue + 10).coerceAtMost(goal.targetValue)
                                    onToggle(goal.copy(currentValue = newVal, isCompleted = newVal >= goal.targetValue))
                                }
                            ) {
                                Text("+10 Progress")
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Complete", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Checkbox(
                                    checked = goal.isCompleted,
                                    onCheckedChange = {
                                        onToggle(goal.copy(isCompleted = it, currentValue = if (it) goal.targetValue else 0))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
