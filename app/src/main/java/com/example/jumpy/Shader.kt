package com.example.jumpy

import android.content.res.AssetManager
import android.opengl.GLES30
import android.opengl.GLException
import android.util.Log
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class Shader(vertexShaderCode: String, fragmentShaderCode: String) : Closeable {
    companion object {
        private val TAG: String = Shader::class.java.simpleName

        @Throws(IOException::class)
        private fun inputStreamToString(stream: InputStream): String? {
            val reader = InputStreamReader(stream, StandardCharsets.UTF_8.name())
            val buffer = CharArray(1024 * 4)
            val builder = StringBuilder()
            var amount = 0
            while (reader.read(buffer).also { amount = it } != -1) {
                builder.append(buffer, 0, amount)
            }
            reader.close()
            return builder.toString()
        }
    }

    private var programId = 0

    init {
        var vertexShaderId = 0
        var fragmentShaderId = 0
        try {
            vertexShaderId = createShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
            fragmentShaderId = createShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)

            programId = GLES30.glCreateProgram()
            GLError.maybeThrowGLException("Shader program creation failed", "glCreateProgram")
            GLES30.glAttachShader(programId, vertexShaderId)
            GLError.maybeThrowGLException("Failed to attach vertex shader", "glAttachShader")
            GLES30.glAttachShader(programId, fragmentShaderId)
            GLError.maybeThrowGLException("Failed to attach fragment shader", "glAttachShader")
            GLES30.glLinkProgram(programId)
            GLError.maybeThrowGLException("Failed to link shader program", "glLinkProgram")

            var linkStatus = IntArray(1)
            GLES30.glGetProgramiv(programId, GLES30.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == GLES30.GL_FALSE) {
                val infoLog = GLES30.glGetProgramInfoLog(programId )
                GLError.maybeLogGLError(Log.WARN, TAG,
                    "Failed to retrieve shader program info log", "glGetProgramInfoLog")
                throw GLException(0, "Shader link failed: "+infoLog)
            }
        } catch (t : Throwable) {
            close()
            throw t
        } finally {
            // Shader objects can be flagged for deletion immediately after program creation.

            // Shader objects can be flagged for deletion immediately after program creation.
            if (vertexShaderId != 0) {
                GLES30.glDeleteShader(vertexShaderId)
                GLError.maybeLogGLError(
                    Log.WARN,
                    Shader.TAG,
                    "Failed to free vertex shader",
                    "glDeleteShader"
                )
            }
            if (fragmentShaderId != 0) {
                GLES30.glDeleteShader(fragmentShaderId)
                GLError.maybeLogGLError(
                    Log.WARN,
                    Shader.TAG,
                    "Failed to free fragment shader",
                    "glDeleteShader"
                )
            }

        }
    }

    fun createShader(type: Int, code: String): Int {
        val shaderId = GLES30.glCreateShader(type)
        GLError.maybeThrowGLException("Shader creation failed", "glCreateShader")
        GLES30.glShaderSource(shaderId, code)
        GLError.maybeThrowGLException("Shader source failed", "glShaderSource")
        GLES30.glCompileShader(shaderId)
        GLError.maybeThrowGLException("Shader compilation failed", "glCompileShader")
        val compileStatus = IntArray(1)
        GLES30.glGetShaderiv(shaderId, GLES30.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == GLES30.GL_FALSE) {
            val infoLog = GLES30.glGetShaderInfoLog(shaderId)
            GLError.maybeLogGLError(
                Log.WARN,
                Shader.TAG,
                "Failed to retrieve shader info log",
                "glGetShaderInfoLog"
            )
            GLES30.glDeleteShader(shaderId)
            GLError.maybeLogGLError(
                Log.WARN,
                Shader.TAG,
                "Failed to free shader",
                "glDeleteShader"
            )
            throw GLException(0, "Shader compilation failed: $infoLog")
        }
        return shaderId
    }

    @Throws(IOException::class)
    fun createFromAssets(
        render: RendererBase,
        vertexShaderFileName: String?,
        fragmentShaderFileName: String?,
    ): Shader?
    {
        val assets: AssetManager = render.assets

        val streamVertex = Shader.inputStreamToString(assets.open(vertexShaderFileName!!))
        val streamFrag = Shader.inputStreamToString(assets.open(fragmentShaderFileName!!))

        streamVertex?.let { first ->
            streamFrag?.let { second ->
                return Shader(first,second)
            }
        }
        return null
    }

    override fun close() {
        if (programId != 0) {
            GLES30.glDeleteProgram(programId)
            programId = 0
        }
    }
}
