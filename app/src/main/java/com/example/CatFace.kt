package com.example

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import com.example.Activity.Global
import com.google.ar.core.AugmentedFace
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.AugmentedFaceNode
import com.example.jumpy.R
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.SceneView

class  CatFace(
    augmentedFace: AugmentedFace?,
    val context: Context,
    scene: Scene,
) : AugmentedFaceNode(augmentedFace) {

    var characterNode: Node? = null
    private val gravity = -1f // gravity acceleration in m/s^2

    private lateinit var anime: Animator
    private lateinit var characterIV : ImageView

    private var maxPosY = 0f //cat max Y position for whole of the cat to still be shown on screen
    override fun onActivate() {
        super.onActivate()

        val FRAME_DURATION = 0.2f //seconds
        val spriteSheet = BitmapFactory.decodeResource(context.resources, R.drawable.idle)
        val frames = Spritesheet.slice(spriteSheet, 1, 3)
        anime = Animator(context.resources, frames, FRAME_DURATION)

        characterNode = Node()
        characterNode?.setParent(this)
        ViewRenderable.builder()
            .setView(context, R.layout.character_layout)
            .build()
            .thenAccept { uiRenderable: ViewRenderable ->
                uiRenderable.isShadowCaster = false
                uiRenderable.isShadowReceiver = false
                characterNode?.renderable = uiRenderable

                characterIV =
                    (characterNode?.renderable as ViewRenderable)?.view?.findViewById(R.id.characterIV)!!
                characterIV.let {
                    it.setBackgroundDrawable(anime.getAnime())
                Global.catWidth = characterIV.layoutParams.width.toFloat()
                Global.catHeight = characterIV.layoutParams.height.toFloat()
                }

                // Start the animation
                anime.start()
            }
            .exceptionally { throwable: Throwable? ->
                Log.e("CatFace", "Could not create ui element", throwable)
                null
            }
        Global.catPosY = -0.18f// Global.bottomPosY
        characterNode?.worldPosition = Vector3(0f, Global.catPosY, Global.spawnPosZ)

//        var screenCoord = CatMath.worldToScreenCoordinates(scene!!, Global.topLefttPos!!)
//        screenCoord.y -= Global.catHeight
        maxPosY = 0.05f//CatMath.screenToWorldCoordinates(scene!!, screenCoord).y
    }

    override fun onUpdate(frameTime: FrameTime?) {
        super.onUpdate(frameTime)

        val dt = frameTime?.deltaSeconds ?: 0f
        if(Global.catJumping)
        { //is jumping
            Global.catJumping = false
        }
        else
        { //Not jumping
            if(Global.catStartedJumping && Global.catVelocity > -Global.catMaxVel / 2)
            {
                Global.catVelocity += gravity * dt
            }
        }

        Log.d("jumpingVelocity", Global.catVelocity.toString())
        Log.d("isJumping", Global.catJumping.toString())

        augmentedFace?.let { face ->
            Log.d("CatPosY",  Global.catPosY.toString())
            val nose = face.getRegionPose(AugmentedFace.RegionType.NOSE_TIP)
            Global.catPosY += Global.catVelocity * dt
            characterNode?.worldPosition = Vector3(nose.tx(), Global.catPosY, Global.spawnPosZ)
            Log.d("velocity",  Global.catVelocity.toString())

            // characterNode?.worldPosition = Vector3(0f, characterNode?.worldPosition?.y!!, Global.spawnPosZ)
        }

        Log.d("currworldpos", characterNode?.worldPosition?.y.toString())
        Log.d("currtoppos", Global.topLefttPos!!.y.toString())
        if(characterNode?.worldPosition?.y!! > maxPosY)
        {
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
            val time = 0.08f // adjust this value to control the speed of the movement
            val newPositionLerp = Vector3.lerp(characterNode?.worldPosition!!, newPosition, 1f)

            characterNode?.worldPosition = newPositionLerp

            for(i in 0 until Global.MAX_FISHES_ON_SCREEN) {
                val tempPos = Global.fishPool[i].worldPosition

                // Use lerp to move the object smoothly
                val newTempPos = Vector3(tempPos.x, tempPos.y - offset, tempPos.z)
                val newTempPosLerp = Vector3.lerp(tempPos, newTempPos, time)

                Global.fishPool[i].worldPosition = newTempPosLerp
            }
        }


        // Update the ImageView to show the current frame of the animation
        val uiRenderable = (characterNode?.renderable as? ViewRenderable)
        if (uiRenderable != null) {
            val imageView = uiRenderable.view.findViewById<ImageView>(R.id.characterIV)
            imageView?.let {
                it.invalidateDrawable(anime.getAnime().current)
            }
        }
    }
}