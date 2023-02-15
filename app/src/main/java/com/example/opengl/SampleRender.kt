/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.opengl

import android.content.res.AssetManager
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.example.opengl.SampleRender
import java.nio.FloatBuffer
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/** A SampleRender context.  */
class SampleRender constructor(
    glSurfaceView: GLSurfaceView, renderer: Renderer, /* package-private */
    val assets: AssetManager
) {
    private var viewportWidth: Int = 1
    private var viewportHeight: Int = 1
    private lateinit var quadMesh : Mesh
    /**
     * Constructs a SampleRender object and instantiates GLSurfaceView parameters.
     *
     * @param glSurfaceView Android GLSurfaceView
     * @param renderer Renderer implementation to receive callbacks
     * @param assetManager AssetManager for loading Android resources
     */
    init {
        glSurfaceView.setPreserveEGLContextOnPause(true)
        glSurfaceView.setEGLContextClientVersion(3)
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        glSurfaceView.setRenderer(
            object : GLSurfaceView.Renderer {
                public override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
                    GLES30.glEnable(GLES30.GL_BLEND)
                    GLError.maybeThrowGLException("Failed to enable blending", "glEnable")
                    renderer.onSurfaceCreated(this@SampleRender)
                }

                public override fun onSurfaceChanged(gl: GL10, w: Int, h: Int) {
                    viewportWidth = w
                    viewportHeight = h
                    renderer.onSurfaceChanged(this@SampleRender, w, h)
                }

                public override fun onDrawFrame(gl: GL10) {
                    clear( /*framebuffer=*/null, 0f, 0f, 0f, 1f)
                    renderer.onDrawFrame(this@SampleRender)
                }
            })
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        glSurfaceView.setWillNotDraw(false)


        val indices = intArrayOf(0, 1, 2, 2, 3,0)
        val vertexIndices: IntBuffer = IntBuffer.wrap(indices)

        val vertex = floatArrayOf(
            -0.5f, -0.5f, 0.0f,   // bottom left
            0.5f, -0.5f, 0.0f,   // bottom right
            0.5f,  0.5f, 0.0f,  // top right
            -0.5f,  0.5f, 0.0f)    // top left

        val positions : FloatBuffer = FloatBuffer.wrap(vertex)

        val uv = floatArrayOf(
            0.0f, 0.0f,  // bottom left
            1.0f, 0.0f,  // bottom right
            1.0f, 1.0f, // top right
            0.0f, 1.0f  // top left
        )
        val uvs: FloatBuffer = FloatBuffer.wrap(uv)
        //val normals: FloatBuffer = ObjData.getNormals(obj)
        val vertexBuffers: Array<VertexBuffer> = arrayOf(
            VertexBuffer(this, 3, positions),
            VertexBuffer(this, 2, uvs),
            //(render, 3, normals)
        )
        val indexBuffer = IndexBuffer(this, vertexIndices)
        quadMesh = Mesh(this, Mesh.PrimitiveMode.TRIANGLES, indexBuffer, vertexBuffers)
    }
    /**
     * Draw a [Mesh] with the specified [Shader] to the given [Framebuffer].
     *
     *
     * The `framebuffer` argument may be null, in which case the default framebuffer is used.
     */
    /** Draw a [Mesh] with the specified [Shader].  */
    fun draw(mesh: Mesh, shader: Shader?, framebuffer: Framebuffer? =  /*framebuffer=*/null) {
        useFramebuffer(framebuffer)
        shader!!.lowLevelUse()
        mesh.lowLevelDraw()
    }

    fun drawSprite(shader: Shader?, framebuffer: Framebuffer? =  /*framebuffer=*/null) {
        useFramebuffer(framebuffer)
        shader!!.lowLevelUse()
        //TODO not tested yet
        quadMesh.lowLevelDraw()
    }

    /**
     * Clear the given framebuffer.
     *
     *
     * The `framebuffer` argument may be null, in which case the default framebuffer is
     * cleared.
     */
    fun clear(framebuffer: Framebuffer?, r: Float, g: Float, b: Float, a: Float) {
        useFramebuffer(framebuffer)
        GLES30.glClearColor(r, g, b, a)
        GLError.maybeThrowGLException("Failed to set clear color", "glClearColor")
        GLES30.glDepthMask(true)
        GLError.maybeThrowGLException("Failed to set depth write mask", "glDepthMask")
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        GLError.maybeThrowGLException("Failed to clear framebuffer", "glClear")
    }

    /** Interface to be implemented for rendering callbacks.  */
    open interface Renderer {
        /**
         * Called by [SampleRender] when the GL render surface is created.
         *
         *
         * See [GLSurfaceView.Renderer.onSurfaceCreated].
         */
        fun onSurfaceCreated(render: SampleRender?)

        /**
         * Called by [SampleRender] when the GL render surface dimensions are changed.
         *
         *
         * See [GLSurfaceView.Renderer.onSurfaceChanged].
         */
        fun onSurfaceChanged(render: SampleRender?, width: Int, height: Int)

        /**
         * Called by [SampleRender] when a GL frame is to be rendered.
         *
         *
         * See [GLSurfaceView.Renderer.onDrawFrame].
         */
        fun onDrawFrame(render: SampleRender?)
    }

    private fun useFramebuffer(framebuffer: Framebuffer?) {
        val framebufferId: Int
        val viewportWidth: Int
        val viewportHeight: Int
        if (framebuffer == null) {
            framebufferId = 0
            viewportWidth = this.viewportWidth
            viewportHeight = this.viewportHeight
        } else {
            framebufferId = framebuffer.getFramebufferId()
            viewportWidth = framebuffer.width
            viewportHeight = framebuffer.height
        }
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, framebufferId)
        GLError.maybeThrowGLException("Failed to bind framebuffer", "glBindFramebuffer")
        GLES30.glViewport(0, 0, viewportWidth, viewportHeight)
        GLError.maybeThrowGLException("Failed to set viewport dimensions", "glViewport")
    }

    companion object {
        private val TAG: String = SampleRender::class.java.getSimpleName()
    }
}