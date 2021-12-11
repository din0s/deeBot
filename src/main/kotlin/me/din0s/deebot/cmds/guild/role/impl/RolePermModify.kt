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

package me.din0s.deebot.cmds.guild.role.impl

import me.din0s.util.reply
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Modifies (adds/removes) a role's server permissions.
 *
 * @author Dinos Papakostas
 */
abstract class RolePermModify(val add: Boolean) : RoleSub(
    subName = when {
        add -> "addperm"
        else -> "removeperm"
    },
    subDescription = "Modifies the specified role's permissions",
    subMinArgs = 2,
    subRequiredParams = arrayOf("role name / id", "permission"),
    subExamples = arrayOf("DJ MESSAGE_MANAGE")
) {
    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        val role = event.getMentionedRoleOrError(dropLast = true) ?: return
        val perm = try {
            Permission.valueOf(args.last().uppercase())
        } catch (e : IllegalArgumentException) {
            event.reply("**That's not a valid permission!**")
            return
        }

        if (!event.member!!.permissions.contains(perm)) {
            event.reply("**You cannot modify a permission that you don't have!**")
            return
        }
        if (!event.guild.selfMember.permissions.contains(perm)) {
            event.reply("**The bot doesn't have that permission, so it cannot modify the role!**")
            return
        }
        val action = when {
            add -> role.manager.givePermissions(perm)
            else -> role.manager.revokePermissions(perm)
        }
        action.reason("Modified by ${event.author.asTag} (${event.author.id})")
            .queue {
                event.reply("*Successfully modified the role!*")
            }
    }
}
