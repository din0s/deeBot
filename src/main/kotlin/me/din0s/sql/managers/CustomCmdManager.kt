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

import me.din0s.deebot.entities.CustomCmdResponse
import me.din0s.sql.tables.Commands
import net.dv8tion.jda.api.entities.Guild
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Manages all custom commands registered.
 *
 * @author Dinos Papakostas
 */
object CustomCmdManager : ISqlManager {
    private val log = LogManager.getLogger()
    private val guilds = mutableMapOf<Long, MutableMap<String, MutableList<CustomCmdResponse>>>()

    override fun preReady() {
        log.info("Loading Custom Commands")
        transaction {
            Commands.selectAll().forEach {
                val label = it[Commands.label]
                val response = it[Commands.response]
                val guildId = it[Commands.guildId]
                val private = it[Commands.private]
                val delete = it[Commands.delete]
                val cmd = CustomCmdResponse(response, private, delete)
                log.trace("Registering command {} for G#{}", label, guildId)

                if (!guilds.containsKey(guildId)) {
                    guilds[guildId] = mutableMapOf()
                }
                val map = guilds[guildId]!!
                if (!map.containsKey(label)) {
                    map[label] = mutableListOf(cmd)
                } else{
                    map[label]!!.add(cmd)
                }
            }
        }
        log.info("Loaded {} total mappings!", guilds.values.stream().mapToInt { it.size }.sum())
    }

    /**
     * Returns whether the specified guild has any custom commands set.
     *
     * @param guild The guild to look up for.
     * @return True if there's an entry for this guild.
     */
    fun hasEntries(guild: Guild) : Boolean {
        return guilds.containsKey(guild.idLong)
    }

    /**
     * Returns all of the custom commands for the specified guild.
     *
     * @param guild The guild to look up for.
     * @return A map containing all command labels and their responses respectively,
     * or an empty map if the guild doesn't have any commands.
     */
    fun getAll(guild: Guild) : Map<String, List<CustomCmdResponse>> {
        return guilds[guild.idLong] ?: emptyMap()
    }

    /**
     * Returns all responses for a specified label.
     *
     * @param label The command's name.
     * @param guild The guild to look up for.
     * @return A list of all responses for the custom command,
     * or null if that label is not found for this guild.
     */
    fun getByLabel(label: String, guild: Guild) : List<CustomCmdResponse>? {
        return guilds[guild.idLong]?.get(label.toLowerCase())
    }

    /**
     * Deletes a command based on its label for the specified guild.
     *
     * @param label The command's name.
     * @param guild The guild to look up for.
     * @param logEntry Whether to log the event.
     * @return True if the command was found and was deleted, false otherwise.
     */
    fun delete(label: String, guild: Guild, logEntry: Boolean = true) : Boolean {
        val name = label.toLowerCase()
        return when (guilds[guild.idLong]?.remove(name)) {
            null -> false
            else -> {
                if (logEntry) {
                    log.info("Deleting command {} for G#{}", name, guild.id)
                }
                transaction { Commands.deleteWhere { Commands.label eq name } }
                true
            }
        }
    }

    /**
     * Deletes all commands for the specified guild.
     *
     * @param guild The guild to look up for.
     * @return True if the guild contained any commands in the first place, false otherwise.
     */
    fun deleteAll(guild: Guild) : Boolean {
        return when (guilds.remove(guild.idLong)) {
            null -> false
            else -> {
                log.info("Deleting all commands for G#{}", guild.id)
                transaction { Commands.deleteWhere { Commands.guildId eq guild.idLong } }
                true
            }
        }
    }

    /**
     * Adds a custom command to the specified guild's mapping.
     *
     * @param name The command's name.
     * @param list A list of all possible responses.
     * @param guild The guild to create the command for.
     */
    fun add(name: String, list: List<CustomCmdResponse>, guild: Guild) {
        if (!hasEntries(guild)) {
            guilds[guild.idLong] = mutableMapOf()
        } else {
            delete(name, guild, false)
        }
        val map = guilds[guild.idLong]!!
        val nameLower = name.toLowerCase()
        map[nameLower] = list.toMutableList()
        log.info("Adding command {} for G#{}", nameLower, guild.id)
        transaction {
            list.forEach { command ->
                Commands.insert {
                    it[label] = name.toLowerCase()
                    it[response] = command.response
                    it[guildId] = guild.idLong
                    it[private] = command.private
                    it[delete] = command.delete
                }
            }
        }
    }
}
