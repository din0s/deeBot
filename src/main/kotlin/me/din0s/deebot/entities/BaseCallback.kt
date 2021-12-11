/*
 * MIT License
 *
 * Copyright (c) 2019 Dinos Papakostas
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.din0s.deebot.entities

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.apache.logging.log4j.LogManager
import org.json.JSONObject
import java.io.IOException

/**
 * Basic implementation of a [Callback] to be used for HTTP requests.
 *
 * @author Dinos Papakostas
 */
open class BaseCallback : Callback {
    private val log = LogManager.getLogger()

    override fun onFailure(call: Call, e: IOException) {
        log.warn("Request to {} failed", call.request().url.host)
        e.printStackTrace()
    }

    override fun onResponse(call: Call, response: Response) {
        log.trace("Request to {} returned {}", call.request().url.host, response.code)
        handleResponse(call, response)
        response.close()
    }

    /**
     * Handles the response received.
     *
     * @param call The call that was made.
     * @param response The response received.
     */
    open fun handleResponse(call: Call, response: Response) {}

    /**
     * Convert the body of the response to a JSON object.
     *
     * @return The parsed JSONObject from the response body.
     */
    fun Response.asJson() : JSONObject {
        val bodyContent = body!!.string()
        log.trace(bodyContent)
        return JSONObject(bodyContent)
    }

    /**
     * Converts the response body to a [ByteArray].
     *
     * @return A byte array of the response's body contents.
     */
    fun Response.toBytes() : ByteArray {
        val stream = body!!.byteStream()
        val bytes = stream.readBytes()
        stream.close()
        return bytes
    }
}
