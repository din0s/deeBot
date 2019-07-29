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

import me.din0s.deebot.cmds.Command
import me.din0s.util.loadClasses
import me.din0s.util.register
import me.din0s.util.reply
import me.din0s.util.showUsage
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.apache.logging.log4j.LogManager

/**
 * The base command for role management related operations.
 *
 * @author Dinos Papakostas
 */
object RoleBase : Command(
    name = "role",
    description = "Advanced management for a server role",
    guildOnly = true,
    userPermissions = arrayOf(Permission.MANAGE_ROLES)
) {
    private val log = LogManager.getLogger()
    private lateinit var subMap: Map<String, Command>

    override fun postRegister() {
        val reflections = loadClasses("me.din0s.deebot.cmds.guild.role", Command::class.java)
        subMap = reflections.register(log)
        log.debug("Registered {} commands", reflections.size)
    }

    /**
     * Returns the subcommand, if existent, for the given label.
     *
     * @param label The label to look up the subcommand for.
     * @return The command if it exists, null otherwise.
     */
    fun getSub(label: String) : Command? {
        return subMap[label.toLowerCase()]
    }

    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        if (args.isNotEmpty()) {
            val subLabel = "$name ${args[0].toLowerCase()}"
            val sub = subMap[subLabel]
            when {
                sub == null -> event.reply("*That subcommand doesn't exist!*")
                args.size > sub.minArgs && args.size - 1 <= sub.maxArgs -> sub.execute(event, args.drop(1))
                else -> event.showUsage(sub)
            }
            return
        }
        RoleHelp.execute(event, args)
    }
}
