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

class  CatFace(
    augmentedFace: AugmentedFace?,
    val context: Context,
) : AugmentedFaceNode(augmentedFace) {

    var characterNode: Node? = null
    private val gravity = -1f // gravity acceleration in m/s^2

    private lateinit var anime: Animator
    private lateinit var characterIV : ImageView

    private var maxPosY = 0f
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

        maxPosY = Global.topLefttPos!!.x - Global.catHeight
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
            if(Global.catStartedJumping) Global.catVelocity += gravity * dt
        }

        Log.d("jumpingVelocity", Global.catVelocity.toString())
        Log.d("isJumping", Global.catJumping.toString())

        augmentedFace?.let { face ->
            Log.d("CatPosY",  Global.catPosY.toString())
            val nose = face.getRegionPose(AugmentedFace.RegionType.NOSE_TIP)
            Global.catPosY += Global.catVelocity * dt
            characterNode?.worldPosition = Vector3(nose.tx(), Global.catPosY, Global.spawnPosZ)
            // characterNode?.worldPosition = Vector3(0f, characterNode?.worldPosition?.y!!, Global.spawnPosZ)
        }

        if(characterNode?.worldPosition?.y!! > Global.topLefttPos!!.x)
        {
            val offset = characterNode?.worldPosition?.y!! - maxPosY
            characterNode?.worldPosition?.y = maxPosY

            for(i in 0 until Global.MAX_FISHES_ON_SCREEN) {
               Global.fishPool[i].worldPosition.y -= offset
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