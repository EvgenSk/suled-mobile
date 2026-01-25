package com.suled.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for tracked pairs
 */
@Entity(tableName = "tracked_pairs")
data class TrackedPairEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tournamentId: String,
    val tournamentName: String,
    val pairId: Int,
    val pairDisplayName: String,
    val addedDate: Long = System.currentTimeMillis()
)
