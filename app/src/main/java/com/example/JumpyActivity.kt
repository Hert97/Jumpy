///*
// * Copyright 2021 Google LLC
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
package com.example
//
//import android.os.Bundle
//import android.util.Log
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.google.ar.core.Config
//import com.google.ar.core.Config.InstantPlacementMode
//import com.google.ar.core.Session
//import com.example.helpers.CameraPermissionHelper
//import com.example.helpers.DepthSettings
//import com.example.helpers.FullScreenHelper
//import com.example.helpers.InstantPlacementSettings
//import com.example.helpers.ARCoreSessionLifecycleHelper
//import com.example.opengl.SampleRender
//import com.google.ar.core.CameraConfig
//import com.google.ar.core.CameraConfigFilter
//import com.google.ar.core.exceptions.*
//
//
///**
// * This is a simple example that shows how to create an augmented reality (AR) application using the
// * ARCore API. The application will display any detected planes and will allow the user to tap on a
// * plane to place a 3D model.
// */
//class JumpyActivity : AppCompatActivity() {
//  companion object {
//    private const val TAG = "JumpyActivity"
//  }
//
//  lateinit var arCoreSessionHelper: ARCoreSessionLifecycleHelper
//  lateinit var view: JumpyArView
//  lateinit var renderer: JumpyArRenderer
//
//  val instantPlacementSettings = InstantPlacementSettings()
//  val depthSettings = DepthSettings()
//
//  override fun onCreate(savedInstanceState: Bundle?) {
//    super.onCreate(savedInstanceState)
//
//    // Setup ARCore session lifecycle helper and configuration.
//    arCoreSessionHelper = ARCoreSessionLifecycleHelper(this)
//    // If Session creation or Session.resume() fails, display a message and log detailed
//    // information.
//    arCoreSessionHelper.exceptionCallback =
//      { exception ->
//        val message =
//          when (exception) {
//            is UnavailableUserDeclinedInstallationException ->
//              "Please install Google Play Services for AR"
//            is UnavailableApkTooOldException -> "Please update ARCore"
//            is UnavailableSdkTooOldException -> "Please update this app"
//            is UnavailableDeviceNotCompatibleException -> "This device does not support AR"
//            is CameraNotAvailableException -> "Camera not available. Try restarting the app."
//            else -> "Failed to create AR session: $exception"
//          }
//        Log.e(TAG, "ARCore threw an exception", exception)
//        view.snackbarHelper.showError(this, message)
//      }
//
//    // Configure session features, including: Lighting Estimation, Depth mode, Instant Placement.
//    arCoreSessionHelper.beforeSessionResume = ::configureSession
//    lifecycle.addObserver(arCoreSessionHelper)
//
//    // Set up the Hello AR renderer.
//    renderer = JumpyArRenderer(this)
//    lifecycle.addObserver(renderer)
//
//    // Set up AR UI.
//    view = JumpyArView(this)
//    lifecycle.addObserver(view)
//    setContentView(view.root)
//
//    // Sets up renderer.
//    SampleRender(view.surfaceView, renderer, assets)
//
//    depthSettings.onCreate(this)
//    instantPlacementSettings.onCreate(this)
//  }
//
//  // Configure the session, using Lighting Estimation, and Depth mode.
//  fun configureSession(session: Session) {
////    session.configure(
////      session.config.apply {
////        lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
////
////        // Depth API is used if it is configured in Hello AR's settings.
////        depthMode =
////          if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
////            Config.DepthMode.AUTOMATIC
////          } else {
////            Config.DepthMode.DISABLED
////          }
////
////        // Instant Placement is used if it is configured in AR's settings.
////        instantPlacementMode =
////          if (instantPlacementSettings.isInstantPlacementEnabled()) {
////            InstantPlacementMode.LOCAL_Y_UP
////          } else {
////            InstantPlacementMode.DISABLED
////          }
////      }
////    )
//
//      // Augmented faces
//      val filter = CameraConfigFilter(session).setFacingDirection(CameraConfig.FacingDirection.FRONT)
//      val cameraConfig = session.getSupportedCameraConfigs(filter)[0]
//      session.cameraConfig = cameraConfig
//
//      val config = Config(session)
//      config.augmentedFaceMode = Config.AugmentedFaceMode.MESH3D
//      session.configure(config)
//  }
//
//  override fun onRequestPermissionsResult(
//    requestCode: Int,
//    permissions: Array<String>,
//    results: IntArray
//  ) {
//    super.onRequestPermissionsResult(requestCode, permissions, results)
//    if (!CameraPermissionHelper.hasCameraPermission(this)) {
//      // Use toast instead of snackbar here since the activity will exit.
//      Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
//        .show()
//      if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
//        // Permission denied with checking "Do not ask again".
//        CameraPermissionHelper.launchPermissionSettings(this)
//      }
//      finish()
//    }
//  }
//
//  override fun onWindowFocusChanged(hasFocus: Boolean) {
//    super.onWindowFocusChanged(hasFocus)
//    FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus)
//  }
//}