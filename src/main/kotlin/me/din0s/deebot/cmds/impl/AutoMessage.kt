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

package me.din0s.deebot.cmds.impl

import me.din0s.const.Regex
import me.din0s.deebot.cmds.Command
import me.din0s.util.getAllArgs
import me.din0s.util.reply
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import kotlin.streams.toList

/**
 * Automatically sends a message when a member joins/leaves.
 *
 * @author Dinos Papakostas
 */
abstract class AutoMessage(
    private val action: String,
    alt: String,
    private val getMessage: (Guild) -> String?,
    private val getChannel: (Guild) -> TextChannel,
    private val setMessage: (Guild, String?) -> Unit,
    private val setChannel: (Guild, TextChannel?) -> Unit
) : Command(
    name = "${action}message",
    description = "Set a message to be sent when a new member ${action}s",
    alias = setOf("${alt}message", "${alt}msg", "${action}msg"),
    guildOnly = true,
    userPermissions = arrayOf(Permission.MESSAGE_MANAGE),
    optionalParams = arrayOf("message / channel #channel / reset / channel reset"),
    variables = mapOf(
        Pair("user", "The user's name"),
        Pair("userID", "The user's ID"),
        Pair("tag", "The user as name#tag"),
        Pair("mention", "The user as @mention"),
        Pair("server", "The server's name"),
        Pair("usercount", "The server's user count")
    ),
    examples = arrayOf(
        "${action.capitalize()}: %tag%",
        "channel #my-channel",
        "reset",
        "channel reset"
    )
) {
    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        if (args.isEmpty()) {
            event.reply("__Current $action message:__ ${getMessage(event.guild) ?: "None."}")
            return
        }
        if (args[0].equals("reset", true)) {
            setMessage(event.guild, null)
            event.reply("__Reset the $action message!__")
            return
        }
        if (args[0].equals("channel", true)) {
            if (args.size == 1) {
                event.reply("__Current $action channel:__ ${getChannel(event.guild).asMention}")
                return
            }
            if (args[1].equals("reset", true)) {
                setChannel(event.guild, null)
                event.reply("__Reset the $action channel!__")
                return
            }
            val channelName = event.getAllArgs().substringAfter(' ')
            val channels = when {
                event.message.mentionedChannels.isNotEmpty() -> event.message.mentionedChannels
                channelName.matches(Regex.DISCORD_ID) -> listOf(event.guild.getTextChannelById(channelName))
                else -> event.guild.textChannelCache.applyStream { it.filter { c -> c.name == channelName }.toList() }
            }
            when {
                channels.isNullOrEmpty() -> event.reply("*No channels were found!*")
                channels.size != 1 -> event.reply("**Too many channels were found! Narrow down your selection**")
                else -> {
                    val channel = channels.first()!!
                    setChannel(event.guild, channel)
                    event.reply("__Successfully set the $action channel to:__ ${channel.asMention}")
                }
            }
        } else {
            val msg = event.getAllArgs()
            setMessage(event.guild, msg)
            event.reply("__Successfully set the $action message to:__\n$msg")
        }
    }
}
