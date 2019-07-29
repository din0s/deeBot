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
import me.din0s.sql.managers.TimerManager
import me.din0s.util.*
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Uses a countdown to send a message,
 * with an option to be repeated indefinitely.
 *
 * @author Dinos Papakostas
 */
abstract class Timer(
    private val private: Boolean
) : Command(
    name = when {
        private -> "reminder"
        else -> "announcement"
    },
    description =  "Set a countdown timer to send a message in the future",
    alias = when {
        private -> setOf("remindme", "reminders")
        else -> setOf("announce", "announcements")
    },
    guildOnly = !private,
    optionalParams = arrayOf("info / cancel [id] / [message] | [duration]"),
    flags = mapOf(Pair("repeat", "Set this timer to repeat")),
    examples = arrayOf(
        "Join Minecraft Server | 10 minutes",
        "Morning Workout | 09:00 GMT+3",
        "Weekly Check-up | Monday 12:00 UTC --repeat",
        "info",
        "cancel 1"
    )
) {
    private val deleteKeywords = setOf("cancel", "delete")
    private val listKeywords = setOf("info", "list")
    private val repeatFlags = setOf("-r", "--repeat")
    private val maxPreviewLength = 100

    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        if (args.isEmpty() || (args.size == 1 && listKeywords.contains(args[0].toLowerCase()))) {
            val set = TimerManager.get(event.author)
            if (set.isEmpty()) {
                event.reply("You don't have any ${name}s set!")
            } else {
                val timers = set
                    .filter { private && it.private || !private && !it.private }
                    .sortedBy { it.next }
                    .mapNotNull {
                        if (private) {
                            "**`#${it.uuid}`** __${it.next.asString()}__\n${it.message}"
                        } else {
                            val channel = event.guild.getTextChannelById(it.channelId)
                            if (channel == null) {
                                null
                            } else {
                                "**`#${it.uuid}`** __${it.next.asString()}__ in ${channel.asMention}\n${it.message}"
                            }
                        }
                    }
                    .joinToString("\n\n", limit = maxPreviewLength)
                event.reply("**Your timers:**\n${timers.ifBlank { "None." }}")
            }
            return
        } else if (args.size == 2 && deleteKeywords.contains(args[0].toLowerCase())) {
            if (!args[1].matches(Regex.INTEGER)) {
                event.reply("**That's not a valid timer ID.**")
            } else {
                val id = args[1].toLong()
                val timer = TimerManager.get(event.author).find { it.uuid == id }
                if (timer == null) {
                    event.reply("*You don't have a timer with that ID!*")
                } else {
                    timer.cancel()
                    event.reply("__Your timer has been cancelled!__")
                }
            }
            return
        }

        val allArgs = event.getAllArgs()
        val params = allArgs.split(Regex.PIPE)
        if (params.size == 1) {
            event.showUsage(this)
            return
        }
        val repeat = repeatFlags.contains(args.last().toLowerCase())
        val duration = when {
            repeat -> params.last().substringBeforeLast('-').trim()
            else -> params.last()
        }
        val message = allArgs.substringBeforeLast('|').trim()
        event.handleUserError {
            if (private) {
                event.author.openPrivateChannel().queue {
                    event.createTimer(message, it, duration, repeat)
                }
            } else {
                event.createTimer(message, event.channel, duration, repeat)
            }
        }
    }

    /**
     * Creates the timer and replies to the user who issued the command.
     *
     * @param message The message to be broadcaster once the countdown hits zero.
     * @param channel The channel to send the message in.
     * @param duration The duration string that determines when the message will be sent.
     * @param repeat Whether the timer should repeat.
     */
    private fun MessageReceivedEvent.createTimer(
        message: String,
        channel: MessageChannel,
        duration: String,
        repeat: Boolean
    ) {
        val timer = TimerManager.create(
            msg = message,
            user = author,
            channel = channel,
            durationStr = duration,
            repeat = repeat,
            inPrivate = private
        )
        reply("Set a timer for __${timer.next.asString()}__")
    }
}
