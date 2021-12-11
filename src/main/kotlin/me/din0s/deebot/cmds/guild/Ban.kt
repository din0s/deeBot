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

import me.din0s.const.Regex
import me.din0s.deebot.cmds.Command
import me.din0s.util.*
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.apache.logging.log4j.LogManager

/**
 * Bans a (list of) user(s).
 *
 * @author Dinos Papakostas
 */
object Ban : Command(
    name = "ban",
    description = "Ban a user",
    guildOnly = true,
    minArgs = 1,
    botPermissions = arrayOf(Permission.BAN_MEMBERS),
    userPermissions = arrayOf(Permission.BAN_MEMBERS),
    requiredParams = arrayOf("user"),
    optionalParams = arrayOf("days to purge"),
    examples = arrayOf("spam#1234 1", "hacker#6969")
) {
    private val log = LogManager.getLogger()

    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        val hasDays = args.last().matches(Regex.INTEGER)
        val days = when {
            hasDays -> args.last().toInt()
            else -> 0
        }
        if (days !in 0..7) {
            event.reply("**That's not a valid amount of days! You can select a value between 1 and 7.**")
            return
        }
        val allArgs = event.getAllArgs()
        val usersString = when {
            hasDays -> allArgs.substringBeforeLast(args.last()).trim()
            else -> allArgs
        }
        val ids = event.jda.matchUserIds(usersString)
        when {
            ids.isEmpty() -> {
                event.reply("*No users were found that meet the given criteria!*")
                return
            }
            ids.size > 5 -> {
                event.reply("*For safety reasons, you cannot ban more than 5 users at a time.*")
                return
            }
        }
        Thread {
            log.info("New ban thread for G#{}", event.guild.id)
            val sb = StringBuilder()
            ids.forEach {
                val member = event.guild.getMemberById(it)
                when (member) {
                    null -> sb.append("User#").append(it).append(": ")
                    else -> sb.append(member.user.asTag.escaped()).append(": ")
                }
                if (member != null && !event.member!!.canInteract(member)) {
                    sb.appendLine("Couldn't be banned since they are higher in hierarchy than you!")
                } else if (member != null &&  !event.guild.selfMember.canInteract(member)) {
                    sb.appendLine("Couldn't be banned since they are higher in hierarchy than the bot!")
                } else if (member != null && member == event.guild.selfMember) {
                    sb.appendLine("Please use ${event.getPrefix().escaped()} to kick me!")
                } else {
                    try {
                        event.guild.ban(it, days)
                            .reason("Banned by ${event.author.asTag} (${event.author.id})")
                            .complete()
                        sb.appendLine("**Banned successfully!**")
                    } catch (e: Exception) {
                        sb.appendLine("**That's not a valid user!**")
                    }
                }
            }
            event.reply(sb.toString())
        }.start()
    }
}
