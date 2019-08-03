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

package me.din0s.util

import me.din0s.config.Config
import me.din0s.const.Regex
import me.din0s.deebot.cmds.Command
import me.din0s.exceptions.UserErrorException
import me.din0s.sql.managers.GuildDataManager
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import kotlin.streams.toList

/**
 * Executes an action and handles a [UserErrorException] by catching it
 * and replying with the error message.
 *
 * @param action The action to invoke.
 */
fun MessageReceivedEvent.handleUserError(action: () -> Unit) {
    try {
        action.invoke()
    } catch (e: UserErrorException) {
        reply("**${e.message}**")
    }
}

/**
 * Returns all of the arguments supplied by the user when executing a command.
 *
 * @return The command's arguments.
 */
fun MessageReceivedEvent.getAllArgs() : String {
    return message.contentRaw
        .substringAfter(getPrefix())
        .substringAfter(' ')
        .trim()
}

/**
 * Returns the appropriate prefix for the server/channel the event was fired in.
 *
 * @return The prefix for this guild or the default prefix for DMs.
 */
fun MessageReceivedEvent.getPrefix() : String {
    return when {
        isFromGuild -> GuildDataManager.getPrefix(guild)
        else -> Config.defaultPrefix
    }
}

/**
 * Convenience method used to retrieve a single role from the user's arguments.
 * If more/no roles match then an error message is being displayed.
 *
 * @param identifier The arguments to be used for parsing the role.
 * @param hierarchy Whether to only return roles that the user can interact with.
 * @return The role if found, null otherwise.
 */
fun MessageReceivedEvent.getRoleOrShowError(identifier: String, hierarchy: Boolean = true) : Role? {
    val roles = when {
        message.mentionedRoles.isNotEmpty() -> message.mentionedRoles
        identifier.matches(Regex.DISCORD_ID) -> listOf(guild.getRoleById(identifier))
        else -> guild.roleCache.applyStream { it.filter { r -> r.name == identifier }.toList() }
    }
    when {
        roles.isNullOrEmpty() -> reply("*No roles matched that criteria!*")
        roles.size != 1 -> reply("**Too many roles match that criteria! Please narrow down your selection.**")
        else -> {
            val role = roles.first()
            when {
                role == null -> reply("**That role doesn't exist!**")
                hierarchy && !member!!.canInteract(role) -> reply("**You cannot interact with that role because it's higher in hierarchy!**")
                hierarchy && !guild.selfMember.canInteract(role) -> reply("**The bot cannot interact with the specified role because it's higher in hierarchy!**")
                else -> return role
            }
        }
    }
    return null
}

/**
 * Replies to the user with the command's usage.
 *
 * @param cmd The command to display the usage of.
 */
fun MessageReceivedEvent.showUsage(cmd: Command) {
    reply("**Usage:** ${getPrefix().escaped()}${cmd.usage}")
}
