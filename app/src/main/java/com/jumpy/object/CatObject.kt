package com.jumpy.`object`

import android.util.Log
import android.widget.ImageView
import com.example.jumpy.R
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ViewRenderable
import com.jumpy.Animator
import com.jumpy.Physics
import com.jumpy.activity.Global
import android.content.Context
import android.graphics.BitmapFactory
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.math.Vector3
import com.jumpy.Spritesheet
import kotlin.math.abs
import kotlin.math.sign

class CatObject : Node() {
    private var initialized = false

    //ImageView
    private lateinit var characterIV: ImageView
    var catWidth = 1f
    var catHeight = 1f

    //Physics
    val physics = Physics(-9.8f)

    //Animation
    private lateinit var anime: Animator
    private var animeStarted = false
    private val animeFrameDuration = 0.2f //seconds

    //Game Logic
    private var clampPosY = 0.05f //cat max Y position for whole of the cat to still be shown on screen
    var startedJumping = false
    var isJumping = false


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


    //---------------------------------------------------
    fun reset() {
        startedJumping = false
        isJumping = false
        setPosY(-0.18f) // Global.bottomPosY
        physics.reset()
        startIdleAnim()
    }

    fun startIdleAnim() {
        if(!initialized) return
        characterIV.setBackgroundDrawable(anime.getAnime())
        // Start the animation
        anime.start()
    }
    //---------------------------------------------------

    //---------------------------------------------------
    fun initAnim(context: Context)
    {
        val spriteSheet = BitmapFactory.decodeResource(context.resources, R.drawable.idle)
        val frames = Spritesheet.slice(spriteSheet, 1, 3)
        anime = Animator(context.resources, frames, animeFrameDuration)

        ViewRenderable.builder()
            .setView(context, R.layout.character_layout)
            .build()
            .thenAccept { uiRenderable: ViewRenderable ->
                uiRenderable.isShadowCaster = false
                uiRenderable.isShadowReceiver = false
                renderable = uiRenderable

                characterIV =
                    (renderable as ViewRenderable).view?.findViewById(R.id.characterIV)!!

                catWidth = characterIV.layoutParams.width.toFloat()
                catHeight = characterIV.layoutParams.height.toFloat()

                initialized = true
            }
            .exceptionally { throwable: Throwable? ->
                Log.e("CatFace", "Could not create ui element", throwable)
                null
            }
    }

    fun initialize(context: Context)
    {
        initAnim(context)
        reset()
    }
    //---------------------------------------------------

    //---------------------------------------------------
    override fun onUpdate(frameTime: FrameTime?)
    {
        super.onUpdate(frameTime)
        if(!initialized) return
        if(!animeStarted)
        {
            startIdleAnim()
            animeStarted = true
        }

        val dt = frameTime?.deltaSeconds ?: 0f

        //======================== Jumping ========================
        if (isJumping)
        { //is jumping
            isJumping = false
            characterIV.setBackgroundResource(R.drawable.eat)
        }
        else
        { //Not jumping
            if (startedJumping && physics.velocity > -Global.catMaxVel / 2) {
                physics.update(frameTime)
                characterIV.setBackgroundResource(R.drawable.jump)
            }
        }
        setPos(physics.applyVelocity(frameTime,getPos()))

        //======================== Camera ========================
        val catPos = getPos()
        if (catPos.y > clampPosY) //If cat reaches beyond the top of the screen
        {
            // Calculate the offset to move the fish nodes
            val offset = abs(catPos.y - clampPosY) * -sign(physics.velocity)
            val newPosition = Vector3(catPos.x, 0f, catPos.z)

            // Use lerp to move the object smoothly
            // val time = 0.08f // adjust this value to control the speed of the movement
            val newPositionLerp = Vector3.lerp(catPos, newPosition, dt * Global.camLerpSpeed)
            //Log.d("DT:",dt.toString())
            setPos(newPositionLerp)

            for (i in 0 until Global.MAX_FISHES_ON_SCREEN) {
                val tempPos = Global.fishPool[i].worldPosition
                // Use lerp to move the object smoothly
                val newTempPos = Vector3(tempPos.x, tempPos.y + offset, tempPos.z)
                val newTempPosLerp = Vector3.lerp(tempPos, newTempPos,  dt * Global.camLerpSpeed)
                Global.fishPool[i].worldPosition = newTempPosLerp
            }
        }

        //====================== Animation =====================
        // Update the ImageView to show the current frame of the animation
        val uiRenderable = (renderable as? ViewRenderable)
        if (uiRenderable != null) {
            val imageView = uiRenderable.view.findViewById<ImageView>(R.id.characterIV)
            imageView?.invalidateDrawable(anime.getAnime().current)
        }
    }
    //---------------------------------------------------

}