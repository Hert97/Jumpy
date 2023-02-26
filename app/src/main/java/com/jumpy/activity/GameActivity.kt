package com.jumpy.activity

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.example.SoundSystem
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
    const val SPAWN_RATE = 2
    const val catJumpPower = 3.5f

    var hasInit = false

    var spawnPosZ = 0f
    var camLerpSpeed = 3.0f
    var topLefttPos : Vector3? = null
    var bottomRightPos : Vector3? = null

    var gamePaused = false

    var numFishesOnScreen = 0
    var score = 0

    var catObject : CatObject? = null
    var fishPool = Array(MAX_FISHES_ON_SCREEN) { FishObject() }

    var gameOver = false
}

class GameActivity : AppCompatActivity() {
    companion object {
        const val MIN_OPENGL_VERSION = 3.0
//        const val SPAWN_DELAY_MS = 2000L //2 seconds
        const val SPAWN_DELAY_MS = 1000L
    }

    private lateinit var vm: ScoreViewModel
    private lateinit var listView: ListView
    private lateinit var currScore: TextView
    private lateinit var arFragment: FaceArFragment
    private var faceNodeMap = HashMap<AugmentedFace, CatFace>()
    private val handler = Handler(Looper.getMainLooper())
    private var isSpawningFishes = true
    private var checkHighScore = false // add highscore to database flag

    private lateinit var gameOverTextView : TextView
    private lateinit var restartButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkIsSupportedDeviceOrFinish()) {
            return
        }

        SoundSystem.playBgMusic(this, R.raw.ingamebgm )

        setContentView(R.layout.activity_ui)
        arFragment = supportFragmentManager.findFragmentById(R.id.face_fragment) as FaceArFragment

        val sceneView = arFragment.arSceneView
        sceneView.cameraStreamRenderPriority = Renderable.RENDER_PRIORITY_FIRST
        val scene = sceneView.scene

        val database = AppDatabase.getDatabase(this) //crashes here
        val repository = ScoreRepo(database.scoreDao())

        gameOverTextView = findViewById(R.id.gameover)
        restartButton = findViewById(R.id.restart_button)


        listView = findViewById(R.id.leaderboard)
        currScore = findViewById(R.id.curr_score)
        val headerView = LayoutInflater.from(this).inflate(R.layout.list_item_score, null)
        listView.addHeaderView(headerView)

        //reset game btn
        restartButton.setOnClickListener{
            reset()
        }

        scene.addOnUpdateListener {
            val str = "Score: ${Global.score}"
            findViewById<TextView>(R.id.score).text = str

            val allScores = vm.getAllScore().value
            val topScores = allScores?.take(5)
            val adapter = topScores?.let { ScoreAdapter(this, it.toList()) }
            listView.adapter = adapter

            //game over
            if (Global.gameOver)
            {
                if(checkHighScore) {
                    checkHighScore(Global.score)
                    checkHighScore = false
                }
                displayHighScore(true)
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
            if(!Global.gameOver)
            {
                Global.gamePaused = true

                val popupLayout = LayoutInflater.from(this).inflate(R.layout.pause_menu, null) as LinearLayout
                val popupWindow = PopupWindow(
                    popupLayout,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    true
                )

                val restartBtn = popupLayout.findViewById<Button>(R.id.pauseRestartBtn)
                val returnMenuBtn = popupLayout.findViewById<Button>(R.id.pauseReturnMenu)

                restartBtn.setOnClickListener()
                {
                    reset()
                    popupWindow.dismiss() // Close the popup window when restart button is clicked
                }

                returnMenuBtn.setOnClickListener()
                {
                    reset()
                    Global.hasInit = false
                    val intent = Intent(this@GameActivity, MainActivity::class.java)
                    startActivity(intent)
                    popupWindow.dismiss() // Close the popup window when return to menu button is clicked
                }

                // Show the popup window
                popupWindow.showAtLocation(findViewById(R.id.game_container), Gravity.CENTER, 0, 0)

                //Popup gone
                popupWindow.setOnDismissListener {
                    Global.gamePaused = false
                }
            }
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
    }

    fun onUpdate() {
        val scene = arFragment.arSceneView.scene
        val arframe = arFragment.arSceneView.arFrame
        if (arframe != null && scene.view.width != 0 && scene.view.height != 0) {
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

            Global.topLefttPos!!.y = 0.25f //hardcoded
            Global.hasInit = true

            FishObject.initializeFishProp(this)
            for(i in 0 until Global.MAX_FISHES_ON_SCREEN)
            {
                Global.fishPool[i].initialize()
            }
            startSpawningFishes()
        }

    }
    //TODO display as UI, call during gameover?? -yg
    fun checkHighScore(score: Int) {

        val currScore = Score(0, score)
        vm.insertScore(currScore)

        // Get top 5 scores
        vm.getAllScore().observeForever { scores ->
            val top5 = scores.sortedByDescending { it.value }.take(5)

            // delete all scores except the top 5
            scores.filter { it !in top5 }.forEach { vm.deleteScore(it) }

            // debug display top 5 scores
            /*top5.forEachIndexed { index, score ->
                Log.d("Display_HighScore", "${index + 1}:${score.value}")
            }*/
        }


    }
    private fun randomPosition(): Vector3? {
        if (Global.topLefttPos != null && Global.bottomRightPos != null) {
            val minX = Global.bottomRightPos!!.x
            val maxX = -Global.bottomRightPos!!.x
            val x = (Math.random() * (maxX - minX) + minX).toFloat()

            val y = Global.topLefttPos!!.y
            return Vector3(x, y, Global.spawnPosZ)
        }
        return null
    }

    private fun startSpawningFishes() {
        isSpawningFishes = true
        handler.postDelayed(object : Runnable {
            override fun run() {
                //checkHighScore(1000)
                if (!Global.gamePaused && isSpawningFishes) {
                    spawnFishes(Global.SPAWN_RATE)
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
    private fun displayHighScore(display : Boolean) {
        var visiblity = View.VISIBLE
        if(!display)
            visiblity = View.INVISIBLE
        gameOverTextView.visibility = visiblity;
        restartButton.visibility = visiblity;
        listView.visibility = visiblity;
        currScore.setText("You scored ${Global.score}!")
        currScore.visibility = visiblity;
    }
    private fun reset()
    {
        //reset for session to check highscore
        checkHighScore = true
        Global.gamePaused = false
        Global.gameOver = false
        for(i in 0 until Global.fishPool.size)
        {
            Global.fishPool[i].reset()
        }
        Global.catObject?.reset()

        Global.numFishesOnScreen = 0
        Global.score = 0

        displayHighScore(false)
    }

    override fun onPause() {
        super.onPause()
        SoundSystem.pauseAll()
    }

    override fun onResume() {
        super.onResume()
        SoundSystem.resumeAll()
    }
}