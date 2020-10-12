/*
 * MIT License
 *
 * Copyright (c) 2020 Dinos Papakostas
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
import me.din0s.config.Config
import me.din0s.deebot.handlers.CommandHandler
import me.din0s.sql.Database
import me.din0s.util.loadClasses
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.DisconnectEvent
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.ReconnectedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.utils.ChunkingFilter
import org.apache.logging.log4j.LogManager

/**
 * Contains static fields to be used in various commands, managers and handlers.
 *
 * @author Dinos Papakostas
 */
object Bot : ListenerAdapter() {
    private val log = LogManager.getLogger()!!
    private var shardsReady = 0

    val DEV_ID = Variables.DEV_ID.value.toLong()
    val SERVER_INVITE = Variables.SERVER_INVITE.value!!

    lateinit var DEV: User
    lateinit var SHARD_MANAGER: ShardManager

    /**
     * Initializes the bot, which includes setting up the Database,
     * registering Event Handlers, and connecting to Discord.
     */
    fun init() {
        Database.init()

        val builder = DefaultShardManagerBuilder
            .createDefault(Config.token)
            .enableIntents(GatewayIntent.GUILD_MEMBERS)
            .setChunkingFilter(ChunkingFilter.ALL)
            .setBulkDeleteSplittingEnabled(false)
            .addEventListeners(this)

        log.info("Registering Event Handlers")
        loadClasses("me.din0s.deebot.handlers", ListenerAdapter::class.java).forEach {
            log.debug("Registering {}", it.javaClass.simpleName)
            builder.addEventListeners(it)
        }

        log.info("Connecting to Discord")
        builder.build()
    }

    override fun onReady(event: ReadyEvent) {
        log.info("Shard #{} READY", event.jda.shardInfo.shardId)
        shardsReady++

        val sm = event.jda.shardManager!!
        if (shardsReady == sm.shardsTotal) {
            SHARD_MANAGER = sm
            Database.postReady()
            CommandHandler.init()

            sm.retrieveUserById(DEV_ID).queue {
                DEV = it
            }
        }
    }

    override fun onDisconnect(event: DisconnectEvent) {
        log.info("Shard #{} DISCONNECTED", event.jda.shardInfo.shardId)
    }

    override fun onReconnect(event: ReconnectedEvent) {
        log.info("Shard #{} RECONNECTED", event.jda.shardInfo.shardId)
    }
}

/**
 * Main method. Execution starts here.
 */
fun main() = Bot.init()
