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
import me.din0s.util.asString
import me.din0s.util.escaped
import me.din0s.util.reply
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * Returns information related to the guild.
 *
 * @author Dinos Papakostas
 */
object ServerInfo : Command(
    name = "serverinfo",
    description = "Get information about the server",
    alias = setOf("guildinfo", "server", "guild"),
    guildOnly = true
) {
    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        val ownerTag = event.guild.owner!!.user.asTag
        val verifLevel = event.guild.verificationLevel
        val timeoutSecs = event.guild.afkTimeout.seconds
        val timeout = when {
            timeoutSecs < 60 -> "$timeoutSecs seconds"
            else -> "${timeoutSecs / 60} minutes"
        }
        event.reply("""
            Server Info for **${event.guild.name.escaped()}**
            (ID: ${event.guild.id})
            
            ${Unicode.CROWN} | **Owner** › $ownerTag
            ${Unicode.COP}${verifLevel.getColor()} | **Verification Level** › ${verifLevel.name}
            ${Unicode.CALENDAR} | **Creation Date** › ${event.guild.timeCreated.asString()}
            ${Unicode.PICTURE} | **Server Icon** › ${event.guild.iconUrl ?: "None"}
            
            ${Unicode.SILHOUETTES} | **Members** › ${event.guild.memberCache.size()}
            ${Unicode.NOTE} | **Text Channels** › ${event.guild.textChannelCache.size()}
            ${Unicode.LOUDSPEAKER} | **Voice Channels** › ${event.guild.voiceChannelCache.size()}
            
            ${Unicode.MAP} | **Voice Region** › ${event.guild.region.name}
            ${Unicode.MUTE} | **AFK Channel** › ${event.guild.afkChannel?.name ?: "None"}
            ${Unicode.CLOCK} | **AFK Timeout** › $timeout
        """.trimIndent())
    }

    /**
     * Get a color based on the guild's verification level.
     *
     * @return A unicode character that alters the previous emoji's skin color.
     */
    private fun Guild.VerificationLevel.getColor() : String {
        return when (this) {
            Guild.VerificationLevel.NONE -> Unicode.SKIN_PALE
            Guild.VerificationLevel.LOW -> Unicode.SKIN_LIGHT
            Guild.VerificationLevel.MEDIUM -> Unicode.SKIN_DARK
            Guild.VerificationLevel.HIGH -> Unicode.SKIN_BLACK
            else -> ""
        }
    }
}
