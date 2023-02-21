package com.example.Activity

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class ScoreRepo(private val scoreDao: ScoreDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allDigits: Flow<List<Score>> = scoreDao.getAllScores()

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertScore(digit: Score)  {
        scoreDao.insertScore(digit)
        //Log.d("DAO:", "Insert " + digit.value.toString())
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteScore(digit: Score) {
        scoreDao.deleteScore(digit)
    }


}