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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

class CatObject : Node() {
    private var initialized = false

    //ImageView
    private lateinit var characterIV: ImageView
    var catWidth = 1f
    var catHeight = 1f

    //Physics
    val physics = Physics(-15f)

    //Animation
    private lateinit var anime: Animator
    private var animeStarted = false
    private val animeFrameDuration = 0.4f //seconds

    //Game Logic
    private var clampPosY = 0.05f //cat max Y position for whole of the cat to still be shown on screen
    private var originY = -0.18f

    var currPosY = originY
    var startedJumping = false
    var isJumping = false
    var isEating = false
    var isDed = false


    //---------------------------------------------------
    fun ifIsDed() : Boolean
    {
        return isDed
    }
    fun getPos() : Vector3
    {
        return Vector3(worldPosition.x, currPosY, Global.spawnPosZ)
    }
    fun setPos(vec : Vector3)
    {
        currPosY = vec.y
        worldPosition = Vector3(vec.x, vec.y, Global.spawnPosZ)
    }
    fun setPosx(x : Float)
    {
        worldPosition = Vector3(x, currPosY, Global.spawnPosZ)
    }
    fun setPosY(y : Float)
    {
        currPosY = y
        worldPosition = Vector3(worldPosition.x, currPosY, Global.spawnPosZ)
    }
    //---------------------------------------------------


    //---------------------------------------------------
    fun reset() {
        startedJumping = false
        isJumping = false
        isEating = false
        setPosY(originY)
        physics.reset()
        startIdleAnim()
        isDed = false
    }

    fun startIdleAnim() {
        if(!initialized) return
        characterIV.setImageDrawable(anime.getAnime())
        // Start the animation
        if(!anime.isPlaying()) anime.start()
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
        if(Global.gamePaused || Global.gameOver) return

        if(!initialized) return
        if(!animeStarted)
        {
            startIdleAnim()
            animeStarted = true
        }

        val dt = frameTime?.deltaSeconds ?: 0f


        //========================= Dead ==========================
        if (getPos().y < -0.2f)
        {
            Log.d("GameOver", "Cat Dieded")
            Global.gameOver = true
        }

        //======================== Jumping ========================
        Log.d("CatVelocity", physics.velocity.toString())

        if (startedJumping) {
            physics.update(frameTime)
            isJumping = physics.velocity >= 0f
        }

        if(!isEating)
        {
            if(startedJumping && isJumping)
            {
                Log.d("jumping", "true")
                characterIV.setImageResource(R.drawable.jump)
            }
            else
            {
                Log.d("falling", "true")
                startIdleAnim()
            }
        }
        else
        {
            Log.d("eating", "true")
            characterIV.setImageResource(R.drawable.eat)
            isEating = false
        }

        // clamp position to stay on screen
        val calculatedPos = physics.applyVelocity(frameTime,getPos())
        calculatedPos.y = min(clampPosY, max(originY - 0.1f,  calculatedPos.y ))
        setPos(calculatedPos)
        // Use lerp to move the object smoothly
        //======================== Camera ========================
        val catPos = getPos()
        if (catPos.y >= clampPosY) //If cat reaches top of the screen scroll the fishes
        {
            val newPositionLerp = Vector3.lerp(getPos(), calculatedPos, dt  * Global.camLerpSpeed )
            setPos( Vector3(catPos.x,newPositionLerp.y,catPos.z))

            Log.d("scrolling", "true")
            // Calculate the offset to move the fish nodes
            //val offset = abs(catPos.y - originY) * -sign(physics.velocity)
            //val newPosition = Vector3(catPos.x, 0f, catPos.z)
            // Use lerp to move the object smoothly
            // val time = 0.08f // adjust this value to control the speed of the movement
            //val newPositionLerp = Vector3.lerp(catPos, newPosition, dt * Global.camLerpSpeed)
            //Log.d("DT:",dt.toString())
            //setPos(newPositionLerp)

            // increase acceleration for scrolling effect
            for (i in 0 until Global.MAX_FISHES_ON_SCREEN) {
                /*val tempPos = Global.fishPool[i].worldPosition
                // Use lerp to move the object smoothly
                val newTempPos = Vector3(tempPos.x, tempPos.y + offset, tempPos.z)
                val newTempPosLerp = Vector3.lerp(tempPos, newTempPos,  dt * Global.camLerpSpeed)
                Global.fishPool[i].worldPosition = newTempPosLerp*/

                if( !Global.fishPool[i].activated)
                    continue

                val fishPos = Global.fishPool[i].worldPosition
                val v = Vector3.subtract(fishPos,catPos)
                val dist2 = Vector3.dot(v,v)
                //reset fishes which are too far away because they will never die as cat is also going away from the fish
                if(dist2 > 10.0f &&  sign(physics.velocity) == sign(Global.fishPool[i].physics.velocity)) {
                    Global.fishPool[i].destroy()
                }
                Global.fishPool[i].physics.acceleration += -abs(physics.acceleration) * 0.3f
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