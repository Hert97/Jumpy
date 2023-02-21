package com.example.Activity

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Insert (onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertScore(digit: Score)

    @Query("SELECT * FROM scores ORDER BY value DESC")
    fun getAllScores(): Flow<List<Score>>

    @Delete
    suspend fun deleteScore(digit: Score)
}