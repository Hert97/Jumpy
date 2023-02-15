package com.example.helpers

import kotlin.math.sqrt

class Vector3(var x: Float, var y: Float, var z: Float) {
    companion object {
        val ZERO = Vector3(0f, 0f, 0f)
        val ONE  = Vector3(1.0f, 1.0f, 1.0f)
        val UNIT_X = Vector3(1f, 0f, 0f)
        val UNIT_Y = Vector3(0f, 1f, 0f)
        val UNIT_Z = Vector3(0f, 0f, 1f)
    }

    operator fun plus(other: Vector3): Vector3 {
        return Vector3(x + other.x, y + other.y, z + other.z)
    }

    operator fun minus(other: Vector3): Vector3 {
        return Vector3(x - other.x, y - other.y, z - other.z)
    }

    operator fun times(scalar: Float): Vector3 {
        return Vector3(x * scalar, y * scalar, z * scalar)
    }

    operator fun div(scalar: Float): Vector3 {
        return Vector3(x / scalar, y / scalar, z / scalar)
    }

    fun dot(other: Vector3): Float {
        return x * other.x + y * other.y + z * other.z
    }

    fun cross(other: Vector3): Vector3 {
        return Vector3(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )
    }

    fun length(): Float {
        return sqrt(x * x + y * y + z * z)
    }

    fun normalize(): Vector3 {
        val length = length()
        return Vector3(x / length, y / length, z / length)
    }

    override fun toString(): String {
        return "($x, $y, $z)"
    }
}
