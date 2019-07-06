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

package me.din0s.deebot

import me.din0s.const.Errors
import org.apache.logging.log4j.LogManager
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.system.exitProcess

object Config {
    private val log = LogManager.getLogger(Config::class.java)

    private val cfgFile = File("config.json")

    init {
        if (!cfgFile.exists()) {
            if (cfgFile.createNewFile()) {
                val jsonObj = JSONObject()
                jsonObj.put("token", "")
                jsonObj.put("prefix", "")
                jsonObj.put("google_key", "")
                jsonObj.put("google_search", "")
                cfgFile.writeText(jsonObj.toString(4))

                log.info("Created config.json! Please fill in your credentials.")
                exitProcess(0)
            } else {
                log.error(Errors.CONFIG_GEN_IO)
                throw IOException()
            }
        }
    }

    private val cfgJson by lazy {
        try {
            verify(JSONObject(Files.readAllLines(cfgFile.toPath()).joinToString("")))
        } catch (e: IOException) {
            log.error(Errors.CONFIG_READ_IO)
            throw e
        }
    }

    val token: String = cfgJson.getString("token")
    val defaultPrefix: String = cfgJson.getString("prefix")
    val googleKey: String = cfgJson.getString("google_key")
    val googleSearch: String = cfgJson.getString("google_search")

    private fun verify(jsonObj: JSONObject) : JSONObject {
        for (key in setOf("token", "prefix", "google_key", "google_search")) {
            if (!jsonObj.has(key) || jsonObj.get(key).toString().isEmpty()) {
                log.warn(Errors.CONFIG_MISSING_VAL, key)

                try {
                    val default = Config.DefaultValues.valueOf(key.toUpperCase())
                    jsonObj.put(key, default.value)
                    log.info("Using default: {}", default.value)
                } catch (ignored: Exception) {
                    // ignored
                }
            }
        }

        return jsonObj
    }

    private enum class DefaultValues(val value: String) {
        PREFIX("!!")
    }
}
