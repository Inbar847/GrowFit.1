package com.example.growfit1

data class WorkoutEntry(
    val id: String = "",
    val type: String = "",
    val durationMin: Int = 0,
    val weight: Float = 0f,
    val notes: String = "",
    val photoUrl: String? = null,
    val ts: Long = System.currentTimeMillis()
)

data class PlanItem(
    val id: String = "",
    val day: String = "",         // Sun / Mon / Tue / Wed / Thu / Fri / Sat
    val exercise: String = "",
    val sets: Int = 0,
    val reps: Int = 0,
    val weight: Float = 0f
)
