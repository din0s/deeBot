/*
 * MIT License
 *
 * Copyright (c) 2020 Dinos Papakostas
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
import me.din0s.util.*
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Kicks a (list of) user(s).
 *
 * @author Dinos Papakostas
 */
object Kick : Command(
    name = "kick",
    description = "Kick a member from the server",
    alias = setOf("boot"),
    guildOnly = true,
    minArgs = 1,
    botPermissions = arrayOf(Permission.KICK_MEMBERS),
    userPermissions = arrayOf(Permission.KICK_MEMBERS),
    requiredParams = arrayOf("user"),
    examples = arrayOf("bad guy#1111")
) {
    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        val members = event.guild.matchMembers(event.getAllArgs())
        when {
            members.isEmpty() -> {
                event.reply("*No members were found that meet the given criteria!*")
                return
            }
            members.size > 10 -> {
                event.reply("*For safety reasons, you cannot kick more than 10 members at a time.*")
                return
            }
        }
        val sb = StringBuilder()
        members.forEach {
            sb.append(it.user.asTag.escaped()).append(": ")
            if (!event.member!!.canInteract(it)) {
                sb.appendln("Couldn't be kicked since they are higher in hierarchy than you!")
            } else if (!event.guild.selfMember.canInteract(it)) {
                sb.appendln("Couldn't be kicked since they are higher in hierarchy than the bot!")
            } else if (it == event.guild.selfMember) {
                sb.appendln("Please use ${event.getPrefix().escaped()} to kick me!")
            } else {
                event.guild.kick(it).reason("Kicked by ${event.author.asTag} (${event.author.id})").queue()
                sb.appendln("**Kicked successfully!**")
            }
        }
        event.reply(sb.toString())
    }
}
