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
import net.dv8tion.jda.api.entities.Guild

object CustomCmdManager {
    private val guilds = mutableMapOf<Long, MutableMap<String, List<CustomCommand>>>()

    init {
        // TODO: init
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
        return when (guilds[guild.idLong]?.remove(label.toLowerCase())) {
            null -> false
            else -> true
        }
    }

    fun deleteAll(guild: Guild) : Boolean {
        return when (guilds.remove(guild.idLong)) {
            null -> false
            else -> true
        }
    }

    fun add(label: String, list: List<CustomCommand>, guild: Guild) {
        if (!hasEntries(guild)) {
            guilds[guild.idLong] = mutableMapOf()
        }
        val map = guilds[guild.idLong]!!
        map[label.toLowerCase()] = list
    }
}
