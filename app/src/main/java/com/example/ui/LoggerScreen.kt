package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassmorphicCard
import com.example.viewmodel.LifeSyncViewModel

@Composable
fun LoggerScreen(
    viewModel: LifeSyncViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val journal by viewModel.selectedDateJournal.collectAsState()
    val healthLog by viewModel.selectedDateHealthLog.collectAsState()

    var activeSubTab by remember { mutableStateOf("Journal") } // Journal, Health, Routine

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Tab Selector Row
        TabRow(
            selectedTabIndex = when (activeSubTab) {
                "Journal" -> 0
                "Health" -> 1
                else -> 2
            },
            modifier = Modifier.padding(vertical = 8.dp),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(selected = activeSubTab == "Journal", onClick = { activeSubTab = "Journal" }) {
                Text("Journal & Mood", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Tab(selected = activeSubTab == "Health", onClick = { activeSubTab = "Health" }) {
                Text("Health Metrics", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Tab(selected = activeSubTab == "Routine", onClick = { activeSubTab = "Routine" }) {
                Text("Routine Checklist", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (activeSubTab == "Journal") {
                JournalSubScreen(
                    selectedDate = selectedDate,
                    currentMood = journal?.mood ?: "Normal",
                    currentText = journal?.text ?: "",
                    currentGratitude = journal?.gratitude ?: "",
                    currentWins = journal?.wins ?: "",
                    currentMistakes = journal?.mistakes ?: "",
                    currentLessons = journal?.lessons ?: "",
                    onSave = { mood, txt, grat, wins, mist, less ->
                        viewModel.saveJournal(mood, txt, grat, wins, mist, less)
                    }
                )
            } else if (activeSubTab == "Health") {
                HealthMetricsSubScreen(
                    currentWater = healthLog?.waterIntakeMl ?: 0,
                    currentSteps = healthLog?.steps ?: 0,
                    currentSleep = healthLog?.sleepHours ?: 0.0,
                    currentCals = healthLog?.calories ?: 0,
                    currentProtein = healthLog?.proteinG ?: 0,
                    currentWeight = healthLog?.weightKg ?: 70.0,
                    currentHr = healthLog?.heartRate,
                    currentBp = healthLog?.bloodPressure,
                    onSaveWater = { viewModel.logWater(it) },
                    onSaveSteps = { viewModel.logSteps(it) },
                    onSaveSleep = { viewModel.logSleep(it) },
                    onSaveNutri = { cals, prot -> viewModel.logCalories(cals, prot) },
                    onSaveWeight = { viewModel.logWeight(it) },
                    onSaveVitals = { hr, bp -> viewModel.logVitals(hr, bp) }
                )
            } else {
                RoutineChecklistSubScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun JournalSubScreen(
    selectedDate: String,
    currentMood: String,
    currentText: String,
    currentGratitude: String,
    currentWins: String,
    currentMistakes: String,
    currentLessons: String,
    onSave: (String, String, String, String, String, String) -> Unit
) {
    var mood by remember(currentMood) { mutableStateOf(currentMood) }
    var text by remember(currentText) { mutableStateOf(currentText) }
    var gratitude by remember(currentGratitude) { mutableStateOf(currentGratitude) }
    var wins by remember(currentWins) { mutableStateOf(currentWins) }
    var mistakes by remember(currentMistakes) { mutableStateOf(currentMistakes) }
    var lessons by remember(currentLessons) { mutableStateOf(currentLessons) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val moods = listOf(
        "Excellent" to "🤩",
        "Happy" to "😊",
        "Normal" to "😐",
        "Sad" to "😢",
        "Stressed" to "🤯",
        "Angry" to "😡"
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
    ) {
        // Mood Selector Grid
        item {
            Text(
                text = "How are you feeling?",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                moods.forEach { (name, emoji) ->
                    val isSelected = mood == name
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.25f))
                            .border(
                                1.5.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.4f),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { mood = name }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(text = emoji, fontSize = 28.sp)
                        Text(text = name, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Journal Prompts
        item {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Daily Reflections & Notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .testTag("journal_notes_input"),
                maxLines = 5,
                shape = RoundedCornerShape(16.dp)
            )
        }

        item {
            OutlinedTextField(
                value = gratitude,
                onValueChange = { gratitude = it },
                label = { Text("What are you grateful for today?") },
                placeholder = { Text("List 1-3 simple things...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                shape = RoundedCornerShape(16.dp)
            )
        }

        item {
            OutlinedTextField(
                value = wins,
                onValueChange = { wins = it },
                label = { Text("Today's Wins & Successes") },
                placeholder = { Text("Any positive milestone...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                shape = RoundedCornerShape(16.dp)
            )
        }

        item {
            OutlinedTextField(
                value = mistakes,
                onValueChange = { mistakes = it },
                label = { Text("Mistakes / Stumbles") },
                placeholder = { Text("What went sideways?") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                shape = RoundedCornerShape(16.dp)
            )
        }

        item {
            OutlinedTextField(
                value = lessons,
                onValueChange = { lessons = it },
                label = { Text("Lessons Learned") },
                placeholder = { Text("What did today teach you?") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                shape = RoundedCornerShape(16.dp)
            )
        }

        item {
            Button(
                onClick = {
                    onSave(mood, text, gratitude, wins, mistakes, lessons)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_journal_button"),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = "Save")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Reflection Log (+10 XP)")
            }
        }
    }
}

@Composable
fun HealthMetricsSubScreen(
    currentWater: Int,
    currentSteps: Int,
    currentSleep: Double,
    currentCals: Int,
    currentProtein: Int,
    currentWeight: Double,
    currentHr: Int?,
    currentBp: String?,
    onSaveWater: (Int) -> Unit,
    onSaveSteps: (Int) -> Unit,
    onSaveSleep: (Double) -> Unit,
    onSaveNutri: (Int, Int) -> Unit,
    onSaveWeight: (Double) -> Unit,
    onSaveVitals: (Int?, String?) -> Unit
) {
    var stepsInput by remember(currentSteps) { mutableStateOf(currentSteps.toString()) }
    var sleepInput by remember(currentSleep) { mutableStateOf(currentSleep.toString()) }
    var calsInput by remember(currentCals) { mutableStateOf(currentCals.toString()) }
    var proteinInput by remember(currentProtein) { mutableStateOf(currentProtein.toString()) }
    var weightInput by remember(currentWeight) { mutableStateOf(currentWeight.toString()) }
    var hrInput by remember(currentHr) { mutableStateOf(currentHr?.toString() ?: "") }
    var bpInput by remember(currentBp) { mutableStateOf(currentBp ?: "") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp)
    ) {
        // Hydration quick log
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("💧 Hydration logger", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Quickly adjust hydration logged for today", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledIconButton(onClick = { onSaveWater(-250) }) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Sub")
                        }
                        Text("$currentWater ml", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        FilledIconButton(onClick = { onSaveWater(250) }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                }
            }
        }

        // Steps Tracker Card
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("👟 Daily Steps", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = stepsInput,
                            onValueChange = { stepsInput = it },
                            label = { Text("Steps") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(onClick = { onSaveSteps(stepsInput.toIntOrNull() ?: 0) }) {
                            Text("Save")
                        }
                    }
                }
            }
        }

        // Sleep Tracker Card
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🛌 Sleep Duration", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = sleepInput,
                            onValueChange = { sleepInput = it },
                            label = { Text("Hours Slept") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(onClick = { onSaveSleep(sleepInput.toDoubleOrNull() ?: 0.0) }) {
                            Text("Save")
                        }
                    }
                }
            }
        }

        // Nutrition Card (Calories / Protein)
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🍎 Nutrition (Calories & Protein)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = calsInput,
                            onValueChange = { calsInput = it },
                            label = { Text("Calories (kcal)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        OutlinedTextField(
                            value = proteinInput,
                            onValueChange = { proteinInput = it },
                            label = { Text("Protein (g)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onSaveNutri(calsInput.toIntOrNull() ?: 0, proteinInput.toIntOrNull() ?: 0) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Log Food intake")
                    }
                }
            }
        }

        // Weight Card
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⚖️ Weight Tracker", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = { Text("Weight (kg)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(onClick = { onSaveWeight(weightInput.toDoubleOrNull() ?: 70.0) }) {
                            Text("Save")
                        }
                    }
                }
            }
        }

        // Vitals card (Heart Rate / Blood Pressure)
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("❤️ Manual Vitals Logger", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = hrInput,
                            onValueChange = { hrInput = it },
                            label = { Text("Heart Rate (bpm)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        OutlinedTextField(
                            value = bpInput,
                            onValueChange = { bpInput = it },
                            label = { Text("Blood Pressure (systolic/diastolic)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { onSaveVitals(hrInput.toIntOrNull(), bpInput.ifEmpty { null }) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Log Vital Diagnostics")
                    }
                }
            }
        }
    }
}

@Composable
fun RoutineChecklistSubScreen(
    viewModel: LifeSyncViewModel
) {
    val tasks by viewModel.selectedDateTasks.collectAsState()
    val timetableTasks = remember(tasks) { tasks.filter { it.isTimetable } }
    
    if (timetableTasks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EventNote,
                    contentDescription = "No tasks",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No daily routine scheduled for today.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Go to the Planner tab to set up your routine!",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
        ) {
            item {
                Text(
                    text = "Habit check-off for scheduled routine activities",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            
            items(timetableTasks) { task ->
                var isExpanded by remember { mutableStateOf(true) } // Keep them expanded in logger screen for easy check-off!
                val priorityColor = when (task.priority) {
                    "High" -> Color(0xFFEF4444)
                    "Medium" -> Color(0xFFF59E0B)
                    else -> Color(0xFF10B981)
                }

                val checklist = remember(task.notes, task.title) { parseChecklist(task.title, task.notes) }
                val completedCount = checklist.count { it.isChecked }
                val totalCount = checklist.size
                val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth()
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
                                    Text(task.title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${task.startTime ?: "06:00"} - ${task.endTime ?: "07:30"}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { viewModel.updateTask(task.copy(isCompleted = it)) }
                                )
                                IconButton(onClick = { isExpanded = !isExpanded }) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Expand"
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, start = 18.dp)
                            ) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.height(8.dp))

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
                                            text = "$completedCount/$totalCount Done",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

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
                                                viewModel.updateTask(
                                                    task.copy(
                                                        notes = formatChecklist(updatedChecklist),
                                                        isCompleted = allChecked
                                                    )
                                                )
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
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
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = item.text,
                                            fontSize = 12.sp,
                                            color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
