package com.example.jumpy

import android.content.res.AssetManager
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

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
class RendererBase(
    glSurfaceView: GLSurfaceView,
    renderer: Renderer, /* package-private */
    val assets: AssetManager
) {
    private var viewportWidth = 1
    private var viewportHeight = 1

    /**
     * Constructs a SampleRender object and instantiates GLSurfaceView parameters.
     *
     * @param glSurfaceView Android GLSurfaceView
     * @param renderer Renderer implementation to receive callbacks
     * @param assetManager AssetManager for loading Android resources
     */
    init {
        glSurfaceView.preserveEGLContextOnPause = true
        glSurfaceView.setEGLContextClientVersion(3)
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        glSurfaceView.setRenderer(
            object : GLSurfaceView.Renderer {
                override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
                    GLES30.glEnable(GLES30.GL_BLEND)
                    GLError.maybeThrowGLException("Failed to enable blending", "glEnable")
                    renderer.onSurfaceCreated(this@RendererBase)
                }

                override fun onSurfaceChanged(gl: GL10, w: Int, h: Int) {
                    viewportWidth = w
                    viewportHeight = h
                    renderer.onSurfaceChanged(this@RendererBase, w, h)
                }

                override fun onDrawFrame(gl: GL10) {
                    clear( /*framebuffer=*/null, 0f, 0f, 0f, 1f)
                    renderer.onDrawFrame(this@RendererBase)
                }
            })
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        glSurfaceView.setWillNotDraw(false)
    }

    fun draw(mesh: Mesh, shader: Shader) {
        //draw(mesh, shader,  /*framebuffer=*/null)
    }


    fun draw(mesh: Mesh, shader: Shader,primitiveMode : Int ,framebuffer: Framebuffer?) {
       /* useFramebuffer(framebuffer)
        shader.lowLevelUse()

        val vertexArrayId = mesh.getVertexArrayID()
        check(vertexArrayId != 0) { "Tried to draw a freed Mesh" }

        GLES30.glBindVertexArray(vertexArrayId)
        GLError.maybeThrowGLException("Failed to bind vertex array object", "glBindVertexArray")

        //no indices
        if (mesh.indexBuffer == null) {
            // Sanity check for debugging
            val numberOfVertices: Int = mesh.vertexBuffers.get(0).getNumberOfVertices()
            for (i in 1 until mesh.vertexBuffers.size) {
                check(
                    !(mesh.vertexBuffers.get(i).getNumberOfVertices() !== numberOfVertices)
                ) { "Vertex buffers have mismatching numbers of vertices" }
            }
            GLES30.glDrawArrays(primitiveMode, 0, numberOfVertices)
            GLError.maybeThrowGLException("Failed to draw vertex array object", "glDrawArrays")
        } else {
            GLES30.glDrawElements(
                primitiveMode, indexBuffer.getSize(), GLES30.GL_UNSIGNED_INT, 0
            )
            GLError.maybeThrowGLException(
                "Failed to draw vertex array object with indices", "glDrawElements"
            )
        }*/
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
    interface Renderer {
        /**
         * Called by [SampleRender] when the GL render surface is created.
         *
         *
         * See [GLSurfaceView.Renderer.onSurfaceCreated].
         */
        fun onSurfaceCreated(render: RendererBase?)

        /**
         * Called by [SampleRender] when the GL render surface dimensions are changed.
         *
         *
         * See [GLSurfaceView.Renderer.onSurfaceChanged].
         */
        fun onSurfaceChanged(render: RendererBase?, width: Int, height: Int)

        /**
         * Called by [SampleRender] when a GL frame is to be rendered.
         *
         *
         * See [GLSurfaceView.Renderer.onDrawFrame].
         */
        fun onDrawFrame(render: RendererBase?)
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
            viewportWidth = framebuffer.getWidth()
            viewportHeight = framebuffer.getHeight()
        }
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, framebufferId)
        GLError.maybeThrowGLException("Failed to bind framebuffer", "glBindFramebuffer")
        GLES30.glViewport(0, 0, viewportWidth, viewportHeight)
        GLError.maybeThrowGLException("Failed to set viewport dimensions", "glViewport")
    }


    companion object {
        private val TAG = RendererBase::class.java.simpleName
    }
}