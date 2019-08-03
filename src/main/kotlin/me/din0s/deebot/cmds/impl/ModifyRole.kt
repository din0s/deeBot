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

import me.din0s.deebot.cmds.Command
import me.din0s.util.escaped
import me.din0s.util.getAllArgs
import me.din0s.util.getRoleOrShowError
import me.din0s.util.reply
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction

/**
 * Adds/removes a role from a member.
 *
 * @author Dinos Papakostas
 */
abstract class ModifyRole(
    private val action: String,
    private val restAction: (Member, Role) -> AuditableRestAction<Void>
) : Command(
    name = "${action}role",
    description = "Modifies a member's roles",
    guildOnly = true,
    minArgs = 2,
    botPermissions = arrayOf(Permission.MANAGE_ROLES),
    userPermissions = arrayOf(Permission.MANAGE_ROLES),
    requiredParams = arrayOf("@user", "role"),
    examples = arrayOf("@dinos DJ", "@guy#1234 Moderator")
) {
    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        when {
            event.message.mentionedMembers.isEmpty() -> event.reply("*No users found! Please @mention a user.*")
            event.message.mentionedMembers.size != 1 -> event.reply("*You mentioned too many users!*")
            else -> {
                val member = event.message.mentionedMembers.first()
                val roleName = event.getAllArgs().substringAfter('>').trim()
                val role = event.getRoleOrShowError(roleName) ?: return
                restAction(member, role)
                    .reason("${action.capitalize()} by ${event.author.asTag} (${event.author.id})")
                    .queue()
                event.reply("${role.name.escaped()} was ${getAction()} ${member.user.asTag.escaped()}!")
            }
        }
    }

    /**
     * Returns the proper action verb depending on the action field.
     *
     * @return One of "given to", "taken from" or "modified for".
     */
    private fun getAction() : String {
        return when (action) {
            "add" -> "given to"
            "remove" -> "taken from"
            else -> "modified for"
        }
    }
}
