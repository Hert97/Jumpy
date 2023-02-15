package com.example

import com.example.opengl.*
import java.nio.FloatBuffer
import java.nio.IntBuffer

//TODO not tested yet
class Sprite(val render: SampleRender?) {
    val transform : Transform = Transform()
    val sprite : Texture? = null

    fun draw(shader: Shader) {
        render?.drawSprite(shader)
    }

}