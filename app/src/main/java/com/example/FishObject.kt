package com.example

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.jumpy.R
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable

class FishObject(context: Context, position: Vector3) : Node() {

    init {
        localPosition = position

        val imageView = ImageView(context)
        imageView.setImageResource(R.drawable.fish)

        // Get screen size
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels
        val screenWidth = displayMetrics.widthPixels

        // Set layout parameters of ImageView
        val widthInPercentage = 5 // in %, e.g.5%
        val heightInPercentage = 5 // in %, e.g.5%
        val layoutParams = ConstraintLayout.LayoutParams(screenWidth * widthInPercentage / 100, screenHeight * heightInPercentage / 100)
        imageView.layoutParams = layoutParams

        // Build view renderable
        ViewRenderable.builder()
            .setView(context, imageView)
            .build()
            .thenAccept { renderable -> this.renderable = renderable }
            .exceptionally { throwable ->
                Log.e(TAG, "Unable to load renderable", throwable)
                null
            }
    }
}
