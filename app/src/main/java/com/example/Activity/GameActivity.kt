package com.example.Activity

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.FaceArFragment
import com.example.FilterFace
import com.example.FishObject
import com.example.jumpy.R
import com.google.ar.core.ArCoreApk
import com.google.ar.core.AugmentedFace
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Renderable
import kotlin.properties.Delegates


class GameActivity : AppCompatActivity() {
    companion object {
        const val MIN_OPENGL_VERSION = 3.0
    }

    lateinit var arFragment: FaceArFragment
    var spawnPosY = 0.0f
    var spawnPosZ by Delegates.notNull<Float>()
    var faceNodeMap = HashMap<AugmentedFace, FilterFace>()

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
                            val faceNode = FilterFace(f, this)
                            faceNode.setParent(scene)
                            faceNodeMap.put(f, faceNode)
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
        spawnPosZ = outValue.float

        spawnFishes(100)
    }

    private fun randomPosition(): Vector3 {
        val x = (Math.random() * 0.5 - 0.25).toFloat()
        return Vector3(x, spawnPosY, spawnPosZ)
    }

    private fun spawnFishes(numObjects: Int)
    {
        for (i in 0 until numObjects) {
            val position = randomPosition()
            val imageView = FishObject(this, position)
            imageView.setParent(arFragment.arSceneView.scene)
        }
    }

    private fun checkIsSupportedDeviceOrFinish() : Boolean {
        if (ArCoreApk.getInstance().checkAvailability(this) == ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE) {
            Toast.makeText(this, "Augmented Faces requires ARCore", Toast.LENGTH_LONG).show()
            finish()
            return false
        }
        val openGlVersionString =  (getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)
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