package com.example.jumpy

import android.opengl.GLES30
import android.util.Log
import java.io.Closeable

class Mesh(val vertexBuffers : Array<GpuBuffer>, val indexBuffer : GpuBuffer ) : Closeable {

    companion object {
        private val TAG = Mesh.javaClass.simpleName
    }

    private val vertexArrayId = intArrayOf(0)


    init {
        try {
            // Create vertex array
            GLES30.glGenVertexArrays(1, vertexArrayId, 0)
            GLError.maybeThrowGLException("Failed to generate a vertex array", "glGenVertexArrays")

            // Bind vertex array
            GLES30.glBindVertexArray(vertexArrayId[0])
            GLError.maybeThrowGLException("Failed to bind vertex array object", "glBindVertexArray")
            if (indexBuffer != null) {
                GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.getBufferId())
            }
            // one vertex attribute index is one buffer
            for (i in vertexBuffers.indices) {
                // Bind each vertex buffer to vertex array
                GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vertexBuffers.get(i).getBufferId())
                GLError.maybeThrowGLException("Failed to bind vertex buffer", "glBindBuffer")
                GLES30.glVertexAttribPointer(
                    i,
                    vertexBuffers.get(i).numberOfBytesPerEntry(),
                    GLES30.GL_FLOAT,
                    false,
                    0,
                    0
                )
                GLError.maybeThrowGLException(
                    "Failed to associate vertex buffer with vertex array", "glVertexAttribPointer"
                )
                GLES30.glEnableVertexAttribArray(i)
                GLError.maybeThrowGLException(
                    "Failed to enable vertex buffer", "glEnableVertexAttribArray"
                )
            }
        } catch (t: Throwable) {
            close()
            throw t
        }
    }
    fun getVertexArrayID() : Int {
        return vertexArrayId[0]
    }
    override fun close() {
        if (vertexArrayId[0] != 0) {
            GLES30.glDeleteVertexArrays(1, vertexArrayId, 0)
            GLError.maybeLogGLError(
                Log.WARN, Mesh.TAG, "Failed to free vertex array object", "glDeleteVertexArrays"
            )
        }
    }

}
