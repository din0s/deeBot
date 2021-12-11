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

import me.din0s.const.Regex
import me.din0s.deebot.cmds.guild.role.impl.RoleSub
import me.din0s.util.noBackTicks
import me.din0s.util.paginate
import me.din0s.util.reply
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Lists all of the guild's roles.
 *
 * @author Dinos Papakostas
 */
object RoleList : RoleSub(
    subName = "list",
    subDescription = "Display all of the roles in this server",
    subAlias = setOf("listroles"),
    subMaxArgs = 1,
    subOptionalParams = arrayOf("page number"),
    subExamples = arrayOf("1")
) {
    private const val ROLES_PER_PAGE = 10

    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        if (event.guild.roleCache.size() == 1L) {
            event.reply("*This server doesn't have any roles!*")
            return
        }

        val index = when {
            args.isEmpty() -> 0
            args[0].matches(Regex.INTEGER) -> args[0].toInt() - 1
            else -> {
                event.reply("That's not a valid page index!")
                return
            }
        }
        if (index < 0) {
            event.reply("Please select a positive page index!")
            return
        }

        val rolePages = event.guild.roles
            .filter { !it.isPublicRole }
            .sortedBy { it.name }
            .toList()
            .paginate({
                "+ ${it.name.noBackTicks()} (${it.id})"
            }, ROLES_PER_PAGE)
        if (index >= rolePages.size) {
            event.reply("There aren't that many commands! (Max page: ${rolePages.size}")
        } else {
            event.reply("```diff\n${rolePages[index]}\n\n- Page ${index + 1} of ${rolePages.size}```")
        }
    }
}
