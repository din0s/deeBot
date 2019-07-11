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

package me.din0s.deebot.cmds.global

import me.din0s.const.Regex
import me.din0s.deebot.entities.Command
import me.din0s.deebot.reply
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.sharding.ShardManager

class Shard : Command(
    name = "shard",
    description = "Get information for the bot's shards",
    alias = setOf("sharding", "shards", "shardmanager"),
    maxArgs = 1,
    optionalParams = arrayOf("shard id")
) {
    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        val sm = event.jda.shardManager!!
        if (args.isEmpty()) {
            event.reply(sm.getInfo(event.jda.shardInfo.shardId))
        } else if (args[0] != "*" && !args[0].matches(Regex.INTEGER)) {
            event.reply("That's not a valid shard number!")
        } else {
            val id = when {
                args[0] == "*" -> null
                else -> args[0].toInt()
            }
            event.reply(sm.getInfo(id))
        }
    }

    private fun ShardManager.getInfo(id: Int? = null) : String {
        val shardList = when {
            id == null -> shards
            id < -1 || id.toInt() >= shardCache.size() -> return "That's not a valid shard number!"
            else -> listOf(getShardById(id)!!)
        }

        val sb = StringBuilder()
        shardList.sortedBy { it.shardInfo.shardId }.forEach {
            sb.append("__**`Shard #").append(it.shardInfo.shardId).append("`**__\n")
                .append("**› ").append(it.guildCache.size()).append("** servers\n")
                .append("**› ").append(it.textChannelCache.size()).append("** text channels\n")
                .append("**› ").append(it.voiceChannelCache.size()).append("** voice channels\n")
                .append("**› ").append(it.userCache.size()).append("** users\n\n")
        }
        return sb.toString()
    }
}
