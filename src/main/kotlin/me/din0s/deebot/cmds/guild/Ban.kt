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
import me.din0s.deebot.entities.Command
import me.din0s.deebot.getUser
import me.din0s.deebot.reply
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class Ban : Command(
    name = "ban",
    description = "Ban a user",
    guildOnly = true,
    minArgs = 1,
    maxArgs = 2,
    botPermissions = arrayOf(Permission.BAN_MEMBERS),
    userPermissions = arrayOf(Permission.BAN_MEMBERS),
    requiredParams = arrayOf("user"),
    optionalParams = arrayOf("days to purge"),
    examples = arrayOf("spammer#1234 1", "squeaker#6969")
) {
    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        val users = when (event.message.mentionedUsers.size) {
            0 -> event.guild.getUser(args[0])
            else -> event.message.mentionedUsers
        }
        val user : User?
        val id = when (users.size) {
            0 -> {
                if (args[0].matches(Regex.DISCORD_ID)) {
                    user = null
                    args[0]
                } else {
                    event.reply("*I didn't find any users matching that criteria!*")
                    return
                }
            }
            1 -> {
                user = users[0]
                users[0].id
            }
            else -> {
                event.reply("**Too many users were found! Please narrow that down.**")
                return
            }
        }

        val days = if (args.size == 2 && args[1].matches(Regex.INTEGER)) {
            val num = args[1].toInt()
            if (num in 1..7) {
                num
            } else {
                event.reply("**That's not a valid amount of days! You can select a value between 1 and 7.**")
                return
            }
        } else {
            0
        }

        if (user != null) {
            val target = event.guild.getMember(user)
            if (target != null) {
                if (!event.member!!.canInteract(target)) {
                    event.reply("**You are not allowed to ban this user!**")
                    return
                }
                if (!event.guild.selfMember.canInteract(target)) {
                    event.reply("**The bot's role is lower in hierarchy than the user's role.**\n" +
                            "__Please drag the role to the top of the list to bypass this!__")
                    return
                }
            }
        }
        event.guild.ban(id, days, "Ban by ${event.author.asMention}")
            .queue(
                {
                    val userMention = when (user) {
                        null -> "U($id)"
                        else -> user.asMention
                    }
                    event.reply("$userMention was **banned** by ${event.author.asMention}")
                },
                {
                    event.reply("**That's not a valid user!**")
                }
            )
    }
}
