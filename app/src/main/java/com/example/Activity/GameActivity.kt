package com.example.Activity

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.room.Update
import com.example.FaceArFragment
import com.example.CatFace
import com.example.CatMath
import com.example.FishObject
import com.example.jumpy.R
import com.google.ar.core.*
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene.OnUpdateListener
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Renderable

object Global {
    const val MAX_FISHES_ON_SCREEN = 20

    var spawnPosZ = 0f
    var numFishesOnScreen = 0
    var currCatFace: Node? = null
    var catWidth = 1f
    var catHeight = 1f
    var score = 0

    var hasInit = false //Camera needs to be active to init the vars
    var topLefttPos: Vector3? = null
    var bottomRightPos: Vector3? = null

    var catVelocity = 0f
    var catPosY = 0f

    var catJumping = false
    var catStartedJumping = false

    const val catJumpPower = 0.35f

    var fishPool = Array(MAX_FISHES_ON_SCREEN) { FishObject() }
}

class GameActivity : AppCompatActivity() {
    companion object {
        const val MIN_OPENGL_VERSION = 3.0
        const val SPAWN_DELAY_MS = 2000L //2 seconds
    }

    private lateinit var vm: ScoreViewModel
    lateinit var arFragment: FaceArFragment
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

        val database = AppDatabase.getDatabase(this) //crashes here
        val repository = ScoreRepo(database.scoreDao())

        scene.addOnUpdateListener {
            findViewById<TextView>(R.id.score).text = "Score: ${Global.score}"

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
            if (!Global.hasInit) {
                onUpdate()
            }
        }


        findViewById<ImageButton>(R.id.settings_button).setOnClickListener {
            //Restart Button
            //Back to main menu Button
        }

        var test = Score(0, 10)
        var test1 = Score(0, 20)
        var test2 = Score(0, 30)
        vm = ViewModelProvider(this, ScoreViewModelFactory(repository))[ScoreViewModel::class.java]
        vm.insertScore(test)
        vm.insertScore(test1)
        vm.insertScore(test2)


        /*============================== Game logic ==============================*/
        val outValue = TypedValue()
        resources.getValue(R.dimen.gamePosZ, outValue, true)
        Global.spawnPosZ = outValue.float

        vm.getAllScore().observe(this) {
            for (i in it.indices) {
                Log.d(
                    "MainActivity",
                    "Total score = ${it[i].value}"
                )

            }

        }

    }

    fun onUpdate() {
        val scene = arFragment.arSceneView.scene
        val arframe = arFragment.arSceneView.arFrame
        if (arframe != null && arframe?.camera != null
            && scene.view.width != 0 && scene.view.height != 0
        ) {
            // Camera is active
            Global.topLefttPos = CatMath.screenToWorldCoordinates(scene, Vector3(0f, 0f, 0f))
            Global.bottomRightPos =
                arFragment.arSceneView.arFrame?.camera?.imageIntrinsics?.let {
                    CatMath.screenToWorldCoordinates(
                        arFragment.arSceneView.scene,
                        Vector3(
                            it.imageDimensions[1].toFloat(),
                            it.imageDimensions[0].toFloat(),
                            0f
                        )
                    )
                }
            Global.bottomRightPos!!.x *= -1f
            Global.bottomRightPos!!.y *= -1f

            Log.d("Top Left Pos", "(${Global.topLefttPos!!.x}, ${Global.topLefttPos!!.y})")
            Log.d(
                "Bottom Right Pos",
                "(${Global.bottomRightPos!!.x}, ${Global.bottomRightPos!!.y})"
            )

            /*NOT WORKING SADGEEEEEE*/
//            var temp = CatMath.calculateObjectPosition(Global.spawnPosZ, Global.topLefttPos!!.x, Global.topLefttPos!!.y, arFragment.arSceneView.arFrame?.camera?.imageIntrinsics?.getFocalLength()
//                ?.get(0) ?: 0f)
//            Global.topLefttPos = Vector3(temp.first,temp.second, 0f)
//            temp = CatMath.calculateObjectPosition(Global.spawnPosZ, Global.bottomRightPos!!.x, Global.bottomRightPos!!.y, arFragment.arSceneView.arFrame?.camera?.imageIntrinsics?.getFocalLength()
//                ?.get(0) ?: 0f)
//            Global.bottomRightPos = Vector3(temp.first,temp.second, 0f)

            Global.topLefttPos!!.x = -0.3f //hardcoded

            Global.hasInit = true

            FishObject.initializeFishProp(this)
            for(i in 0 until Global.MAX_FISHES_ON_SCREEN)
            {
                Global.fishPool[i].initialize()
            }
            startSpawningFishes()
            //spawnFishes(3)
        }
    }

    private fun randomPosition(): Vector3? {
        if (Global.topLefttPos != null && Global.bottomRightPos != null) {
            val minX = Global.bottomRightPos!!.x
            val maxX = -Global.bottomRightPos!!.x
            val x = (Math.random() * (maxX - minX) + minX).toFloat()

            val y = 0.25f //Temp hax, suppose to use -> Global.topLefttPos!!.y
            Log.d("spawn pos x", x.toString())
            Log.d("spawn pos y", y.toString())
            return Vector3(x, y, Global.spawnPosZ)
        }
        return null
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
        var toSpawn = numObjects
        for(i in 0 until Global.fishPool.size)
        {
            if(Global.numFishesOnScreen < Global.MAX_FISHES_ON_SCREEN &&
                toSpawn > 0 && !Global.fishPool[i].activated)
            {
                val position = randomPosition() ?: return
                Global.fishPool[i].create(position)
                Global.fishPool[i].setParent(arFragment.arSceneView.scene)

                //Global.score++
                //val newScore = Score(value = Global.score)
                //db.scoreDao().insertScore(newScore)

                Global.numFishesOnScreen++
                Log.d(
                    "MainActivity",
                    "Fish added to screen. numFishesOnScreen = ${Global.numFishesOnScreen}"
                )
                toSpawn--
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

    private fun showHighScores() {
        // val scores = db.scoreDao().getAllScores().take(10)
        //val scoreText = StringBuilder("High Scores:\n")
        // for ((index, score) in scores.withIndex()) {
        //    scoreText.append("${index + 1}. ${score.value}\n")
        // }
        //findViewById<TextView>(R.id.score).text = scoreText.toString()
    }
}