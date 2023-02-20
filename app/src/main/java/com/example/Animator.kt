package com.example

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView

class Spritesheet {
    companion object {
        fun slice(bitmap: Bitmap, rows: Int, columns: Int): List<Bitmap> {
            val frameWidth = bitmap.width / columns
            val frameHeight = bitmap.height / rows
            val frames = ArrayList<Bitmap>(rows * columns)
            for (i in 0 until rows) {
                for (j in 0 until columns) {
                    val x = j * frameWidth
                    val y = i * frameHeight
                    frames.add(Bitmap.createBitmap(bitmap, x, y, frameWidth, frameHeight))
                }
            }
            return frames
        }
    }
}

class Animator {
    private val anime: AnimationDrawable
    private var isPlaying: Boolean = false
    private var isLooping: Boolean = false
    private var enterFadeDuration: Float = 0f
    private var exitFadeDuration: Float = 0f

    constructor(
        resources: Resources, //needa pass the resources from the activity classes to here
        sprites: List<Bitmap>, //List of images
        frameDuration: Float? //In Seconds. If "null", default frame duration will be used
    ) {
        // If "null" is passed in for frameDuration parameter,
        // default frameduration of 200 will be used
        var fDuration = 200 // 0.2 seconds
        if (frameDuration != null) fDuration = (frameDuration * 1000f).toInt()

        anime = AnimationDrawable()
        sprites.forEach { bitmap ->
            if (fDuration != null) {
                anime.addFrame(BitmapDrawable(resources, bitmap), fDuration)
            }
        }
    }

    fun setImageView(
        displayImage: ImageView, //The image view to display the animation on
    ) {
        displayImage.setImageDrawable(anime)
    }

    fun start() {
        anime.start()
        isPlaying = true
    }

    fun stop() {
        anime.stop()
        isPlaying = false
    }

    fun loop(flag: Boolean) {
        anime.isOneShot = !flag
        isLooping = flag
    }

    fun setEnterFadeDuration(duration: Float) {
        val msDuration = (duration * 1000f).toInt()
        anime.setEnterFadeDuration(msDuration)
        enterFadeDuration = duration
    }

    fun setExitFadeDuration(duration: Float) {
        val msDuration = (duration * 1000f).toInt()
        anime.setExitFadeDuration(msDuration)
        exitFadeDuration = duration
    }


    fun getAnime() : AnimationDrawable
    {
        return anime
    }

    fun isPlaying(): Boolean {
        return isPlaying
    }

    fun isLooping(): Boolean {
        return isLooping
    }

    fun getEnterFadeDuration(): Float {
        return enterFadeDuration
    }

    fun getExitFadeDuration(): Float {
        return exitFadeDuration
    }
}
