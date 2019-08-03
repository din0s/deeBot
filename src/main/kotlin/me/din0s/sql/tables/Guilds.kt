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

package me.din0s.sql.tables

import org.jetbrains.exposed.sql.Table

/**
 * Table of all guild data, which are the custom prefix,
 * the ID for the role given to new members, and the
 * join/leave messages & channels.
 *
 * @author Dinos Papakostas
 */
object Guilds : Table() {
    const val MAX_PREFIX_LENGTH = 32

    val id = long("guild_id").primaryKey()

    val prefix = varchar("prefix", MAX_PREFIX_LENGTH).nullable()
    val autoRole = long("autorole_id").nullable()

    val joinChannel = long("join_channel_id").nullable()
    val joinMessage = varchar("join_msg", 2000).nullable()

    val leaveChannel = long("leave_channel_id").nullable()
    val leaveMessage = varchar("leave_msg", 2000).nullable()
}
