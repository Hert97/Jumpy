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
import com.example.CatFace
import com.example.CatMath
import com.example.FaceArFragment
import com.example.FishObject
import com.example.jumpy.R
import com.google.ar.core.ArCoreApk
import com.google.ar.core.AugmentedFace
import com.google.ar.core.TrackingState
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
    private lateinit var arFragment: FaceArFragment
    private var faceNodeMap = HashMap<AugmentedFace, CatFace>()
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
            val str = "Score: ${Global.score}"
            findViewById<TextView>(R.id.score).text = str

            /* Ensuring that there is only 1 face being tracked at a time*/
            sceneView.session
                ?.getAllTrackables(AugmentedFace::class.java)?.let {
                    for (f in it) {
                        if (!faceNodeMap.containsKey(f)) {
                            val faceNode = CatFace(f, this)
                            faceNode.setParent(scene)
                            faceNodeMap[f] = faceNode
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
        }

        findViewById<ImageButton>(R.id.settings_button).setOnClickListener {
            //Restart Button
            //Back to main menu Button
        }

        // setting up viewmodel
        vm = ViewModelProvider(this, ScoreViewModelFactory(repository))[ScoreViewModel::class.java]
        vm.getAllScore().observe(this) {
            Log.d("Database","Score has changed")
        }


        /*============================== Game logic ==============================*/
        val outValue = TypedValue()
        resources.getValue(R.dimen.gamePosZ, outValue, true)
        Global.spawnPosZ = outValue.float

        startSpawningFishes()


        //spawnFishes(1)
    }
    //TODO display as UI, call during gameover?? -yg
    fun checkHighScore(score: Int) {
        val highScores = vm.getAllScore().value?.toMutableList()
        val currScore = Score(0,score)
        if(highScores != null)  {
            //insert high score as all are top 5
            if(highScores.size < 5) {
                vm.insertScore(currScore)
                return
            }
            // sort highscore
            highScores.sortByDescending { s-> s.value }
            vm.deleteAll()
            //reinsert
            for (i in highScores.indices) {
                if(i > 4) // insert top 5
                    break
                vm.insertScore(highScores[i])
                Log.d("Display_HighScore", "${i + 1}:${highScores[i].value}")
            }
        }
        else  vm.insertScore(currScore)

        //display highscore
        val l = vm.getAllScore().value
        if (l != null) {
            for (i in l.indices) {
                if(i > 4) // display top 5
                    break
                Log.d("Display_HighScore", "${i + 1}:${l[i].value}")
            }
            Log.d("Display_HighScore", "Size:${l.count()}")
        }
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
                //checkHighScore(1000)

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

                Global.score++
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

        openGlVersionString?.let {
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