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

import me.din0s.Variables
import me.din0s.deebot.entities.Registry
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.*

object Bot {
    private val DISABLED_FLAGS = EnumSet.allOf(CacheFlag::class.java)

    val DEV_ID = Variables.DEV_ID.value.toLong()
    val LOG: Logger = LogManager.getLogger(Bot::class.java)

    fun init() {
        DefaultShardManagerBuilder()
            .setBulkDeleteSplittingEnabled(false)
            .setDisabledCacheFlags(DISABLED_FLAGS)
            .setToken(Config.token)
            .addEventListeners(OnReadyListener)
            .build()
    }
}

private object OnReadyListener : ListenerAdapter() {
    override fun onReady(event: ReadyEvent) {
        Bot.LOG.trace("Received READY")
        event.jda.addEventListener(Registry)
        Bot.LOG.trace("Setup DONE")
    }
}

fun main() {
    Bot.init()
}
