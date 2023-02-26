package com.jumpy.activity

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.jumpy.ar.CatFace
import com.jumpy.CatMath
import com.jumpy.ar.FaceArFragment
import com.jumpy.`object`.FishObject
import com.example.jumpy.R
import com.google.ar.core.ArCoreApk
import com.google.ar.core.AugmentedFace
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Renderable
import com.jumpy.data.*
import com.jumpy.`object`.CatObject

object Global {
    const val MAX_FISHES_ON_SCREEN = 20
    const val catJumpPower = 0.15f
    const val catJumpPhase = 0.05
    const val catIdlePhase = 0f

    var hasInit = false

    var spawnPosZ = 0f
    var camLerpSpeed = 2.0f
    var topLefttPos : Vector3? = null
    var bottomRightPos : Vector3? = null

    var numFishesOnScreen = 0
    var score = 0

    var catObject : CatObject? = null
    var fishPool = Array(MAX_FISHES_ON_SCREEN) { FishObject() }
}

class GameActivity : AppCompatActivity() {
    companion object {
        const val MIN_OPENGL_VERSION = 3.0
//        const val SPAWN_DELAY_MS = 2000L //2 seconds
        const val SPAWN_DELAY_MS = 700L
    }

    private lateinit var vm: ScoreViewModel
    private lateinit var listView: ListView
    private lateinit var currScore: TextView
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

        val gameOverTextView = findViewById<TextView>(R.id.gameover)
        val restartButton: Button = findViewById(R.id.restart_button)
        listView = findViewById(R.id.leaderboard)
        currScore = findViewById(R.id.curr_score)
        val headerView = LayoutInflater.from(this).inflate(R.layout.list_item_score, null)
        listView.addHeaderView(headerView)


        scene.addOnUpdateListener {
            val str = "Score: ${Global.score}"
            findViewById<TextView>(R.id.score).text = str

            val allScores = vm.getAllScore().value
            val topScores = allScores?.take(5)
            val adapter = topScores?.let { ScoreAdapter(this, it.toList()) }
            listView.adapter = adapter

            // TODO:: Change the losing condition
            if (Global.catObject?.ifIsDed() == true)
            {
                gameOverTextView.setVisibility(View.VISIBLE);
                restartButton.setVisibility(View.VISIBLE);
                listView.setVisibility(View.VISIBLE);
                currScore.setVisibility(View.VISIBLE);
                currScore.setText("Your score is ${Global.score}")
                Global.catObject!!.reset()
            }

            /* Ensuring that there is only 1 face being tracked at a time*/
            sceneView.session
                ?.getAllTrackables(AugmentedFace::class.java)?.let {
                    for (f in it) {
                        if (!faceNodeMap.containsKey(f)) {
                            val faceNode = CatFace(f, this)
                            faceNode.setParent(scene)
                            faceNodeMap[f] = faceNode
                            Global.catObject = faceNode.catNode
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
                if (Global.catObject?.ifIsDed() == false)
                    onUpdate()
            }
        }


        findViewById<ImageButton>(R.id.settings_button).setOnClickListener {
            //Restart Button
            reset()
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

        //startSpawningFishes()
        //spawnFishes(1)

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

            Global.topLefttPos!!.y = 0.25f //hardcoded
            Global.hasInit = true

            FishObject.initializeFishProp(this)
            for(i in 0 until Global.MAX_FISHES_ON_SCREEN)
            {
                Global.fishPool[i].initialize()
            }
            startSpawningFishes()
            //spawnFishes(3)
        }

        //Global.catVelocity += Global.catAccAmuluator * dt
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
        if (Global.topLefttPos != null && Global.bottomRightPos != null) {
            val minX = Global.bottomRightPos!!.x
            val maxX = -Global.bottomRightPos!!.x
            val x = (Math.random() * (maxX - minX) + minX).toFloat()

            val y = Global.topLefttPos!!.y
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
                    spawnFishes(1)
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

    private fun reset()
    {
        for(i in 0 until Global.fishPool.size)
        {
            Global.fishPool[i].reset()
        }
        Global.catObject?.reset()

        Global.numFishesOnScreen = 0
        Global.score = 0
    }

}