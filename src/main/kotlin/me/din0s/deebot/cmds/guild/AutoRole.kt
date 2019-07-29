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
import me.din0s.sql.managers.GuildDataManager
import me.din0s.util.escaped
import me.din0s.util.getAllArgs
import me.din0s.util.getRoleOrShowError
import me.din0s.util.reply
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Gives a role upon a member joining the guild.
 *
 * @author Dinos Papakostas
 */
object AutoRole : Command(
    name = "autorole",
    description = "Set a role to be given to every new member",
    alias = setOf("joinrole"),
    guildOnly = true,
    botPermissions = arrayOf(Permission.MANAGE_ROLES),
    userPermissions = arrayOf(Permission.MANAGE_ROLES),
    optionalParams = arrayOf("role name / reset"),
    examples = arrayOf("Newbie", "reset")
) {
    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        if (args.isEmpty()) {
            val role = GuildDataManager.getAutoRole(event.guild)?.name?.escaped() ?: "None."
            event.reply("__Current autorole:__ $role")
            return
        }
        if (args[0].equals("reset", true)) {
            GuildDataManager.setAutoRole(event.guild, null)
            event.reply("__Reset the autorole!__")
            return
        }
        val role = event.getRoleOrShowError(event.getAllArgs()) ?: return
        GuildDataManager.setAutoRole(event.guild, role)
        event.reply("__Successfully set the autorole to:__ ${role.name.escaped()}")
    }
}
