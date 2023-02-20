package com.example

import com.google.ar.sceneform.math.Vector3

class AABB(center: Vector3, width: Float, height: Float) {

    var min: Vector3
    var max: Vector3

    init {
        val halfWidth = width / 2
        val halfHeight = height / 2

        min = Vector3(center.x - halfWidth, center.y - halfHeight, center.z)
        max = Vector3(center.x + halfWidth, center.y + halfHeight, center.z)
    }

    fun intersects(other: AABB): Boolean {
        return (min.x <= other.max.x && max.x >= other.min.x) &&
                (min.y <= other.max.y && max.y >= other.min.y) &&
                (min.z <= other.max.z && max.z >= other.min.z)
    }
}