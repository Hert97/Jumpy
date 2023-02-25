package com.example

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import com.example.Activity.Global
import com.example.jumpy.R
import com.google.ar.core.AugmentedFace
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.AugmentedFaceNode
import kotlin.math.max
import kotlin.math.min

class CatFace(
    augmentedFace: AugmentedFace?,
    val context: Context,
) : AugmentedFaceNode(augmentedFace) {
    var characterNode: Node? = null
    private val gravity = -9.8f // gravity acceleration in m/s^2

    private lateinit var anime: Animator
    private lateinit var characterIV: ImageView
    private var initialized = false
    private var maxPosY = 0f //cat max Y position for whole of the cat to still be shown on screen
    fun startIdleAnim() {
        ViewRenderable.builder()
            .setView(context, R.layout.character_layout)
            .build()
            .thenAccept { uiRenderable: ViewRenderable ->
                Log.d("enter", "start idle anim 2")
                uiRenderable.isShadowCaster = false
                uiRenderable.isShadowReceiver = false
                characterNode?.renderable = uiRenderable

                characterIV =
                    (characterNode?.renderable as ViewRenderable).view?.findViewById(R.id.characterIV)!!

                Global.catWidth = characterIV.layoutParams.width.toFloat()
                Global.catHeight = characterIV.layoutParams.height.toFloat()

                characterIV.setBackgroundDrawable(anime.getAnime())
                // Start the animation
                anime.start()

                initialized = true
            }
            .exceptionally { throwable: Throwable? ->
                Log.e("CatFace", "Could not create ui element", throwable)
                null
            }
    }

    fun reset() {
        Global.catVelocity = 0f
        Global.catStartedJumping = false
        Global.catJumping = false
        Global.catPosY = -0.18f// Global.bottomPosY
        worldPosition = Vector3(0f, Global.catPosY, Global.spawnPosZ)
        startIdleAnim()
    }

    override fun onActivate() {
        super.onActivate()
        Log.d("enter", "onactive")

        val frameDuration = 0.2f //seconds
        val spriteSheet = BitmapFactory.decodeResource(context.resources, R.drawable.idle)
        val frames = Spritesheet.slice(spriteSheet, 1, 3)
        anime = Animator(context.resources, frames, frameDuration)

        characterNode = Node()
        characterNode?.setParent(this)

        Log.d("here", "here")
        startIdleAnim()

        Global.catPosY = -0.18f// Global.bottomPosY
        characterNode?.worldPosition = Vector3(0f, Global.catPosY, Global.spawnPosZ)

//        var screenCoord = CatMath.worldToScreenCoordinates(scene!!, Global.topLefttPos!!)
//        screenCoord.y -= Global.catHeight
        maxPosY = 0.05f//CatMath.screenToWorldCoordinates(scene!!, screenCoord).y


    }


    override fun onUpdate(frameTime: FrameTime?) {
        super.onUpdate(frameTime)

        if(!initialized) return

        if (Global.catReset) {
            reset()
            Global.catReset = false
            return
        }

        val dt = frameTime?.deltaSeconds ?: 0f
        if (Global.catJumping) { //is jumping
            Global.catJumping = false
            characterIV.setBackgroundResource(R.drawable.eat)

        } else { //Not jumping
            if (Global.catStartedJumping && Global.catVelocity > -Global.catMaxVel / 2) {
                Global.catVelocity += gravity * dt * dt
                characterIV.setBackgroundResource(R.drawable.jump)
            }
        }
        //Global.catVelocity += gravity * dt

        Log.d("jumpingVelocity", Global.catVelocity.toString())
        Log.d("isJumping", Global.catJumping.toString())

        augmentedFace?.let { face ->
            Log.d("CatPosY", Global.catPosY.toString())
            val nose = face.getRegionPose(AugmentedFace.RegionType.NOSE_TIP)
            Global.catPosY += Global.catVelocity * dt
            //try to clamp cat to top position
            var finalY = Global.catPosY
            Global.topLefttPos?.let {
                finalY = min(Global.catPosY,max(it.y, Global.catPosY) )
            }
            characterNode?.worldPosition = Vector3(nose.tx(), finalY, Global.spawnPosZ)

            Log.d("Positional add", (Global.catVelocity * dt).toString())

            // characterNode?.worldPosition = Vector3(0f, characterNode?.worldPosition?.y!!, Global.spawnPosZ)
        }

        //Log.d("currworldpos", characterNode?.worldPosition?.y.toString())
        //Log.d("currtoppos", Global.topLefttPos!!.y.toString())
        if (characterNode?.worldPosition?.y!! > maxPosY) {
//            val offset = characterNode?.worldPosition?.y!! - maxPosY
//            characterNode?.worldPosition = Vector3(characterNode?.worldPosition!!.x, maxPosY, Global.spawnPosZ)
//            Log.d("NOWcurrworldpos", characterNode?.worldPosition?.y.toString())
//            Log.d("offset", offset.toString())
//
//            for(i in 0 until Global.MAX_FISHES_ON_SCREEN) {
//                val tempPos = Global.fishPool[i].worldPosition
//               Global.fishPool[i].worldPosition = Vector3(tempPos.x, tempPos.y - offset, tempPos.z)
//            }
            // Calculate the offset to move the fish nodes
            val offset = characterNode?.worldPosition?.y!! - maxPosY
            val newPosition = Vector3(characterNode?.worldPosition!!.x, 0f, Global.spawnPosZ)

            // Use lerp to move the object smoothly
            // val time = 0.08f // adjust this value to control the speed of the movement
            val newPositionLerp = Vector3.lerp(characterNode?.worldPosition!!, newPosition, dt)
            //Log.d("DT:",dt.toString())
            characterNode?.worldPosition = newPositionLerp

            for (i in 0 until Global.MAX_FISHES_ON_SCREEN) {
                val tempPos = Global.fishPool[i].worldPosition
                // Use lerp to move the object smoothly
                val newTempPos = Vector3(tempPos.x, tempPos.y - offset, tempPos.z)
                val newTempPosLerp = Vector3.lerp(tempPos, newTempPos, dt)
                Global.fishPool[i].worldPosition = newTempPosLerp
            }
        }


        // Update the ImageView to show the current frame of the animation
        val uiRenderable = (characterNode?.renderable as? ViewRenderable)
        if (uiRenderable != null) {
            val imageView = uiRenderable.view.findViewById<ImageView>(R.id.characterIV)
            imageView?.invalidateDrawable(anime.getAnime().current)
        }
    }
}