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

import me.din0s.deebot.cmds.Command
import me.din0s.util.*
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Returns info related to a user, and any info relevant to this guild,
 * if this command was issued within a guild context.
 *
 * @author Dinos Papakostas
 */
object Whois : Command(
    name = "whois",
    description = "Get information on a user",
    alias = setOf("userinfo", "user"),
    optionalParams = arrayOf("@user / username / user#tag"),
    examples = arrayOf("deeBot#0996")
) {
    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        val members = when {
            args.isEmpty() -> setOf(event.member)
            else -> event.guild.matchMembers(event.getAllArgs())
        }
        val user = when (members.size) {
            0 -> {
                event.reply("*I couldn't find that user!*")
                return
            }
            1 -> members.first()!!.user
            else -> {
                event.reply("**Too many users matched that criteria! Be more specific.**")
                return
            }
        }

        val sb = StringBuilder("**__User Info__**\n")
            .append("**Username:** ").append(user.asTag).append("\n")
            .append("**ID**: ").append(user.id).append("\n")
            .append("**Creation Date:** ").append(user.timeCreated.asString()).append("\n")

        val avatar = user.avatarUrl
        if (avatar != null) {
            sb.append("**Avatar:** ").append(avatar).append("\n")
        }
        if (event.isFromGuild) {
            sb.append("\n**__Server Info__**\n")
            val member = event.guild.getMember(user)!!
            val nick = member.nickname
            if (nick != null) {
                sb.append("**Nickname:** ").append(nick.escaped()).append("\n")
            }
            sb.append("**Join Date:** ").append(member.timeJoined.asString()).append("\n")
                .append("**Roles:** ").append(member.roles.joinToString(", ") { it.name }.ifEmpty { "@\u180Eeveryone" })
        }
        event.reply(sb.toString())
    }
}
