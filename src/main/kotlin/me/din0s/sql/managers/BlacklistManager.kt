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

import me.din0s.sql.tables.Blacklist
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Manages the blacklisted channels, which get ignored for commands.
 *
 * @author Dinos Papakostas
 */
object BlacklistManager : ISqlManager {
    private val log = LogManager.getLogger()
    private val ids = mutableSetOf<Long>()

    override fun preReady() {
        log.info("Loading Blacklisted Channels")
        transaction {
            Blacklist.selectAll().forEach {
                val id = it[Blacklist.channelId]
                log.trace("Adding TC#{} to the blacklist", id)
                ids.add(id)
            }
        }
    }

    /**
     * Toggle the blacklist to be either enabled or disabled
     * for a channel, based on its previous state.
     *
     * @param id The channel's ID.
     * @return True if the blacklist was enabled, false if it was disabled.
     */
    fun toggle(id: Long) : Boolean {
        return transaction {
            if (ids.contains(id)) {
                log.info("Removing TC#{} from the blacklist", id)
                Blacklist.deleteWhere { Blacklist.channelId eq id }
                ids.remove(id)
                false
            } else {
                log.info("Adding TC#{} to the blacklist", id)
                Blacklist.insert { it[channelId] = id }
                ids.add(id)
                true
            }
        }
    }

    /**
     * Returns whether the blacklist contains the channel.
     *
     * @param id The channel's ID.
     * @return True if the blacklist contains the channel, false otherwise.
     */
    fun isBlacklisted(id: Long) : Boolean {
        return ids.contains(id)
    }

    /**
     * Returns all blacklisted channels for a specific guild.
     *
     * @param guild The discord server to find blacklisted channels for.
     * @return A set of all blacklisted channels. May be empty.
     */
    fun getForGuild(guild: Guild) : Set<TextChannel> {
        return guild.textChannels.filter { ids.contains(it.idLong) }.toSet()
    }
}
