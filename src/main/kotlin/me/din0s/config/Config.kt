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

package me.din0s.config

import me.din0s.config.errors.ConfigGenerationError
import me.din0s.config.errors.ConfigReadError
import me.din0s.config.errors.ConfigValueError
import me.din0s.util.showWarning
import me.din0s.util.throwError
import org.apache.logging.log4j.LogManager
import org.json.JSONObject
import org.reflections.ReflectionUtils
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.system.exitProcess

/**
 * This object handles the config file that contains all parameters needed to run the bot.
 *
 * @author Dinos Papakostas
 */
object Config {
    private val log = LogManager.getLogger()
    private val cfgFile = File("config.json")
    private val fields = arrayOf(
        Pair("token", "token"),
        Pair("prefix", "defaultPrefix"),
        Pair("carbon_key", "carbonKey"),
        Pair("google_key", "googleKey"),
        Pair("google_search", "googleSearch"),
        Pair("patreon", "patreon"),
        Pair("sql_db", "sqlDb"),
        Pair("sql_user", "sqlUser"),
        Pair("sql_pwd", "sqlPwd")
    )

    @JvmField var token: String = ""
    @JvmField var defaultPrefix: String = ""
    @JvmField var carbonKey: String = ""
    @JvmField var googleKey: String = ""
    @JvmField var googleSearch: String = ""
    @JvmField var patreon: String = ""
    @JvmField var sqlDb: String = ""
    @JvmField var sqlUser: String = ""
    @JvmField var sqlPwd: String = ""

    /**
     * Create the file if it doesn't exist or proceed to read the existing values.
     */
    init {
        if (!cfgFile.exists()) {
            if (cfgFile.createNewFile()) {
                val jsonObj = JSONObject()
                fields.forEach { jsonObj.put(it.first, "") }
                cfgFile.writeText(jsonObj.toString(4))
                log.info("Created config.json! Please fill in your credentials.")
                exitProcess(0)
            } else {
                log.throwError(ConfigGenerationError)
            }
        }
        readValues()
    }

    /**
     * Read all keys that are present in the the fields map.
     * If a required value is missing, the program will terminate.
     * There will be an attempt to use a default value, if provided.
     */
    fun readValues() {
        try {
            val lines = Files.readAllLines(cfgFile.toPath()).joinToString("")
            val json = JSONObject(lines)
            for (key in fields) {
                val cfgKey = key.first
                if (!json.has(cfgKey) || json.getString(cfgKey).isEmpty()) {
                    val cfgError = ConfigValueError(cfgKey)
                    try {
                        val default = DefaultValues.valueOf(cfgKey.uppercase()).value
                        log.showWarning(cfgError)
                        log.warn("Using default value for {}: '{}'", cfgKey, default)
                        json.put(cfgKey, default)
                    } catch (e: IllegalArgumentException) {
                        log.throwError(cfgError)
                    }
                }
                ReflectionUtils.getFields(Config::class.java)
                    .first { it.name == key.second }
                    .set(Config, json.getString(cfgKey))
            }
        }  catch (e: IOException) {
            log.throwError(ConfigReadError)
        }
    }

    /**
     * This enum contains the default values that may be used for a key
     * that is not present/populated in the config file.
     */
    private enum class DefaultValues(val value: String) {
        PREFIX("!!"),
        SQL_DB("discord"),
        SQL_USER("root"),
        SQL_PWD("root"),
        CARBON_KEY(""),
        PATREON(""),
        GOOGLE_KEY(""),
        GOOGLE_SEARCH("")
    }
}
