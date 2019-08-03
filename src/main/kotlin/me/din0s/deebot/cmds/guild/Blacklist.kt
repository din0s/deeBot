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

package me.din0s.deebot.cmds.guild

import me.din0s.deebot.cmds.Command
import me.din0s.sql.managers.BlacklistManager
import me.din0s.util.reply
import me.din0s.util.showUsage
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Adds a channel to the blacklist, where commands will be ignored.
 *
 * @author Dinos Papakostas
 */
object Blacklist : Command(
    name = "blacklist",
    description = "Disable the bot in a specific channel",
    guildOnly = true,
    maxArgs = 1,
    userPermissions = arrayOf(Permission.ADMINISTRATOR),
    optionalParams = arrayOf("info / #channel"),
    examples = arrayOf("#new-people", "info")
) {
    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        if (args.isEmpty() || args[0].equals("info", true)) {
            val channels = mutableListOf<String>()
            BlacklistManager.getForGuild(event.guild).forEach { channels.add(" ${it.asMention}") }
            if (channels.isEmpty()) {
                event.reply("Blacklisted Channels: __None__")
                return
            }
            val sb = StringBuilder("Blacklisted Channels:")
            channels.forEach { sb.append(it) }
            if (sb.length > 2000) {
                event.reply("Wow, you have blacklisted too many channels to display here...")
            } else {
                event.reply(sb.toString())
            }
            return
        }
        val channels = event.message.mentionedChannels
        when (channels.size) {
            0 -> event.showUsage(this)
            1 -> {
                val id = channels[0].idLong
                val toggle = BlacklistManager.toggle(id)
                when {
                    toggle -> event.reply("Added ${channels[0].asMention} to the blacklist")
                    else -> event.reply("Removed ${channels[0].asMention} from the blacklist")
                }
            }
            else -> event.reply("*You mentioned too many channels!*")
        }
    }
}
