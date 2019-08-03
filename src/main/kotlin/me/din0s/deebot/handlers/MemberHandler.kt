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

import me.din0s.const.Unicode
import me.din0s.sql.managers.GuildDataManager
import me.din0s.util.send
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.guild.member.GenericGuildMemberEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.apache.logging.log4j.LogManager

/**
 * Handles events related to members joining or leaving the guild.
 *
 * @author Dinos Papakostas
 */
object MemberHandler : ListenerAdapter() {
    private val log = LogManager.getLogger()

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        event.handle(true)
    }

    override fun onGuildMemberLeave(event: GuildMemberLeaveEvent) {
        event.handle(false)
    }

    /**
     * Handles the [GenericGuildMemberEvent] in order to send a message
     * in the join/leave channel and set the autorole.
     */
    private fun GenericGuildMemberEvent.handle(join: Boolean) {
        if (!GuildDataManager.hasJoinLeaveData(guild) || user == jda.selfUser) {
            return
        }

        val m = when {
            join -> GuildDataManager.getJoinMessage(guild)
            else -> GuildDataManager.getLeaveMessage(guild)
        }
        if (m != null) {
            val tc = when {
                join -> GuildDataManager.getJoinChannel(guild)
                else -> GuildDataManager.getLeaveChannel(guild)
            }
            tc.send(m.parseVariables(this), false)
        }

        if (join) {
            val autoRole = GuildDataManager.getAutoRole(guild) ?: return
            if (!guild.selfMember.hasPermission(Permission.MANAGE_ROLES)) {
                guild.defaultChannel?.send("${Unicode.SOS} **The bot cannot interact with the autorole due to missing permissions!**", false)
                return
            }
            if (!guild.selfMember.canInteract(autoRole)) {
                guild.defaultChannel?.send("${Unicode.SOS} **The bot cannot interact with the autorole due to hierarchy!**", false)
                return
            }
            guild.modifyMemberRoles(member, autoRole).reason("autorole").queue({}, {
                log.error("AutoRole assignment failed: {}", it.message)
            })
        }
    }

    /**
     * Parses the string for variables to replace in the join/leave message.
     *
     * @param event The [GenericGuildMemberEvent] that was fired.
     * @return The message with the variables replaced with their values.
     */
    private fun String.parseVariables(event: GenericGuildMemberEvent) : String {
        val user = event.member.user
        return replace("%user%", user.name, true)
            .replace("%userid%", user.id, true)
            .replace("%tag%", user.asTag, true)
            .replace("%mention%", user.asMention, true)
            .replace("%server%", event.guild.name, true)
            .replace("%usercount%", event.guild.memberCache.size().toString(), true)
    }
}
