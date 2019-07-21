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

import me.din0s.deebot.entities.CustomCommand
import me.din0s.deebot.entities.sql.Commands
import net.dv8tion.jda.api.entities.Guild
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object CustomCmdManager : ISqlManager {
    private val guilds = mutableMapOf<Long, MutableMap<String, MutableList<CustomCommand>>>()
    private val log = LogManager.getLogger(CustomCmdManager::class.java)

    override fun init() {
        log.info("Loading custom commands...")
        transaction {
            Commands.selectAll().forEach {
                val label = it[Commands.label]
                val response = it[Commands.response]
                val guildId = it[Commands.guildId]
                val private = it[Commands.private]
                val delete = it[Commands.delete]
                val cmd = CustomCommand(response, private, delete)
                log.trace("Registering command {} for G({})", label, guildId)

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
    }

    fun hasEntries(guild: Guild) : Boolean {
        return guilds.containsKey(guild.idLong)
    }

    fun getAll(guild: Guild) : Map<String, List<CustomCommand>>? {
        return guilds[guild.idLong]
    }

    fun getByLabel(label: String, guild: Guild) : List<CustomCommand>? {
        return guilds[guild.idLong]?.get(label.toLowerCase())
    }

    fun delete(label: String, guild: Guild) : Boolean {
        val name = label.toLowerCase()
        return when (guilds[guild.idLong]?.remove(name)) {
            null -> false
            else -> {
                transaction { Commands.deleteWhere { Commands.label eq name } }
                true
            }
        }
    }

    fun deleteAll(guild: Guild) : Boolean {
        return when (guilds.remove(guild.idLong)) {
            null -> false
            else -> {
                transaction { Commands.deleteWhere { Commands.guildId eq guild.idLong } }
                true
            }
        }
    }

    fun add(name: String, list: List<CustomCommand>, guild: Guild) {
        if (!hasEntries(guild)) {
            guilds[guild.idLong] = mutableMapOf()
        } else {
            deleteAll(guild)
        }

        val map = guilds[guild.idLong]!!
        map[name.toLowerCase()] = list.toMutableList()

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
