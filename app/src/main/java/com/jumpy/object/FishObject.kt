package com.jumpy.`object`

import android.app.Activity
import android.content.Context
import android.opengl.Visibility
import android.os.CountDownTimer
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.SoundSystem
import com.jumpy.activity.Global
import com.example.jumpy.R
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import com.jumpy.AABB
import com.jumpy.CatMath
import com.jumpy.Physics
import kotlin.reflect.jvm.internal.impl.descriptors.Visibilities.InvisibleFake


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
    var physics : Physics
    //private var id = -1
    var activated = false

    //---------------------------------------------------
    fun getPos() : Vector3
    {
        return worldPosition
    }
    fun setPos(vec : Vector3)
    {
        worldPosition = vec
    }
    fun setPosx(x : Float)
    {
        worldPosition = Vector3(x, worldPosition.y, worldPosition.z)
    }
    fun setPosY(y : Float)
    {
        worldPosition = Vector3(worldPosition.x, y, worldPosition.z)
    }
    fun setPosZ(z : Float)
    {
        worldPosition = Vector3(worldPosition.x, worldPosition.y, z)
    }
    //---------------------------------------------------

    fun reset()
    {
        destroy()
        initialize()
        physics.reset()
    }

    fun initialize() {
        fishImageView = ImageView(mContext)

        fishImageView.layoutParams = ConstraintLayout.LayoutParams(
            fishWidth,
            fishHeight
        )
        activated = false
    }
    init {
        physics = Physics(0.0f)
    }
    fun create(position: Vector3) {
        localPosition = Vector3(position.x, position.y, Global.spawnPosZ)
        val gravity = (Math.random() * (maxGravity - minGravity) + minGravity).toFloat()
        physics = Physics(gravity)
        //id = idCounter++
        activated = true

        fishImageView.setImageResource(R.drawable.fish_25p)
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

    override fun onUpdate(frameTime: FrameTime?) {
        super.onUpdate(frameTime)
        if(Global.gamePaused || Global.gameOver) return

        if (!activated) return

        physics.update(frameTime)
        setPos(physics.applyVelocity(frameTime, getPos()))

        catMunching(frameTime)

        Log.d("Fishposition", localPosition.y.toString())
        if (localPosition.y < -0.2f) {

            Log.d("Fishdestroy", localPosition.y.toString())
            destroy()
        }
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
       // parent?.removeChild(this) //remove this node from the parent "arscene"
    }


    fun catMunching(frameTime: FrameTime?)
    {
        val dt = frameTime?.deltaSeconds ?: 0f

        val cat = Global.catObject
        //Check collision
        if (cat != null) {
            var objectAABB = AABB(
                CatMath.worldToScreenCoordinates(scene!!, worldPosition),
                fishWidth.toFloat(),
                fishHeight.toFloat()
            )
            var catAABB = AABB(
                CatMath.worldToScreenCoordinates(scene!!, cat.getPos()),
                cat.catWidth,
                cat.catHeight
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
                cat.startedJumping = true
                if (!cat.isJumping && !cat.isEating) //cat not eating other fishes
                {
                    //Log.d( "FishObject","Cat munching" )
                    destroy()
                    Global.score += 10
                    //Log.d( "Score", Global.score.toString() )

                    val eatingCD = object : CountDownTimer(500, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            // called every second, update UI or do something
                        }

                        override fun onFinish() {
                            // called when the timer finishes
                            cat.isEating = false
                            Log.d("eating", "false")
                        }
                    }

                    eatingCD.start() // start the timer
                    cat.isEating = true
                    cat.isJumping = true
                    cat.physics.acceleration += Global.catJumpPower
                    SoundSystem.playSFX(mContext, R.raw.jump)

                }
            }
        }
    }

}
