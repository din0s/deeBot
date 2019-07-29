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

package me.din0s.deebot.cmds.guild.role

import me.din0s.deebot.cmds.guild.role.impl.RoleSub
import me.din0s.util.getAllArgs
import me.din0s.util.reply
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Creates a new role.
 *
 * @author Dinos Papakostas
 */
object RoleCreate : RoleSub(
    subName = "create",
    subDescription = "Create a new server role",
    subAlias = setOf("new", "make"),
    subMinArgs = 1,
    subRequiredParams = arrayOf("role name"),
    subFlags = mapOf(
        Pair("null", "Do not add any permissions"),
        Pair("full", "Add all available permissions")
        ),
    subExamples = arrayOf("MyRole", "Admin --full")
) {
    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        val isFull = args.size > 1 && args.last().equals("--full", true)
        if (isFull) {
            if (!event.member!!.permissions.contains(Permission.ADMINISTRATOR)) {
                event.reply("**You need ADMIN permission to create a role with all permissions!**")
                return
            }
            if (!event.guild.selfMember.permissions.contains(Permission.ADMINISTRATOR)) {
                event.reply("**The bot has to have ADMIN permission to create a role with all permissions!**")
                return
            }
        }
        val isNull = args.size > 1 && args.last().equals("--null", true)
        val skipLast = isFull || isNull
        val allArgs = event.getAllArgs().substringAfter(' ')
        val name = when {
            skipLast -> allArgs.substringBeforeLast(' ')
            else -> allArgs
        }

        val action = event.guild.createRole().setName(name)
        if (isFull) {
            action.setPermissions(Permission.ALL_GUILD_PERMISSIONS)
        } else if (isNull) {
            action.setPermissions(Permission.EMPTY_PERMISSIONS.toList())
        }

        action.reason("Created by ${event.author.asTag} (${event.author.id})")
            .queue {
                event.reply("*Role successfully created with ID: ${it.id}*")
            }
    }
}
