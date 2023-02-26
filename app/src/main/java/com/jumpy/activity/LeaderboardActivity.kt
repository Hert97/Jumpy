package com.jumpy.activity

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.jumpy.R
import com.jumpy.data.AppDatabase
import com.jumpy.data.ScoreRepo
import com.jumpy.data.ScoreViewModel
import com.jumpy.data.ScoreViewModelFactory

class LeaderboardActivity : AppCompatActivity() {
    private inner class HighScore(val name: String, val score: Int)
    private val highScores = mutableListOf<HighScore>()
    private lateinit var vm: ScoreViewModel
    private lateinit var listView: ListView

    private val hs = mutableListOf(
        HighScore("Alice", 100),
        HighScore("Bob", 90),
        HighScore("Charlie", 80),
        HighScore("David", 70),
        HighScore("Emma", 60)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        listView = findViewById(R.id.leaderboard)
        val headerView = LayoutInflater.from(this).inflate(R.layout.list_item_score, null)
        listView.addHeaderView(headerView)

        // setting up viewmodel
        val database = AppDatabase.getDatabase(this)
        val repository = ScoreRepo(database.scoreDao())
        vm = ViewModelProvider(this, ScoreViewModelFactory(repository))[ScoreViewModel::class.java]
        vm.getAllScore().observe(this) {
            Log.d("Database","Score has changed")
        }

        vm.getAllScore().observeForever { scores ->
            val top5 = scores.sortedByDescending { it.value }.take(5)

            scores.filter { it !in top5 }.forEach { vm.deleteScore(it) }
            
            val allScores = vm.getAllScore().value
            val topScores = allScores?.take(5)
            val adapter = topScores?.let { ScoreAdapter(this, it.toList()) }
            listView.adapter = adapter
        }
    }

}
