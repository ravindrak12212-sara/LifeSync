package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class LifeSyncViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LifeSyncRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = LifeSyncRepository(database.lifeSyncDao())
        
        // Prep empty database on start
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }
    }

    // --- Reactive Selected Date ---
    private val _selectedDate = MutableStateFlow(LocalDate.now().toString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    // --- User Profile ---
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // --- Habits & Completion Logs ---
    val habits: StateFlow<List<Habit>> = repository.habits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val habitLogs: StateFlow<List<HabitLog>> = repository.allHabitLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current selected date's logs
    val selectedDateHabitLogs: StateFlow<List<HabitLog>> = _selectedDate
        .flatMapLatest { date -> repository.getHabitLogsByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Planner Tasks ---
    val selectedDateTasks: StateFlow<List<PlannerTask>> = _selectedDate
        .flatMapLatest { date -> repository.getTasksByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<PlannerTask>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Goals ---
    val goals: StateFlow<List<UserGoal>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Journal Logs ---
    val selectedDateJournal: StateFlow<DailyJournal?> = _selectedDate
        .flatMapLatest { date -> repository.getJournalByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allJournals: StateFlow<List<DailyJournal>> = repository.allJournals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Health Logs ---
    val selectedDateHealthLog: StateFlow<HealthLog?> = _selectedDate
        .flatMapLatest { date -> repository.getHealthLogByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allHealthLogs: StateFlow<List<HealthLog>> = repository.allHealthLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Notes ---
    val notes: StateFlow<List<QuickNote>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- AI Coach Responses ---
    private val _aiCoachState = MutableStateFlow<AiCoachUiState>(AiCoachUiState.Idle)
    val aiCoachState: StateFlow<AiCoachUiState> = _aiCoachState.asStateFlow()

    // --- Theme Preference ---
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    // --- Methods & Actions ---

    // 1. Habit Operations
    fun addHabit(habit: Habit) {
        viewModelScope.launch {
            repository.insertHabit(habit)
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    fun toggleHabitLog(habitId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            val date = _selectedDate.value
            repository.logHabit(habitId, date, isCompleted)

            // Dynamic Gamification Engine
            val profile = userProfile.value ?: UserProfile()
            val xpGain = if (isCompleted) 15 else -15
            val coinGain = if (isCompleted) 5 else -5

            var newXp = (profile.xp + xpGain).coerceAtLeast(0)
            var newCoins = (profile.coins + coinGain).coerceAtLeast(0)
            var newLevel = profile.level

            // Level Up logic at 100 XP intervals
            if (newXp >= 100) {
                newLevel += 1
                newXp -= 100
            } else if (newXp < 0 && newLevel > 1) {
                newLevel -= 1
                newXp += 100
            }

            // Streak calculations
            var streak = profile.currentStreak
            var longestStreak = profile.longestStreak
            if (date == LocalDate.now().toString()) {
                if (isCompleted) {
                    streak = (streak + 1).coerceAtLeast(1)
                    if (streak > longestStreak) longestStreak = streak
                } else {
                    streak = (streak - 1).coerceAtLeast(0)
                }
            }

            repository.insertOrUpdateProfile(
                profile.copy(
                    xp = newXp,
                    coins = newCoins,
                    level = newLevel,
                    currentStreak = streak,
                    longestStreak = longestStreak
                )
            )
        }
    }

    // 2. Task Operations
    fun addTask(task: PlannerTask) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    fun updateTask(task: PlannerTask) {
        viewModelScope.launch {
            repository.updateTask(task)
            
            // Gamify completing a checklist task
            if (task.isCompleted) {
                val profile = userProfile.value ?: UserProfile()
                var newXp = profile.xp + 5
                var newLevel = profile.level
                if (newXp >= 100) {
                    newLevel += 1
                    newXp -= 100
                }
                repository.insertOrUpdateProfile(
                    profile.copy(xp = newXp, level = newLevel, coins = profile.coins + 2)
                )
            }
        }
    }

    fun deleteTask(task: PlannerTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // 3. Goal Operations
    fun addGoal(goal: UserGoal) {
        viewModelScope.launch {
            repository.insertGoal(goal)
        }
    }

    fun updateGoal(goal: UserGoal) {
        viewModelScope.launch {
            repository.updateGoal(goal)
            
            // Big rewards for milestone/goal completions
            if (goal.isCompleted) {
                val profile = userProfile.value ?: UserProfile()
                var newXp = profile.xp + 50
                var newLevel = profile.level
                if (newXp >= 100) {
                    newLevel += 1
                    newXp -= 100
                }
                repository.insertOrUpdateProfile(
                    profile.copy(xp = newXp, level = newLevel, coins = profile.coins + 20)
                )
            }
        }
    }

    fun deleteGoal(goal: UserGoal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    // 4. Journal / Mood Operations
    fun saveJournal(mood: String, text: String, gratitude: String, wins: String, mistakes: String, lessons: String) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val existing = selectedDateJournal.value
            val journal = existing?.copy(
                mood = mood,
                text = text,
                gratitude = gratitude,
                wins = wins,
                mistakes = mistakes,
                lessons = lessons
            ) ?: DailyJournal(
                date = date,
                mood = mood,
                text = text,
                gratitude = gratitude,
                wins = wins,
                mistakes = mistakes,
                lessons = lessons
            )
            repository.insertJournal(journal)

            // Log mood rewards!
            val profile = userProfile.value ?: UserProfile()
            var newXp = profile.xp + 10
            var newLevel = profile.level
            if (newXp >= 100) {
                newLevel += 1
                newXp -= 100
            }
            repository.insertOrUpdateProfile(
                profile.copy(xp = newXp, level = newLevel, coins = profile.coins + 3)
            )
        }
    }

    // 5. Health Log Operations
    fun logWater(ml: Int) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val existing = selectedDateHealthLog.value
            val healthLog = existing?.copy(
                waterIntakeMl = (existing.waterIntakeMl + ml).coerceAtLeast(0)
            ) ?: HealthLog(date = date, waterIntakeMl = ml)
            repository.insertHealthLog(healthLog)

            // Hydra-reward!
            val profile = userProfile.value ?: UserProfile()
            repository.insertOrUpdateProfile(profile.copy(xp = profile.xp + 1))
        }
    }

    fun logSteps(stepsCount: Int) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val existing = selectedDateHealthLog.value
            val healthLog = existing?.copy(steps = stepsCount)
                ?: HealthLog(date = date, steps = stepsCount)
            repository.insertHealthLog(healthLog)
        }
    }

    fun logSleep(hours: Double) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val existing = selectedDateHealthLog.value
            val healthLog = existing?.copy(sleepHours = hours)
                ?: HealthLog(date = date, sleepHours = hours)
            repository.insertHealthLog(healthLog)
        }
    }

    fun logCalories(cals: Int, protein: Int) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val existing = selectedDateHealthLog.value
            val healthLog = existing?.copy(calories = cals, proteinG = protein)
                ?: HealthLog(date = date, calories = cals, proteinG = protein)
            repository.insertHealthLog(healthLog)
        }
    }

    fun logWeight(weight: Double) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val existing = selectedDateHealthLog.value
            val healthLog = existing?.copy(weightKg = weight)
                ?: HealthLog(date = date, weightKg = weight)
            repository.insertHealthLog(healthLog)
        }
    }

    fun logVitals(hr: Int?, bp: String?) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val existing = selectedDateHealthLog.value
            val healthLog = existing?.copy(heartRate = hr, bloodPressure = bp)
                ?: HealthLog(date = date, heartRate = hr, bloodPressure = bp)
            repository.insertHealthLog(healthLog)
        }
    }

    // 6. Note Operations
    fun saveNote(title: String, content: String, isPinned: Boolean, folder: String) {
        viewModelScope.launch {
            repository.insertNote(
                QuickNote(
                    title = title,
                    content = content,
                    isPinned = isPinned,
                    folder = folder,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteNote(note: QuickNote) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // 7. Reset all profile & stats data (Gamification reset / debug option)
    fun resetAllData() {
        viewModelScope.launch {
            val profile = userProfile.value ?: UserProfile()
            repository.insertOrUpdateProfile(
                UserProfile(
                    username = profile.username,
                    email = profile.email,
                    xp = 0,
                    coins = 0,
                    level = 1,
                    currentStreak = 0,
                    longestStreak = 0
                )
            )
        }
    }

    // 8. Edit Profile Username
    fun updateUsername(name: String) {
        viewModelScope.launch {
            val profile = userProfile.value ?: UserProfile()
            repository.insertOrUpdateProfile(profile.copy(username = name))
        }
    }

    // 8.5 Complete Pomodoro Focus Session
    fun completeFocusSession() {
        viewModelScope.launch {
            val profile = userProfile.value ?: UserProfile()
            var newXp = profile.xp + 15
            var newLevel = profile.level
            if (newXp >= 100) {
                newLevel += 1
                newXp -= 100
            }
            repository.insertOrUpdateProfile(
                profile.copy(xp = newXp, level = newLevel, coins = profile.coins + 5)
            )
        }
    }

    // 9. Call AI Coach Advisor
    fun queryAiCoach() {
        viewModelScope.launch {
            _aiCoachState.value = AiCoachUiState.Loading
            val username = userProfile.value?.username ?: "Achiever"
            val activeHabits = habits.value
            val logs = habitLogs.value
            val tasks = selectedDateTasks.value
            val currentGoals = goals.value
            val journals = allJournals.value
            val health = allHealthLogs.value

            val advice = GeminiService.getCoachingSuggestion(
                username = username,
                habits = activeHabits,
                logs = logs,
                tasks = tasks,
                goals = currentGoals,
                journals = journals,
                healthLogs = health
            )
            _aiCoachState.value = AiCoachUiState.Success(advice)
        }
    }

    // --- Gemini Chatbot States ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _chatRole = MutableStateFlow("Life Coach")
    val chatRole: StateFlow<String> = _chatRole.asStateFlow()

    private val _chatModel = MutableStateFlow("gemini-3.1-flash-lite-preview")
    val chatModel: StateFlow<String> = _chatModel.asStateFlow()

    private val _useThinking = MutableStateFlow(false)
    val useThinking: StateFlow<Boolean> = _useThinking.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // Specific system role definitions
    val chatRoles = mapOf(
        "Life Coach" to "You are the LifeSync AI Coach, an elite, highly encouraging wellness guide. You help the user structure their habits, analyze routines, keep their motivation high, and reach their full potential. Use markdown list items for structure.",
        "Mindfulness Guide" to "You are the LifeSync Mindfulness Guide. Speak in a serene, peaceful, and poetic tone. Guide the user in breathing exercises, stress management, self-care, and finding tranquility in busy schedules.",
        "Fitness Trainer" to "You are the LifeSync Gym Coach. Speak with high energy, athletic drive, and motivational intensity! Push the user to complete their workouts, log water intake, track sleep, and reach new physiological peaks.",
        "Productivity Master" to "You are the LifeSync Productivity Consultant. Speak with extreme focus, clinical efficiency, and absolute time-management expertise. Help the user structure time blocks, destroy procrastination, and optimize schedules."
    )

    fun setChatRole(role: String) {
        _chatRole.value = role
    }

    fun setChatModel(model: String) {
        _chatModel.value = model
        if (model != "gemini-3.1-pro-preview") {
            _useThinking.value = false
        }
    }

    fun toggleThinking(enabled: Boolean) {
        _useThinking.value = enabled
    }

    fun clearChat() {
        _chatMessages.value = emptyList()
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val userMsg = ChatMessage(role = "user", text = text)
            val updatedList = _chatMessages.value + userMsg
            _chatMessages.value = updatedList
            _isChatLoading.value = true

            val systemInstruction = chatRoles[_chatRole.value] ?: ""
            val modelName = _chatModel.value
            val thinking = _useThinking.value

            val responseText = GeminiService.generateChatResponse(
                history = updatedList,
                roleInstruction = systemInstruction,
                modelName = modelName,
                useThinking = thinking
            )

            val modelMsg = ChatMessage(role = "model", text = responseText)
            _chatMessages.value = _chatMessages.value + modelMsg
            _isChatLoading.value = false
        }
    }
}

sealed interface AiCoachUiState {
    object Idle : AiCoachUiState
    object Loading : AiCoachUiState
    data class Success(val advice: String) : AiCoachUiState
}
