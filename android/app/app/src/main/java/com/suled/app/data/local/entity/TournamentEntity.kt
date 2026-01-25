package com.suled.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.suled.app.data.models.Tournament

/**
 * Room entity for Tournament
 */
@Entity(tableName = "tournaments")
data class TournamentEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val location: String,
    val division: String,
    val description: String,
    val startDate: String?,
    val endDate: String?,
    val status: String,
    val gameCount: Int,
    val createdDate: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Convert domain model to entity
 */
fun Tournament.toEntity() = TournamentEntity(
    id = id,
    name = name,
    location = location,
    division = division,
    description = description,
    startDate = startDate,
    endDate = endDate,
    status = status,
    gameCount = gameCount,
    createdDate = createdDate
)

/**
 * Convert entity to domain model
 */
fun TournamentEntity.toDomain() = Tournament(
    id = id,
    name = name,
    location = location,
    division = division,
    description = description,
    startDate = startDate,
    endDate = endDate,
    status = status,
    gameCount = gameCount,
    createdDate = createdDate
)
