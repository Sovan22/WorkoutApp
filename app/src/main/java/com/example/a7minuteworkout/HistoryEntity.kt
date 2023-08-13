package com.example.a7minuteworkout

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout-history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: String ="",
    val time: String =""
)
