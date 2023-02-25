package com.jumpy.Activity

//import com.example.JumpyActivity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.jumpy.Animator
import com.jumpy.Spritesheet
import com.example.jumpy.R

class MainActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var anime : Animator
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_menu)

        val buttonPlay : Button = findViewById(R.id.button_play)
        val buttonExit : Button = findViewById(R.id.button_exit)
        val buttonLeaderboard : Button = findViewById(R.id.button_leaderboard)

        buttonPlay.setOnClickListener(this)
        buttonExit.setOnClickListener(this)
        buttonLeaderboard.setOnClickListener(this)

        val catImageView : ImageView = findViewById(R.id.mm_image_1)

    //        val context = this
    //        val resources = context.resources
    //        val drawableId = R.drawable.animation
    //
    //        val animDrawable = ResourcesCompat.getDrawable(resources, drawableId, null) as AnimationDrawable
    //        catImageView.setImageDrawable(animDrawable)
    //        animDrawable.start()

        val FRAME_DURATION = 1f //seconds
        val spriteSheet = BitmapFactory.decodeResource(resources, R.drawable.idle)
        val frames = Spritesheet.slice(spriteSheet, 1, 3)
        anime = Animator(resources, frames, FRAME_DURATION)
        //val anime = Animator(catImageView, resources, frames, null)
        anime.setImageView(catImageView)
        anime.start()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.button_play -> {
                val intent = Intent(this, GameActivity::class.java)
                startActivity(intent)
            }
            R.id.button_exit -> {
                finish()
            }
            R.id.button_leaderboard -> {
                val intent = Intent(this, LeaderboardActivity::class.java)
                startActivity(intent)
            }
        }
    }
}