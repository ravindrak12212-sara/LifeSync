package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val username: String = "Achiever",
    val email: String = "user@lifesync.ai",
    val xp: Int = 100,
    val coins: Int = 15,
    val level: Int = 1,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val dailyProgress: Float = 0f
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String = "",
    val category: String = "Health", // Health, Fitness, Reading, Coding, Yoga, Meditation, Study, etc.
    val emoji: String = "📝",
    val colorHex: String = "#4F46E5",
    val priority: String = "Medium", // High, Medium, Low
    val difficulty: String = "Medium", // Easy, Medium, Hard
    val repeatDays: String = "Mon,Tue,Wed,Thu,Fri,Sat,Sun", // Comma-separated days
    val reminderTime: String? = null, // e.g., "08:00"
    val notes: String = "",
    val isArchived: Boolean = false
)

@Entity(tableName = "habit_logs")
data class HabitLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val date: String, // YYYY-MM-DD
    val status: String = "Completed" // Completed, Missed, Partial
)

@Entity(tableName = "planner_tasks")
data class PlannerTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String = "Personal",
    val date: String, // YYYY-MM-DD
    val timeBlock: String? = null, // Morning, Afternoon, Evening, Night
    val startTime: String? = null, // e.g., "09:00"
    val endTime: String? = null, // e.g., "10:00"
    val priority: String = "Medium", // High, Medium, Low
    val isCompleted: Boolean = false,
    val isTimetable: Boolean = false, // Whether to show on Smart Timetable
    val notes: String = ""
)

@Entity(tableName = "user_goals")
data class UserGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String = "Learning", // Fitness, Career, Financial, Travel, Relationship, Health, Learning, Custom
    val deadline: String, // YYYY-MM-DD
    val priority: String = "Medium",
    val targetValue: Int = 100,
    val currentValue: Int = 0,
    val milestones: String = "", // Comma-separated or simple descriptions
    val isCompleted: Boolean = false
)

@Entity(tableName = "daily_journals")
data class DailyJournal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val mood: String = "Normal", // Excellent, Happy, Normal, Sad, Stressed, Angry
    val text: String = "",
    val gratitude: String = "",
    val wins: String = "",
    val mistakes: String = "",
    val lessons: String = ""
)

@Entity(tableName = "health_logs")
data class HealthLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val waterIntakeMl: Int = 0,
    val waterGoalMl: Int = 2000,
    val calories: Int = 0,
    val proteinG: Int = 0,
    val steps: Int = 0,
    val weightKg: Double = 70.0,
    val sleepHours: Double = 0.0,
    val heartRate: Int? = null,
    val bloodPressure: String? = null
)

@Entity(tableName = "quick_notes")
data class QuickNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val isPinned: Boolean = false,
    val folder: String = "General",
    val updatedAt: Long = System.currentTimeMillis()
)
