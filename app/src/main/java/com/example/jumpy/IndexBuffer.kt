package com.example.jumpy

import android.opengl.GLES30
import java.io.Closeable
import java.nio.IntBuffer

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
/**
 * A list of vertex indices stored GPU-side.
 *
 *
 * When constructing a [Mesh], an [IndexBuffer] may be passed to describe the
 * ordering of vertices when drawing each primitive.
 *
 * @see [glDrawElements](https://www.khronos.org/registry/OpenGL-Refpages/es3.0/html/glDrawElements.xhtml)
 */
class IndexBuffer(render: SampleRender?, entries: IntBuffer?) : Closeable {
    private val buffer: GpuBuffer

    /**
     * Construct an [IndexBuffer] populated with initial data.
     *
     *
     * The GPU buffer will be filled with the data in the *direct* buffer `entries`,
     * starting from the beginning of the buffer (not the current cursor position). The cursor will be
     * left in an undefined position after this function returns.
     *
     *
     * The `entries` buffer may be null, in which case an empty buffer is constructed
     * instead.
     */
    init {
        buffer = GpuBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, GpuBuffer.INT_SIZE, entries)
    }

    /**
     * Populate with new data.
     *
     *
     * The entire buffer is replaced by the contents of the *direct* buffer `entries`
     * starting from the beginning of the buffer, not the current cursor position. The cursor will be
     * left in an undefined position after this function returns.
     *
     *
     * The GPU buffer is reallocated automatically if necessary.
     *
     *
     * The `entries` buffer may be null, in which case the buffer will become empty.
     */
    fun set(entries: IntBuffer?) {
        buffer.set(entries)
    }

    override fun close() {
        buffer.free()
    }

    /* package-private */
    val bufferId: Int
        get() = buffer.getBufferId()

    /* package-private */
    val size: Int
        get() = buffer.size
}