package com.jumpy.activity

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.jumpy.R

class LeaderboardActivity : AppCompatActivity() {
    private inner class HighScore(val name: String, val score: Int)
    private val highScores = mutableListOf<HighScore>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        highScores.add(HighScore("Alice", 100))
        highScores.add(HighScore("Bob", 80))
        highScores.add(HighScore("Charlie", 70))
        highScores.add(HighScore("David", 60))
        highScores.add(HighScore("Eve", 50))

        val sortedHighScores = highScores.sortedByDescending { it.score }
        val topFiveHighScores = sortedHighScores.take(5)

        val leaderboardList = findViewById<LinearLayout>(R.id.leaderboard_list)

        topFiveHighScores.forEachIndexed { index, highScore ->
            val rankTextView = TextView(this)
            rankTextView.text = (index + 1).toString()
            rankTextView.textSize = 18f
            rankTextView.layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            )
            rankTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER

            val nameTextView = TextView(this)
            nameTextView.text = highScore.name
            nameTextView.textSize = 18f
            nameTextView.layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f
            )
            nameTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER

            val scoreTextView = TextView(this)
            scoreTextView.text = highScore.score.toString()
            scoreTextView.textSize = 18f
            scoreTextView.layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            )
            scoreTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER

            val rowLayout = LinearLayout(this)
            rowLayout.orientation = LinearLayout.HORIZONTAL
            rowLayout.addView(rankTextView)
            rowLayout.addView(nameTextView)
            rowLayout.addView(scoreTextView)

            leaderboardList.addView(rowLayout)
        }
    }
}
