package com.example

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.AnimationDrawable
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import com.google.ar.core.AugmentedFace
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.AugmentedFaceNode
import com.example.jumpy.R

class FilterFace(augmentedFace: AugmentedFace?,
                 val context: Context): AugmentedFaceNode(augmentedFace) {

    private var characterNode: Node? = null

    private lateinit var anime: Animator
    private lateinit var mHandler: Handler
//    private lateinit var mRunnable:Runnable


//    val animals =  arrayOf("Dog", "Cat", "Tiger", "Frog", "Zebra", "Monkey", "Lion")

    override fun onActivate() {
        super.onActivate()

        val FRAME_DURATION = 0.2f //seconds
        val spriteSheet = BitmapFactory.decodeResource(context.resources, R.drawable.idle)
        val frames = Spritesheet.slice(spriteSheet, 1, 3)
        anime = Animator(context.resources, frames, FRAME_DURATION)

        characterNode = Node()
        characterNode?.setParent(this)
        mHandler = Handler()

        ViewRenderable.builder()
            .setView(context, R.layout.character_layout)
            .build()
            .thenAccept { uiRenderable: ViewRenderable ->
                uiRenderable.isShadowCaster = false
                uiRenderable.isShadowReceiver = false
                characterNode?.renderable = uiRenderable

                val imageView = (characterNode?.renderable as ViewRenderable)?.view?.findViewById<ImageView>(R.id.characterIV)
                imageView?.let {
                    it.setBackgroundDrawable(anime.getAnime())
                }

                // Start the animation
                anime.start()
            }
            .exceptionally { throwable: Throwable? ->
                Log.e("FilterFace", "Could not create ui element", throwable)
                null
            }
    }

    override fun onUpdate(frameTime: FrameTime?) {
        super.onUpdate(frameTime)
        augmentedFace?.let { face ->
            val nose = face.getRegionPose(AugmentedFace.RegionType.NOSE_TIP)
            characterNode?.worldPosition = Vector3(nose.tx(), nose.ty(), nose.tz())
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

    fun animate() {
//        val index = (animals.indices).random()
//        val rounds = (2..4).random()
//        var currentIndex = 0
//        var currentRound = 0
//
//        mRunnable = Runnable {
//            textView?.text = animals[currentIndex]
//            currentIndex ++
//            if (currentIndex == animals.size) {
//                currentIndex = 0
//                currentRound ++
//            }
//
//            if (currentRound == rounds) {
//                textView?.text = animals[index]
//            } else {
//                // Schedule the task to repeat
//                mHandler.postDelayed(
//                    mRunnable, // Runnable
//                    100 // Delay in milliseconds
//                )
//            }
//        }
//
//        // Schedule the task to repeat
//        mHandler.postDelayed(
//            mRunnable, // Runnable
//            100 // Delay in milliseconds
//        )
    }

    fun refresh() {
//        textView?.text = "What animal are you?"
    }
}