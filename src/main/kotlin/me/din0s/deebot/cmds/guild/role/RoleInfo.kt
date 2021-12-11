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
import java.awt.Color

/**
 * Displays information related to a role.
 *
 * @author Dinos Papakostas
 */
object RoleInfo : RoleSub(
    subName = "info",
    subDescription = "Display information related to a server role",
    subMinArgs = 1,
    subRequiredParams = arrayOf("role name / id"),
    subExamples = arrayOf("Admin")
) {
    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        val role = event.getMentionedRoleOrError(hierarchy = false) ?: return
        event.reply(
            """
            __**Role:** ${role.name.escaped()}__ (${role.id})
            Color: **${role.color?.getHex() ?: "None."}**
            Position: **${role.position}**
            
            Is displayed separately? **${role.isHoisted}**
            Is mentionable? **${role.isMentionable}**
            Is managed by an integration? **${role.isManaged}**
            
            Permissions: ```prolog
            ${role.permissions.joinToString(" - ").ifBlank { "None." }}
            ```
        """.trimIndent()
        )
    }

    /**
     * Returns the hex representation of this color.
     *
     * @return The hex value, such as #FF00FF.
     */
    private fun Color.getHex() : String {
        return "#${Integer.toHexString(rgb).substring(2).uppercase()}"
    }
}
