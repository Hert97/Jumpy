package com.example

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import com.example.jumpy.R

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
    val anime :AnimationDrawable

    constructor(
            displayImage: ImageView, //The image view to display the animation on
            resources: Resources, //needa pass the resources from the activity classes to here
            sprites : List<Bitmap>, //List of images
            frameDuration: Int? // If "null", default frame duration will be used
            )
        {
            // If "null" is passed in for frameDuration parameter,
            // default frameduration of 200 will be used
            var fDuration = 200
            if(frameDuration != null) fDuration = frameDuration

            anime = AnimationDrawable()
            sprites.forEach { bitmap ->
                if (fDuration != null) {
                    anime.addFrame(BitmapDrawable(resources, bitmap), fDuration)
                }
            }
            displayImage.setImageDrawable(anime)
        }

        fun start()
        {
            anime.start()
        }

        fun setPlayOnce(flag: Boolean) {
            anime.isOneShot = flag
        }

        fun setEnterFadeDuration(duration : Int)
        {
            anime.setEnterFadeDuration(duration)
        }

        fun setExitFadeDuration(duration : Int)
        {
            anime.setExitFadeDuration(duration)
        }
}
