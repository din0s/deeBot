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

package me.din0s.deebot.handlers

import me.din0s.const.Regex
import me.din0s.deebot.entities.CustomCmdResponse
import me.din0s.sql.managers.CustomCmdManager
import me.din0s.sql.managers.GuildDataManager
import me.din0s.util.send
import me.din0s.util.whisper
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.apache.logging.log4j.LogManager

/**
 * Handles the custom commands for guilds that have registered some.
 *
 * @author Dinos Papakostas
 */
object CustomCmdHandler : ListenerAdapter() {
    private val log = LogManager.getLogger()

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (!CustomCmdManager.hasEntries(event.guild) || event.author.isBot) {
            return
        }
        val rawMessage = event.message.contentRaw
        val prefix = GuildDataManager.getPrefix(event.guild)
        if (rawMessage.startsWith(prefix) && rawMessage.length > prefix.length) {
            val allArgs = rawMessage.substring(prefix.length).split(Regex.WHITESPACE)
            val label = allArgs[0].lowercase()
            val cmd = CustomCmdManager.getByLabel(label, event.guild)?.random() ?: return
            log.trace("TC#{} {}: {}", event.channel.id, event.author.asTag, label)
            if (cmd.delete) {
                event.message.delete().queue { event.execute(cmd) }
            } else {
                event.execute(cmd)
            }
        }
    }

    /**
     * Parses the $random{} element that may exist in a command response.
     *
     * @return The response without any $random{} elements.
     */
    private fun String.handleRandom() : String {
        var field = this
        Regex.RANDOM.findAll(this).forEach {
            field = field.replace(it.value, it.groupValues[1].split(";").random())
        }
        return field
    }

    /**
     * Returns all arguments specified by the user after the custom command.
     *
     * @return All arguments that follow the command.
     */
    private fun GuildMessageReceivedEvent.getAllArgsOptional() : String {
        return message.contentRaw
            .substringAfter(GuildDataManager.getPrefix(guild), "")
            .substringAfter(' ')
            .trim()
    }

    /**
     * Parses the variable placeholders from the response.
     *
     * @param event The event that was fired for the custom command
     * @return The response after parsing every placeholder variable.
     */
    private fun String.parseVars(event: GuildMessageReceivedEvent) : String {
        return replace("%user%", event.author.name)
            .replace("%userid%", event.author.id)
            .replace("%nickname%", event.member!!.nickname ?: event.author.name)
            .replace("%mention%", event.author.asMention)
            .replace("%input%", event.getAllArgsOptional())
    }

    /**
     * Sends the response of the custom command, either in private or not, depending on the command's flag.
     *
     * @param cmd The custom command to execute.
     */
    private fun GuildMessageReceivedEvent.execute(cmd: CustomCmdResponse) {
        val msg = cmd.response.handleRandom().parseVars(this)
        if (cmd.private) {
            author.whisper(msg)
        } else {
            channel.send(msg, false)
        }
    }
}
