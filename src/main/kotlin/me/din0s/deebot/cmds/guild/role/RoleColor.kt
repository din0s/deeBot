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
import me.din0s.util.reply
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Sets a role's color.
 *
 * @author Dinos Papakostas
 */
object RoleColor : RoleSub(
    subName = "color",
    subDescription = "Set a role's color",
    subAlias = setOf("colour"),
    subMinArgs = 2,
    subRequiredParams = arrayOf("role name / id", "hex code / reset"),
    subExamples = arrayOf("Member #fff", "Newbie reset")
) {
    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        val role = event.getMentionedRoleOrError(dropLast = true) ?: return
        val last = args.last()
        if (last.equals("reset", true)) {
            role.manager.setColor(0)
                .reason("Reset by ${event.author.asTag} (${event.author.id})")
                .queue {
                    event.reply("*Successfully reset the role's color!*")
                }
            return
        }
        val colorMatch = Regex.HEX.find(last)
        if (colorMatch?.groupValues?.size != 2) {
            event.reply("**That's not a valid HEX code!**")
            return
        }
        val color = when (val hex = Integer.parseInt(colorMatch.groupValues[1], 16)) {
            0 -> 1
            else -> hex
        }
        role.manager.setColor(color)
            .reason("Set by ${event.author.asTag} (${event.author.id})")
            .queue {
                event.reply("*Successfully updated the role's color!*")
            }
    }
}
