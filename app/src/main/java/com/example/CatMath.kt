package com.example

import android.opengl.Matrix
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Vector3
import kotlin.math.atan
import kotlin.math.tan

class CatMath {
    companion object
    {
        fun worldToScreenCoordinates(scene: Scene, worldPos: Vector3): Vector3 {
            val viewProjectionMatrix = FloatArray(16)
            val cam = scene.camera

            Matrix.multiplyMM(
                viewProjectionMatrix,
                0,
                cam.viewMatrix.data,
                0,
                cam.projectionMatrix.data,
                0
            )
            val worldPosHomogeneous = floatArrayOf(worldPos.x, worldPos.y, worldPos.z, 1.0f)

            val clipPos = FloatArray(4)
            Matrix.multiplyMV(clipPos, 0, viewProjectionMatrix, 0, worldPosHomogeneous, 0)
            val ndcPos = Vector3(clipPos[0], clipPos[1], clipPos[2])
            if (clipPos[3] != 0.0f) {
                ndcPos.scaled(1f / clipPos[3])
            }
            val screenWidth = scene.view.width.toFloat()
            val screenHeight = scene.view.height.toFloat()

            return Vector3(
                ((ndcPos.x + 1) / 2) * screenWidth,
                //((ndcPos.y + 1) / 2) * screenHeight,
                ((1 - ndcPos.y) / 2) * screenHeight,
                ndcPos.z
            )
        }

        fun screenToWorldCoordinates(scene: Scene, screenPos: Vector3): Vector3 {
            val invViewProjectionMatrix = FloatArray(16)
            val cam = scene.camera

            Matrix.multiplyMM(invViewProjectionMatrix, 0, cam.viewMatrix.data, 0, cam.projectionMatrix.data, 0)
            Matrix.invertM(invViewProjectionMatrix, 0, invViewProjectionMatrix, 0)

            val ndcPos = floatArrayOf(2 * (screenPos.x / scene.view.width) - 1, 1 - 2 * (screenPos.y / scene.view.height), screenPos.z, 1.0f)
 //           val ndcPos = floatArrayOf(2 * (screenPos.x / scene.view.width) - 1, 2 * (screenPos.y / scene.view.height) - 1, screenPos.z, 1.0f)
            var clipPos = FloatArray(4)
            Matrix.multiplyMV(clipPos, 0, invViewProjectionMatrix, 0, ndcPos, 0)

            val worldPos = Vector3(clipPos[0], clipPos[1], clipPos[2])
            if (clipPos[3] != 0.0f) {
                worldPos.scaled(1f / clipPos[3])
            }

            return worldPos
        }

//        fun calculateObjectPosition(posZ: Float, posX: Float, posY: Float, focalLength: Float): Pair<Float, Float> {
//            // Calculate the distance from the camera to the object
//            val distance = focalLength / (1f - posZ / 1000f)
//
//            // Calculate the object position based on the distance from the camera
//            val x = posX * distance / focalLength
//            val y = posY * distance / focalLength
//
//            return Pair(x, y)
//        }

        fun calculateObjectPosition(posZ: Float, posX: Float, posY: Float, focalLength: Float): Pair<Float, Float> {
            val distanceFromCamera = posZ
            val aspectRatio = 1080.0/2186.0 // assuming a square viewport for simplicity
            val fovRadians = 2 * atan((0.5 * aspectRatio) / focalLength)
            val visibleHeight = 2 * distanceFromCamera * tan(fovRadians / 2)
            val visibleWidth = visibleHeight * aspectRatio
            val newPosX = posX * (visibleWidth / focalLength)
            val newPosY = posY * (visibleHeight / focalLength)
            return Pair(newPosX.toFloat(), newPosY.toFloat())
        }
    }
}