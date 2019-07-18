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
import me.din0s.deebot.Config
import me.din0s.deebot.managers.CustomCmdManager
import me.din0s.deebot.managers.ServerManager
import me.din0s.deebot.send
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import kotlin.random.Random

class CustomCmdHandler : ListenerAdapter() {
    private val defaultPrefix = Config.defaultPrefix

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (!CustomCmdManager.hasEntries(event.guild) || event.author.isBot) {
            return
        }

        val rawMessage = event.message.contentRaw
        val serverPrefix = ServerManager.get(event.guild.id)?.prefix ?: defaultPrefix
        if (rawMessage.startsWith(serverPrefix) && rawMessage.length > serverPrefix.length) {
            val allArgs = rawMessage.substring(serverPrefix.length).split(Regex.WHITESPACE)
            val label = allArgs[0].toLowerCase()
            val cmd = CustomCmdManager.getByLabel(label, event.guild) ?: return
            val randomResponse = cmd[Random.nextInt(cmd.size)].response
            event.channel.send(randomResponse) // TODO, obv
        }
    }
}
