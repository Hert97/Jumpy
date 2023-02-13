package com.example.jumpy

import android.opengl.GLES30
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.jumpy.arcore.BackgroundRenderer
import com.example.jumpy.helpers.DisplayRotationHelper
import com.google.ar.core.Camera
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import java.nio.ByteBuffer

class JumpyArRenderer(private val jumpyApp: JumpyApp) : SampleRender.Renderer, DefaultLifecycleObserver {
    companion object {
        val TAG = "JumpyArRenderer"


        private val Z_NEAR = 0.1f
        private val Z_FAR = 100f

    }
    private lateinit var bgRenderer : BackgroundRenderer
    private lateinit var virtualSceneFramebuffer : Framebuffer

    val displayRotationHelper = DisplayRotationHelper(jumpyApp)
    var hasSetTextureNames = false

    val session
        get() = jumpyApp.arCoreSessionHelper.session

    override fun onResume(owner: LifecycleOwner) {
        displayRotationHelper.onResume()
        hasSetTextureNames = false
    }

    override fun onPause(owner: LifecycleOwner) {
        displayRotationHelper.onPause()
    }

    override fun onSurfaceCreated(render: SampleRender?) {
        Log.d(TAG,"onSurfaceCreated")

        bgRenderer = BackgroundRenderer(render)
        virtualSceneFramebuffer = Framebuffer(render, /*width=*/ 1, /*height=*/ 1)

    }

    override fun onSurfaceChanged(render: SampleRender?, width: Int, height: Int) {
        Log.d(TAG,"onSurfaceChanged")
        displayRotationHelper.onSurfaceChanged(width, height)
        virtualSceneFramebuffer.resize(width, height)
    }

    override fun onDrawFrame(render: SampleRender?) {
        //Log.d(TAG,"onDrawFrame")

        val session = session ?: return

        // Texture names should only be set once on a GL thread unless they change. This is done during
        // onDrawFrame rather than onSurfaceCreated since the session is not guaranteed to have been
        // initialized during the execution of onSurfaceCreated.
        if (!hasSetTextureNames) {
            session.setCameraTextureNames(IntArray(bgRenderer.cameraColorTexture.getTextureId()))
            hasSetTextureNames = true
        }

        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        displayRotationHelper.updateSessionIfNeeded(session)

        // Obtain the current frame from ARSession. When the configuration is set to
        // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
        // camera framerate.
        val frame =
            try {
                session.update()
            } catch (e: CameraNotAvailableException) {
                Log.e(TAG, "Camera not available during onDrawFrame", e)
                //showError("Camera not available. Try restarting the app.")
                return
            }

        val camera = frame.camera


        bgRenderer.updateDisplayGeometry(frame)

        // -- Draw background
        if (frame.timestamp != 0L) {
            // Suppress rendering if the camera did not produce the first frame yet. This is to avoid
            // drawing possible leftover data from previous sessions if the texture is reused.
            render?.let {
                bgRenderer.drawBackground(render)
            }
        }

        // Visualize anchors created by touch.
        // render!!.clear(virtualSceneFramebuffer, 0f, 0f, 0f, 0f)

        // Compose the virtual scene with the background.
        //bgRenderer.drawVirtualScene(render, virtualSceneFramebuffer, Z_NEAR, Z_FAR)


        // 3D obj rendering
        // If not tracking, don't draw 3D objects.
        if (camera.trackingState == TrackingState.PAUSED) {
            return
        }

    }

}
