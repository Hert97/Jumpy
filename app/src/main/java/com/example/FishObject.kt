package com.example

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.Activity.Global
import com.example.jumpy.R
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable

class FishObject(context: Context, position: Vector3) : Node() {

    companion object {
        const val basePosY = -0.4f
    }

    private var gravity = 0f // gravity acceleration in m/s^2
    private var velocity = 0f
    private var fishImageView: ImageView
    private var mContext: Context


    init {
        localPosition = position
        mContext = context

        fishImageView = ImageView(context)
        fishImageView.setImageResource(R.drawable.fish)

        // Get screen size
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels
        val screenWidth = displayMetrics.widthPixels

        // Set layout parameters of ImageView
        val widthInPercentage = 4 // in %, e.g.5%
        val heightInPercentage = 4 // in %, e.g.5%
        val layoutParams = ConstraintLayout.LayoutParams(
            screenWidth * widthInPercentage / 100,
            screenHeight * heightInPercentage / 100
        )
        fishImageView.layoutParams = layoutParams
        val minGravity = -0.05f // Minimum gravity value
        val maxGravity = -0.1f // Maximum gravity value
        gravity = (Math.random() * (maxGravity - minGravity) + minGravity).toFloat()
    }

    fun Setup() {// Need to call this after creating the fish object
        // Build view renderable
        ViewRenderable.builder()
            .setView(mContext, fishImageView)
            .build()
            .thenAccept { renderable: ViewRenderable -> this.renderable = renderable }
            .exceptionally { throwable ->
                Log.e("Fish Object", "Unable to load renderable", throwable)
                null
            }
    }

    var flag: Boolean = false
    override fun onUpdate(frameTime: com.google.ar.sceneform.FrameTime?) {
        super.onUpdate(frameTime)
        if (flag)
            return

        // update the position by applying gravity
        val dt = frameTime?.deltaSeconds ?: 0f
        velocity += gravity * dt
        val pos = localPosition
        localPosition = Vector3(pos.x, pos.y + velocity * dt, Global.spawnPosZ)

        // check if the fish is below the ground plane and remove it
        if (pos.y < basePosY) {
            Global.numFishesOnScreen--
            Log.d(
                "FishObject",
                "Fish removed from screen. numFishesOnScreen = ${Global.numFishesOnScreen}"
            )

            onDeactivate()
            parent?.removeChild(this) //remove this node from the parent "arscene"

            flag = true
            if (parent == null)
                Log.d("success", "remove liao")
        }
    }

    override fun onDeactivate() {
        // Perform cleanup operations
        fishImageView.setImageDrawable(null)
        fishImageView.setImageBitmap(null)
        // Call super method
        super.onDeactivate()
    }
}
