package com.example.Activity

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.FaceArFragment
import com.example.CatFace
import com.example.FishObject
import com.example.jumpy.R
import com.google.ar.core.*
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Renderable
import kotlin.math.atan

object Global {
    var spawnPosZ = 0f
    var numFishesOnScreen = 0
    var currCatFace : Node? = null
    var catWidth = 1f
    var catHeight = 1f
}

class GameActivity : AppCompatActivity() {
    companion object {
        const val MIN_OPENGL_VERSION = 3.0
        const val SPAWN_DELAY_MS = 2000L //2 seconds
        const val MAX_FISHES_ON_SCREEN = 20
    }

    lateinit var arFragment: FaceArFragment
    var spawnPosY = 0.1f
    var faceNodeMap = HashMap<AugmentedFace, CatFace>()
    private val handler = Handler(Looper.getMainLooper())
    private var isSpawningFishes = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkIsSupportedDeviceOrFinish()) {
            return
        }

        setContentView(R.layout.activity_ui)
        arFragment = supportFragmentManager.findFragmentById(R.id.face_fragment) as FaceArFragment

        val sceneView = arFragment.arSceneView
        sceneView.cameraStreamRenderPriority = Renderable.RENDER_PRIORITY_FIRST
        val scene = sceneView.scene

        scene.addOnUpdateListener {
            /* Ensuring that there is only 1 face being tracked at a time*/
            sceneView.session
                ?.getAllTrackables(AugmentedFace::class.java)?.let {
                    for (f in it) {
                        if (!faceNodeMap.containsKey(f)) {
                            val faceNode = CatFace(f, this)
                            faceNode.setParent(scene)
                            faceNodeMap.put(f, faceNode)
                            Global.currCatFace = faceNode.characterNode
                        }
                    }
                    // Remove any AugmentedFaceNodes associated with an AugmentedFace that stopped tracking.
                    val iter = faceNodeMap.entries.iterator()
                    while (iter.hasNext()) {
                        val entry = iter.next()
                        val face = entry.key
                        if (face.trackingState == TrackingState.STOPPED) {
                            val faceNode = entry.value
                            faceNode.setParent(null)
                            iter.remove()
                        }
                    }
                }
        }

        findViewById<ImageButton>(R.id.settings_button).setOnClickListener {
            //Restart Button
            //Back to main menu Button
        }


        /*============================== Game logic ==============================*/
        val outValue = TypedValue()
        resources.getValue(R.dimen.gamePosZ, outValue, true)
        Global.spawnPosZ = outValue.float

      startSpawningFishes()
        //spawnFishes(1)
    }

    private fun randomPosition(): Vector3 {
        val minX = -0.05f
        val maxX = 0.05f
        val x = (Math.random() * (maxX - minX) + minX).toFloat()

        val y  = spawnPosY //arFragment.arSceneView.arFrame?.camera?.pose?.ty()?.minus(0.5f)?: 0.0f
        Log.d("pos y", y.toString())
        return Vector3(x, y, Global.spawnPosZ)
    }

    private fun startSpawningFishes() {
        isSpawningFishes = true
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isSpawningFishes) {
                    spawnFishes(3)
                    handler.postDelayed(this, SPAWN_DELAY_MS)
                }
            }
        }, SPAWN_DELAY_MS)
    }

    private fun stopSpawningFishes() {
        isSpawningFishes = false
    }

    private fun spawnFishes(numObjects: Int) {
        for (i in 0 until numObjects) {

            if (Global.numFishesOnScreen < MAX_FISHES_ON_SCREEN) {
                val position = randomPosition()
                val imageView = FishObject(this, position, arFragment.arSceneView.scene)
                imageView.Setup()
                imageView.setParent(arFragment.arSceneView.scene)

                Global.numFishesOnScreen++
                Log.d(
                    "MainActivity",
                    "Fish added to screen. numFishesOnScreen = ${Global.numFishesOnScreen}"
                )
            }
        }
    }

    private fun checkIsSupportedDeviceOrFinish(): Boolean {
        if (ArCoreApk.getInstance()
                .checkAvailability(this) == ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE
        ) {
            Toast.makeText(this, "Augmented Faces requires ARCore", Toast.LENGTH_LONG).show()
            finish()
            return false
        }
        val openGlVersionString = (getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)
            ?.deviceConfigurationInfo
            ?.glEsVersion

        openGlVersionString?.let { s ->
            if (java.lang.Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
                Toast.makeText(this, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show()
                finish()
                return false
            }
        }
        return true
    }
}