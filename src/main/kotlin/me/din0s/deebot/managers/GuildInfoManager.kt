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

package me.din0s.deebot.managers

import me.din0s.deebot.Config
import me.din0s.deebot.entities.sql.Guilds
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.TextChannel
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.reflections.ReflectionUtils

object GuildInfoManager : ISqlManager {
    private val data = mutableMapOf<Long, GuildInfo>()
    private val log = LogManager.getLogger(GuildInfoManager::class.java)

    override fun init() {
        log.info("Loading guild information...")
        transaction {
            Guilds.selectAll().forEach {
                val id = it[Guilds.id]
                log.trace("Loading data for G({})", id)
                data[id] = GuildInfo(
                    it[Guilds.prefix],
                    it[Guilds.autoRole],
                    it[Guilds.joinChannel],
                    it[Guilds.joinMessage],
                    it[Guilds.leaveChannel],
                    it[Guilds.leaveMessage]
                )
            }
        }
    }

    fun getPrefix(guild: Guild): String {
        return data[guild.idLong]?.prefix ?: Config.defaultPrefix
    }

    fun getAutoRole(guild: Guild) : Role? {
        val id = data[guild.idLong]?.autoRole ?: return null
        val role = guild.getRoleById(id)
        if (role == null) {
            setAutoRole(guild, null)
        }
        return role
    }

    fun getJoinChannel(guild: Guild) : TextChannel {
        val id = data[guild.idLong]?.joinChannel ?: return guild.textChannels[0]
        val channel = guild.getTextChannelById(id)
        return if (channel == null) {
            setJoinChannel(guild, null)
            guild.textChannels[0]
        } else {
            channel
        }
    }

    fun getJoinMessage(guild: Guild) : String? {
        return data[guild.idLong]?.joinMessage
    }

    fun getLeaveChannel(guild: Guild) : TextChannel {
        val id = data[guild.idLong]?.leaveChannel ?: return guild.textChannels[0]
        val channel = guild.getTextChannelById(id)
        return if (channel == null) {
            setLeaveChannel(guild, null)
            guild.textChannels[0]
        } else {
            channel
        }
    }

    fun getLeaveMessage(guild: Guild) : String? {
        return data[guild.idLong]?.leaveMessage
    }

    fun setPrefix(guild: Guild, newPrefix: String?) {
        val prefix = when (newPrefix) {
            Config.defaultPrefix -> null
            else -> newPrefix
        }

        guild.update(prefix, Guilds.prefix, prefix)
    }

    fun setAutoRole(guild: Guild, role: Role?) {
        guild.updateId(role, Guilds.autoRole)
    }

    fun setJoinChannel(guild: Guild, channel: TextChannel?) {
        guild.updateId(channel, Guilds.joinChannel)
    }

    fun setJoinMessage(guild: Guild, message: String?) {
        guild.update(message, Guilds.joinMessage)
    }

    fun setLeaveChannel(guild: Guild, channel: TextChannel?) {
        guild.updateId(channel, Guilds.leaveChannel)
    }

    fun setLeaveMessage(guild: Guild, message: String?) {
        guild.update(message, Guilds.leaveMessage)
    }

    private fun <T : ISnowflake?> Guild.updateId(key: T, col: Column<Long?>) {
        update(key, col, key?.idLong)
    }

    private fun <T> Guild.update(key: T, col: Column<T>) {
        update(key, col, key)
    }

    private fun <T> Guild.update(key: Any?, col: Column<T>, value: T) {
        if (!data.containsKey(idLong) && key == null) {
            return
        } else if (!data.containsKey(idLong)) {
            data[idLong] = GuildInfo()
        }

        val entry = data[idLong]!!
        val fieldName = when (col.name) {
            "autorole_id" -> "autoRole"
            "join_channel_id" -> "joinChannel"
            "join_msg" -> "joinMessage"
            "leave_channel_id" -> "leaveChannel"
            "leave_msg" -> "leaveMessage"
            else -> col.name
        }

        ReflectionUtils
            .getAllFields(GuildInfo::class.java)
            .first { it.name == fieldName }
            .set(entry, value)

        transaction {
            when {
                entry.isEmpty() -> {
                    data.remove(idLong)
                    Guilds.deleteWhere { Guilds.id eq idLong }
                }
                data.containsKey(idLong) -> {
                    Guilds.update({ Guilds.id eq idLong }) { it[col] = value }
                }
                else -> {
                    Guilds.insert {
                        it[id] = idLong
                        it[col] = value
                    }
                }
            }
        }
    }

    private data class GuildInfo(
        @JvmField var prefix: String? = null,
        @JvmField var autoRole: Long? = null,
        @JvmField var joinChannel: Long? = null,
        @JvmField var joinMessage: String? = null,
        @JvmField var leaveChannel: Long? = null,
        @JvmField var leaveMessage: String? = null
    ) {
        fun isEmpty() : Boolean {
            return prefix == null
                    && autoRole == null
                    && joinChannel == null
                    && joinMessage == null
                    && leaveChannel == null
                    && leaveMessage == null
        }
    }
}
