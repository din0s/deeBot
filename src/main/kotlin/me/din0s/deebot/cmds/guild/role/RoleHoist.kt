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
import me.din0s.util.escaped
import me.din0s.util.reply
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Displays a role separately in the user list.
 *
 * @author Dinos Papakostas
 */
object RoleHoist : RoleSub(
    subName = "hoist",
    subDescription = "Display a role separately in the role list",
    subAlias = setOf("separate", "display"),
    subMinArgs = 1,
    subRequiredParams = arrayOf("role name / id"),
    subExamples = arrayOf("deeBot")
) {
    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        val role = event.getMentionedRoleOrError() ?: return
        val setHoisted = !role.isHoisted
        role.manager.setHoisted(setHoisted)
            .reason("Toggled by ${event.author.asTag} (${event.author.id})")
            .queue {
                event.reply("Displaying ${role.name.escaped()} separately: **$setHoisted**")
        }
    }
}
