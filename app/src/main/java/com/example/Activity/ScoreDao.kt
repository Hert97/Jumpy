package com.example.Activity

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Insert (onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertScore(score: Score)

    @Query("SELECT * FROM scores ORDER BY value DESC")
    fun getAllScores(): Flow<List<Score>>

    @Query("DELETE FROM scores")
    suspend fun deleteAll()

    @Delete
    suspend fun deleteScore(score: Score)
}