package com.example.Activity

import android.app.ActivityManager
import android.content.Context
import android.opengl.Matrix
import android.opengl.Matrix.invertM
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
import java.nio.FloatBuffer
import kotlin.math.atan
import kotlin.math.tan
import kotlin.random.Random

object Global {
    var spawnPosZ = 0.1f
    var numFishesOnScreen = 0
    var currCatFace : Node? = null
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

        startSpawningFishes()
        //spawnFishes(1)

        findViewById<ImageButton>(R.id.settings_button).setOnClickListener {
            //Restart Button
            //Back to main menu Button
        }

        /*============================== Game logic ==============================*/
        val outValue = TypedValue()
        resources.getValue(R.dimen.gamePosZ, outValue, true)
        Global.spawnPosZ = outValue.float


    }

    private fun screenToWorldPosition(screenX : Float, screenY : Float) : FloatArray?
    {
        //Log.d("MainActivity", "Try screen pos")
        // Get the ARCore session and frame
        val frame = arFragment.arSceneView.arFrame ?: return null


        // Get the screen width and height
        val screenWidth = arFragment.arSceneView.width.toFloat()
        val screenHeight = arFragment.arSceneView.height.toFloat()
        if(screenWidth <= 0.0f || screenHeight <= 0.0f )
            return null
        // Convert the vector from screen coordinates to normalized device coordinates (NDC)
        val x = ( screenX / screenWidth) * 2.0f - 1.0f
        val y = ( screenY / screenHeight) * 2.0f - 1.0f

        // Create a 4D vector with the NDC coordinates and a depth value of 0
        val ndcPoint = floatArrayOf( x ,y, 0.0f, 1.0f)

        // Get the camera pose and projection matrix
        val cameraPose = frame!!.camera.pose
        val projectionMatrix = FloatArray(16)
        frame.camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100.0f)


        // Get camera view matrix
       /* val viewMat = FloatArray(16)
        cameraPose.toMatrix(viewMat,0)
        val vpMat = FloatArray(16)
        Matrix.multiplyMM(vpMat, 0, projectionMatrix, 0, viewMat, 0)

        // Inverse vpMat
        val invVpMat = FloatArray(16)
        Matrix.invertM(invVpMat,0,vpMat,0)
        var worldPoint = FloatArray(4)
        Matrix.multiplyMV(worldPoint, 0, invVpMat, 0, ndcPoint, 0)

        val inv_w = 1.0f / worldPoint[3]
        worldPoint[0] *= inv_w
        worldPoint[1] *= inv_w
        worldPoint[2] *= inv_w
        return worldPoint*/

        // Multiply the 4D vector by the inverse of the camera's projection matrix
        val projectionMatrixInverse = FloatArray(16)
        Matrix.invertM(projectionMatrixInverse, 0, projectionMatrix, 0)
        val viewPoint = FloatArray(4)
        Matrix.multiplyMV(viewPoint, 0, projectionMatrixInverse, 0, ndcPoint, 0)

        // Multiply the resulting vector by the inverse of the camera's pose matrix
        val viewMatrix = FloatArray(16)
        val viewMatrixInverse = FloatArray(16)
        cameraPose.inverse().toMatrix(viewMatrix, 0)
        Matrix.invertM(viewMatrixInverse, 0, viewMatrix, 0)
        val worldPoint = FloatArray(4)
        Matrix.multiplyMV(worldPoint, 0, viewMatrixInverse, 0, viewPoint, 0)

        // The resulting vector contains the world position of the screen pixel point
        worldPoint[0]  = worldPoint[0] / worldPoint[3]
        worldPoint[1]  = worldPoint[1]/ worldPoint[3]
        worldPoint[2]  = worldPoint[2] / worldPoint[3]

        return worldPoint
    }

    private fun randomPosition(): Vector3? {

        val minX = -0.05f
        val maxX = 0.05f

        val screenWidth = arFragment.arSceneView.width.toDouble()

        if(screenWidth > 0) {
            val x = Random.nextDouble(0.0,screenWidth).toFloat()
            val xy = screenToWorldPosition( 0.5f,0.0f) ?: return null

            Log.d("MainActivity:", "X:${x}")
            Log.d("MainActivity:", "xy:${xy[0]},${xy[1]}")
            val camWorldPos = arFragment.arSceneView.scene.camera.localPosition
            Log.d("MainActivity:", "xy:${xy[0]},${xy[1]}")
            Log.d("MainActivity:", "Cam:${camWorldPos.x},${camWorldPos.y},${camWorldPos.z}")
            return Vector3(xy[0], xy[1], Global.spawnPosZ)
        }
        return null
        //val y  = spawnPosY //arFragment.arSceneView.arFrame?.camera?.pose?.ty()?.minus(0.5f)?: 0.0f
        //Log.d("pos y", y.toString())
        //return Vector3(x, y, Global.spawnPosZ)
    }

    private fun startSpawningFishes() {
        Log.d("MainActivity","Start spawn")
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
                val position = randomPosition() ?: continue
                val imageView = FishObject(this, position)
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