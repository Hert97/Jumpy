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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room
import com.example.*
import com.example.jumpy.R
import com.google.ar.core.*
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Renderable

object Global {
    var spawnPosZ = 0f
    var numFishesOnScreen = 0
    var currCatFace: Node? = null
    var catWidth = 1f
    var catHeight = 1f
    var score = 0
    var bottomPosY = 0f
    var catVelocity = 0f
    var catPosY = 0f

    var catJumping = false
    var catStartedJumping = false

    const val catJumpPower = 0.5f
}

class GameActivity : AppCompatActivity() {
    companion object {
        const val MIN_OPENGL_VERSION = 3.0
        const val SPAWN_DELAY_MS = 2000L //2 seconds
        const val MAX_FISHES_ON_SCREEN = 20
    }
    private lateinit var vm: ScoreViewModel
    lateinit var arFragment: FaceArFragment
    var faceNodeMap = HashMap<AugmentedFace, CatFace>()
    private val handler = Handler(Looper.getMainLooper())
    private var isSpawningFishes = true
    var test = Global.score

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
//                    // Remove any AugmentedFaceNodes associated with an AugmentedFace that stopped tracking.
//                    val iter = faceNodeMap.entries.iterator()
//                    while (iter.hasNext()) {
//                        val entry = iter.next()
//                        val face = entry.key
//                        if (face.trackingState == TrackingState.STOPPED) {
//                            val faceNode = entry.value
//                            faceNode.setParent(null)
//                            iter.remove()
//                        }
//                    }
                }
            if(test != Global.score)
            {
                SoundSystem.playSFX(this,R.raw.munch)
                test = Global.score
            }
        }

        findViewById<ImageButton>(R.id.settings_button).setOnClickListener {
            //Restart Button
            //Back to main menu Button
        }

        ///////////////////// TESTING DATABASE /////////////////
        var test = Score(0,10)
        var test1 = Score(0,20)
        var test2 = Score(0,30)
        vm = ViewModelProvider(this, ScoreViewModelFactory(repository))[ScoreViewModel::class.java]
        vm.insertScore(test)
        vm.insertScore(test1)
        vm.insertScore(test2)

        /*============================== Game logic ==============================*/
        val outValue = TypedValue()
        resources.getValue(R.dimen.gamePosZ, outValue, true)
        Global.spawnPosZ = outValue.float

        ///////////////////  HIGHSCORE /////////////////
        vm.getAllScore().observe(this) {
            for (i in it.indices){
                Log.d(
                    "MainActivity",
                    "Total score = ${it[i].value}"
                )

            }

        }

      startSpawningFishes()
        Global.bottomPosY = arFragment.arSceneView.arFrame?.camera?.imageIntrinsics?.let {
            CatMath.screenToWorldCoordinates(
                arFragment.arSceneView.scene,
                Vector3(it.imageDimensions[0].toFloat(), 0.0f, 0.0f)
            )
        }?.y ?: 0.4f
        Global.bottomPosY = Global.bottomPosY!! * -1f
        Log.d("Base Pos Y", Global.bottomPosY.toString())

   //   SoundSystem.playBgMusic(this, R.raw.water)

      startSpawningFishes()
        //spawnFishes(1)
    }


    private fun randomPosition(): Vector3? {

        val topRightPos = arFragment.arSceneView.arFrame?.camera?.imageIntrinsics?.let {
            CatMath.screenToWorldCoordinates(
                arFragment.arSceneView.scene,
                Vector3(it.imageDimensions[1].toFloat(), 0.0f, 0.0f)
            )
        }

        if (topRightPos != null) {
            val minX = -topRightPos.x
            val maxX = topRightPos.x
            val x = (Math.random() * (maxX - minX) + minX).toFloat()

            val y = topRightPos.y
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
        for (i in 0 until numObjects) {

            if (Global.numFishesOnScreen < MAX_FISHES_ON_SCREEN) {
                val position = randomPosition() ?: return
                val imageView = FishObject(this, position, arFragment)
                imageView.Setup()
                imageView.setParent(arFragment.arSceneView.scene)

               // Global.score++
                //val newScore = Score(value = Global.score)
                //db.scoreDao().insertScore(newScore)

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

    //SOUND SYSTEM
    override fun onPause() {
        super.onPause()
        SoundSystem.pauseAll()
    }

    override fun onResume() {
        super.onResume()
        SoundSystem.resumeAll()
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