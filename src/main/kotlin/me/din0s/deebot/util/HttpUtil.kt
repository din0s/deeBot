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

package me.din0s.deebot.util

import me.din0s.deebot.entities.BaseCallback
import okhttp3.*
import org.apache.logging.log4j.LogManager
import java.util.*

object HttpUtil {
    private val client = OkHttpClient()
    private val log = LogManager.getLogger(HttpUtil::class.java)
    private val JSON = MediaType.parse("application/json; charset=utf-8")

    fun get(url: String, auth: Pair<String, String>? = null, cb: Callback) {
        val httpUrl = HttpUrl.parse(url)!!.newBuilder().build()
        get(httpUrl, auth, cb)
    }

    fun get(url: HttpUrl, auth: Pair<String, String>? = null, cb: Callback) {
        log.trace("GET Request to {}", url)
        val request = Request.Builder().url(url).auth(auth)
        call(request, cb)
    }

    fun post(url: String, body: String, auth: Pair<String, String>? = null, cb: Callback = BaseCallback()) {
        val httpUrl = HttpUrl.parse(url)!!.newBuilder().build()
        post(httpUrl, body, auth, cb)
    }

    fun post(url: HttpUrl, body: String, auth: Pair<String, String>? = null, cb: Callback = BaseCallback()) {
        log.trace("POST Request to {}", url)
        val postBody = RequestBody.create(JSON, body)
        val request = Request.Builder().url(url).post(postBody).auth(auth)
        call(request, cb)
    }

    private fun Request.Builder.auth(auth: Pair<String, String>?) : Request.Builder {
        auth ?: return this
        val token = when {
            auth.first.isBlank() -> auth.second
            else -> {
                val str = "${auth.first}:${auth.second}"
                Base64.getEncoder().encodeToString(str.toByteArray())
            }
        }
        addHeader("Authorization", token)
        return this
    }

    private fun call(request: Request.Builder, cb: Callback) {
//        request.addHeader("Content-Type", "application-json")
        client.newCall(request.build()).enqueue(cb)
    }
}
