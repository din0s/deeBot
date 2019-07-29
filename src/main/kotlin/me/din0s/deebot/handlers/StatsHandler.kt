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

package me.din0s.deebot.handlers

import me.din0s.config.Config
import me.din0s.util.HttpUtil
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.apache.logging.log4j.LogManager
import org.json.JSONObject

/**
 * Handles the bot's stats.
 *
 * @author Dinos Papakostas
 */
object StatsHandler : ListenerAdapter() {
    private val log = LogManager.getLogger()
    private const val CARBON_URL = "https://www.carbonitex.net/discord/data/botdata.php"

    var sent = 0
    var read = 0

    override fun onGuildJoin(event: GuildJoinEvent) {
        log.info("Joined new server {}", event.guild.id)
        val carbonKey = Config.carbonKey
        if (carbonKey.isNotEmpty()) {
            val data = JSONObject()
                .put("server_count", event.jda.shardManager!!.guildCache.size())
//                .put("shard_id", event.jda.shardInfo.shardId)
//                .put("shard_count", event.jda.shardInfo.shardTotal)
                .put("key", carbonKey)
            HttpUtil.post(CARBON_URL, data.toString())
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        when (event.author == event.jda.selfUser) {
            true -> sent++
            false -> read++
        }
    }
}
