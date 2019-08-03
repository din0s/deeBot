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

import me.din0s.const.Unicode
import me.din0s.deebot.cmds.Command
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Prompts the bot to leave the guild.
 *
 * @author Dinos Papakostas
 */
object Leave : Command(
    name = "leave",
    description = "Force the bot to leave your server",
    guildOnly = true,
    userPermissions = arrayOf(Permission.KICK_MEMBERS)
) {
    private val SKIN_COLORS = arrayOf(
        Unicode.SKIN_BLACK,
        Unicode.SKIN_DARK,
        Unicode.SKIN_LIGHT,
        Unicode.SKIN_PALE
    )

    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        event.channel.sendMessage("${Unicode.WAVE}${SKIN_COLORS.random()}").queue {
            event.guild.leave().queue()
        }
    }
}
