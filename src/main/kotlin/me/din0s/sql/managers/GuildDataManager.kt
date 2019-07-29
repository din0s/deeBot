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

package me.din0s.sql.managers

import me.din0s.config.Config
import me.din0s.sql.tables.Guilds
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.ISnowflake
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.TextChannel
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.reflections.ReflectionUtils

/**
 * Manages all guild data, such as prefix, the role given
 * to new members and join/leave messages & channels.
 *
 * @author Dinos Papakostas
 */
object GuildDataManager : ISqlManager {
    private val log = LogManager.getLogger()
    private val data = mutableMapOf<Long, GuildData>()

    override fun preReady() {
        log.info("Loading Guild Data")
        transaction {
            Guilds.selectAll().forEach {
                val id = it[Guilds.id]
                log.debug("Loading data for G#{}", id)
                data[id] = GuildData(
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

    /**
     * Returns whether the guild has any data
     * related to members joining/leaving.
     *
     * @param guild The guild to look up for.
     * @return True if there is an entry related to
     * join/leave events, false otherwise.
     */
    fun hasJoinLeaveData(guild: Guild) : Boolean {
        val entry = data[guild.idLong] ?: return false
        return entry.autoRole != null
                || (entry.joinChannel != null && entry.joinMessage != null)
                || (entry.leaveChannel != null && entry.leaveMessage != null)
    }

    /**
     * Returns the prefix for the specified guild.
     *
     * @param guild The guild to look up for.
     * @return The guild's prefix.
     */
    fun getPrefix(guild: Guild) : String {
        return data[guild.idLong]?.prefix ?: Config.defaultPrefix
    }

    /**
     * Returns the role given to new members for the specified guild.
     *
     * @param guild The guild to look up for.
     * @return The guild's autorole if set, null otherwise.
     */
    fun getAutoRole(guild: Guild) : Role? {
        val id = data[guild.idLong]?.autoRole ?: return null
        val role = guild.getRoleById(id)
        if (role == null) {
            setAutoRole(guild, null)
        }
        return role
    }

    /**
     * Returns the channel in which join messages are sent for the specified guild.
     *
     * @param guild The guild to look up for.
     * @return The guild's join channel if set, or the first available channel otherwise.
     */
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

    /**
     * Returns the message that is sent when a member joins the specified guild.
     *
     * @param guild The guild to look up for.
     * @return The guild's join message if set, null otherwise.
     */
    fun getJoinMessage(guild: Guild) : String? {
        return data[guild.idLong]?.joinMessage
    }

    /**
     * Returns the channel in which leave messages are sent for the specified guild.
     *
     * @param guild The guild to look up for.
     * @return The guild's leave channel if set, or the first available channel otherwise.
     */
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

    /**
     * Returns the message that is sent when a member leaves the specified guild.
     *
     * @param guild The guild to look up for.
     * @return The guild's leave message if set, null otherwise.
     */
    fun getLeaveMessage(guild: Guild) : String? {
        return data[guild.idLong]?.leaveMessage
    }

    /**
     * Sets the guild's prefix to the one specified.
     * If the value is null then it deletes the entry.
     *
     * @param guild The guild to set the prefix for.
     * @param newPrefix The new value for the prefix.
     */
    fun setPrefix(guild: Guild, newPrefix: String?) {
        val prefix = when (newPrefix) {
            Config.defaultPrefix -> null
            else -> newPrefix
        }

        guild.update(Guilds.prefix, prefix)
    }

    /**
     * Sets the guild's role given to new members to the one specified.
     * If the value is null then it deletes the entry.
     *
     * @param guild The guild to set the auto role for.
     * @param role The new value for the auto role.
     */
    fun setAutoRole(guild: Guild, role: Role?) {
        guild.updateId(Guilds.autoRole, role)
    }

    /**
     * Sets the guild's channel in which join messages are sent to the one specified.
     * If the value is null then it deletes the entry.
     *
     * @param guild The guild to set the join channel for.
     * @param channel The new value for the join channel.
     */
    fun setJoinChannel(guild: Guild, channel: TextChannel?) {
        guild.updateId(Guilds.joinChannel, channel)
    }

    /**
     * Sets the message sent when a member joins the guild to the one specified.
     * If the value is null then it deletes the entry.
     *
     * @param guild The guild to set the join message for.
     * @param message The new value for the join message.
     */
    fun setJoinMessage(guild: Guild, message: String?) {
        guild.update(Guilds.joinMessage, message)
    }

    /**
     * Sets the guild's channel in which leave messages are sent to the one specified.
     * If the value is null then it deletes the entry.
     *
     * @param guild The guild to set the leave channel for.
     * @param channel The new value for the leave channel.
     */
    fun setLeaveChannel(guild: Guild, channel: TextChannel?) {
        guild.updateId(Guilds.leaveChannel, channel)
    }

    /**
     * Sets the message sent when a member leaves the guild to the one specified.
     * If the value is null then it deletes the entry.
     *
     * @param guild The guild to set the leave message for.
     * @param message The new value for the leave message.
     */
    fun setLeaveMessage(guild: Guild, message: String?) {
        guild.update(Guilds.leaveMessage, message)
    }

    /**
     * Updates the entry for a value that represents a Discord [ISnowflake].
     * If the value is null then it deletes the entry.
     *
     * @param col The column in the database that contains this value.
     * @param value The new value to be set.
     */
    private fun Guild.updateId(col: Column<Long?>, value: ISnowflake?) {
        update(col, value?.idLong)
    }

    /**
     * Updates the entry for a value of the guild's data.
     * If the value is null then it deletes the entry.
     *
     * @param col The column in the database that contains this value.
     * @param value The new value to be set.
     */
    private fun <T> Guild.update(col: Column<T?>, value: T?) {
        if (!data.containsKey(idLong) && value == null) {
            return
        }

        val update = data.containsKey(idLong)
        if (!update) {
            data[idLong] = GuildData()
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
            .getAllFields(GuildData::class.java)
            .first { it.name == fieldName }
            .set(entry, value)

        log.info("Updating {} for G#{} to {}", fieldName, id, value)
        transaction {
            when {
                entry.isEmpty() -> {
                    data.remove(idLong)
                    Guilds.deleteWhere { Guilds.id eq idLong }
                }
                update -> {
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

    /**
     * Represents a set of data saved for each guild.
     *
     * @author Dinos Papakostas
     */
    private data class GuildData(
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
