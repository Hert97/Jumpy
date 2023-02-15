package com.example

import android.opengl.Matrix
import com.example.helpers.Vector3
import kotlin.math.*
//TODO not tested yet
class Transform {
    val position : Vector3 = Vector3.ZERO
    val euler : Vector3 = Vector3.ZERO
    val scale : Vector3 = Vector3.ONE

    fun eulerToRotationMatrix(angleX: Float, angleY: Float, angleZ: Float): FloatArray {
        val rotationMatrix = FloatArray(16)

        val cx = cos(angleX)
        val sx = sin(angleX)
        val cy = cos(angleY)
        val sy = sin(angleY)
        val cz = cos(angleZ)
        val sz = sin(angleZ)

        // Note: The matrix is transposed for use with OpenGL ES.
        rotationMatrix[0] = cy * cz
        rotationMatrix[1] = -cy * sz
        rotationMatrix[2] = sy
        rotationMatrix[3] = 0f

        rotationMatrix[4] = sx * sy * cz + cx * sz
        rotationMatrix[5] = -sx * sy * sz + cx * cz
        rotationMatrix[6] = -sx * cy
        rotationMatrix[7] = 0f

        rotationMatrix[8] = -cx * sy * cz + sx * sz
        rotationMatrix[9] = cx * sy * sz + sx * cz
        rotationMatrix[10] = cx * cy
        rotationMatrix[11] = 0f

        rotationMatrix[12] = 0f
        rotationMatrix[13] = 0f
        rotationMatrix[14] = 0f
        rotationMatrix[15] = 1f

        return rotationMatrix
    }


    fun getTransformMatrix() : FloatArray {
        var scaleM =  FloatArray(16)
        var rotateM =  FloatArray(16)
        var translateM =  FloatArray(16)

       Matrix.setIdentityM(scaleM,0)
       Matrix.setIdentityM(rotateM,0)
       Matrix.setIdentityM(translateM,0)
        rotateM = eulerToRotationMatrix(euler.x,euler.y,euler.z)

       Matrix.translateM(translateM,0,position.x,position.y,position.z)
       Matrix.scaleM(scaleM,0,scale.x,scale.y,scale.z)
        var m =  FloatArray(16)
        Matrix.multiplyMM(m,0,rotateM,0,scaleM,0)
        Matrix.multiplyMM(scaleM,0,translateM,0,m,0)
       return scaleM
   }

}