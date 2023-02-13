package com.example.jumpy

import android.opengl.GLSurfaceView
import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import com.example.jumpy.helpers.TapHelper

class JumpyArView(val activity: JumpyApp) : DefaultLifecycleObserver {
    val root = View.inflate(activity, R.layout.main_activity, null)
    val surfaceView = root.findViewById<GLSurfaceView>(R.id.surfaceview)

    val session
        get() = activity.arCoreSessionHelper.session

    //val snackbarHelper = SnackbarHelper()
    val tapHelper = TapHelper(activity).also {
        surfaceView.setOnTouchListener(it)
    }
}