package com.jumpy.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.SoundSystem
import com.jumpy.Animator
import com.jumpy.Spritesheet
import com.example.jumpy.R

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var anime : Animator
    private lateinit var catImageView : ImageView
    private var gameStart = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_menu)

        SoundSystem.playBgMusic(this, R.raw.bgm )

        val buttonPlay : ImageView = findViewById(R.id.imagePlay)
        val buttonExit : LinearLayout = findViewById(R.id.button_exit)
        val buttonLeaderboard : LinearLayout = findViewById(R.id.button_leaderboard)

        buttonPlay.setOnClickListener(this)
        buttonExit.setOnClickListener(this)
        buttonLeaderboard.setOnClickListener(this)

        catImageView = findViewById(R.id.mm_image_1)

    //        val context = this
    //        val resources = context.resources
    //        val drawableId = R.drawable.animation
    //
    //        val animDrawable = ResourcesCompat.getDrawable(resources, drawableId, null) as AnimationDrawable
    //        catImageView.setImageDrawable(animDrawable)
    //        animDrawable.start()

        val frameDuration = 0.5f //seconds
        val spriteSheet = BitmapFactory.decodeResource(resources, R.drawable.idle)
        val frames = Spritesheet.slice(spriteSheet, 1, 3)
        anime = Animator(resources, frames, frameDuration)
        //val anime = Animator(catImageView, resources, frames, null)
        anime.setImageView(catImageView)
        anime.start()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.imagePlay -> {
                if(gameStart) return
                else gameStart = true

                catImageView.setImageResource(R.drawable.jump)
                val timer = object: CountDownTimer(1000, 1000) {
                    override fun onTick(millisUntilFinished: Long)
                    {
                        val btm = findViewById<ConstraintLayout>(R.id.container).height.toFloat() - catImageView.height.toFloat()
                        // Animate the image view during the countdown
                        val animation = TranslateAnimation(0f, 0f, 0f, btm)
                        animation.duration = 1000
                        catImageView.startAnimation(animation)
                    }

                    override fun onFinish() {
                        val intent = Intent(this@MainActivity, GameActivity::class.java)
                        startActivity(intent)
                    }
                }
                timer.start()

            }
            R.id.button_leaderboard -> {
                val intent = Intent(this, LeaderboardActivity::class.java)
                startActivity(intent)
            }
            R.id.button_exit -> {
                finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        SoundSystem.pauseAll()
    }

    override fun onResume() {
        super.onResume()
        SoundSystem.resumeAll()
    }
}
