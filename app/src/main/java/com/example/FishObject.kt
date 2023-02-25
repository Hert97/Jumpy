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
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable


class FishObject : Node() {

    companion object {
        const val minGravity = -.01f // Minimum gravity value
        const val maxGravity = -.05f // Maximum gravity value

        private var fishWidth: Int = 0
        private var fishHeight: Int = 0

        //private var idCounter = 0

        private lateinit var mContext: Context
        fun initializeFishProp(context: Context)
        {
            mContext = context
            if (fishWidth == 0 || fishHeight == 0) {
                // Get screen size
                val displayMetrics = DisplayMetrics()
                (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)

                // Set layout parameters of ImageView
                val widthInPercentage = 2 // in %, e.g.5%
                val heightInPercentage = 2// in %, e.g.5%
                fishWidth = displayMetrics.widthPixels * widthInPercentage / 100
                fishHeight = displayMetrics.heightPixels * heightInPercentage / 100
            }
            //fishWidth = 25
            // fishHeight = 25
        }
    }

    private lateinit var fishImageView: ImageView
    private var gravity = 0f // gravity acceleration in m/s^2
    private var velocity = 0f
    //private var id = -1
    var activated = false

    fun reset()
    {
        destroy()
        initialize()
    }


    fun initialize() {
        fishImageView = ImageView(mContext)

        fishImageView.layoutParams = ConstraintLayout.LayoutParams(
            fishWidth,
            fishHeight
        )
        activated = false
    }

    fun create(position: Vector3) {
        localPosition = Vector3(position.x, position.y, Global.spawnPosZ)
        gravity = (Math.random() * (maxGravity - minGravity) + minGravity).toFloat()
        //id = idCounter++
        activated = true
        velocity = 0f

        fishImageView.setImageResource(R.drawable.fish_25p)
        // Build view renderable
        ViewRenderable.builder()
            .setView(mContext, fishImageView)
            .build()
            .thenAccept { renderable: ViewRenderable -> this?.renderable = renderable }
            .exceptionally { throwable ->
                Log.e("Fish Object", "Unable to load renderable", throwable)
                null
            }
    }
    var dt = 0.0f
    override fun onUpdate(frameTime: com.google.ar.sceneform.FrameTime?) {
        super.onUpdate(frameTime)

        if(!activated) return

        // update the position by applying gravity
        dt = frameTime?.deltaSeconds ?: 0f
        velocity += gravity * dt
        val pos = localPosition
        localPosition = Vector3(pos.x, pos.y + velocity * dt, pos.z)

        catMunching()

        if (localPosition.y < -0.2f)
        {
            destroy()
        }
//        if (Global.bottomRightPos != null) {
//            if (worldPosition.y < Global.bottomRightPos!!.y) {
//                //destroy()
//            }
//        }
//        else
//        {
//            if (localPosition.y < -1f) {
//                //destroy()
//            }
//        }
    }

    fun destroy() {
        // check if the fish is below the ground plane and remove it
        Global.numFishesOnScreen--
        Log.d(
            "FishObject",
            "Fish removed from screen. numFishesOnScreen = ${Global.numFishesOnScreen}"
        )

        fishImageView.setImageDrawable(null)
        fishImageView.setImageBitmap(null)
        activated = false
        parent?.removeChild(this) //remove this node from the parent "arscene"
    }
    private fun calculateJumpVelocity(h: Float, v0 : Float , t : Float): Float {
        val v =  (h / t) * 2.0f - v0
        Log.d("Velocity:",v.toString())
        return v
    }
    fun catMunching()
    {
        //Check collision
        if (Global.currCatFace != null) {
            var objectAABB = AABB(
                CatMath.worldToScreenCoordinates(scene!!, worldPosition),
                fishWidth.toFloat(),
                fishHeight.toFloat()
            )
            var catAABB = AABB(
                CatMath.worldToScreenCoordinates(scene!!, Global.currCatFace!!.worldPosition),
                Global.catWidth,
                Global.catHeight
            )
//            Log.d("FishObject", "objectAABB = ${objectAABB.min.x}, ${objectAABB.min.y}, ${objectAABB.max.x}, ${objectAABB.max.y} ")
//            Log.d("FishObject", "catAABB = ${catAABB.min.x}, ${catAABB.min.y}, ${catAABB.max.x}, ${catAABB.max.y}")
//
//            Log.d("FishObject", "objectWorldPos = ${worldPosition.x},${worldPosition.y},${worldPosition.z}")
//            Log.d("FishObject", "catWorldPos = ${Global.currCatFace!!.worldPosition.x},${Global.currCatFace!!.worldPosition.y},${Global.currCatFace!!.worldPosition.z}")
//
//            Log.d("FishObject", "objectLocalPos = ${localPosition.x},${localPosition.y},${localPosition.z}")
//            Log.d("FishObject", "catLocalPos = ${Global.currCatFace!!.localPosition.x},${Global.currCatFace!!.localPosition.y},${Global.currCatFace!!.localPosition.z}")

            if (objectAABB.intersects(catAABB))
            {
                //Log.d( "FishObject","Cat munching" )
                destroy()
                Global.score += 10
                //Log.d( "Score", Global.score.toString() )

                Global.catStartedJumping = true
                if (!Global.catJumping) //cat not eating other fishes
                {
                    Global.catJumping = true
                    if(Global.catVelocity < Global.catMaxVel)
                        Global.catVelocity += calculateJumpVelocity(0.005f, Global.catVelocity,dt)

                    /*if(Global.catVelocity < Global.catMaxVel)
                        Global.catVelocity += Global.catJumpPower*/
                }
            }
        }
    }

}
