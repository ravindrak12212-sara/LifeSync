package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LifeSyncDao {

    // --- User Profile ---
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    // --- Habits ---
    @Query("SELECT * FROM habits WHERE isArchived = 0")
    fun getActiveHabits(): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    // --- Habit Logs ---
    @Query("SELECT * FROM habit_logs")
    fun getAllHabitLogs(): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun getHabitLogsByDate(date: String): Flow<List<HabitLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitLog(log: HabitLog)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun deleteHabitLog(habitId: Int, date: String)

    // --- Planner Tasks ---
    @Query("SELECT * FROM planner_tasks")
    fun getAllTasks(): Flow<List<PlannerTask>>

    @Query("SELECT * FROM planner_tasks WHERE date = :date")
    fun getTasksByDate(date: String): Flow<List<PlannerTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: PlannerTask)

    @Update
    suspend fun updateTask(task: PlannerTask)

    @Delete
    suspend fun deleteTask(task: PlannerTask)

    // --- User Goals ---
    @Query("SELECT * FROM user_goals")
    fun getAllGoals(): Flow<List<UserGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: UserGoal)

    @Update
    suspend fun updateGoal(goal: UserGoal)

    @Delete
    suspend fun deleteGoal(goal: UserGoal)

    // --- Daily Journal ---
    @Query("SELECT * FROM daily_journals")
    fun getAllJournals(): Flow<List<DailyJournal>>

    @Query("SELECT * FROM daily_journals WHERE date = :date LIMIT 1")
    fun getJournalByDate(date: String): Flow<DailyJournal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journal: DailyJournal)

    // --- Health Logs ---
    @Query("SELECT * FROM health_logs")
    fun getAllHealthLogs(): Flow<List<HealthLog>>

    @Query("SELECT * FROM health_logs WHERE date = :date LIMIT 1")
    fun getHealthLogByDate(date: String): Flow<HealthLog?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthLog(healthLog: HealthLog)

    // --- Quick Notes ---
    @Query("SELECT * FROM quick_notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<QuickNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: QuickNote)

    @Delete
    suspend fun deleteNote(note: QuickNote)
}
