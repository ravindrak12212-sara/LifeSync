package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LifeSyncRepository(private val dao: LifeSyncDao) {

    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val habits: Flow<List<Habit>> = dao.getActiveHabits()
    val allHabitLogs: Flow<List<HabitLog>> = dao.getAllHabitLogs()
    val allTasks: Flow<List<PlannerTask>> = dao.getAllTasks()
    val allGoals: Flow<List<UserGoal>> = dao.getAllGoals()
    val allJournals: Flow<List<DailyJournal>> = dao.getAllJournals()
    val allHealthLogs: Flow<List<HealthLog>> = dao.getAllHealthLogs()
    val allNotes: Flow<List<QuickNote>> = dao.getAllNotes()

    fun getTasksByDate(date: String): Flow<List<PlannerTask>> = dao.getTasksByDate(date)
    fun getHabitLogsByDate(date: String): Flow<List<HabitLog>> = dao.getHabitLogsByDate(date)
    fun getJournalByDate(date: String): Flow<DailyJournal?> = dao.getJournalByDate(date)
    fun getHealthLogByDate(date: String): Flow<HealthLog?> = dao.getHealthLogByDate(date)

    suspend fun insertOrUpdateProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        dao.insertOrUpdateProfile(profile)
    }

    suspend fun insertHabit(habit: Habit) = withContext(Dispatchers.IO) {
        dao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: Habit) = withContext(Dispatchers.IO) {
        dao.updateHabit(habit)
    }

    suspend fun deleteHabit(habit: Habit) = withContext(Dispatchers.IO) {
        dao.deleteHabit(habit)
    }

    suspend fun logHabit(habitId: Int, date: String, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        if (isCompleted) {
            dao.insertHabitLog(HabitLog(habitId = habitId, date = date, status = "Completed"))
        } else {
            dao.deleteHabitLog(habitId, date)
        }
    }

    suspend fun insertTask(task: PlannerTask) = withContext(Dispatchers.IO) {
        dao.insertTask(task)
    }

    suspend fun updateTask(task: PlannerTask) = withContext(Dispatchers.IO) {
        dao.updateTask(task)
    }

    suspend fun deleteTask(task: PlannerTask) = withContext(Dispatchers.IO) {
        dao.deleteTask(task)
    }

    suspend fun insertGoal(goal: UserGoal) = withContext(Dispatchers.IO) {
        dao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: UserGoal) = withContext(Dispatchers.IO) {
        dao.updateGoal(goal)
    }

    suspend fun deleteGoal(goal: UserGoal) = withContext(Dispatchers.IO) {
        dao.deleteGoal(goal)
    }

    suspend fun insertJournal(journal: DailyJournal) = withContext(Dispatchers.IO) {
        dao.insertJournal(journal)
    }

    suspend fun insertHealthLog(healthLog: HealthLog) = withContext(Dispatchers.IO) {
        dao.insertHealthLog(healthLog)
    }

    suspend fun insertNote(note: QuickNote) = withContext(Dispatchers.IO) {
        dao.insertNote(note)
    }

    suspend fun deleteNote(note: QuickNote) = withContext(Dispatchers.IO) {
        dao.deleteNote(note)
    }

    // Prepopulate starting items on first run
    suspend fun prepopulateIfEmpty() = withContext(Dispatchers.IO) {
        val currentProfile = dao.getUserProfile().firstOrNull()
        if (currentProfile == null) {
            dao.insertOrUpdateProfile(
                UserProfile(
                    username = "Alex Carter",
                    email = "alex.carter@lifesync.ai",
                    xp = 240,
                    coins = 45,
                    level = 3,
                    currentStreak = 5,
                    longestStreak = 12
                )
            )

            // Core Premium Habits
            val defaultHabits = listOf(
                Habit(name = "Morning Meditation", description = "Calm the mind & set active intentions", category = "Meditation", emoji = "🧘", colorHex = "#6366F1", priority = "High", reminderTime = "07:00"),
                Habit(name = "Drink Water", description = "Target at least 8 glasses", category = "Water", emoji = "💧", colorHex = "#06B6D4", priority = "Medium", reminderTime = "09:00"),
                Habit(name = "Daily Exercise", description = "Cardio, weight training, or yoga", category = "Fitness", emoji = "🏃", colorHex = "#10B981", priority = "High", reminderTime = "18:00"),
                Habit(name = "Read 10 Pages", description = "Non-fiction or personal development", category = "Reading", emoji = "📚", colorHex = "#F59E0B", priority = "Medium", reminderTime = "21:30"),
                Habit(name = "LeetCode or Side Project", description = "Keep coding skills sharp", category = "Coding", emoji = "💻", colorHex = "#8B5CF6", priority = "Medium", reminderTime = "15:00")
            )
            for (h in defaultHabits) {
                dao.insertHabit(h)
            }

            // Quick default planner tasks for today
            val today = java.time.LocalDate.now().toString()
            val defaultTasks = listOf(
                PlannerTask(title = "Morning Standup", category = "Office", date = today, timeBlock = "Morning", startTime = "09:30", endTime = "10:00", priority = "High", isTimetable = true),
                PlannerTask(title = "Review Q3 Health Goals", category = "Personal", date = today, timeBlock = "Afternoon", startTime = "14:00", endTime = "14:30", priority = "Medium", isTimetable = true),
                PlannerTask(title = "Read Chapter 4 of Atom Habits", category = "Reading", date = today, timeBlock = "Evening", startTime = "20:00", endTime = "20:30", priority = "Low", isTimetable = true)
            )
            for (t in defaultTasks) {
                dao.insertTask(t)
            }

            // Default Quick Goals
            dao.insertGoal(
                UserGoal(
                    title = "Complete 100K Steps",
                    category = "Fitness",
                    deadline = java.time.LocalDate.now().plusDays(15).toString(),
                    priority = "High",
                    targetValue = 100000,
                    currentValue = 42000
                )
            )
            dao.insertGoal(
                UserGoal(
                    title = "Read 3 Books This Month",
                    category = "Learning",
                    deadline = java.time.LocalDate.now().plusDays(30).toString(),
                    priority = "Medium",
                    targetValue = 3,
                    currentValue = 1
                )
            )

            // Some initial notes
            dao.insertNote(
                QuickNote(
                    title = "💡 LifeSync Growth Rules",
                    content = "1. Consistently check off habits.\n2. Do Pomodoro for 50 minutes deep work.\n3. Complete daily challenges to earn XP and Coins.\n4. Log your mood to see AI analysis reports.",
                    isPinned = true,
                    folder = "Ideas"
                )
            )
        }
    }
}
