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

import me.din0s.config.Config
import me.din0s.deebot.cmds.Command
import me.din0s.sql.managers.GuildDataManager
import me.din0s.sql.tables.Guilds
import me.din0s.util.escaped
import me.din0s.util.getAllArgs
import me.din0s.util.reply
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Sets the guild's custom prefix.
 *
 * @author Dinos Papakostas
 */
object Prefix : Command(
    name = "prefix",
    description = "Set the prefix for this server",
    alias = setOf("setprefix"),
    guildOnly = true,
    minArgs = 1,
    userPermissions = arrayOf(Permission.ADMINISTRATOR),
    requiredParams = arrayOf("new prefix / reset"),
    examples = arrayOf("?", "reset")
) {
    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        if (args.size == 1 && args[0].equals("reset", true)) {
            GuildDataManager.setPrefix(event.guild, null)
            event.reply("__Reset the prefix to:__ ${Config.defaultPrefix}")
        } else {
            val prefix = event.getAllArgs()
            if (prefix.length > Guilds.MAX_PREFIX_LENGTH) {
                event.reply("**The prefix cannot be longer than ${Guilds.MAX_PREFIX_LENGTH} characters!**")
                return
            } else {
                GuildDataManager.setPrefix(event.guild, prefix)
                event.reply("__Set the prefix to:__ ${prefix.escaped()}")
            }
        }
    }
}
