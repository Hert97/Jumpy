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
//import com.google.ar.sceneform.math.Matrix
import android.opengl.Matrix


class FishObject(context: Context, position: Vector3, scene: Scene) : Node() {

    companion object {
        const val basePosY = -0.4f
        const val minGravity = -0.05f // Minimum gravity value
        const val maxGravity = -0.1f // Maximum gravity value

        private var fishWidth: Int = 0
        private var fishHeight: Int = 0
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

        if (fishWidth == 0 || fishHeight == 0) {
            // Get screen size
            val displayMetrics = DisplayMetrics()
            (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)

            // Set layout parameters of ImageView
            val widthInPercentage = 4 // in %, e.g.5%
            val heightInPercentage = 4 // in %, e.g.5%
            fishWidth = displayMetrics.widthPixels * widthInPercentage / 100
            fishHeight = displayMetrics.heightPixels * heightInPercentage / 100
        }

        val layoutParams = ConstraintLayout.LayoutParams(
            fishWidth,
            fishHeight
        )

        fishImageView.layoutParams = layoutParams
        gravity = (Math.random() * (maxGravity - minGravity) + minGravity).toFloat()
    }

    fun Setup() {// Need to call this after creating the fish object
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

        //Check collision
        if(Global.currCatFace != null)
        {
            var objectAABB = AABB(worldToScreenCoordinates(scene!!,worldPosition), fishWidth.toFloat(), fishHeight.toFloat())
            var catAABB = AABB(
                worldToScreenCoordinates(scene!!, Global.currCatFace!!.worldPosition),
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

            if (objectAABB.intersects(catAABB)) {
                Log.d(
                    "FishObject",
                    "Fish touching cat"
                )
                destroy()
            }
        }

        if (localPosition.y < basePosY) {
            destroy()
        }
    }

    override fun onDeactivate() {
        // Perform cleanup operations
        fishImageView.setImageDrawable(null)
        fishImageView.setImageBitmap(null)
        // Call super method
        super.onDeactivate()
    }

    fun destroy() {
        // check if the fish is below the ground plane and remove it
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

    fun worldToScreenCoordinates(scene: Scene, worldPos: Vector3): Vector3 {
        val viewProjectionMatrix = FloatArray(16)
        val cam = scene.camera

        Matrix.multiplyMM(viewProjectionMatrix, 0, cam.viewMatrix.data, 0, cam.projectionMatrix.data, 0)
        val worldPosHomogeneous = floatArrayOf(worldPos.x, worldPos.y, worldPos.z, 1.0f)

        val clipPos = FloatArray(4)
        Matrix.multiplyMV(clipPos, 0, viewProjectionMatrix, 0, worldPosHomogeneous, 0)
        val ndcPos = Vector3(clipPos[0], clipPos[1], clipPos[2])
        if(clipPos[3] != 0.0f)
        {
            ndcPos.scaled(1f/clipPos[3])
        }
        val screenWidth = scene.view.width.toFloat()
        val screenHeight = scene.view.height.toFloat()
        val screenPos = Vector3(
            ((ndcPos.x + 1) / 2) * screenWidth,
            ((1 - ndcPos.y) / 2) * screenHeight,
            ndcPos.z
        )
        return screenPos
    }
}
