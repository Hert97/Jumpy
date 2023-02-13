package com.example.jumpy

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver

class JumpyArRenderer(jumpyApp: JumpyApp) : SampleRender.Renderer, DefaultLifecycleObserver {
    companion object {
        val TAG = "JumpyArRenderer"
    }

    override fun onSurfaceCreated(render: SampleRender?) {
        Log.d(TAG,"onSurfaceCreated")
    }

    override fun onSurfaceChanged(render: SampleRender?, width: Int, height: Int) {
        Log.d(TAG,"onSurfaceChanged")
    }

    override fun onDrawFrame(render: SampleRender?) {
        Log.d(TAG,"onDrawFrame")
    }

}
