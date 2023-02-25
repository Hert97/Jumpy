package com.jumpy

import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.math.Vector3

class Physics (_gravity : Float)
{
    private val gravity : Float
    var accelerationAcc = 0.0f //Accumulator
    var acceleration = 0.0f
    var velocity = 0.0f

    init
    {
        gravity = _gravity
    }

    fun reset()
    {
        velocity = 0.0f
        accelerationAcc = 0.0f
        accelerationAcc = 0.0f
    }
    fun update(frameTime: FrameTime?) {
        val dt = frameTime?.deltaSeconds ?: 0f
        acceleration += gravity * dt
        accelerationAcc += acceleration * dt
        velocity += accelerationAcc * dt
    }

    fun applyVelocity(frameTime: FrameTime?, position : Vector3) : Vector3
    {
        val dt = frameTime?.deltaSeconds ?: 0f
        return Vector3(position.x, position.y + velocity * dt, position.z)
    }
}