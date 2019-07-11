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

package me.din0s.deebot.cmds.global

import me.din0s.Variables
import me.din0s.deebot.asTime
import me.din0s.deebot.entities.Command
import me.din0s.deebot.reply
import net.dv8tion.jda.api.JDAInfo
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.lang.management.ManagementFactory

class Info : Command(
    name = "info",
    description = "Get information related to the bot"
) {
    private val UNIT = 1024
    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory() / UNIT
        val freeMemory = runtime.freeMemory() / UNIT
        val usedMemory = totalMemory - freeMemory
        event.reply("""
            __Bot Info__:
            **Bot Creator**: dinos#0649
            **Bot Version**: ${Variables.VERSION}
            **API Library**: JDA ${JDAInfo.VERSION}
            **RAM Used**: ${usedMemory / UNIT}/${totalMemory / UNIT} MB
            **Bot Uptime**: ${ManagementFactory.getRuntimeMXBean().uptime.asTime()}
            
            Do you have any suggestions for the bot?
            Join the official server and let me know!
            https://discord.gg/0wEZsVCXid2URhDY
        """.trimIndent())
    }
}
