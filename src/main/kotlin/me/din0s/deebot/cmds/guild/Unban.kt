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
import me.din0s.util.escaped
import me.din0s.util.getAllArgs
import me.din0s.util.matchUsers
import me.din0s.util.reply
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Unbans a (list of) user(s).
 *
 * @author Dinos Papakostas
 */
object Unban : Command(
    name = "unban",
    description = "Unban a previously banned user",
    alias = setOf("pardon"),
    guildOnly = true,
    minArgs = 1,
    botPermissions = arrayOf(Permission.BAN_MEMBERS),
    userPermissions = arrayOf(Permission.BAN_MEMBERS),
    requiredParams = arrayOf("user name / id"),
    examples = arrayOf("user#1234", "137904252214444032")
) {
    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        event.guild.retrieveBanList().queue {
            val banList = it.map { ban -> ban.user }.toList()
            val users = banList.matchUsers(event.getAllArgs())
            when {
                users.isEmpty() -> event.reply("*No users were found matching that criteria!*")
                users.size > 5 -> event.reply("*For security reasons you cannot unban more than 5 users at a time!*")
                else -> {
                    users.forEach { user ->
                        event.guild.unban(user)
                            .reason("Unban by ${event.author.asTag} (${event.author.id})")
                            .queue()
                    }
                    event.reply("Unbanned:\n${users.joinToString(", ") { u -> u.asTag.escaped() }}")
                }
            }
        }
    }
}
