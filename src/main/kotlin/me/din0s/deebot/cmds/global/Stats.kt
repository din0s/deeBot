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

import me.din0s.deebot.entities.Command
import me.din0s.deebot.handlers.StatsHandler
import me.din0s.deebot.reply
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

object Stats : Command(
    name = "stats",
    description = "Display the bot's statistics",
    alias = setOf("statistics")
) {
    private val c = "\u00b7"

    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        val sm = event.jda.shardManager!!
        event.reply("""
            __Connections__
            **$c ${sm.guildCache.size()}** total servers
            **$c ${sm.textChannelCache.size()}** text channels
            **$c ${sm.voiceChannelCache.size()}** voice channels
            **$c ${sm.userCache.size()}** unique users
            
            __Callbacks__
            **$c ${StatsHandler.read}** read messages
            **$c ${StatsHandler.sent}** sent messages
        """.trimIndent())
    }
}
